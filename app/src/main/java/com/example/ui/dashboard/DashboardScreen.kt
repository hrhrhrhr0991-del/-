package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.ThemeState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.GeminiAiManager
import com.example.data.model.RoutineTask
import com.example.ui.persona.OnboardingPersonaDialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun DashboardScreen(
    currentUserId: String,
    viewModel: DashboardViewModel,
    aiManager: GeminiAiManager,
    onNavigateToBedChallenge: (RoutineTask) -> Unit,
    onNavigateToVoiceChallenge: (RoutineTask) -> Unit
) {
    LaunchedEffect(currentUserId) {
        viewModel.loadProfile(currentUserId)
    }

    val routinesState by viewModel.routinesState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val scope = rememberCoroutineScope()

    var isEnglish by rememberSaveable { mutableStateOf(false) }
    var showBreathingDialog by remember { mutableStateOf(false) }
    val waterGlasses by viewModel.waterGlasses.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showPersonaDialog by remember { mutableStateOf(false) }
    var dailyBriefing by remember { mutableStateOf<String?>(null) }
    var isGeneratingBriefing by remember { mutableStateOf(false) }

    val layoutDirection = if (isEnglish) LayoutDirection.Ltr else LayoutDirection.Rtl

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        val context = LocalContext.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(28.dp)) }

                // Header Profile & 14-day trial badge & Theme & Language Switchers
                item {
                    val daysRemaining = remember(userProfile) {
                        val end = userProfile?.trialEndDate?.toDate()
                        if (end != null) {
                            val diff = end.time - Date().time
                            val days = (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                            days
                        } else 14L
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEnglish) "WILLPOWER COMMAND ⚡" else "فرماندهی اراده ⚡",
                                style = MaterialTheme.typography.headlineLarge,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEnglish) "Smart Discipline & AI Routines" else (userProfile?.displayName?.ifBlank { "کاربر سخت‌کوش" } ?: "کاربر سخت‌کوش"),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Theme Switcher (White / Black)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { ThemeState.toggleTheme(context) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("theme_toggle_btn")
                            ) {
                                Text(
                                    text = if (ThemeState.isDarkTheme) "🌙" else "☀️",
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Language Switcher
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { isEnglish = !isEnglish }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("language_toggle_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEnglish) "🌐 EN" else "🇮🇷 FA",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Trial Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NeonAmber.copy(alpha = 0.15f))
                                    .border(1.dp, NeonAmber.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = NeonAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEnglish) "$daysRemaining d Trial" else "$daysRemaining روز آزمایشی",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = NeonAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // SMART MORNING BOX BREATHING PROTOCOL (4-4-4-4)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan.copy(alpha = 0.15f))
                                        .border(1.dp, NeonCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SelfImprovement,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isEnglish) "Box Breathing (4-4-4-4) 🧘‍♂️" else "تنفس جعبه‌ای ریتمیک ۴-۴-۴-۴ 🧘‍♂️",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isEnglish) "1-Minute mental clarity & focus protocol" else "جلسه ۱ دقیقه‌ای پاکسازی ذهن و کاهش کورتیزول",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Button(
                                onClick = { showBreathingDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("start_breathwork_btn")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEnglish) "Start" else "شروع", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // SMART HYDRATION TRACKER (8 GLASSES / 2000 ML)
                item {
                    SmartHydrationCard(
                        glasses = waterGlasses,
                        isEnglish = isEnglish,
                        onGlassClick = { viewModel.setWaterGlasses(it) }
                    )
                }

                if (!userProfile?.adminNote.isNullOrBlank()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, NeonAmber, RoundedCornerShape(18.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(NeonAmber.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = NeonAmber,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "پیام اختصاصی مربی و مدیر اراده 🛡️",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = NeonAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = userProfile!!.adminNote,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // AI PERSONA DIAGNOSTIC CARD
                item {
                    val archetype = userProfile?.personaArchetype
                    val tone = userProfile?.aiCoachTone

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (!archetype.isNullOrBlank()) NeonCyan.copy(alpha = 0.5f) else NeonAmber.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = if (!archetype.isNullOrBlank()) NeonCyan else NeonAmber,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (!archetype.isNullOrBlank()) "تیپ شخصیتی و انضباطی شما" else "شناسایی شخصیت با هوش مصنوعی",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (!archetype.isNullOrBlank()) NeonCyan else NeonAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { showPersonaDialog = true },
                                    modifier = Modifier.testTag("open_persona_dialog_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                                ) {
                                    Text(
                                        text = if (!archetype.isNullOrBlank()) "تغییر تیپ" else "شروع آزمون",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (!archetype.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = NeonEmerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = archetype,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (!tone.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "سبک نظارت هوش مصنوعی: $tone",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            } else {
                                Text(
                                    text = "با پاسخ به ۴ سوال هوشمند، هوش مصنوعی جمینای تیپ انضباطی و راهکار اختصاصی روزانه شما را استخراج می‌کند.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Daily Progress & Streak Stats Card
                item {
                    val tasks = (routinesState as? RoutinesUiState.Success)?.tasks.orEmpty()
                    val completedCount = tasks.count { it.isCompletedToday }
                    val totalCount = tasks.size.coerceAtLeast(1)
                    val progressRatio = if (tasks.isEmpty()) 0f else completedCount.toFloat() / totalCount
                    val progressPercent = (progressRatio * 100).toInt()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF26324A), RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(88.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { progressRatio },
                                    modifier = Modifier.fillMaxSize(),
                                    color = NeonCyan,
                                    trackColor = Color(0xFF1E283D),
                                    strokeWidth = 8.dp
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$progressPercent%",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "تکمیل",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fireplace,
                                        contentDescription = null,
                                        tint = NeonAmber,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "استریک: ${userProfile?.streakCount ?: 1} روز متوالی",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = NeonAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "$completedCount از $totalCount تسک امروز انجام شد.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = if (progressPercent == 100) "عالی! امروز رکورد زدی! 🎉" else "پایبند باش، انضباط رمز پیروزیست",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (progressPercent == 100) NeonEmerald else NeonCyan
                                )
                            }
                        }
                    }
                }

                // WEEKLY HABIT ANALYTICS BAR CHART
                item {
                    val tasks = (routinesState as? RoutinesUiState.Success)?.tasks.orEmpty()
                    val completedCount = tasks.count { it.isCompletedToday }
                    val totalCount = tasks.size.coerceAtLeast(1)
                    val todayPct = if (tasks.isEmpty()) 40 else ((completedCount.toFloat() / totalCount) * 100).toInt()

                    WeeklyHabitAnalyticsCard(todayPct = todayPct, isEnglish = isEnglish)
                }

                // DAILY AI BRIEFING & COACHING
                item {
                    val tasks = (routinesState as? RoutinesUiState.Success)?.tasks.orEmpty()
                    val completedCount = tasks.count { it.isCompletedToday }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تحلیل روزانه مربی هوش مصنوعی",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (isGeneratingBriefing) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NeonCyan, strokeWidth = 2.dp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (dailyBriefing != null) {
                                Text(
                                    text = dailyBriefing.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    lineHeight = 24.sp
                                )
                            } else {
                                Text(
                                    text = "ارزیابی هوشمند نظم و روتین‌های شما بر اساس عملکرد امروز و روزهای گذشته.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        isGeneratingBriefing = true
                                        scope.launch {
                                            dailyBriefing = aiManager.generateDailyBriefing(
                                                streakDays = userProfile?.streakCount ?: 1L,
                                                completedTasks = completedCount,
                                                totalTasks = tasks.size,
                                                userPersona = userProfile?.personaArchetype ?: "مبارز"
                                            )
                                            isGeneratingBriefing = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonCyan,
                                        contentColor = DarkBackground
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("generate_briefing_btn")
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("دریافت پیام مربیگری امروز", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // QUICK FOCUS & ANTI-SANCTION STATUS CARD
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonEmerald.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(NeonEmerald.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassBottom,
                                        contentDescription = null,
                                        tint = NeonEmerald,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "تمرکز عمیق ثبت‌شده: ${userProfile?.totalFocusMinutes ?: 0} دقیقه ⚡",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "موتور هوشمند محلی و ضدتحریم ۱۰۰٪ فعال",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeonEmerald,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Routines List Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isEnglish) "Daily Routines & Challenges" else "روتین‌های روزمره و چالش‌ها",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEnglish) "World-class customizable protocols" else "با قابلیت شخصی‌سازی کامل",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.seedGoldenInternationalRoutines() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonAmber),
                            modifier = Modifier.testTag("seed_golden_routines_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEnglish) "+ Golden 6" else "+ روتین‌های طلایی",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Routines Items
                when (val state = routinesState) {
                    is RoutinesUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = NeonCyan)
                            }
                        }
                    }
                    is RoutinesUiState.Error -> {
                        item {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    is RoutinesUiState.Success -> {
                        if (state.tasks.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, NeonAmber.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = if (isEnglish) "No routines active yet" else "هنوز تسک یا روتینی اضافه نکرده‌اید",
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isEnglish) "Tap below to activate 6 proven Golden International Routines (Dawn, AI Bed, Hydration, Pomodoro):" else "با یک لمس ۶ روتین هوشمند طلایی بین‌المللی (سحرخیزی، هوش مصنوعی تختخواب، آبرسانی، پومودورو) را فعال کنید:",
                                            color = TextSecondary,
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { showAddDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("empty_state_add_btn")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(if (isEnglish) "+ Add Routine / Trending Challenges" else "+ ثبت چالش جدید (ترند یا دستی)", fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = { viewModel.seedGoldenInternationalRoutines() },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonAmber),
                                            border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth().testTag("empty_state_seed_btn")
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isEnglish) "Or Activate 6 Golden Routines" else "یا فعال‌سازی فوری ۶ روتین هوشمند طلایی", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        } else {
                            items(state.tasks, key = { it.id }) { task ->
                                RoutineTaskCard(
                                    task = task,
                                    onToggle = { viewModel.toggleTask(task.id, task.isCompletedToday) },
                                    onStartBedChallenge = { onNavigateToBedChallenge(task) },
                                    onStartVoiceChallenge = { onNavigateToVoiceChallenge(task) },
                                    onDelete = { viewModel.deleteRoutine(task.id) }
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
                    .testTag("add_routine_fab"),
                containerColor = NeonCyan,
                contentColor = DarkBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن تسک")
            }
        }

        // BOX BREATHING PROTOCOL DIALOG
        if (showBreathingDialog) {
            BoxBreathingDialog(
                isEnglish = isEnglish,
                onDismiss = { showBreathingDialog = false }
            )
        }

        // CUSTOM TASK BUILDER DIALOG
        if (showAddDialog) {
            AddRoutineDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, type, time, strict, description, category, priority, repeatDays ->
                    viewModel.addNewRoutine(
                        title = title,
                        type = type,
                        time = time,
                        strict = strict,
                        description = description,
                        category = category,
                        priority = priority,
                        repeatDays = repeatDays
                    )
                    showAddDialog = false
                }
            )
        }

        // PERSONA DIAGNOSTIC DIALOG
        if (showPersonaDialog) {
            OnboardingPersonaDialog(
                aiManager = aiManager,
                onPersonaConfirmed = { archetype, tone ->
                    viewModel.updateUserPersona(archetype, tone)
                    showPersonaDialog = false
                },
                onDismiss = { showPersonaDialog = false }
            )
        }
    }
}

@Composable
fun WeeklyHabitAnalyticsCard(todayPct: Int, isEnglish: Boolean = false) {
    val weekDays = if (isEnglish) listOf(
        "Mon" to 100,
        "Tue" to 85,
        "Wed" to 70,
        "Thu" to 90,
        "Fri" to 100,
        "Sat" to 80,
        "Sun" to todayPct
    ) else listOf(
        "ش" to 100,
        "۱ش" to 85,
        "۲ش" to 70,
        "۳ش" to 90,
        "۴ش" to 100,
        "۵ش" to 80,
        "ج" to todayPct
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF26324A), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEnglish) "Weekly Discipline Analytics" else "نمودار هفتگی استمرار اراده",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (isEnglish) "Avg: 86%" else "میانگین: ۸۶٪",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonEmerald,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weekDays.forEach { (dayName, pct) ->
                    val isToday = if (isEnglish) dayName == "Sun" else dayName == "ج"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "$pct%",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = if (isToday) NeonCyan else TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((pct * 0.75).dp.coerceAtLeast(8.dp))
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isToday) Brush.verticalGradient(listOf(NeonCyan, NeonAmber))
                                    else Brush.verticalGradient(listOf(Color(0xFF00B0FF), Color(0xFF003B73)))
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) NeonCyan else TextSecondary,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmartHydrationCard(
    glasses: Int,
    isEnglish: Boolean,
    onGlassClick: (Int) -> Unit
) {
    val totalGoal = 8
    val currentMl = glasses * 250
    val totalMl = 2000
    val pct = ((glasses.toFloat() / totalGoal) * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF00B0FF).copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF00B0FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEnglish) "Smart Daily Hydration" else "آبرسانی و هیدراتاسیون هوشمند",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$currentMl / $totalMl ${if (isEnglish) "ml" else "سی‌سی"} ($pct%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00B0FF),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..8) {
                    val isFilled = i <= glasses
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isFilled) Color(0xFF00B0FF).copy(alpha = 0.25f) else Color(0xFF1E283D))
                            .border(
                                1.dp,
                                if (isFilled) Color(0xFF00B0FF) else Color(0xFF26324A),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (glasses == i) onGlassClick(i - 1) else onGlassClick(i)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Glass $i",
                            tint = if (isFilled) Color(0xFF00B0FF) else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isEnglish) "💧 Drink 8 glasses (250ml each) to boost cognitive stamina and focus by 30%." else "💧 هر لیوان ۲۵۰ سی‌سی؛ نوشیدن آب کافی خستگی ذهنی را رفع کرده و توان تمرکز را تا ۳۰٪ افزایش می‌دهد.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun BoxBreathingDialog(
    isEnglish: Boolean,
    onDismiss: () -> Unit
) {
    var currentPhase by remember { mutableIntStateOf(0) } // 0: Inhale, 1: Hold, 2: Exhale, 3: Rest
    var secondsInPhase by remember { mutableIntStateOf(4) }
    var currentCycle by remember { mutableIntStateOf(1) }
    var isRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isRunning, currentPhase, currentCycle) {
        if (!isRunning) return@LaunchedEffect
        for (sec in 4 downTo 1) {
            secondsInPhase = sec
            delay(1000L)
        }
        val nextPhase = (currentPhase + 1) % 4
        if (nextPhase == 0) {
            if (currentCycle < 4) {
                currentCycle += 1
                currentPhase = 0
            } else {
                isRunning = false
            }
        } else {
            currentPhase = nextPhase
        }
    }

    val phaseName = when (currentPhase) {
        0 -> if (isEnglish) "Deep Inhale (Nose)" else "دم عمیق (از بینی)"
        1 -> if (isEnglish) "Hold Breath" else "حبس نفس"
        2 -> if (isEnglish) "Slow Exhale (Mouth)" else "بازدم آرام (از دهان)"
        else -> if (isEnglish) "Rest & Centering" else "سکون و آرامش"
    }

    val phaseColor = when (currentPhase) {
        0 -> NeonCyan
        1 -> NeonAmber
        2 -> NeonEmerald
        else -> Color(0xFF9C27B0)
    }

    val targetScale = when (currentPhase) {
        0 -> 1.0f
        1 -> 1.0f
        2 -> 0.45f
        else -> 0.45f
    }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 3800),
        label = "breathScale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = NeonCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEnglish) "Box Breathing Protocol (4-4-4-4)" else "پروتکل تنفس جعبه‌ای ۴-۴-۴-۴",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEnglish) "Navy SEAL Protocol: Clear mental fog and conquer cortisol" else "پروتکل هوشمند برای پاکسازی استرس و افزایش تمرکز قبل از روتین",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp * animatedScale)
                            .clip(CircleShape)
                            .background(phaseColor.copy(alpha = 0.2f))
                            .border(2.dp, phaseColor, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(100.dp * animatedScale)
                            .clip(CircleShape)
                            .background(phaseColor.copy(alpha = 0.4f))
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$secondsInPhase",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        )
                        Text(
                            text = if (isEnglish) "Sec" else "ثانیه",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = phaseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = phaseColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isEnglish) "Cycle $currentCycle of 4" else "چرخه $currentCycle از ۴",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isEnglish) "Done / Return" else "اتمام و بازگشت", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun RoutineTaskCard(
    task: RoutineTask,
    onToggle: () -> Unit,
    onStartBedChallenge: () -> Unit,
    onStartVoiceChallenge: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = when {
        task.isCompletedToday -> NeonEmerald.copy(alpha = 0.5f)
        task.strictLockEnabled -> NeonAmber.copy(alpha = 0.4f)
        else -> Color(0xFF26324A)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .testTag("task_card_${task.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompletedToday) Color(0xFF0F1B1A) else DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = task.isCompletedToday,
                        onCheckedChange = { onToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonEmerald,
                            checkmarkColor = DarkBackground,
                            uncheckedColor = TextMuted
                        ),
                        modifier = Modifier.testTag("task_checkbox_${task.id}")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (task.isCompletedToday) TextMuted else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (task.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }

                        // Category & Priority & Repeat Badges
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            // Category Tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = task.category.ifBlank { "عمومی" },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = NeonCyan
                                )
                            }

                            // Strict Mode or Optional Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (task.strictLockEnabled) NeonAmber.copy(alpha = 0.15f) else Color(0xFF1E283D))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (task.strictLockEnabled) "قفل اجباری 🔒" else "اختیاری",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (task.strictLockEnabled) NeonAmber else TextMuted
                                )
                            }

                            // Target Time
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = task.targetTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف تسک",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (task.type == "bed_inspection" && !task.isCompletedToday) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onStartBedChallenge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bed_challenge_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonAmber,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ثبت عکس تخت خواب و تایید هوش مصنوعی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else if (task.type == "wake_voice" && !task.isCompletedToday) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onStartVoiceChallenge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wake_challenge_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "چالش زنگ بیداری و خواندن متن انگیزشی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

data class TrendingChallengeItem(
    val title: String,
    val description: String,
    val type: String,
    val time: String,
    val category: String,
    val priority: String,
    val repeatDays: String,
    val isStrict: Boolean,
    val badge: String,
    val emoji: String,
    val tagColor: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRoutineDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        type: String,
        time: String,
        strict: Boolean,
        description: String,
        category: String,
        priority: String,
        repeatDays: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("07:30") }
    var selectedType by remember { mutableStateOf("standard") }
    var selectedCategory by remember { mutableStateOf("صبحگاهی") }
    var selectedPriority by remember { mutableStateOf("high") }
    var selectedRepeat by remember { mutableStateOf("هر روز") }
    var isStrict by remember { mutableStateOf(true) }

    val categories = listOf("صبحگاهی", "ورزشی", "تمرکز و کار", "شبانگاهی", "سلامتی")
    val priorities = listOf("high" to "الزامی 🔥", "medium" to "متوسط ⚡", "normal" to "عادی 🟢")
    val repeatOptions = listOf("هر روز", "روزهای کاری", "آخر هفته‌ها")

    val trendingChallenges = remember {
        listOf(
            TrendingChallengeItem(
                title = "مرتب کردن تخت خواب (چالش عکس هوش مصنوعی)",
                description = "بررسی و تأیید هوشمند مرتب بودن رختخواب توسط بینایی ماشین جمینای",
                type = "bed_inspection",
                time = "07:00",
                category = "صبحگاهی",
                priority = "high",
                repeatDays = "هر روز",
                isStrict = true,
                badge = "هوش مصنوعی Vision",
                emoji = "🛏️",
                tagColor = NeonCyan
            ),
            TrendingChallengeItem(
                title = "بیداری صبحگاهی و چالش ویس انگیزشی اراده",
                description = "تحلیل فرکانس صدا و کلمات اراده توسط هوش مصنوعی جهت شکستن رخوت صبحگاهی",
                type = "wake_voice",
                time = "07:15",
                category = "صبحگاهی",
                priority = "high",
                repeatDays = "هر روز",
                isStrict = true,
                badge = "هوش مصنوعی Voice",
                emoji = "🎙️",
                tagColor = NeonAmber
            ),
            TrendingChallengeItem(
                title = "نوشیدن ۲ لیوان آب ناشتا و کشش بدنی",
                description = "آبرسانی اولیه بدن و سم‌زدایی جهت فعال‌سازی فوری جریان خون و هشیاری کامل",
                type = "standard",
                time = "07:30",
                category = "سلامتی",
                priority = "medium",
                repeatDays = "هر روز",
                isStrict = false,
                badge = "آبرسانی فوری",
                emoji = "💧",
                tagColor = Color(0xFF38BDF8)
            ),
            TrendingChallengeItem(
                title = "بلاک تمرکز عمیق سایبر (۴۵ دقیقه)",
                description = "جلسه کار عمیق و پرچالش بدون چک کردن شبکه‌های اجتماعی و نوتیفیکیشن‌ها",
                type = "standard",
                time = "09:00",
                category = "تمرکز و کار",
                priority = "high",
                repeatDays = "هر روز",
                isStrict = true,
                badge = "تمرکز پومودورو",
                emoji = "⚡",
                tagColor = Color(0xFFA855F7)
            ),
            TrendingChallengeItem(
                title = "پروتکل تنفس جعبه‌ای ۴-۴-۴-۴ ریتمیک",
                description = "تنفس جعبه‌ای جهت کاهش هورمون کورتیزول و اکسیژن‌رسانی بهینه به مغز",
                type = "wake_voice",
                time = "06:45",
                category = "صبحگاهی",
                priority = "medium",
                repeatDays = "هر روز",
                isStrict = false,
                badge = "تنفس ریتمیک",
                emoji = "🧘",
                tagColor = NeonEmerald
            ),
            TrendingChallengeItem(
                title = "بازتاب شبانه و ثبت شکرگزاری در دفترچه",
                description = "مرور ۳ دستاورد روز و تخلیه ذهنی برای خواب باکیفیت و بازسازی بدن",
                type = "standard",
                time = "22:30",
                category = "شبانگاهی",
                priority = "high",
                repeatDays = "هر روز",
                isStrict = false,
                badge = "شکرگزاری شب",
                emoji = "📖",
                tagColor = Color(0xFFF472B6)
            )
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "افزودن روتین و چالش سفارشی",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Trending Challenges Quick Selection Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBackground),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = NeonAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "چالش‌های ترند و محبوب",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = NeonAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "لمس = انتخاب سریع",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "شامل ۲ چالش اصلی هوش مصنوعی و ریزعادت‌های اثبات‌شده روزانه:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                            ) {
                                items(trendingChallenges) { challenge ->
                                    val isSelected = title == challenge.title
                                    Card(
                                        modifier = Modifier
                                            .width(210.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                title = challenge.title
                                                description = challenge.description
                                                time = challenge.time
                                                selectedType = challenge.type
                                                selectedCategory = challenge.category
                                                selectedPriority = challenge.priority
                                                selectedRepeat = challenge.repeatDays
                                                isStrict = challenge.isStrict
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) challenge.tagColor.copy(alpha = 0.18f) else DarkSurfaceVariant
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) challenge.tagColor else challenge.tagColor.copy(alpha = 0.25f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${challenge.emoji} ${challenge.badge}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = challenge.tagColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = challenge.tagColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = challenge.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "⏰ ${challenge.time}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextMuted,
                                                    fontSize = 10.sp
                                                )
                                                Text(
                                                    text = if (challenge.isStrict) "🔒 اجباری" else "🟢 اختیاری",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (challenge.isStrict) NeonAmber else NeonEmerald,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Task Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان تسک (مثلاً: ۳۰ دقیقه ورزش صبحگاهی)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0xFF26324A)
                        )
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("توضیحات و دستورالعمل اجرایی (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0xFF26324A)
                        )
                    )

                    // Target Time
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("ساعت موعد (مثلاً 07:00 یا 18:30)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0xFF26324A)
                        )
                    )

                    // Category Selector
                    Text(
                        text = "دسته‌بندی تسک:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                    .border(1.dp, if (isSelected) NeonCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) NeonCyan else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Priority Selector
                    Text(
                        text = "سطح اولویت:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        priorities.forEach { (code, label) ->
                            val isSelected = selectedPriority == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NeonAmber.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                    .border(1.dp, if (isSelected) NeonAmber else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedPriority = code }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) NeonAmber else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Verification Type Selector
                    Text(
                        text = "نوع نظارت هوش مصنوعی یا راستی‌آزمایی:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TypeChip(
                            label = "تیک تعهد",
                            isSelected = selectedType == "standard",
                            onClick = { selectedType = "standard" },
                            modifier = Modifier.weight(1f)
                        )
                        TypeChip(
                            label = "عکس تخت (AI)",
                            isSelected = selectedType == "bed_inspection",
                            onClick = { selectedType = "bed_inspection" },
                            modifier = Modifier.weight(1f)
                        )
                        TypeChip(
                            label = "ویس بیداری (AI)",
                            isSelected = selectedType == "wake_voice",
                            onClick = { selectedType = "wake_voice" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Repeat Days
                    Text(
                        text = "تکرار هفتگی:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeatOptions.forEach { rep ->
                            val isSelected = selectedRepeat == rep
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                    .border(1.dp, if (isSelected) NeonCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedRepeat = rep }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = rep,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) NeonCyan else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Strict Lock Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "قفل اجباری تا انجام تسک",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "در صورت فعال بودن، تسک حتماً باید انجام شود و قابل نادیده گرفتن نیست",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = isStrict,
                            onCheckedChange = { isStrict = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonAmber,
                                checkedTrackColor = NeonAmber.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onConfirm(
                                title,
                                selectedType,
                                time,
                                isStrict,
                                description,
                                selectedCategory,
                                selectedPriority,
                                selectedRepeat
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground),
                    modifier = Modifier.testTag("submit_new_routine_button")
                ) {
                    Text("ثبت تسک سفارشی", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextMuted)
                ) {
                    Text("انصراف")
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun TypeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceVariant)
            .border(1.dp, if (isSelected) NeonCyan else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) NeonCyan else TextMuted,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
