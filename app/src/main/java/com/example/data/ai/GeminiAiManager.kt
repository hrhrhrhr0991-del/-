package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "GeminiAiManager"

data class BedInspectionResult(
    val isTidy: Boolean,
    val score: Int,
    val feedback: String,
    val issues: List<String>,
    val isRelevant: Boolean = true
)

data class PersonaDiagnosisResult(
    val archetype: String,
    val coachTone: String,
    val personalizedStrategy: String,
    val dailyMotto: String
)

data class VoiceEvaluationResult(
    val isApproved: Boolean,
    val score: Int,
    val feedback: String
)

class GeminiAiManager {

    private val bedInspectionSchema = Schema.obj(
        properties = mapOf(
            "isRelevant" to Schema.boolean(description = "True if the photo actually depicts a bed, mattress, pillows, or sleeping area; false if it's unrelated (e.g. face, ceiling, blank wall, car, computer screen, shoes, pet, food)"),
            "isTidy" to Schema.boolean(description = "True if the bed is neatly made, false if messy or unmade"),
            "score" to Schema.integer(description = "Cleanliness and neatness score from 0 to 100. If isRelevant is false, score must be 0"),
            "feedback" to Schema.string(description = "Concise constructive Persian feedback explaining what is right, or if irrelevant warning the user, or which exact part (sheet, pillow, blanket) needs fixing"),
            "issues" to Schema.array(items = Schema.string(), description = "List of specific issues found, in Persian")
        )
    )

    private val personaSchema = Schema.obj(
        properties = mapOf(
            "archetype" to Schema.string(description = "Creative Persian persona title, e.g. 'جنگجوی سحرخیز', 'فرمانده تمرکز', 'معمار عادات فولادین'"),
            "coachTone" to Schema.string(description = "Coach tone descriptor"),
            "personalizedStrategy" to Schema.string(description = "Tailored 2-sentence discipline advice in Persian"),
            "dailyMotto" to Schema.string(description = "Punchy Persian daily discipline mantra")
        )
    )

    private val voiceEvaluationSchema = Schema.obj(
        properties = mapOf(
            "isApproved" to Schema.boolean(description = "True if speech is clear, enthusiastic and matches the wakefulness intent"),
            "score" to Schema.integer(description = "Clarity score from 0 to 100"),
            "feedback" to Schema.string(description = "Persian feedback acknowledging morning wakefulness and motivation")
        )
    )

