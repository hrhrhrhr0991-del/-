package com.example.ui.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class DisciplineBadge(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val requirement: String,
    val isUnlocked: Boolean
)

@Composable
fun AchievementsCard(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val streak = userProfile.streakCount
    val totalFocus = userProfile.totalFocusMinutes

    val badges = listOf(
        DisciplineBadge("wake_1", "سحرخیز پولادین", "🌅", "اولین بیداری صبحگاهی با چالش صوتی", streak >= 1),
        DisciplineBadge("bed_1", "فرمانده نظم", "🛏️", "تایید چالش مرتب‌سازی تخت خواب", streak >= 1),
        DisciplineBadge("focus_1", "ذهن زلال و متمرکز", "⚡", "تکمیل جلسه تمرکز عمیق", totalFocus >= 20),
        DisciplineBadge("streak_3", "آتش استمرار ۳ روزه", "🔥", "حفظ اراده به مدت ۳ روز متوالی", streak >= 3),
        DisciplineBadge("streak_7", "انضباط آهنین ۷ روزه", "🛡️", "۷ روز پایبندی مداوم به روتین‌ها", streak >= 7),
        DisciplineBadge("conqueror", "فاتح دوره اراده", "👑", "پایان پیروزمندانه دوره ۱۴ روزه", streak >= 14)
    )

    val unlockedCount = badges.count { it.isUnlocked }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NeonAmber.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
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
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = NeonAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "نشان‌ها و مدال‌های افتخار",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$unlockedCount از ${badges.size} باز شده",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                badges.forEach { badge ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (badge.isUnlocked) NeonAmber.copy(alpha = 0.10f) else DarkSurfaceVariant)
                            .border(
                                1.dp,
                                if (badge.isUnlocked) NeonAmber.copy(alpha = 0.4f) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = badge.iconEmoji,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = badge.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (badge.isUnlocked) TextPrimary else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = badge.requirement,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = if (badge.isUnlocked) TextSecondary else TextMuted
                            )
                        }
                        if (badge.isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "باز شده",
                                tint = NeonAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "قفل",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
