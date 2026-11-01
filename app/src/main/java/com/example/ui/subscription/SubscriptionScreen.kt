package com.example.ui.subscription

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentRequest
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
import java.io.ByteArrayOutputStream

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val subtitle: String,
    val durationDays: Long,
    val priceToman: Long,
    val badge: String = "",
    val features: List<String>
)

const val TARGET_CARD_NUMBER = "6219 8618 1285 3249"
const val TARGET_CARD_HOLDER = "رئیسیان"

@Composable
fun SubscriptionScreen(
    userProfile: UserProfile,
    repository: FirestoreRepository,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val plans = remember {
        listOf(
            SubscriptionPlan(
                id = "plan_1m",
                title = "پلن برنزی (۱ ماهه)",
                subtitle = "برای شروع و استمرار انضباط فردی",
                durationDays = 30L,
                priceToman = 99_000L,
                features = listOf(
                    "دسترسی کامل به چالش‌های هوش مصنوعی",
                    "چالش روزانه عکس تخت خواب با هوش مصنوعی",
                    "چالش زنگ و ویس انگیزشی صبحگاهی",
                    "اتاق تمرکز عمیق و تایمر پومودورو"
                )
            ),
            SubscriptionPlan(
                id = "plan_3m",
                title = "پلن نقره‌ای (۳ ماهه)",
                subtitle = "اقتصادی‌ترین انتخاب برای تثبیت عادات",
                durationDays = 90L,
                priceToman = 249_000L,
                badge = "۱۵٪ تخفیف",
                features = listOf(
                    "تمام امکانات پلن برنزی",
                    "شخصی‌سازی نامحدود تسک‌های اجباری",
                    "نمودارهای پیشرفته تحلیلی هفتگی",
                    "پشتیبانی ضدتحریم و پردازش سریع محلی"
                )
            ),
            SubscriptionPlan(
                id = "plan_6m",
                title = "پلن طلایی (۶ ماهه)",
                subtitle = "محبوب‌ترین انتخاب برای فتح انضباط",
                durationDays = 180L,
                priceToman = 429_000L,
                badge = "🔥 محبوب‌ترین",
                features = listOf(
                    "تمام امکانات پلن نقره‌ای",
                    "تحلیل عمیق شخصیتی مربی هوش مصنوعی",
                    "نشان‌ها و مدال‌های تالار افتخار",
                    "اولویت در تایید فیش و بررسی توسط مربی"
                )
            ),
            SubscriptionPlan(
                id = "plan_1y",
                title = "پلن الماس (۱ ساله)",
                subtitle = "تغییر کامل سبک زندگی و عادات پایدار",
                durationDays = 365L,
                priceToman = 699_000L,
                badge = "💎 بیشترین تخفیف",
                features = listOf(
                    "تمام امکانات نامحدود اپلیکیشن اراده",
                    "دسترسی به تمامی آپدیت‌های آینده",
                    "پشتیبانی اختصاصی مدیر و مربی اراده",
                    "صرفه‌جویی بیش از ۵۰٪ در هزینه اشتراک"
                )
            )
        )
    }

    // rememberSaveable ensures the dialog stays OPEN even if user minimizes the app to open their mobile banking app!
    var selectedPlanId by rememberSaveable { mutableStateOf<String?>(null) }
    var senderCardLast4 by rememberSaveable { mutableStateOf("") }
    var senderCardHolder by rememberSaveable { mutableStateOf("") }
    var trackingCode by rememberSaveable { mutableStateOf("") }
    var receiptBase64 by rememberSaveable { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionSuccessMessage by remember { mutableStateOf<String?>(null) }

    // User's own payment requests
    val userRequests by repository.observeUserPaymentRequests(userProfile.userId).collectAsState(initial = emptyList())
    val pendingRequest = userRequests.firstOrNull { it.status == "pending" }

    // Photo picker for receipt
    val receiptPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                // Compress to compact Base64 for receipt verification
                val scaled = Bitmap.createScaledBitmap(bitmap, 480, (480 * bitmap.height / bitmap.width.coerceAtLeast(1)), true)
                val out = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 70, out)
                val bytes = out.toByteArray()
                receiptBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                Toast.makeText(context, "فیش واریزی پیوست شد ✅", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "خطا در بارگذاری تصویر فیش", Toast.LENGTH_SHORT).show()
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Header Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonAmber.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(NeonAmber.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "دوره ۱۴ روزه آزمایشی به پایان رسید",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonAmber,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "برای دسترسی کامل به قابلیت‌ها، پلن مورد نظر خود را انتخاب کنید",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pending review banner if exists
            if (pendingRequest != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NeonCyan, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = NeonCyan,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "درخواست واریز در حال بررسی است ⏳",
                                style = MaterialTheme.typography.titleSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "پلن انتخابی: ${pendingRequest.planTitle} | مدیر در حال بررسی و تایید فیش است و به زودی حساب شما فعال می‌گردد.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "پلن‌های اشتراک اراده ⚡",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "فعال‌سازی فوری پس از واریز کارت به کارت و تایید مدیر",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Plans List
            plans.forEach { plan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(
                            1.dp,
                            if (plan.badge.isNotBlank()) NeonCyan.copy(alpha = 0.5f) else Color(0xFF26324A),
                            RoundedCornerShape(18.dp)
                        )
                        .clickable { selectedPlanId = plan.id },
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = plan.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = plan.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            if (plan.badge.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonAmber.copy(alpha = 0.15f))
                                        .border(1.dp, NeonAmber, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = plan.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val formattedPrice = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(plan.priceToman)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formattedPrice,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تومان",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            Button(
                                onClick = { selectedPlanId = plan.id },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = DarkBackground
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("انتخاب و واریز", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Option
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("خروج از حساب کاربری")
            }

            Spacer(modifier = Modifier.height(36.dp))
        }

        // CARD-TO-CARD PAYMENT DIALOG (Preserved across minimize!)
        val activePlan = plans.firstOrNull { it.id == selectedPlanId }
        if (activePlan != null) {
            AlertDialog(
                onDismissRequest = {
                    if (!isSubmitting) selectedPlanId = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                containerColor = DarkSurface,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "واریز کارت به کارت",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activePlan.title,
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
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card Details Box
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, NeonAmber, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkBackground),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "شماره کارت جهت واریز:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = TARGET_CARD_NUMBER,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = NeonAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Card Number", TARGET_CARD_NUMBER.replace(" ", "")))
                                            Toast.makeText(context, "شماره کارت کپی شد ✅", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "کپی", tint = NeonAmber)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "به نام: $TARGET_CARD_HOLDER",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                val priceFormatted = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(activePlan.priceToman)
                                Text(
                                    text = "مبلغ قابل پرداخت: $priceFormatted تومان",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NeonEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "💡 راهنما: می‌توانید برنامه را کوچک کرده و با همراه بانک خود واریز کنید؛ اطلاعات این صفحه پاک نخواهد شد. پس از واریز، فرم زیر را پر کنید:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )

                        // Sender 4 Last Digits
                        OutlinedTextField(
                            value = senderCardLast4,
                            onValueChange = { if (it.length <= 4) senderCardLast4 = it },
                            label = { Text("۴ رقم آخر کارت واریز کننده") },
                            placeholder = { Text("مثلاً: 1234") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sender_card_last4_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0xFF26324A)
                            )
                        )

                        // Sender Name
                        OutlinedTextField(
                            value = senderCardHolder,
                            onValueChange = { senderCardHolder = it },
                            label = { Text("نام و نام‌خانوادگی صاحب کارت واریزی") },
                            placeholder = { Text("نام مندرج روی کارت") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sender_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0xFF26324A)
                            )
                        )

                        // Tracking Code (Optional)
                        OutlinedTextField(
                            value = trackingCode,
                            onValueChange = { trackingCode = it },
                            label = { Text("شماره پیگیری / کد ارجاع (اختیاری)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0xFF26324A)
                            )
                        )

                        // Attach Receipt Image Button
                        OutlinedButton(
                            onClick = {
                                receiptPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (receiptBase64.isNotBlank()) NeonEmerald else NeonCyan
                            )
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (receiptBase64.isNotBlank()) "فیش پیوست شد (تغییر عکس)" else "آپلود تصویر فیش واریز",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (senderCardLast4.length != 4) {
                                Toast.makeText(context, "لطفاً ۴ رقم آخر کارت را دقیق وارد کنید", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (senderCardHolder.isBlank()) {
                                Toast.makeText(context, "نام صاحب کارت واریز کننده الزامی است", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSubmitting = true
                            scope.launch {
                                try {
                                    val req = PaymentRequest(
                                        userId = userProfile.userId,
                                        userEmail = userProfile.email,
                                        userDisplayName = userProfile.displayName,
                                        userPhone = userProfile.phoneNumber,
                                        planId = activePlan.id,
                                        planTitle = activePlan.title,
                                        planDurationDays = activePlan.durationDays,
                                        amountToman = activePlan.priceToman,
                                        senderCardLast4 = senderCardLast4,
                                        senderCardHolder = senderCardHolder,
                                        trackingCode = trackingCode,
                                        receiptImageBase64 = receiptBase64
                                    )
                                    repository.submitPaymentRequest(req)
                                    isSubmitting = false
                                    selectedPlanId = null
                                    Toast.makeText(context, "درخواست شما ثبت شد و برای مدیر ارسال گردید ✅", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    isSubmitting = false
                                    Toast.makeText(context, "خطا در ارسال: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonEmerald,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isSubmitting,
                        modifier = Modifier.testTag("confirm_payment_submit_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("واریز کردم و ثبت نهایی", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    if (!isSubmitting) {
                        OutlinedButton(
                            onClick = { selectedPlanId = null },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("انصراف", color = TextMuted)
                        }
                    }
                }
            )
        }
    }
}
