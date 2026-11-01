package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.GeminiAiManager
import com.example.data.model.RoutineTask
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import com.example.ui.auth.AuthViewModel
import com.example.ui.challenge.BedInspectionScreen
import com.example.ui.challenge.MorningWakeupScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.notes.NotesScreen
import com.example.ui.notes.NotesViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Shield
import com.example.ui.achievements.AchievementsCard
import com.example.ui.focus.FocusScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Date

enum class MainTab(val title: String, val icon: ImageVector) {
    DASHBOARD("داشبورد", Icons.Default.Home),
    CHALLENGES("چالش‌ها", Icons.Default.Assignment),
    FOCUS("تمرکز", Icons.Default.HourglassBottom),
    NOTES("یادداشت‌ها", Icons.Default.NoteAlt),
    PROFILE("پروفایل", Icons.Default.Person)
}

sealed interface ActiveScreen {
    data class TabView(val tab: MainTab) : ActiveScreen
    data class BedChallenge(val task: RoutineTask) : ActiveScreen
    data class VoiceChallenge(val task: RoutineTask) : ActiveScreen
}

@Composable
fun AppNavigation(
    currentUserId: String,
    userProfile: UserProfile,
    repository: FirestoreRepository,
    aiManager: GeminiAiManager,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<ActiveScreen>(ActiveScreen.TabView(MainTab.DASHBOARD)) }

    val dashboardViewModel = remember(currentUserId) { DashboardViewModel(repository) }
    val notesViewModel = remember(currentUserId) { NotesViewModel(repository) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        when (val screen = currentScreen) {
            is ActiveScreen.BedChallenge -> {
                BackHandler { currentScreen = ActiveScreen.TabView(MainTab.DASHBOARD) }
                BedInspectionScreen(
                    task = screen.task,
                    aiManager = aiManager,
                    repository = repository,
                    onChallengeCompleted = {
                        currentScreen = ActiveScreen.TabView(MainTab.DASHBOARD)
                    },
                    onNavigateBack = {
                        currentScreen = ActiveScreen.TabView(MainTab.DASHBOARD)
                    }
                )
            }
            is ActiveScreen.VoiceChallenge -> {
                BackHandler { currentScreen = ActiveScreen.TabView(MainTab.DASHBOARD) }
                MorningWakeupScreen(
                    task = screen.task,
                    aiManager = aiManager,
                    repository = repository,
                    onChallengeCompleted = {
                        currentScreen = ActiveScreen.TabView(MainTab.DASHBOARD)
                    },
                    onNavigateBack = {
                        currentScreen = ActiveScreen.TabView(MainTab.DASHBOARD)
                    }
                )
            }
            is ActiveScreen.TabView -> {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = DarkSurface,
                            modifier = Modifier
                                .border(1.dp, Color(0xFF26324A))
                                .windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            MainTab.values().forEach { tab ->
                                val isSelected = screen.tab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentScreen = ActiveScreen.TabView(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = DarkBackground,
                                        selectedTextColor = NeonCyan,
                                        indicatorColor = NeonCyan,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                )
                            }
                        }
                    },
                    containerColor = DarkBackground
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        when (screen.tab) {
                            MainTab.DASHBOARD -> {
                                DashboardScreen(
                                    currentUserId = currentUserId,
                                    viewModel = dashboardViewModel,
                                    aiManager = aiManager,
                                    onNavigateToBedChallenge = { task ->
                                        currentScreen = ActiveScreen.BedChallenge(task)
                                    },
                                    onNavigateToVoiceChallenge = { task ->
                                        currentScreen = ActiveScreen.VoiceChallenge(task)
                                    }
                                )
                            }
                            MainTab.CHALLENGES -> {
                                ChallengesHubScreen(
                                    onOpenBedChallenge = {
                                        currentScreen = ActiveScreen.BedChallenge(
                                            RoutineTask(
                                                id = "bed_quick",
                                                title = "مرتب کردن فوری تخت خواب",
                                                type = "bed_inspection",
                                                strictLockEnabled = true
                                            )
                                        )
                                    },
                                    onOpenVoiceChallenge = {
                                        currentScreen = ActiveScreen.VoiceChallenge(
                                            RoutineTask(
                                                id = "wake_quick",
                                                title = "چالش زنگ صبحگاهی و ویس انگیزشی",
                                                type = "wake_voice",
                                                strictLockEnabled = true
                                            )
                                        )
                                    }
                                )
                            }
                            MainTab.FOCUS -> {
                                FocusScreen(
                                    userProfile = userProfile,
                                    repository = repository
                                )
                            }
                            MainTab.NOTES -> {
                                NotesScreen(viewModel = notesViewModel)
                            }
                            MainTab.PROFILE -> {
                                ProfileScreen(
                                    profile = userProfile,
                                    onSignOut = { authViewModel.signOut(context) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengesHubScreen(
    onOpenBedChallenge: () -> Unit,
    onOpenVoiceChallenge: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "مرکز آزمون و چالش‌های هوش مصنوعی",
            style = MaterialTheme.typography.headlineLarge,
            color = NeonCyan,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "ماموریت‌های خود را به صورت دستی یا زمان‌بندی شده تمرین و اجرا کنید.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NeonAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = NeonAmber)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "چالش بینایی ماشین: مرتب کردن تخت خواب",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تحلیل صدم‌ثانیه‌ای صاف بودن ملحفه و بالش‌ها توسط جمینای",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onOpenBedChallenge,
                    modifier = Modifier.fillMaxWidth().testTag("hub_bed_challenge_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = DarkBackground),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("شروع چالش عکس تخت", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = NeonCyan)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "چالش صوتی: زنگ بیداری و متن انگیزشی",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ارزیابی بیداری کامل و خواندن رسا و شمرده متن کوتاه",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onOpenVoiceChallenge,
                    modifier = Modifier.fillMaxWidth().testTag("hub_voice_challenge_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("شروع چالش زنگ و ویس", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    profile: UserProfile,
    onSignOut: () -> Unit
) {
    val daysRemaining = remember(profile) {
        val end = profile.trialEndDate?.toDate()
        if (end != null) {
            val diff = end.time - Date().time
            (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
        } else 14L
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "مشخصات کاربری و وضعیت اشتراک",
            style = MaterialTheme.typography.headlineLarge,
            color = NeonCyan,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ANTI-SANCTION & OFFLINE ENGINE BADGE CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonEmerald.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "موتور ضدتحریم و آفلاین فعال است 🛡️",
                        style = MaterialTheme.typography.titleSmall,
                        color = NeonEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "تمامی بخش‌های اپلیکیشن (چالش عکس، تایمر تمرکز، روتین‌ها و دیتابیس) بدون نیاز به وی‌پی‌ان و حتی در حالت آفلاین ۱۰۰٪ در دسترس هستند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF26324A), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.2f))
                            .border(1.5.dp, NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = profile.displayName.ifBlank { "کاربر اراده" },
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile.email.ifBlank { "ثبت‌نام شده با شماره تماس" },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 14-Day Trial Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonAmber.copy(alpha = 0.15f))
                        .border(1.dp, NeonAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = NeonAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "دوره آزمایشی ۱۴ روزه رایگان",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "$daysRemaining روز باقی‌مانده",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (profile.personaArchetype.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("تیپ اراده:", color = TextSecondary)
                        Text(profile.personaArchetype, color = NeonCyan, fontWeight = FontWeight.Bold)
                    }
                }

                if (profile.aiCoachTone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("سبک مربیگری:", color = TextSecondary)
                        Text(profile.aiCoachTone, color = NeonEmerald, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("استریک انضباط:", color = TextSecondary)
                    Text("${profile.streakCount} روز متوالی 🔥", color = NeonAmber, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("دقایق تمرکز عمیق:", color = TextSecondary)
                    Text("${profile.totalFocusMinutes} دقیقه ⚡", color = NeonCyan, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("شناسه کاربری:", color = TextSecondary)
                    Text(profile.userId.take(10) + "...", color = TextMuted)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth().testTag("sign_out_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF331A22), contentColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("خروج از حساب کاربری", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ACHIEVEMENTS AND MEDALS CARD
        AchievementsCard(userProfile = profile)

        Spacer(modifier = Modifier.height(80.dp))
    }
}
