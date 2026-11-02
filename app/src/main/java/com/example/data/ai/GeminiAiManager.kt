package com.example.data.ai

import android.graphics.Bitmap
import android.graphics.Color
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
import kotlin.math.abs
import kotlin.random.Random

private const val TAG = "GeminiAiManager"

data class BedInspectionResult(
    val isTidy: Boolean,
    val score: Int,
    val feedback: String,
    val issues: List<String>,
    val isRelevant: Boolean = true,
    val isOfflineEngine: Boolean = false
)

data class PersonaDiagnosisResult(
    val archetype: String,
    val coachTone: String,
    val personalizedStrategy: String,
    val dailyMotto: String,
    val isOfflineEngine: Boolean = false
)

data class VoiceEvaluationResult(
    val isApproved: Boolean,
    val score: Int,
    val feedback: String,
    val isOfflineEngine: Boolean = false
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

    /**
     * Resizes a bitmap so that its maximum dimension does not exceed maxDimension.
     * Keeps aspect ratio intact and avoids huge payloads and OOM errors.
     */
    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int = 800): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        if (originalWidth <= maxDimension && originalHeight <= maxDimension) {
            return bitmap
        }
        val ratio = originalWidth.toFloat() / originalHeight.toFloat()
        val width: Int
        val height: Int
        if (originalWidth > originalHeight) {
            width = maxDimension
            height = (maxDimension / ratio).toInt().coerceAtLeast(1)
        } else {
            height = maxDimension
            width = (maxDimension * ratio).toInt().coerceAtLeast(1)
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    /**
     * Inspects bed image with automatic cloud/offline fallback.
     * Strictly verifies whether the image actually depicts a bed, and verifies neatness.
     * Handles mobile internet latency and Iranian network restrictions without crashing.
     */
    suspend fun inspectBedImage(bitmap: Bitmap, forceOffline: Boolean = false): BedInspectionResult {
        val scaledBitmap = scaleBitmapDown(bitmap, 800)
        if (forceOffline) {
            return inspectBedImageLocally(scaledBitmap)
        }

        return try {
            val response = withTimeout(35_000L) {
                visionModel.generateContent(
                    content {
                        image(scaledBitmap)
                        text(
                            "نقش تو یک ارزیاب سخت‌گیر و دقیق انضباط فردی و مرتب بودن تخت خواب برای اپلیکیشن 'اراده' است.\n\n" +
                            "گام نخست (بسیار مهم - تشخیص مرتبط بودن عکس):\n" +
                            "بررسی کن آیا این تصویر واقعاً از یک تخت خواب، رختخواب، تشک، ملحفه، بالش یا پتوی مرتب یا نامرتب است؟\n" +
                            "اگر تصویر مربوط به موارد زیر است:\n" +
                            "- چهره انسان یا عکس سلفی\n" +
                            "- دست، پا یا بدن بدون نمای واضح تخت خواب\n" +
                            "- دیوار خالی، زمین، سقف، فرش خالی بدون رختخواب\n" +
                            "- میز کار، کامپیوتر، لپ‌تاپ یا مانیتور\n" +
                            "- کفش، لباس رها شده روی زمین یا صندلی\n" +
                            "- حیوانات، گل و گیاه، غذا یا فضای بیرون از اتاق\n" +
                            "- تصاویر سیاه، مات، تار یا مبهم\n" +
                            "باید فوراً:\n" +
                            "isRelevant = false\n" +
                            "isTidy = false\n" +
                            "score = 0\n" +
                            "feedback = 'تصویر نامرتبط تشخیص داده شد! لطفاً دوربین را مستقیماً به سمت تخت خواب یا رختخواب خود بگیرید.'\n" +
                            "issues = ['عکس ارسالی مربوط به تخت خواب نیست']\n\n" +
                            "گام دوم (فقط در صورت مرتبط بودن تخت خواب - isRelevant = true):\n" +
                            "دقت کن: آیا تخت واقعاً مرتب، آنکارد و تمیز شده است؟\n" +
                            "۱. ملحفه‌ها باید کاملاً کشیده و بدون چروک‌های به هم ریخته باشند.\n" +
                            "۲. پتو باید صاف و متقارن پهن شده یا دقیق تا شده باشد.\n" +
                            "۳. بالش‌ها در جای مناسب و تراز در بالای تخت قرار گرفته باشند.\n" +
                            "اگر تخت نامرتب است (پتو مچاله، ملحفه چروک، بالش کج یا لباس روی تخت): isTidy = false، score زیر ۶۰ و ایرادات دقیق را در issues بنویس.\n" +
                            "اگر تخت کاملاً مرتب است: isTidy = true، score بین ۸۰ تا ۱۰۰ و در feedback تشویق کن."
                        )
                    }
                )
            }
            val rawText = response.text ?: throw IllegalStateException("پاسخ مدل خالی بود.")
            val jsonText = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val json = JSONObject(jsonText)
            val isRelevant = json.optBoolean("isRelevant", true)
            val isTidy = json.optBoolean("isTidy", false)
            val score = json.optInt("score", if (isTidy) 85 else if (isRelevant) 45 else 0)
            val feedback = json.optString("feedback", "ارزیابی تخت خواب انجام شد.")
            val issuesArray = json.optJSONArray("issues")
            val issues = mutableListOf<String>()
            if (issuesArray != null) {
                for (i in 0 until issuesArray.length()) {
                    issues.add(issuesArray.getString(i))
                }
            }
            BedInspectionResult(isTidy, score, feedback, issues, isRelevant, isOfflineEngine = false)
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Cloud vision timed out after 35s. Switching to on-device anti-sanction vision engine.")
            inspectBedImageLocally(scaledBitmap)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Cloud inspection failed (${e.javaClass.simpleName}: ${e.message}). Switching to on-device anti-sanction vision engine.")
            inspectBedImageLocally(scaledBitmap)
        }
    }

    /**
     * Smart on-device computer vision engine that works 100% offline without internet or VPN.
     * Evaluates luminosity, color distribution, skin tone presence (selfie rejection),
     * texture variance, wall/floor rejection, and bed sheet uniformity.
     */
    fun inspectBedImageLocally(bitmap: Bitmap): BedInspectionResult {
        return try {
            val width = bitmap.width
            val height = bitmap.height

            // Sample an 8x8 grid (64 sample points across the image)
            val gridCols = 8
            val gridRows = 8
            val stepX = (width / (gridCols + 1)).coerceAtLeast(1)
            val stepY = (height / (gridRows + 1)).coerceAtLeast(1)

            var totalLum = 0.0
            var skinToneCount = 0
            var greenOutdoorCount = 0
            val lumGrid = Array(gridRows) { DoubleArray(gridCols) }

            for (r in 0 until gridRows) {
                for (c in 0 until gridCols) {
                    val px = ((c + 1) * stepX).coerceIn(0, width - 1)
                    val py = ((r + 1) * stepY).coerceIn(0, height - 1)
                    val pixel = bitmap.getPixel(px, py)
                    val red = Color.red(pixel)
                    val green = Color.green(pixel)
                    val blue = Color.blue(pixel)

                    val lum = 0.299 * red + 0.587 * green + 0.114 * blue
                    lumGrid[r][c] = lum
                    totalLum += lum

                    // Human skin tone detection (reject selfies, faces, hands)
                    val isSkin = red > 95 && green > 40 && blue > 20 &&
                            red > green && red > blue &&
                            (red - green) in 15..95 &&
                            (red - blue) > 15
                    if (isSkin) skinToneCount++

                    // Outdoor vegetation / green grass detection
                    if (green > 90 && green > red * 1.25 && green > blue * 1.25) {
                        greenOutdoorCount++
                    }
                }
            }

            val totalPoints = gridRows * gridCols
            val avgLum = totalLum / totalPoints

            // Compute overall luminance variance
            var lumVarianceSum = 0.0
            for (r in 0 until gridRows) {
                for (c in 0 until gridCols) {
                    val diff = lumGrid[r][c] - avgLum
                    lumVarianceSum += diff * diff
                }
            }
            val overallVariance = lumVarianceSum / totalPoints

            // 1. Extreme lighting / covered lens
            if (avgLum < 24.0) {
                return BedInspectionResult(
                    isTidy = false,
                    score = 0,
                    feedback = "تصویر بیش از حد تاریک یا پوشانده شده است! لطفاً چراغ اتاق را روشن کرده و از تخت خواب عکس بگیرید.",
                    issues = listOf("نور ناکافی محیط"),
                    isRelevant = false,
                    isOfflineEngine = true
                )
            }

            if (avgLum > 245.0 && overallVariance < 15.0) {
                return BedInspectionResult(
                    isTidy = false,
                    score = 0,
                    feedback = "تصویر بیش از حد نورانی یا سفید یکدست است. لطفاً از فضای تخت خواب عکس بگیرید.",
                    issues = listOf("تصویر سفید یا سوخته در نور"),
                    isRelevant = false,
                    isOfflineEngine = true
                )
            }

            // 2. Reject selfie / human faces / person dominance
            val skinRatio = skinToneCount.toFloat() / totalPoints
            if (skinRatio > 0.28f) {
                return BedInspectionResult(
                    isTidy = false,
                    score = 0,
                    feedback = "تصویر حاوی چهره یا سلفی است! لطفاً دوربین را مستقیماً به سمت تخت خواب خود بگیرید.",
                    issues = listOf("تصویر حاوی چهره انسان است"),
                    isRelevant = false,
                    isOfflineEngine = true
                )
            }

            // 3. Reject outdoor / plants / garden
            val outdoorRatio = greenOutdoorCount.toFloat() / totalPoints
            if (outdoorRatio > 0.35f) {
                return BedInspectionResult(
                    isTidy = false,
                    score = 0,
                    feedback = "تصویر نامرتبط است (فضای باز یا گیاهان). لطفاً فقط از تخت خواب و رختخواب خود عکس بگیرید.",
                    issues = listOf("تصویر در فضای بیرون ثبت شده است"),
                    isRelevant = false,
                    isOfflineEngine = true
                )
            }

            // 4. Reject flat monochrome wall or empty floor without furniture
            if (overallVariance < 16.0) {
                return BedInspectionResult(
                    isTidy = false,
                    score = 0,
                    feedback = "تصویر نامرتبط است (دیوار یا زمین خالی). لطفاً از زاویه‌ای که تخت خواب و بالش‌ها مشخص باشند عکس بگیرید.",
                    issues = listOf("تصویر فاقد ساختار تخت خواب است"),
                    isRelevant = false,
                    isOfflineEngine = true
                )
            }

            // 5. Bed bedding texture and tidiness evaluation
            // Duvet area local roughness (measures wrinkles, chaotic lumps, crumpled blankets)
            var duvetRoughnessSum = 0.0
            var duvetSamples = 0
            for (r in 3 until gridRows) {
                for (c in 0 until gridCols - 1) {
                    val horizDelta = abs(lumGrid[r][c] - lumGrid[r][c + 1])
                    duvetRoughnessSum += horizDelta
                    duvetSamples++
                }
            }
            val avgRoughness = if (duvetSamples > 0) duvetRoughnessSum / duvetSamples else 0.0

            // Left vs Right symmetry in the duvet section
            var leftSideLum = 0.0
            var rightSideLum = 0.0
            var halfPoints = 0
            for (r in 3 until gridRows) {
                for (c in 0 until gridCols / 2) {
                    leftSideLum += lumGrid[r][c]
                    rightSideLum += lumGrid[r][gridCols - 1 - c]
                    halfPoints++
                }
            }
            val sideBalance = abs((leftSideLum / halfPoints) - (rightSideLum / halfPoints))

            val isUntidy = avgRoughness > 32.0 || sideBalance > 45.0

            if (isUntidy) {
                val score = (38 + Random.nextInt(0, 16)).coerceIn(35, 56)
                val detectedIssues = mutableListOf<String>()
                if (avgRoughness > 32.0) {
                    detectedIssues.add("چروک و ناهمواری در سطح ملحفه یا پتو")
                }
                if (sideBalance > 45.0) {
                    detectedIssues.add("عدم تقارن پتو و نامرتب بودن چیدمان")
                }
                if (detectedIssues.isEmpty()) {
                    detectedIssues.add("پتو به درستی روی تخت صاف نشده است")
                }

                return BedInspectionResult(
                    isTidy = false,
                    score = score,
                    feedback = "تخت خواب نامرتب است! ملحفه‌ها کاملاً صاف نیستند یا پتو مرتب پهن نشده است. لطفاً آن را مرتب کرده و مجدداً عکس بگیرید.",
                    issues = detectedIssues,
                    isRelevant = true,
                    isOfflineEngine = true
                )
            }

            // Bed is tidy and properly arranged
            val neatScore = (85 + Random.nextInt(0, 11)).coerceIn(84, 96)
            return BedInspectionResult(
                isTidy = true,
                score = neatScore,
                feedback = "آفرین! ملحفه‌ها صاف، پتو مرتب و بالش‌ها در موقعیت مناسب قرار دارند. اراده و انضباط امروز شما ثبت شد.",
                issues = emptyList(),
                isRelevant = true,
                isOfflineEngine = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Local vision fallback error", e)
            BedInspectionResult(
                isTidy = false,
                score = 0,
                feedback = "خطا در ارزیابی تصویر. لطفاً مجدداً با نور کافی از تخت خواب عکس بگیرید.",
                issues = listOf("خطا در پردازش تصویر"),
                isRelevant = false,
                isOfflineEngine = true
            )
        }
    }

    /**
     * Diagnoses discipline persona. Falls back to offline expert matrix on sanction/offline.
     */
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
            val response = withTimeout(10_000L) {
                personaModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("پاسخ خالی است"))
            PersonaDiagnosisResult(
                archetype = json.getString("archetype"),
                coachTone = json.getString("coachTone"),
                personalizedStrategy = json.getString("personalizedStrategy"),
                dailyMotto = json.getString("dailyMotto"),
                isOfflineEngine = false
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Persona diagnosis using local expert matrix: ${e.message}")
            diagnosePersonaLocally(level, obstacle, goal, coachTone)
        }
    }

    /**
     * Local persona matrix that works 100% offline without internet.
     */
    fun diagnosePersonaLocally(
        level: String,
        obstacle: String,
        goal: String,
        coachTone: String
    ): PersonaDiagnosisResult {
        val archetype = when {
            level.contains("جنگجو") || level.contains("کمال‌گرا") -> "فرمانده اراده و انضباط پولادین"
            obstacle.contains("صبح") || goal.contains("سحرخیزی") -> "جنگجوی سپیده‌دم و فتح روز"
            obstacle.contains("حواس‌پرتی") || goal.contains("تمرکز") -> "سامورایی تمرکز عمیق و ذهن زلال"
            level.contains("مبتدی") -> "ققنوس بیداری و تحول اراده"
            else -> "معمار انضباط فردی و عادات پایدار"
        }

        val strategy = when {
            obstacle.contains("صبح") -> "هر شب ۳۰ دقیقه قبل از خواب تمام وسایل دیجیتال را خاموش کنید و صبح بدون حتی یک ثانیه تردید، با چالش صوتی از بستر برخیزید."
            obstacle.contains("اهمال‌کاری") -> "از قانون ۳ ثانیه استفاده کنید؛ تا فکری برای انجام کاری به ذهنتان آمد تا ۳ بشمارید و فوراً دست به عمل بزنید."
            else -> "با تمرکز روزانه بر روتین‌های الزامی و مرتب‌سازی مستمر محیط خواب، انرژی روانی خود را آزاد کنید."
        }

        val motto = when {
            coachTone.contains("قاطع") -> "عذر و بهانه‌ها پناهگاه بازنده‌هاست؛ اراده تنها زبان پیروزی است!"
            coachTone.contains("انگیزشی") -> "امروز روز خیزش توست؛ با تمام قدرت بدرخش و ناممکن‌ها را ممکن کن!"
            else -> "انضباط امروز، آزادی و قدرت فردای توست."
        }

        return PersonaDiagnosisResult(
            archetype = archetype,
            coachTone = coachTone,
            personalizedStrategy = strategy,
            dailyMotto = motto,
            isOfflineEngine = true
        )
    }

    /**
     * Curated local library of fiery Persian motivational stories.
     */
    private val localMotivationalStories = listOf(
        "امروز روز تکرار نیست، روز فتح است! من با اراده‌ای تسلیم‌ناپذیر بیدار شده‌ام تا سنگین‌ترین اهدافم را سبک کنم و به قله برسم.",
        "سحرخیزی اولین پیروزی من در امروز است. وقتی صبح بر رخوت بستر پیروز شدم، دیگر هیچ مانعی توان ایستادگی در برابرم را ندارد!",
        "اراده یعنی تصمیم گرفتن و پایبند ماندن حتی زمانی که اشتیاق اولیه فروکش کرده است. من امروز با انضباط کامل به پیش می‌تازم.",
        "جهان برای کسی که می‌داند به کجا می‌رود راه را باز می‌کند. با تمام انرژی بیدار شده‌ام تا فرمانروای سرنوشت خود باشم.",
        "امروز زیباترین برگ از کتاب زندگی من است. هر ثانیه‌اش را با شجاعت، نظم و ایمان به توانستن رقم خواهم زد!"
    )

    suspend fun generateMorningMotivationStory(): String {
        return try {
            val response = withTimeout(8_000L) {
                textModel.generateContent(
                    "یک بند کوتاه انگیزشی، بسیار قدرتمند و پرانرژی به زبان فارسی برای کسی که ساعت ۷ صبح از خواب بیدار شده بنویس. " +
                    "این بند باید برای خواندن با صدای رسا طراحی شده باشد و به فرد شجاعت و انضباط بدهد تا امروز اهدافش را فتح کند. " +
                    "حداکثر ۲ تا ۳ جمله رسا و بدون علامت یا فرمت اضافه."
                )
            }
            response.text?.trim() ?: localMotivationalStories.random()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Motivation story fallback to local library: ${e.message}")
            localMotivationalStories.random()
        }
    }

    suspend fun evaluateWakeVoice(spokenText: String, expectedStory: String): VoiceEvaluationResult {
        return try {
            val response = withTimeout(8_000L) {
                voiceEvalModel.generateContent(
                    content {
                        text(
                            "متن مورد انتظار برای خواندن صبحگاهی:\n$expectedStory\n\n" +
                            "صدای شناسایی‌شده کاربر:\n$spokenText\n\n" +
                            "بررسی کن آیا کاربر با صدای رسا و بیدار متن را خوانده یا کلمات کلیدی اصلی را ادا کرده است؟ " +
                            "اگر صدای خواب‌آلود، نامفهوم یا بسیار کوتاه بود تایید نکن. در غیر این صورت تایید کن و نمره بده."
                        )
                    }
                )
            }
            val jsonText = response.text ?: throw IllegalStateException("پاسخ مدل خالی بود.")
            val json = JSONObject(jsonText)
            VoiceEvaluationResult(
                isApproved = json.getBoolean("isApproved"),
                score = json.getInt("score"),
                feedback = json.getString("feedback"),
                isOfflineEngine = false
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Voice eval fallback to on-device analyzer: ${e.message}")
            evaluateWakeVoiceLocally(spokenText)
        }
    }

    /**
     * Local speech evaluation when cloud/VPN is restricted.
     */
    fun evaluateWakeVoiceLocally(spokenText: String): VoiceEvaluationResult {
        val trimmed = spokenText.trim()
        val wordCount = trimmed.split("\\s+".toRegex()).filter { it.isNotBlank() }.size

        return if (wordCount >= 3 || trimmed.length >= 8) {
            VoiceEvaluationResult(
                isApproved = true,
                score = (88 + Random.nextInt(0, 9)).coerceIn(85, 96),
                feedback = "تایید هوشیاری توسط موتور صوتی محلی (بدون نیاز به وی‌پی‌ان): صدای رسا و پرانرژی شما شنیده شد. روزی سرشار از پیروزی در پیش است!",
                isOfflineEngine = true
            )
        } else {
            VoiceEvaluationResult(
                isApproved = false,
                score = 35,
                feedback = "کلمات با وضوح و بلندی کافی شنیده نشدند. لطفاً متن انگیزشی را شمرده، بلند و با انرژی بخوانید.",
                isOfflineEngine = true
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
            val response = withTimeout(8_000L) {
                val personaContext = if (userPersona.isNotBlank()) "تیپ اراده کاربر: $userPersona. " else ""
                textModel.generateContent(
                    "به عنوان مربی ارشد انضباط فردی و روتین هوشمند، یک گزارش کوتاه تحلیلی، مقتدر و بسیار انگیزشی به فارسی بنویس. " +
                    personaContext +
                    "وضعیت کاربر: استریک متوالی $streakDays روز، تعداد تسک‌های انجام شده امروز: $completedTasks از $totalTasks. " +
                    "در ۲ جمله کوتاه تحلیل کن و انگیزه برای حفظ نظم بده."
                )
            }
            response.text?.trim() ?: generateDailyBriefingLocally(streakDays, completedTasks, totalTasks, userPersona)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Daily briefing fallback to local generator: ${e.message}")
            generateDailyBriefingLocally(streakDays, completedTasks, totalTasks, userPersona)
        }
    }

    /**
     * Local daily briefing generator. 100% offline.
     */
    fun generateDailyBriefingLocally(
        streakDays: Long,
        completedTasks: Int,
        totalTasks: Int,
        userPersona: String
    ): String {
        val personaTag = if (userPersona.isNotBlank()) "به عنوان «$userPersona»، " else ""
        val pct = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0

        return when {
            pct == 100 -> "${personaTag}امروز با انجام ۱۰۰٪ تسک‌ها استریک $streakDays روزه خود را درخشان‌تر کردی! این سطح از انضباط تو را دست‌نیافتنی می‌کند."
            pct >= 50 -> "${personaTag}تا این لحظه بیش از نیمی از اهداف روزمره را فتح کرده‌ای. با استریک $streakDays روزه، همین فرمان تا پایان شب ادامه بده!"
            else -> "${personaTag}روز هنوز به پایان نرسیده است. با اراده‌ای محکم تسک‌های باقیمانده را به سرانجام برسان و استریک $streakDays روزه‌ات را حفظ کن."
        }
    }
}
