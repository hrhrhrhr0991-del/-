package com.example.ui.admin

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PaymentRequest
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import com.example.ui.achievements.ALL_BADGES_DEFINITIONS
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
import java.util.Date

fun isUserAdmin(email: String, role: String = "user"): Boolean {
    val clean = email.trim().lowercase()
    return clean == "hrhrhrhr0991@gmail.com" || clean == "hrhrhrhr201111@gmail.com" || role == "super_admin"
}

fun getRoleTitle(role: String, email: String): String {
    val clean = email.trim().lowercase()
    if (clean == "hrhrhrhr0991@gmail.com" || clean == "hrhrhrhr201111@gmail.com" || role == "super_admin") {
        return "مدیر کل ارشد 🛡️"
    }
    return when (role) {
        "admin" -> "مدیر سیستم ⚙️"
        "coach" -> "مربی انضباط 🧠"
        "vip" -> "کاربر ویژه VIP 💎"
        else -> "کاربر عادی 👤"
    }
}

fun getRoleColor(role: String, email: String): Color {
    val clean = email.trim().lowercase()
    if (clean == "hrhrhrhr0991@gmail.com" || clean == "hrhrhrhr201111@gmail.com" || role == "super_admin") {
        return NeonAmber
    }
    return when (role) {
        "admin" -> NeonCyan
        "coach" -> NeonEmerald
        "vip" -> Color(0xFFD946EF)
        else -> TextSecondary
    }
}

