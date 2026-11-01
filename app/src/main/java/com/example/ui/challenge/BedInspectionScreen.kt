package com.example.ui.challenge

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ai.BedInspectionResult
import com.example.data.ai.GeminiAiManager
import com.example.data.model.RoutineTask
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
fun BedInspectionScreen(
    task: RoutineTask,
    aiManager: GeminiAiManager,
    repository: FirestoreRepository,
    onChallengeCompleted: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var inspectionResult by remember { mutableStateOf<BedInspectionResult?>(null) }
    val scope = rememberCoroutineScope()

    fun evaluateImage(bitmap: Bitmap) {
        capturedBitmap = bitmap
        isAnalyzing = true
        inspectionResult = null
        scope.launch {
            val result = aiManager.inspectBedImage(bitmap)
            inspectionResult = result
            isAnalyzing = false
            if (result.isRelevant && result.isTidy) {
                try {
                    repository.logVerification(
                        routineId = task.id,
                        type = "bed",
                        isApproved = true,
                        aiFeedback = result.feedback,
                        score = result.score.toLong()
                    )
                } catch (e: Exception) {
                    Log.e("BedInspection", "Error logging verification", e)
                }
            }
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            evaluateImage(bitmap)
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                Log.e("BedInspection", "Camera launch error", e)
                Toast.makeText(context, "خطا در فراخوانی دوربین. از گالری استفاده کنید.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "دسترسی به دوربین نیاز است. یا می‌توانید از گالری عکس انتخاب کنید.", Toast.LENGTH_LONG).show()
        }
    }

    // Safe Camera Invocation
    fun requestCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                Log.e("BedInspection", "Camera open error", e)
                Toast.makeText(context, "دوربین در دسترس نیست. عکس را از گالری انتخاب کنید.", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Photo Picker (No permission required, highly reliable on all devices & emulators)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                Log.e("BedInspection", "Error decoding uri", e)
                null
            }
            if (bitmap != null) {
                evaluateImage(bitmap)
            }
        }
    }

    // Scanning Line Animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laser"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "چالش قفل هوشمند: مرتب‌سازی تخت",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Strict Mode Lock Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NeonAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "حالت انضباط قاطع فعال است",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "از تخت خواب خود عکس بگیرید؛ هوش مصنوعی جمینای در چند صدم ثانیه مرتب بودن ملحفه‌ها و بالش را ارزیابی می‌کند.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Image Preview or Camera Capture Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceVariant)
                    .border(
                        2.dp,
                        if (isAnalyzing) NeonCyan else Color(0xFF2B374D),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val currentBitmap = capturedBitmap
                if (currentBitmap != null) {
                    Image(
                        bitmap = currentBitmap.asImageBitmap(),
                        contentDescription = "عکس ثبت شده از تخت خواب",
                        modifier = Modifier.fillMaxSize()
                    )

                    // Scanning animation overlay while analyzing
                    if (isAnalyzing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .align(Alignment.TopCenter)
                                    .padding(top = (scanOffset * 250).dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color.Transparent, NeonCyan, Color.Transparent)
                                        )
                                    )
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "برای تایید مرتب بودن تخت، یک عکس شفاف ثبت کنید",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تصاویر غیرمرتبط به طور خودکار توسط هوش رد خواهند شد",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trigger Buttons: Live Camera & Photo Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Live Camera Button
                Button(
                    onClick = { requestCamera() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("launch_camera_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isAnalyzing
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (capturedBitmap == null) "عکاسی زنده" else "عکاسی مجدد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Gallery / Photo Picker Button (Zero-permission, works everywhere)
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("pick_photo_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NeonCyan
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(NeonCyan, NeonAmber))
                    ),
                    enabled = !isAnalyzing
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "انتخاب از گالری",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Simulator for Quick Test / Offline
            OutlinedButton(
                onClick = {
                    val simBitmap = createSimulatedBedBitmap(isTidy = true)
                    evaluateImage(simBitmap)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("simulate_tidy_bed_button"),
                shape = RoundedCornerShape(10.dp),
                enabled = !isAnalyzing
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تست با تصویر شبیه‌سازی تخت کاملاً مرتب", color = TextMuted, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // AI Analysis Status / Loading
            if (isAnalyzing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = NeonCyan,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "هوش مصنوعی جمینای در حال بررسی بینایی تصویر...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // AI Evaluation Result Card
            inspectionResult?.let { result ->
                if (!result.isRelevant) {
                    // IRRELEVANT IMAGE REJECTION CARD
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(16.dp))
                            .testTag("ai_irrelevant_result_card"),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
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
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "تصویر نامرتبط تشخیص داده شد!",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "نمره انضباط: ۰ از ۱۰۰",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = result.feedback.ifBlank {
                                    "هوش مصنوعی تشخیص داد که تصویر ارسالی مربوط به تخت خواب نیست (مانند چهره، دیوار، مانیتور، کفش، حیوانات یا محیط‌های دیگر). لطفاً صرفاً از فضای تخت خود عکس بگیرید."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { requestCamera() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("عکسبرداری دوباره از تخت خواب", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // RELEVANT BED IMAGE RESULT
                    val isSuccess = result.isTidy || result.score >= 70
                    val resultColor = if (isSuccess) NeonEmerald else MaterialTheme.colorScheme.error

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, resultColor, RoundedCornerShape(16.dp))
                            .testTag("ai_result_card"),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = resultColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isSuccess) "تایید شد! تخت خواب مرتب است" else "رد شد! تخت نامرتب است",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = resultColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "نمره: ${result.score}/۱۰۰",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = resultColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (result.isOfflineEngine) NeonEmerald.copy(alpha = 0.15f) else NeonCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (result.isOfflineEngine) "🛡️ موتور محلی ضدتحریم (بدون نیاز به وی‌پی‌ان)" else "⚡ پردازش ابری جمینای",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (result.isOfflineEngine) NeonEmerald else NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = result.feedback,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )

                            if (result.issues.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "موارد نیازمند اصلاح و صاف کردن:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                result.issues.forEach { issue ->
                                    Text(
                                        text = "• $issue",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isSuccess) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            repository.updateRoutineCompletion(task.id, true)
                                            onChallengeCompleted()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("confirm_bed_challenge_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonEmerald,
                                        contentColor = DarkBackground
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تایید نهایی و بازگشایی قفل تسک", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { requestCamera() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonAmber,
                                        contentColor = DarkBackground
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("اصلاح تخت و عکسبرداری مجدد", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

/**
 * Creates a simulated bitmap of a neatly made bed for zero-latency testing
 */
private fun createSimulatedBedBitmap(isTidy: Boolean): Bitmap {
    val width = 480
    val height = 360
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Bedroom Floor background
    paint.color = AndroidColor.rgb(38, 45, 60)
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    // Bed Frame
    paint.color = AndroidColor.rgb(75, 45, 25)
    canvas.drawRoundRect(40f, 40f, 440f, 330f, 16f, 16f, paint)

    // Mattress & Bed Sheets
    paint.color = if (isTidy) AndroidColor.rgb(240, 243, 248) else AndroidColor.rgb(180, 190, 205)
    canvas.drawRoundRect(55f, 60f, 425f, 315f, 12f, 12f, paint)

    // Folded Blanket / Duvet
    paint.color = AndroidColor.rgb(28, 120, 190)
    canvas.drawRoundRect(55f, 130f, 425f, 315f, 8f, 8f, paint)

    // Pillows (Two neat pillows on top)
    paint.color = AndroidColor.WHITE
    if (isTidy) {
        canvas.drawRoundRect(70f, 70f, 220f, 120f, 10f, 10f, paint)
        canvas.drawRoundRect(260f, 70f, 410f, 120f, 10f, 10f, paint)
    } else {
        canvas.save()
        canvas.rotate(18f, 150f, 95f)
        canvas.drawRoundRect(70f, 70f, 220f, 120f, 10f, 10f, paint)
        canvas.restore()
        canvas.drawRoundRect(250f, 85f, 400f, 135f, 10f, 10f, paint)
    }

    return bitmap
}
