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
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import com.example.ui.auth.CompleteProfileDialog
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

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.ui.admin.AdminPanelScreen
import com.example.ui.admin.getRoleTitle
import com.example.ui.admin.isUserAdmin
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Verified
import com.example.ui.community.CommunityScreen
import com.example.ui.subscription.SubscriptionScreen

enum class MainTab(val title: String, val icon: ImageVector) {
    DASHBOARD("داشبورد", Icons.Default.Home),
    CHALLENGES("چالش‌ها", Icons.Default.Assignment),
    COMMUNITY("گفت‌وگو", Icons.Default.Forum),
    FOCUS("تمرکز", Icons.Default.HourglassBottom),
    PROFILE("پروفایل", Icons.Default.Person)
}

sealed interface ActiveScreen {
    data class TabView(val tab: MainTab) : ActiveScreen
    data class BedChallenge(val task: RoutineTask) : ActiveScreen
    data class VoiceChallenge(val task: RoutineTask) : ActiveScreen
    data object AdminPanel : ActiveScreen
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
    val liveProfile by repository.observeUserProfile(currentUserId).collectAsState(initial = userProfile)
    val currentProfile = liveProfile ?: userProfile
    val isAdmin = isUserAdmin(currentProfile.email, currentProfile.role)

    if (currentProfile.isBanned) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "حساب کاربری مسدود شده است 🚫",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFFF5252),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentProfile.adminNote.ifBlank { "حساب کاربری شما توسط مدیر سیستم به حالت تعلیق درآمده است." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { authViewModel.signOut(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF331A22), contentColor = Color(0xFFFF5252)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("خروج از حساب کاربری")
                        }
                    }
                }
            }
        }
        return
    }

    val isTrialExpired = remember(currentProfile, isAdmin) {
        if (isAdmin) return@remember false
        if (currentProfile.isSubscribed) return@remember false
        val trialEnd = currentProfile.trialEndDate?.toDate()
        trialEnd != null && trialEnd.before(Date())
    }

    if (isTrialExpired) {
        SubscriptionScreen(
            userProfile = currentProfile,
            repository = repository,
            onSignOut = { authViewModel.signOut(context) }
        )
        return
    }

    var currentScreen by remember { mutableStateOf<ActiveScreen>(ActiveScreen.TabView(MainTab.DASHBOARD)) }

    val dashboardViewModel = remember(currentUserId) { DashboardViewModel(repository) }
    val notesViewModel = remember(currentUserId) { NotesViewModel(repository) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        // Mandatory Profile Completion Dialog if phone number is blank
        if (currentProfile.phoneNumber.isBlank()) {
            CompleteProfileDialog(
                userProfile = currentProfile,
                repository = repository,
                onProfileCompleted = {
                    authViewModel.checkCurrentUser()
                }
            )
        }

        when (val screen = currentScreen) {
            is ActiveScreen.AdminPanel -> {
                BackHandler { currentScreen = ActiveScreen.TabView(MainTab.PROFILE) }
                AdminPanelScreen(
                    currentAdminEmail = currentProfile.email,
                    repository = repository,
                    onNavigateBack = { currentScreen = ActiveScreen.TabView(MainTab.PROFILE) }
                )
            }
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
                    var showHandleBanner by remember { mutableStateOf(true) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        if (showHandleBanner && currentProfile.handle.isNotBlank()) {
                            val isPhoneHandle = currentProfile.handle.startsWith("@09") || currentProfile.handle.startsWith("@+98") || currentProfile.handle.startsWith("@98")
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPhoneHandle) Color(0xFF1B2438) else Color(0xFF142233)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "شناسه شما در اراده: ${currentProfile.handle}",
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        if (isPhoneHandle) {
                                            Text(
                                                text = "جهت حفظ حریم خصوصی شماره تماس، می‌توانید در تب «پروفایل» آیدی دلخواه ثبت کنید.",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { showHandleBanner = false },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "بستن",
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
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
                                MainTab.COMMUNITY -> {
                                    CommunityScreen(
                                        currentProfile = currentProfile,
                                        repository = repository
                                    )
                                }
                                MainTab.FOCUS -> {
                                    FocusScreen(
                                        userProfile = currentProfile,
                                        repository = repository
                                    )
                                }
                                MainTab.PROFILE -> {
                                    ProfileScreen(
                                        profile = currentProfile,
                                        isAdmin = isAdmin,
                                        repository = repository,
                                        onOpenAdminPanel = { currentScreen = ActiveScreen.AdminPanel },
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
    isAdmin: Boolean = false,
    repository: FirestoreRepository? = null,
    onOpenAdminPanel: () -> Unit = {},
    onSignOut: () -> Unit
) {
    val daysRemaining = remember(profile) {
        val end = profile.trialEndDate?.toDate()
        if (end != null) {
            val diff = end.time - Date().time
            (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
        } else 14L
    }

    var showEditHandleDialog by remember { mutableStateOf(false) }
    var enteredHandleInput by remember(profile.handle) { mutableStateOf(profile.handle.removePrefix("@")) }
    var handleErrorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

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

        var showAdminPinDialog by remember { mutableStateOf(false) }
        var enteredAdminPin by remember { mutableStateOf("") }
        var adminPinError by remember { mutableStateOf<String?>(null) }

        // ADMIN PANEL ACCESS CARD (Exclusive for hrhrhrhr0991@gmail.com with PIN 0991)
        if (isAdmin) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, NeonAmber, RoundedCornerShape(18.dp))
                    .clickable { showAdminPinDialog = true }
                    .testTag("admin_panel_open_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(NeonAmber.copy(alpha = 0.2f))
                            .border(1.dp, NeonAmber, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ورود به مرکز فرماندهی (پنل مدیریت) 🛡️",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "تایید و رد فیش‌های واریزی، تمدید اشتراک و آمار کل کاربران",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile.displayName.ifBlank { "کاربر اراده" },
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (profile.isVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "حساب تایید شده",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (profile.handle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E2638))
                                    .clickable {
                                        enteredHandleInput = profile.handle.removePrefix("@")
                                        showEditHandleDialog = true
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = profile.handle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "ویرایش آیدی",
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Text(
                            text = profile.email.ifBlank { "ثبت‌نام شده با حساب اراده" },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 14-Day Trial or Active Subscription Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (profile.isSubscribed) NeonEmerald.copy(alpha = 0.15f) else NeonAmber.copy(alpha = 0.15f))
                        .border(1.dp, if (profile.isSubscribed) NeonEmerald.copy(alpha = 0.5f) else NeonAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = if (profile.isSubscribed) NeonEmerald else NeonAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (profile.isSubscribed) "اشتراک فعال (${profile.subscriptionPlan})" else "دوره آزمایشی ۱۴ روزه رایگان",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (profile.isSubscribed) NeonEmerald else NeonAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (isAdmin) getRoleTitle(profile.role, profile.email) else "$daysRemaining روز باقی‌مانده",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isAdmin) NeonAmber else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("سطح دسترسی و نقش:", color = TextSecondary)
                    Text(
                        text = getRoleTitle(profile.role, profile.email),
                        color = if (isAdmin) NeonAmber else NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (profile.adminNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonAmber, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = NeonAmber.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("پیام مربی / مدیر اراده:", color = NeonAmber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(profile.adminNote, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (profile.phoneNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("شماره تماس ثبت‌شده:", color = TextSecondary)
                        Text(profile.phoneNumber, color = NeonCyan, fontWeight = FontWeight.Bold)
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

        if (showAdminPinDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAdminPinDialog = false
                    enteredAdminPin = ""
                    adminPinError = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = NeonAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تأیید هویت مدیر کل اراده 🛡️",
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "حساب کاربری: ${profile.email}",
                            color = NeonCyan,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "جهت ورود به پنل مدیریت، رمز مستر امنیتی ۴ رقمی مدیر اراده را وارد کنید:",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = enteredAdminPin,
                            onValueChange = {
                                if (it.length <= 6) {
                                    enteredAdminPin = it
                                    adminPinError = null
                                }
                            },
                            label = { Text("رمز عبور مدیر (PIN)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("admin_pin_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonAmber,
                                unfocusedBorderColor = Color(0xFF26324A),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            isError = adminPinError != null
                        )
                        if (adminPinError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = adminPinError.orEmpty(),
                                color = Color(0xFFFF5252),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val clean = profile.email.trim().lowercase()
                            val isAuthorized = clean == "hrhrhrhr0991@gmail.com" ||
                                    profile.role == "super_admin" ||
                                    profile.userId == "usr_hrhrhrhr0991_gmail_com"
                            if (isAuthorized && enteredAdminPin.trim() == "0991") {
                                showAdminPinDialog = false
                                enteredAdminPin = ""
                                adminPinError = null
                                onOpenAdminPanel()
                            } else if (!isAuthorized) {
                                adminPinError = "تنها حساب مدیر کل مجاز به ورود به پنل مدیریت است."
                            } else {
                                adminPinError = "رمز عبور اشتباه است! دسترسی غیرمجاز."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = DarkBackground),
                        modifier = Modifier.testTag("submit_admin_pin_btn")
                    ) {
                        Text("ورود به مدیریت", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAdminPinDialog = false
                            enteredAdminPin = ""
                            adminPinError = null
                        }
                    ) {
                        Text("انصراف", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (showEditHandleDialog) {
            AlertDialog(
                onDismissRequest = {
                    showEditHandleDialog = false
                    handleErrorMessage = null
                },
                title = {
                    Text(
                        text = "تنظیم شناسه کاربری (آیدی دلخواه)",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "با ثبت آیدی دلخواه، شماره تماس شما در بخش گفت‌وگو و جستجو مخفی خواهد ماند.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = enteredHandleInput,
                            onValueChange = {
                                enteredHandleInput = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' }
                                handleErrorMessage = null
                            },
                            label = { Text("آیدی (بدون فاصله)") },
                            prefix = { Text("@", color = NeonCyan) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("edit_handle_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0xFF26324A),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            isError = handleErrorMessage != null
                        )
                        if (handleErrorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = handleErrorMessage.orEmpty(),
                                color = Color(0xFFFF5252),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cleanInput = enteredHandleInput.trim()
                            if (cleanInput.length < 3) {
                                handleErrorMessage = "آیدی باید حداقل ۳ کاراکتر باشد."
                            } else {
                                val fullHandle = "@$cleanInput"
                                coroutineScope.launch {
                                    val res = repository?.updateUserHandle(profile.userId, fullHandle)
                                    if (res == true) {
                                        showEditHandleDialog = false
                                        handleErrorMessage = null
                                    } else {
                                        handleErrorMessage = "خطا در برقراری ارتباط. مجدد تلاش کنید."
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground),
                        modifier = Modifier.testTag("save_handle_btn")
                    ) {
                        Text("ذخیره آیدی", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditHandleDialog = false }) {
                        Text("انصراف", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
