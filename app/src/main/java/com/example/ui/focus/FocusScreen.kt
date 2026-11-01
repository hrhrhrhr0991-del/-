package com.example.ui.focus

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FocusScreen(
    userProfile: UserProfile,
    repository: FirestoreRepository
) {
    val presetMinutes = listOf(25, 45, 60)
    var selectedMinutes by remember { mutableIntStateOf(25) }
    var totalSeconds by remember { mutableIntStateOf(selectedMinutes * 60) }
    var remainingSeconds by remember { mutableIntStateOf(selectedMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var isAmbientSoundOn by remember { mutableStateOf(false) }
    var completedSessionsCount by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    val focusQuotes = remember {
        listOf(
            "وقتی در حال ساخت آینده‌ات هستی، هیچ حواس‌پرتی ارزش توجه ندارد.",
            "تمرکز یعنی انضباط در گفتن «نه» به هزاران وسوسه پوچ روزمره.",
            "مغز در حالت تمرکز عمیق، معجزات غیرممکن را خلق می‌کند.",
            "اراده یک ماهیچه است؛ هر دقیقه مقاومت، تو را قوی‌تر می‌سازد.",
            "سکوت، تمرکز مطلق و پیروزی بر لحظه‌ها رمز صعود فرماندهان است."
        )
    }

    var currentQuoteIndex by remember { mutableIntStateOf(0) }

    // Timer Loop
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
            completedSessionsCount++
            // Play completion chime
            try {
                val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
                tone.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
                delay(600)
                tone.startTone(ToneGenerator.TONE_PROP_BEEP, 800)
                tone.release()
            } catch (e: Exception) {
                // Ignore audio exception
            }

            scope.launch {
                repository.logFocusSession(selectedMinutes)
            }
            currentQuoteIndex = (currentQuoteIndex + 1) % focusQuotes.size
            remainingSeconds = selectedMinutes * 60
        }
    }

    val progress = if (totalSeconds > 0) {
        (totalSeconds - remainingSeconds).toFloat() / totalSeconds
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
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
                        text = "اتاق تمرکز عمیق ⚡",
                        style = MaterialTheme.typography.headlineLarge,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تمرکز بدون حاشیه با متد انضباط سایبری",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, NeonEmerald.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "موتور آفلاین 🛡️",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Duration Presets (disabled while timer is running)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                presetMinutes.forEach { mins ->
                    val isSelected = selectedMinutes == mins
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkSurface)
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else Color(0xFF26324A),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = !isRunning) {
                                selectedMinutes = mins
                                totalSeconds = mins * 60
                                remainingSeconds = mins * 60
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$mins دقیقه",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) NeonCyan else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (mins) {
                                    25 -> "پومودورو"
                                    45 -> "عمیق"
                                    else -> "استقامت"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (isSelected) NeonCyan else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Circular Cyber Timer Display
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1E283D),
                    strokeWidth = 12.dp
                )

                // Animated Progress Ring
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (isRunning) NeonCyan else NeonAmber,
                    strokeWidth = 12.dp
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val minutes = remainingSeconds / 60
                    val seconds = remainingSeconds % 60
                    val timeFormatted = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 44.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isRunning) "تمرکز فعال است..." else "آماده برای شروع",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isRunning) NeonCyan else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Timer Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = {
                        isRunning = false
                        remainingSeconds = selectedMinutes * 60
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "ریست تایمر",
                        tint = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Play / Pause Button
                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier
                        .height(56.dp)
                        .width(140.dp)
                        .testTag("toggle_focus_timer_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) NeonAmber else NeonCyan,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "مکث" else "شروع تمرکز",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Ambient Audio Toggle
                IconButton(
                    onClick = { isAmbientSoundOn = !isAmbientSoundOn },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isAmbientSoundOn) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = if (isAmbientSoundOn) Icons.Default.GraphicEq else Icons.Default.VolumeMute,
                        contentDescription = "صوت تمرکز",
                        tint = if (isAmbientSoundOn) NeonCyan else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Focus Quote Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF26324A), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "زمزمه انضباط ذهنی:",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "«${focusQuotes[currentQuoteIndex]}»",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Focus Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF26324A), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$completedSessionsCount جلسه",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "جلسات تکمیل‌شده امروز",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Color(0xFF26324A))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val totalMinutes = userProfile.totalFocusMinutes + (completedSessionsCount * selectedMinutes)
                        Text(
                            text = "$totalMinutes دقیقه",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مجموع دقایق تمرکز عمیق",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