@Composable
fun AdminPanelScreen(
    currentAdminEmail: String = "",
    repository: FirestoreRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val cleanEmail = currentAdminEmail.trim().lowercase()
    if (cleanEmail.isNotBlank() && cleanEmail != "hrhrhrhr0991@gmail.com") {
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
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "دسترسی غیرمجاز به پنل مدیریت",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFFF5252),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "این پنل منحصراً برای حساب مدیر کل hrhrhrhr0991@gmail.com قفل‌گذاری شده است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("بازگشت به برنامه", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        return
    }

    val users by repository.observeAllUsers().collectAsState(initial = emptyList())
    val paymentRequests by repository.observeAllPaymentRequests().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Requests, 1: Users
    var zoomedReceiptBase64 by remember { mutableStateOf<String?>(null) }
    var userSearchQuery by remember { mutableStateOf("") }

    // Dialog state handlers
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showCleanUsersConfirmDialog by remember { mutableStateOf(false) }
    var selectedUserForBadges by remember { mutableStateOf<UserProfile?>(null) }
    var selectedUserForRole by remember { mutableStateOf<UserProfile?>(null) }
    var selectedUserForNote by remember { mutableStateOf<UserProfile?>(null) }
    var selectedUserForAdjust by remember { mutableStateOf<UserProfile?>(null) }

    val pendingCount = paymentRequests.count { it.status == "pending" }
    val approvedCount = paymentRequests.count { it.status == "approved" }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "مرکز فرماندهی و مدیریت کل 🛡️",
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مدیریت کاربران، مدال‌ها، نقش‌ها و واریزی‌ها",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics Summary Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Users
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${users.size}",
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "کل کاربران",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Metric 2: Pending Requests
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$pendingCount",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (pendingCount > 0) NeonAmber else NeonEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "فیش‌های در انتظار",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Metric 3: Approved
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$approvedCount",
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "واریزهای تاییدشده",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs: Requests vs Users
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonAmber
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "فیش‌ها و واریزی‌ها ($pendingCount)",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) NeonAmber else TextMuted
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "مدیریت کاربران (${users.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) NeonAmber else TextMuted
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TAB CONTENT
            if (selectedTab == 0) {
                // PAYMENT REQUESTS LIST
                if (paymentRequests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هنوز درخواست یا فیش واریزی ثبت نشده است.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(paymentRequests, key = { it.id }) { req ->
                            AdminPaymentRequestCard(
                                request = req,
                                onZoomReceipt = { b64 -> zoomedReceiptBase64 = b64 },
                                onApprove = {
                                    scope.launch {
                                        try {
                                            repository.approvePaymentRequest(
                                                requestId = req.id,
                                                targetUserId = req.userId,
                                                durationDays = req.planDurationDays,
                                                planTitle = req.planTitle
                                            )
                                            Toast.makeText(context, "اشتراک کاربر تایید و فعال شد ✅", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "خطا در تایید: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onReject = {
                                    scope.launch {
                                        try {
                                            repository.rejectPaymentRequest(req.id, "اطلاعات واریز با گردش حساب همخوانی ندارد")
                                            Toast.makeText(context, "درخواست رد شد", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "خطا در رد: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                // USERS LIST
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search Bar
                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it },
                            label = { Text("جستجوی کاربر...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0xFF26324A)
                            )
                        )

                        // Add User Button
                        Button(
                            onClick = { showAddUserDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ کاربر", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Clean All Test Users Button
                        Button(
                            onClick = { showCleanUsersConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A1520), contentColor = Color(0xFFFF5252)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("پاکسازی", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredUsers = users.filter { u ->
                        userSearchQuery.isBlank() ||
                                u.email.contains(userSearchQuery, ignoreCase = true) ||
                                u.displayName.contains(userSearchQuery, ignoreCase = true) ||
                                u.phoneNumber.contains(userSearchQuery) ||
                                u.role.contains(userSearchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredUsers, key = { it.userId }) { user ->
                            AdminUserCard(
                                user = user,
                                onOpenBadges = { selectedUserForBadges = user },
                                onOpenRole = { selectedUserForRole = user },
                                onOpenNote = { selectedUserForNote = user },
                                onOpenAdjust = { selectedUserForAdjust = user },
                                onToggleVerified = {
                                    scope.launch {
                                        val nextVerified = !user.isVerified
                                        repository.toggleUserVerified(user.userId, nextVerified)
                                        Toast.makeText(context, if (nextVerified) "تیک آبی فعال شد 💎" else "تیک آبی غیرفعال شد", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDeleteUser = {
                                    scope.launch {
                                        repository.deleteUser(user.userId)
                                        Toast.makeText(context, "کاربر ${user.displayName} حذف شد", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // CONFIRMATION DIALOG: CLEAN ALL TEST USERS
        if (showCleanUsersConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showCleanUsersConfirmDialog = false },
                containerColor = DarkSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFFF5252))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("پاکسازی و حذف کاربران تستی ⚠️", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Text(
                        "آیا از پاکسازی تمام کاربران تستی اطمینان دارید؟ منحصراً حساب مدیر کل ارشد (hrhrhrhr0991@gmail.com) در دیتابیس باقی می‌ماند و تمامی رکوردهای دیگر حذف می‌شوند.",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val count = repository.cleanUpAllNonAdminUsers()
                                showCleanUsersConfirmDialog = false
                                Toast.makeText(context, "$count کاربر تستی با موفقیت حذف شدند و فقط حساب مدیر باقی ماند 🛡️", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252), contentColor = Color.White)
                    ) {
                        Text("بله، پاکسازی کن", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCleanUsersConfirmDialog = false }) {
                        Text("انصراف", color = TextMuted)
                    }
                }
            )
        }

        // DIALOG 1: ADD NEW USER (هواساز کاربر با سطح دسترسی دلخواه و اعطای مدال)
        if (showAddUserDialog) {
            AddUserDialog(
                onDismiss = { showAddUserDialog = false },
                onCreate = { name, email, phone, role, days, badges, isVerified, handle ->
                    scope.launch {
                        try {
                            repository.adminCreateUser(
                                displayName = name,
                                email = email,
                                phone = phone,
                                role = role,
                                durationDays = days,
                                unlockedBadges = badges,
                                isVerified = isVerified,
                                customHandle = handle
                            )
                            showAddUserDialog = false
                            Toast.makeText(context, "کاربر $name با سطح $role با موفقیت ایجاد شد ✅", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا در ایجاد کاربر: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        // DIALOG 2: MANAGE BADGES (اعطا و پس گرفتن مدال‌ها)
        if (selectedUserForBadges != null) {
            val target = selectedUserForBadges!!
            ManageBadgesDialog(
                user = target,
                onDismiss = { selectedUserForBadges = null },
                onToggleBadge = { badgeId, shouldGrant ->
                    scope.launch {
                        try {
                            val currentList = target.unlockedBadges.toMutableList()
                            if (shouldGrant) {
                                if (!currentList.contains(badgeId)) currentList.add(badgeId)
                            } else {
                                currentList.remove(badgeId)
                            }
                            repository.updateUserBadges(target.userId, currentList)
                            selectedUserForBadges = target.copy(unlockedBadges = currentList)
                            Toast.makeText(context, if (shouldGrant) "مدال اعطا شد 🏅" else "مدال پس گرفته شد ❌", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        // DIALOG 3: CHANGE ROLE (تغییر سطح دسترسی کاربر)
        if (selectedUserForRole != null) {
            val target = selectedUserForRole!!
            ChangeRoleDialog(
                user = target,
                onDismiss = { selectedUserForRole = null },
                onSelectRole = { newRole ->
                    scope.launch {
                        try {
                            repository.updateUserRole(target.userId, newRole)
                            selectedUserForRole = null
                            Toast.makeText(context, "سطح دسترسی کاربر به $newRole تغییر یافت ✅", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        // DIALOG 4: ADMIN NOTE (ارسال پیام اختصاصی مربی به کاربر)
        if (selectedUserForNote != null) {
            val target = selectedUserForNote!!
            AdminNoteDialog(
                user = target,
                onDismiss = { selectedUserForNote = null },
                onSaveNote = { note ->
                    scope.launch {
                        try {
                            repository.updateUserAdminNote(target.userId, note)
                            selectedUserForNote = null
                            Toast.makeText(context, "پیام روی داشبورد کاربر ثبت شد 💬", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        // DIALOG 5: ADJUST SUBSCRIPTION & STREAK (تنظیم اشتراک، استریک و انسداد)
        if (selectedUserForAdjust != null) {
            val target = selectedUserForAdjust!!
            AdjustUserDialog(
                user = target,
                onDismiss = { selectedUserForAdjust = null },
                onAdjustDays = { deltaDays ->
                    scope.launch {
                        try {
                            repository.adminAdjustSubscription(target.userId, deltaDays)
                            Toast.makeText(context, "اشتراک کاربر به میزان $deltaDays روز تغییر یافت ✅", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onSetStreak = { newStreak ->
                    scope.launch {
                        try {
                            repository.updateUserStreak(target.userId, newStreak)
                            Toast.makeText(context, "استریک کاربر به $newStreak روز تغییر یافت 🔥", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onToggleBan = { isBanned ->
                    scope.launch {
                        try {
                            repository.toggleUserBan(target.userId, isBanned)
                            selectedUserForAdjust = null
                            Toast.makeText(context, if (isBanned) "حساب کاربر مسدود شد 🚫" else "حساب کاربر رفع مسدودی شد ✅", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        // FULLSCREEN RECEIPT ZOOM MODAL
        if (zoomedReceiptBase64 != null) {
            Dialog(onDismissRequest = { zoomedReceiptBase64 = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تصویر فیش واریزی",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { zoomedReceiptBase64 = null }) {
                                Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val b64 = zoomedReceiptBase64!!
                        val imageBitmap = remember(b64) {
                            try {
                                val bytes = Base64.decode(b64, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (imageBitmap != null) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "فیش واریزی",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Text("عدم امکان بارگذاری تصویر فیش", color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { zoomedReceiptBase64 = null },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground)
                        ) {
                            Text("بستن پیش‌نمایش")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: UserProfile,
    onOpenBadges: () -> Unit,
    onOpenRole: () -> Unit,
    onOpenNote: () -> Unit,
    onOpenAdjust: () -> Unit,
    onToggleVerified: () -> Unit,
    onDeleteUser: () -> Unit = {}
) {
    val daysRemaining = remember(user) {
        val end = user.trialEndDate?.toDate()
        if (end != null) {
            val diff = end.time - Date().time
            (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
        } else 0L
    }

    val roleTitle = getRoleTitle(user.role, user.email)
    val roleColor = getRoleColor(user.role, user.email)
    val isAdmin = isUserAdmin(user.email, user.role)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (user.isBanned) Color(0xFFFF5252) else Color(0xFF26324A), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Name & Role Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.displayName.ifBlank { "کاربر بدون نام" },
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = "تیک آبی رسمی", tint = NeonCyan, modifier = Modifier.size(16.dp))
                        }
                        if (user.isBanned) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF4A151E))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("مسدود 🚫", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.handle.ifBlank { "@user" },
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonEmerald,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = user.email.ifBlank { "بدون ایمیل" },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isAdmin) {
                        IconButton(
                            onClick = onDeleteUser,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف کاربر",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleVerified,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (user.isVerified) Icons.Default.Verified else Icons.Default.CheckCircle,
                            contentDescription = "تغییر وضعیت تیک آبی",
                            tint = if (user.isVerified) NeonCyan else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Role Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(roleColor.copy(alpha = 0.15f))
                            .border(1.dp, roleColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = roleTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = roleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User Info Metrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (daysRemaining > 0) "$daysRemaining روز اشتراک" else "منقضی شده",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (daysRemaining > 0) NeonEmerald else Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "استریک: ${user.streakCount} روز 🔥",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonAmber
                )
                Text(
                    text = "مدال‌ها: ${user.unlockedBadges.size} عدد 🏅",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan
                )
            }

            if (user.phoneNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "شماره تماس: ${user.phoneNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            if (user.adminNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "پیام مربی: ${user.adminNote}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonAmber,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Management Action Buttons Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Button 1: Badges
                Button(
                    onClick = onOpenBadges,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonAmber.copy(alpha = 0.2f), contentColor = NeonAmber)
                ) {
                    Text("مدال‌ها 🏅", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Button 2: Role
                Button(
                    onClick = onOpenRole,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f), contentColor = NeonCyan)
                ) {
                    Text("نقش 🔑", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Button 3: Note
                Button(
                    onClick = onOpenNote,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald.copy(alpha = 0.2f), contentColor = NeonEmerald)
                ) {
                    Text("پیام 💬", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Button 4: Adjust
                Button(
                    onClick = onOpenAdjust,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26324A), contentColor = TextPrimary)
                ) {
                    Text("تنظیمات ⚙️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, email: String, phone: String, role: String, durationDays: Long, badges: List<String>, isVerified: Boolean, handle: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var customHandle by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("user") }
    var selectedDays by remember { mutableStateOf(30L) }
    var isVerified by remember { mutableStateOf(false) }
    var selectedBadges by remember { mutableStateOf(setOf<String>()) }

    val roles = listOf(
        "user" to "کاربر عادی",
        "vip" to "کاربر ویژه VIP",
        "coach" to "مربی انضباط",
        "admin" to "مدیر سیستم",
        "super_admin" to "مدیر ارشد"
    )

    val dayOptions = listOf(14L, 30L, 90L, 180L, 365L)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("افزودن و تعریف کاربر جدید 👤", color = NeonAmber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام و نام خانوادگی") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("شماره تماس") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("آدرس ایمیل (اختیاری)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = customHandle,
                    onValueChange = { customHandle = it },
                    label = { Text("شناسه کاربری دلخواه (مثلاً @ali)") },
                    placeholder = { Text("@username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Verified Blue Badge Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اعطای تیک آبی رسمی 💎", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Checkbox(
                        checked = isVerified,
                        onCheckedChange = { isVerified = it },
                        colors = CheckboxDefaults.colors(checkedColor = NeonCyan)
                    )
                }

                Text("سطح دسترسی و نقش:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    roles.forEach { (roleKey, roleLabel) ->
                        FilterChip(
                            selected = selectedRole == roleKey,
                            onClick = { selectedRole = roleKey },
                            label = { Text(roleLabel, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                                selectedLabelColor = NeonCyan
                            )
                        )
                    }
                }

                Text("مدت اشتراک اولیه:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    dayOptions.forEach { days ->
                        FilterChip(
                            selected = selectedDays == days,
                            onClick = { selectedDays = days },
                            label = { Text("$days روز", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonAmber.copy(alpha = 0.25f),
                                selectedLabelColor = NeonAmber
                            )
                        )
                    }
                }

                Text("اعطای مدال‌های افتخار به کاربر:", style = MaterialTheme.typography.labelSmall, color = NeonAmber, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ALL_BADGES_DEFINITIONS.forEach { badge ->
                        val isChecked = selectedBadges.contains(badge.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChecked) NeonAmber.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    selectedBadges = if (isChecked) selectedBadges - badge.id else selectedBadges + badge.id
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    selectedBadges = if (checked) selectedBadges + badge.id else selectedBadges - badge.id
                                },
                                colors = CheckboxDefaults.colors(checkedColor = NeonAmber)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(badge.iconEmoji + " " + badge.title, color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() && phone.isBlank()) return@Button
                    onCreate(
                        name.trim(),
                        email.trim(),
                        phone.trim(),
                        selectedRole,
                        selectedDays,
                        selectedBadges.toList(),
                        isVerified,
                        customHandle.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = DarkBackground)
            ) {
                Text("ثبت کاربر در سیستم", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@Composable
fun ManageBadgesDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onToggleBadge: (badgeId: String, shouldGrant: Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text("مدیریت مدال‌ها و افتخارات 🏅", color = NeonAmber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("کاربر: ${user.displayName.ifBlank { user.email }}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ALL_BADGES_DEFINITIONS.forEach { badge ->
                    val isGranted = user.unlockedBadges.contains(badge.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isGranted) NeonAmber.copy(alpha = 0.15f) else DarkSurfaceVariant)
                            .border(1.dp, if (isGranted) NeonAmber.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(badge.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(badge.title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text(badge.requirement, color = TextMuted, fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = { onToggleBadge(badge.id, !isGranted) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isGranted) Color(0xFF4A1A22) else NeonAmber,
                                contentColor = if (isGranted) Color(0xFFFF5252) else DarkBackground
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(if (isGranted) "لغو مدال ✕" else "اعطای مدال ✓", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground)) {
                Text("تکمیل و بازگشت")
            }
        }
    )
}

@Composable
fun ChangeRoleDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSelectRole: (String) -> Unit
) {
    val roles = listOf(
        "user" to ("کاربر عادی 👤" to "دسترسی عادی با روتین‌های روزمره"),
        "vip" to ("کاربر ویژه VIP 💎" to "دسترسی ویژه به تمامی پلن‌ها"),
        "coach" to ("مربی انضباط 🧠" to "مربی گری و مشاهده فعالیت‌ها"),
        "admin" to ("مدیر سیستم ⚙️" to "مدیریت و تایید فیش‌ها و کاربران"),
        "super_admin" to ("مدیر کل ارشد 🛡️" to "دسترسی کامل و تام‌الاختیار")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("تعیین سطح دسترسی و نقش 🔑", color = NeonCyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("کاربر: ${user.displayName.ifBlank { user.email }}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))

                roles.forEach { (roleKey, details) ->
                    val isSelected = user.role == roleKey
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isSelected) NeonCyan else Color(0xFF26324A), RoundedCornerShape(10.dp))
                            .clickable { onSelectRole(roleKey) },
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) NeonCyan.copy(alpha = 0.15f) else DarkSurfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(details.first, fontWeight = FontWeight.Bold, color = if (isSelected) NeonCyan else TextPrimary)
                                Text(details.second, fontSize = 10.sp, color = TextMuted)
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@Composable
fun AdminNoteDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSaveNote: (String) -> Unit
) {
    var note by remember { mutableStateOf(user.adminNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("پیام مستقیم مدیر برای کاربر 💬", color = NeonEmerald, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "این پیام مستقیماً در یک کادر نئونی بالای صفحه داشبورد این کاربر نمایش داده خواهد شد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("متن پیام مربی / مدیر") },
                    placeholder = { Text("مثلاً: آفرین علی جان، استریک ۷ روزه ات عالی بود!") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveNote(note.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = DarkBackground)
            ) {
                Text("ثبت و ارسال پیام", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@Composable
fun AdjustUserDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onAdjustDays: (Long) -> Unit,
    onSetStreak: (Long) -> Unit,
    onToggleBan: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("تنظیمات پیشرفته اشتراک و وضعیت ⚙️", color = NeonAmber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("تمدید یا کاهش روزهای اشتراک:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { onAdjustDays(7L) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)) {
                        Text("+۷ روز", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { onAdjustDays(30L) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground)) {
                        Text("+۳۰ روز", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { onAdjustDays(90L) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = DarkBackground)) {
                        Text("+۹۰ روز", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = { onAdjustDays(-7L) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))) {
                        Text("-۷ روز", fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = { onAdjustDays(-365L) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))) {
                        Text("منقضی کردن فوری", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("تنظیم استریک روزهای متوالی:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { onSetStreak(user.streakCount + 5L) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26324A))) {
                        Text("+۵ به استریک 🔥", fontSize = 11.sp)
                    }
                    Button(onClick = { onSetStreak(0L) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF331A22), contentColor = Color(0xFFFF5252))) {
                        Text("صفر کردن استریک", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("وضعیت دسترسی به برنامه:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Button(
                    onClick = { onToggleBan(!user.isBanned) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBanned) NeonEmerald else Color(0xFF4A151E),
                        contentColor = if (user.isBanned) DarkBackground else Color(0xFFFF5252)
                    )
                ) {
                    Text(if (user.isBanned) "رفع مسدودسازی و بازگشایی حساب ✅" else "مسدود کردن حساب کاربر (Ban) 🚫", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) { Text("بستن") }
        }
    )
}

@Composable
fun AdminPaymentRequestCard(
    request: PaymentRequest,
    onZoomReceipt: (String) -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val statusColor = when (request.status) {
        "approved" -> NeonEmerald
        "rejected" -> MaterialTheme.colorScheme.error
        else -> NeonAmber
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Plan & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.planTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (request.status) {
                            "approved" -> "تایید شده ✅"
                            "rejected" -> "رد شده ❌"
                            else -> "در انتظار بررسی ⏳"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User Info
            Text(
                text = "کاربر: ${request.userDisplayName.ifBlank { "کاربر اراده" }} (${request.userEmail})",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            if (request.userPhone.isNotBlank()) {
                Text(
                    text = "شماره تماس: ${request.userPhone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Payment Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "صاحب کارت: ${request.senderCardHolder}", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                    Text(text = "۴ رقم آخر: **** ${request.senderCardLast4}", style = MaterialTheme.typography.labelSmall, color = NeonAmber, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    val formatted = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(request.amountToman)
                    Text(text = "$formatted تومان", style = MaterialTheme.typography.labelSmall, color = NeonEmerald, fontWeight = FontWeight.Bold)
                    if (request.trackingCode.isNotBlank()) {
                        Text(text = "پیگیری: ${request.trackingCode}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }

            // Receipt preview button if exists
            if (request.receiptImageBase64.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onZoomReceipt(request.receiptImageBase64) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاهده و بزرگ‌نمایی تصویر فیش واریز 🔎", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Action Buttons for Pending Request
            if (request.status == "pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = DarkBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("تایید و فعال‌سازی اکانت", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("رد فیش", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
