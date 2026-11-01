package com.example.ui.persona

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.GeminiAiManager
import com.example.data.ai.PersonaDiagnosisResult
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

data class DiagnosticQuestion(
    val title: String,
    val description: String,
    val options: List<String>
)

@Composable
fun OnboardingPersonaDialog(
    aiManager: GeminiAiManager,
    onPersonaConfirmed: (archetype: String, tone: String) -> Unit,
    onDismiss: () -> Unit
) {
    val questions = remember {
        listOf(
            DiagnosticQuestion(
                title = "سطح فعلی انضباط و اراده شخصی شما کجاست؟",
                description = "پاسخ صادقانه به هوش مصنوعی کمک می‌کند فشار مناسبی به شما وارد کند.",
                options = listOf(
                    "مبتدی و نامنظم (به سختی کارها را به سرانجام می‌رسانم)",
                    "متوسط (انگیزه دارم اما استمرار و پایبندی ندارم)",
                    "خوب و متعهد (نیاز به یک ناظر سخت‌گیر برای قله‌های جدید دارم)",
                    "جنگجوی کمال‌گرا (به دنبال انضباط بی‌تردید و حداکثری)"
                )
            ),
            DiagnosticQuestion(
                title = "بزرگ‌ترین مانع یا نقطه ضعف روزمره شما چیست؟",
                description = "هوش مصنوعی روتین‌های سخت‌گیرانه‌تری برای این بخش طراحی خواهد کرد.",
                options = listOf(
                    "بیدار شدن سر موقع و خستگی مفرط صبحگاهی",
                    "اهمال‌کاری و انداختن کارهای مهم به ساعات پایانی شب",
                    "بی‌نظمی محیط، اتاق و شلختگی فضای استراحت",
                    "حواس‌پرتی و اتلاف وقت پای گوشی و فضای مجازی"
                )
            ),
            DiagnosticQuestion(
                title = "هدف اصلی شما در این چالش ۱۴ روزه اراده چیست؟",
                description = "نتیجه ملموسی که انتظار دارید به زندگی شما اضافه شود.",
                options = listOf(
                    "سحرخیزی بی‌چون‌وچرا و شروع آتشین روز ساعت ۷ صبح",
                    "نظم آهنین در فضای زندگی و انجام دقیق وظایف روزانه",
                    "افزایش دوچندان ساعت تمرکز عمیق کاری و درسی",
                    "ایجاد عادات ورزشی، رژیم انضباطی و سلامت جسمی"
                )
            ),
            DiagnosticQuestion(
                title = "لحن مربی‌گری هوش مصنوعی با شما چگونه باشد؟",
                description = "شخصیت هوش مصنوعی بر اساس این انتخاب با شما سخن خواهد گفت.",
                options = listOf(
                    "قاطع، بدون تعارف و بیزار از هرگونه بهانه‌تراشی 🔥",
                    "استراتژیک، منطقی و مبتنی بر بازخورد داده‌ای",
                    "انگیزشی، پرانرژی و الهام‌بخش",
                    "متعادل و مقتدر با رویکرد پله‌پله"
                )
            )
        )
    }

    var currentStep by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateOf(mutableMapOf<Int, String>()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var diagnosisResult by remember { mutableStateOf<PersonaDiagnosisResult?>(null) }
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = {
                if (!isAnalyzing) onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            containerColor = DarkSurface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "شناسایی ماتریس اراده فردی",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تحلیل هوشمند تیپ شخصیتی اراده",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isAnalyzing) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                modifier = Modifier.size(52.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "هوش مصنوعی در حال تحلیل پاسخ‌ها و طراحی تیپ شخصیتی شماست...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonCyan,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "استراتژی اختصاصی ۱۴ روزه برای شکستن مقاومت‌های ذهنی شما تدوین می‌شود.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    } else if (diagnosisResult != null) {
                        val result = diagnosisResult!!
                        // Show result card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, NeonEmerald, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkBackground),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = NeonEmerald,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "تیپ اراده تعیین‌شده:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = result.archetype,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = NeonEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NeonAmber.copy(alpha = 0.12f))
                                        .border(1.dp, NeonAmber.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "شعار انضباطی شما:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonAmber,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "«${result.dailyMotto}»",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "استراتژی شخصی‌سازی‌شده ۱۴ روزه:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = result.personalizedStrategy,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "سبک مربیگری:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = result.coachTone,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        // Current Question Step
                        val currentQ = questions[currentStep]
                        val stepProgress = (currentStep + 1).toFloat() / questions.size

                        LinearProgressIndicator(
                            progress = { stepProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NeonCyan,
                            trackColor = Color(0xFF1E283D)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "سوال ${currentStep + 1} از ${questions.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentQ.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentQ.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val selectedOption = answers.value[currentStep]

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            currentQ.options.forEach { option ->
                                val isSelected = selectedOption == option
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else DarkSurfaceVariant)
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonCyan else Color(0xFF2B374D),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            answers.value = answers.value.toMutableMap().apply {
                                                put(currentStep, option)
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, if (isSelected) NeonCyan else TextMuted, CircleShape)
                                                .background(if (isSelected) NeonCyan else Color.Transparent)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSelected) TextPrimary else TextSecondary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (diagnosisResult != null) {
                    Button(
                        onClick = {
                            val res = diagnosisResult!!
                            onPersonaConfirmed(res.archetype, res.coachTone)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonEmerald,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_persona_button")
                    ) {
                        Text("ثبت و اعمال بر روی سیستم اراده", fontWeight = FontWeight.Bold)
                    }
                } else if (!isAnalyzing) {
                    val hasAnswered = answers.value.containsKey(currentStep)
                    Button(
                        onClick = {
                            if (currentStep < questions.size - 1) {
                                currentStep++
                            } else {
                                // Last step -> Trigger AI analysis
                                isAnalyzing = true
                                scope.launch {
                                    val level = answers.value[0] ?: "متوسط"
                                    val obstacle = answers.value[1] ?: "بیدار شدن صبح"
                                    val goal = answers.value[2] ?: "سحرخیزی و نظم"
                                    val tone = answers.value[3] ?: "قاطع و جدی"

                                    val result = aiManager.diagnoseDisciplinePersona(
                                        level = level,
                                        obstacle = obstacle,
                                        goal = goal,
                                        coachTone = tone
                                    )
                                    diagnosisResult = result
                                    isAnalyzing = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        enabled = hasAnswered,
                        modifier = Modifier.testTag("persona_next_button")
                    ) {
                        Text(
                            text = if (currentStep < questions.size - 1) "مرحله بعد" else "تحلیل و صدور کارت اراده",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                if (diagnosisResult == null && !isAnalyzing) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("قبلی", color = TextSecondary)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("انصراف", color = TextMuted)
                        }
                    }
                }
            }
        )
    }
}
