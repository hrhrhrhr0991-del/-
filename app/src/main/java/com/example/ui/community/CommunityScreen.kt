package com.example.ui.community

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BroadcastPost
import com.example.data.model.PeerMessage
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
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

@Composable
fun CommunityScreen(
    currentProfile: UserProfile,
    repository: FirestoreRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Broadcasts, 1: Peers & Motivation

    val broadcasts by repository.observeBroadcasts().collectAsState(initial = emptyList())
    val receivedMessages by repository.observeReceivedMessages(
        currentProfile.userId,
        currentProfile.handle
    ).collectAsState(initial = emptyList())

    // Admin New Broadcast Dialog State
    var showNewBroadcastDialog by remember { mutableStateOf(false) }
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastContent by remember { mutableStateOf("") }
    var broadcastTag by remember { mutableStateOf("اعلان مهم") }

    // Peer Messaging State
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by repository.searchUsers(searchQuery).collectAsState(initial = emptyList())
    var selectedPeerForMessage by remember { mutableStateOf<UserProfile?>(null) }
    var showSendMessageDialog by remember { mutableStateOf(false) }

    val isAdmin = currentProfile.role == "super_admin" || currentProfile.email.trim().lowercase() == "hrhrhrhr0991@gmail.com"

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "جامعه و ارتباطات اراده 🌐",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اطلاع‌رسانی رسمی و تبادل انگیزه بین همراهان",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs: 0: Official Channel, 1: Peers & Search
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonCyan
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF26324A), RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("کانال رسمی اراده", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("همراهان و پیام‌ها", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 0: OFFICIAL BROADCASTS
                    if (isAdmin) {
                        Button(
                            onClick = { showNewBroadcastDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ارسال پیام و اعلان رسمی مدیریت (با تیک آبی) 📢", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (broadcasts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("هنوز اعلانی از طرف مدیریت ثبت نشده است.", color = TextMuted, fontSize = 13.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(broadcasts, key = { it.id }) { post ->
                                BroadcastPostCard(
                                    post = post,
                                    currentUserId = currentProfile.userId,
                                    onLike = {
                                        scope.launch {
                                            repository.toggleLikeBroadcast(post.id, currentProfile.userId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 1: PEERS, SEARCH & SAFE MESSAGING
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search OutlinedTextField
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("جستجوی آیدی یا نام همراه (مثلاً @ali یا شماره)") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0xFF26324A)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (searchQuery.isNotBlank()) {
                            Text("نتایج جستجو:", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            if (searchResults.isEmpty()) {
                                Text("هیچ کاربری با این آیدی پیدا نشد.", color = TextMuted, fontSize = 11.sp)
                            } else {
                                searchResults.forEach { user ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(NeonCyan.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(user.displayName.ifBlank { "همراه اراده" }, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        if (user.isVerified) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Icon(Icons.Default.Verified, contentDescription = "تیک آبی", tint = NeonCyan, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                    Text(user.handle.ifBlank { "@user_${user.userId.takeLast(4)}" }, color = NeonEmerald, fontSize = 11.sp)
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    selectedPeerForMessage = user
                                                    showSendMessageDialog = true
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = DarkBackground),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("ارسال پیام", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Received Messages Section
                        Text("پیام‌ها و انگیزه‌های دریافتی از همراهان 💬", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (receivedMessages.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Forum, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("هنوز پیامی دریافت نکرده‌اید. با جستجوی آیدی دوستان خود، به آن‌ها انگیزه بدهید!", color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(receivedMessages, key = { it.id }) { msg ->
                                    PeerMessageCard(msg)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog: Publish New Official Broadcast
        if (showNewBroadcastDialog) {
            AlertDialog(
                onDismissRequest = { showNewBroadcastDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = NeonAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ارسال اعلان جدید مدیریت 📢", color = NeonAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = broadcastTitle,
                            onValueChange = { broadcastTitle = it },
                            label = { Text("عنوان اعلان") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = broadcastTag,
                            onValueChange = { broadcastTag = it },
                            label = { Text("برچسب (مثلاً اعلان مهم، چالش سراسری)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = broadcastContent,
                            onValueChange = { broadcastContent = it },
                            label = { Text("متن اعلان برای تمامی کاربران...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 5
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (broadcastTitle.isNotBlank() && broadcastContent.isNotBlank()) {
                                scope.launch {
                                    repository.publishBroadcast(
                                        title = broadcastTitle.trim(),
                                        content = broadcastContent.trim(),
                                        tag = broadcastTag.trim()
                                    )
                                    showNewBroadcastDialog = false
                                    broadcastTitle = ""
                                    broadcastContent = ""
                                    Toast.makeText(context, "اعلان رسمی مدیریت با تیک آبی منتشر شد.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = DarkBackground)
                    ) {
                        Text("انتشار رسمی", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewBroadcastDialog = false }) {
                        Text("انصراف", color = TextMuted)
                    }
                }
            )
        }

        // Dialog: Send Structured Motivation Message to Peer
        val targetPeer = selectedPeerForMessage
        if (showSendMessageDialog && targetPeer != null) {
            val presetMessages = listOf(
                "🔥 امروز روتین‌های روزانه‌ام رو کامل کردم، پرقدرت ادامه بده!",
                "⚡ در آزمون تمرکز عمیق شرکت کردی؟ حریف می‌طلبم!",
                "👏 تبریک بابت پیشرفت عالی و استریکت، ماشاالله به اراده!",
                "💧 امروز آب کافی خوردی؟ سلامت و انضباط اولویته!",
                "🚀 شکست یعنی تسلیم شدن، با تمام توان ادامه بده!",
                "📊 درصد پیشرفت امروز من: ${currentProfile.streakCount * 10 % 100 + 15}٪ تکمیل روتین!"
            )

            AlertDialog(
                onDismissRequest = {
                    showSendMessageDialog = false
                    selectedPeerForMessage = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ارسال پیام انضباطی به ${targetPeer.displayName.ifBlank { "همراه" }}",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "پیام‌های اراده منحصراً از پیش‌تعریف‌شده و انگیزشی هستند تا از اسپم جلوگیری شود:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        presetMessages.forEach { msg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        scope.launch {
                                            repository.sendPeerMessage(
                                                sender = currentProfile,
                                                recipient = targetPeer,
                                                messageText = msg,
                                                type = if (msg.contains("درصد")) "progress" else "motivation"
                                            )
                                            showSendMessageDialog = false
                                            selectedPeerForMessage = null
                                            Toast.makeText(context, "پیام انگیزشی با موفقیت ارسال شد! 🚀", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2E)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(msg, color = TextPrimary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = {
                        showSendMessageDialog = false
                        selectedPeerForMessage = null
                    }) {
                        Text("بستن", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun BroadcastPostCard(
    post: BroadcastPost,
    currentUserId: String,
    onLike: () -> Unit
) {
    val isLiked = post.likedUserIds.contains(currentUserId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NeonAmber.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NeonAmber.copy(alpha = 0.2f))
                            .border(1.5.dp, NeonAmber, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(post.authorName, color = NeonAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = "تیک آبی تایید شده", tint = NeonCyan, modifier = Modifier.size(16.dp))
                        }
                        Text(post.authorRole, color = TextSecondary, fontSize = 11.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(post.tag, color = NeonAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "پسندیدن",
                        tint = if (isLiked) Color(0xFFFF5252) else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "${post.likesCount} انرژی مثبت",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun PeerMessageCard(msg: PeerMessage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF26324A), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(NeonEmerald.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (msg.messageType) {
                        "progress" -> Icons.Default.Bolt
                        else -> Icons.Default.LocalFireDepartment
                    },
                    contentDescription = null,
                    tint = NeonEmerald,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(msg.senderName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (msg.senderIsVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, contentDescription = "تیک آبی", tint = NeonCyan, modifier = Modifier.size(15.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(msg.senderHandle, color = NeonCyan, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(msg.messageText, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