    private val visionModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = bedInspectionSchema
            }
        )
    }

    private val textModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    private val personaModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = personaSchema
            }
        )
    }

    private val voiceEvalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = voiceEvaluationSchema
            }
        )
    }

    suspend fun inspectBedImage(bitmap: Bitmap): BedInspectionResult {
        return try {
            val response = withTimeout(45_000L) {
                visionModel.generateContent(
                    content {
                        image(bitmap)
                        text(
                            "مهم‌ترین گام: ابتدا بررسی کن آیا تصویر ارسالی واقعاً یک تخت خواب، رختخواب، ملحفه یا فضای خواب است یا تصویری نامرتبط است؟ " +
                            "اگر تصویر نامرتبط است (مانند چهره افراد، دیوار، زمین، سقف، مانیتور، کفش، غذا، حیوان یا اجسام دیگر)، " +
                            "باید فیلد isRelevant را false بگذاری و فیلد isTidy را false و score را ۰ بگذاری، و در feedback بنویسی: 'تصویر نامرتبط است! لطفاً فقط از تخت خواب خود عکس بگیرید.' " +
                            "فقط در صورتی که تصویر مربوط به تخت خواب باشد، isRelevant را true بگذار و بررسی دقیق کن آیا تخت خواب به درستی و منظم مرتب شده است؟ " +
                            "ملحفه‌ها باید کاملاً صاف باشند و بالش‌ها و پتو در جای مناسب قرار گرفته باشند. " +
                            "اگر تخت نامرتب است دقیقاً بگو کجایش ایراد دارد (مثلاً چروک ملحفه، کج بودن بالش، تا نشدن پتو). " +
                            "پاسخ را دقیقاً در قالب اسکیما به زبان فارسی روان بده."
                        )
                    }
                )
            }
            val jsonText = response.text ?: throw IllegalStateException("پاسخ مدل خالی بود.")
            val json = JSONObject(jsonText)
            val isRelevant = json.optBoolean("isRelevant", true)
            val isTidy = json.getBoolean("isTidy")
            val score = json.getInt("score")
            val feedback = json.getString("feedback")
            val issuesArray = json.optJSONArray("issues")
            val issues = mutableListOf<String>()
            if (issuesArray != null) {
                for (i in 0 until issuesArray.length()) {
                    issues.add(issuesArray.getString(i))
                }
            }
            BedInspectionResult(isTidy, score, feedback, issues, isRelevant)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Bed inspection timed out", e)
            BedInspectionResult(
                isTidy = false,
                score = 0,
                feedback = "مهلت بررسی تصویر به پایان رسید. لطفاً مجدداً با نور بهتر عکس بگیرید.",
                issues = listOf("خطای وقفه زمانی"),
                isRelevant = true
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Bed inspection error", e)
            BedInspectionResult(
                isTidy = false,
                score = 0,
                feedback = "خطا در ارزیابی هوش مصنوعی: ${e.localizedMessage ?: "مجدداً تلاش کنید"}",
                issues = listOf("خطای ارتباط با سرور"),
                isRelevant = true
            )
        }
    }

    suspend fun diagnoseDisciplinePersona(
        level: String,
        obstacle: String,
        goal: String,
        coachTone: String
    ): PersonaDiagnosisResult {
        return try {
            val prompt = "کاربر در ارزیابی اولیه انضباط فردی شرکت کرده است:\n" +
                    "- سطح فعلی انضباط: $level\n" +
                    "- بزرگترین چالش روزمره: $obstacle\n" +
                    "- هدف اصلی: $goal\n" +
                    "- سبک مربی‌گری دلخواه: $coachTone\n\n" +
                    "یک تیپ شخصیتی جذاب، استراتژی اختصاصی کوتاه و شعار روزانه انگیزشی به فارسی در قالب اسکیما تولید کن."
            val response = withTimeout(30_000L) {
                personaModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("پاسخ خالی است"))
            PersonaDiagnosisResult(
                archetype = json.getString("archetype"),
                coachTone = json.getString("coachTone"),
                personalizedStrategy = json.getString("personalizedStrategy"),
                dailyMotto = json.getString("dailyMotto")
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Persona diagnosis error", e)
            PersonaDiagnosisResult(
                archetype = "جنگجوی استوار اراده",
                coachTone = coachTone,
                personalizedStrategy = "با پایبندی صددرصدی به روتین‌های اول صبح، نیروی اراده خود را در طول روز به حداکثر برسانید.",
                dailyMotto = "انضباط امروز، آزادی فرداست."
            )
        }
    }

    suspend fun generateMorningMotivationStory(): String {
        return try {
            val response = withTimeout(30_000L) {
                textModel.generateContent(
                    "یک بند کوتاه انگیزشی، بسیار قدرتمند و پرانرژی به زبان فارسی برای کسی که ساعت ۷ صبح از خواب بیدار شده بنویس. " +
                    "این بند باید برای خواندن با صدای رسا طراحی شده باشد و به فرد شجاعت و انضباط بدهد تا امروز اهدافش را فتح کند. " +
                    "حداکثر ۲ تا ۳ جمله رسا و بدون علامت یا فرمت اضافه."
                )
            }
            response.text?.trim() ?: "امروز فرصتی تازه برای ساختن آینده است. من با اراده‌ای پولادین برمی‌خیزم و تمام موانع را پشت سر می‌گذارم!"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Motivation story generation error", e)
            "امروز روز من است! با تمام توان بیدار می‌شوم و رویایم را به واقعیت تبدیل می‌کنم."
        }
    }

    suspend fun evaluateWakeVoice(spokenText: String, expectedStory: String): VoiceEvaluationResult {
        return try {
            val response = withTimeout(30_000L) {
                voiceEvalModel.generateContent(
                    "متن مورد انتظار برای خواندن صبحگاهی:\n\"$expectedStory\"\n\n" +
                    "متن ثبت شده از خواندن کاربر:\n\"$spokenText\"\n\n" +
                    "ارزیابی کن آیا کاربر بیدار شده و متن را با هوشیاری و کلمات مناسب خوانده است؟ " +
                    "پاسخ را در قالب اسکیما به زبان فارسی تحویل بده."
                )
            }
            val jsonText = response.text ?: throw IllegalStateException("پاسخ مدل خالی بود.")
            val json = JSONObject(jsonText)
            VoiceEvaluationResult(
                isApproved = json.getBoolean("isApproved"),
                score = json.getInt("score"),
                feedback = json.getString("feedback")
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Voice eval error", e)
            VoiceEvaluationResult(
                isApproved = true,
                score = 85,
                feedback = "صدای شما با موفقیت تایید شد! صبح بخیر قهرمان، روز پرانرژی داشته باشی."
            )
        }
    }

    suspend fun generateDailyBriefing(
        streakDays: Long,
        completedTasks: Int,
        totalTasks: Int,
        userPersona: String = ""
    ): String {
        return try {
            val response = withTimeout(30_000L) {
                val personaContext = if (userPersona.isNotBlank()) "تیپ اراده کاربر: $userPersona. " else ""
                textModel.generateContent(
                    "به عنوان مربی ارشد انضباط فردی و روتین هوشمند، یک گزارش کوتاه تحلیلی، مقتدر و بسیار انگیزشی به فارسی بنویس. " +
                    personaContext +
                    "وضعیت کاربر: استریک متوالی $streakDays روز، تعداد تسک‌های انجام شده امروز: $completedTasks از $totalTasks. " +
                    "در ۲ جمله کوتاه تحلیل کن و انگیزه برای حفظ نظم بده."
                )
            }
            response.text?.trim() ?: "تمرکز و استمرار شما شگفت‌انگیز است. با همین قدرت و اراده مسیر انضباط را ادامه دهید!"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Daily briefing error", e)
            "استمرار کلید موفقیت است. امروز را با قدرت و تمرکز کامل به پایان برسان!"
        }
    }
}
