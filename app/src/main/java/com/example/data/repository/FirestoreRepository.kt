package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.PaymentRequest
import com.example.data.model.RoutineTask
import com.example.data.model.UserNote
import com.example.data.model.UserProfile
import com.example.data.model.VerificationLog
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException

class FirestoreRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ).apply {
            try {
                firestoreSettings = com.google.firebase.firestore.firestoreSettings {
                    setLocalCacheSettings(com.google.firebase.firestore.persistentCacheSettings {})
                }
            } catch (e: Exception) {
                // Ignore if settings were already initialized
            }
        }
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    // 1. User Profile Observation and Creation
    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot -> snapshot.toObject(UserProfile::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun getOrCreateUserProfile(
        userId: String,
        email: String,
        phoneNumber: String = "",
        displayName: String = ""
    ): UserProfile {
        val userDoc = db.collection("users").document(userId)
        val snapshot = try {
            userDoc.get().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, userDoc.path)
            null
        }

        val cleanEmail = email.trim().lowercase()
        val isSuperAdminEmail = cleanEmail == "hrhrhrhr0991@gmail.com"

        if (snapshot != null && snapshot.exists()) {
            val profile = snapshot.toObject(UserProfile::class.java)
            if (profile != null) {
                if (!isSuperAdminEmail && (profile.role == "super_admin" || profile.role == "admin")) {
                    try {
                        userDoc.update("role", "user").await()
                        return profile.copy(role = "user")
                    } catch (e: Exception) {
                        return profile.copy(role = "user")
                    }
                }
                if (isSuperAdminEmail && profile.role != "super_admin") {
                    try {
                        userDoc.update("role", "super_admin").await()
                        return profile.copy(role = "super_admin")
                    } catch (e: Exception) {
                        return profile
                    }
                }
                return profile
            }
        }

        // Calculate 14 days trial end date
        val now = Date()
        val calendar = Calendar.getInstance()
        calendar.time = now
        calendar.add(Calendar.DAY_OF_YEAR, if (isSuperAdminEmail) 3650 else 14)
        val trialEnd = Timestamp(calendar.time)
        val trialStart = Timestamp(now)
        val initialRole = if (isSuperAdminEmail) "super_admin" else "user"

        val newProfileMap = mapOf(
            "userId" to userId,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "displayName" to displayName,
            "role" to initialRole,
            "unlockedBadges" to emptyList<String>(),
            "adminNote" to "",
            "isBanned" to false,
            "trialStartDate" to trialStart,
            "trialEndDate" to trialEnd,
            "streakCount" to 1L,
            "isSubscribed" to isSuperAdminEmail,
            "lastActiveDate" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(now),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            userDoc.set(newProfileMap).await()
            // Create default routine tasks for the new user
            seedDefaultRoutines(userId)
            return UserProfile(
                id = userId,
                userId = userId,
                email = email,
                phoneNumber = phoneNumber,
                displayName = displayName,
                role = initialRole,
                unlockedBadges = emptyList(),
                adminNote = "",
                isBanned = false,
                trialStartDate = trialStart,
                trialEndDate = trialEnd,
                streakCount = 1L,
                isSubscribed = isSuperAdminEmail
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, userDoc.path)
            throw e
        }
    }

    private suspend fun seedDefaultRoutines(userId: String) {
        val routinesCol = db.collection("users").document(userId).collection("routines")

        val bedTask = mapOf(
            "userId" to userId,
            "title" to "مرتب کردن تخت خواب (چالش عکس هوش مصنوعی)",
            "type" to "bed_inspection",
            "targetTime" to "07:30",
            "isCompletedToday" to false,
            "strictLockEnabled" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val wakeTask = mapOf(
            "userId" to userId,
            "title" to "بیداری صبحگاهی و چالش ویس انگیزشی",
            "type" to "wake_voice",
            "targetTime" to "07:00",
            "isCompletedToday" to false,
            "strictLockEnabled" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val standardTask = mapOf(
            "userId" to userId,
            "title" to "نوشیدن ۲ لیوان آب و کشش بدنی",
            "type" to "standard",
            "targetTime" to "07:15",
            "isCompletedToday" to false,
            "strictLockEnabled" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            routinesCol.add(wakeTask).await()
            routinesCol.add(bedTask).await()
            routinesCol.add(standardTask).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, routinesCol.path)
        }
    }

    // 2. Routines Observation and Updates
    fun observeRoutines(): Flow<List<RoutineTask>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/routines"
        emitAll(
            db.collection("users").document(uid).collection("routines")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(RoutineTask::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun updateUserPersona(personaArchetype: String, aiCoachTone: String) {
        val uid = requireUserId()
        val userDoc = db.collection("users").document(uid)
        try {
            userDoc.update(
                mapOf(
                    "personaArchetype" to personaArchetype,
                    "aiCoachTone" to aiCoachTone,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userDoc.path)
            throw e
        }
    }

    suspend fun addRoutine(task: RoutineTask) {
        val uid = requireUserId()
        val routinesCol = db.collection("users").document(uid).collection("routines")
        val payload = mapOf(
            "userId" to uid,
            "title" to task.title,
            "type" to task.type,
            "targetTime" to task.targetTime,
            "isCompletedToday" to false,
            "strictLockEnabled" to task.strictLockEnabled,
            "description" to task.description,
            "category" to task.category,
            "priority" to task.priority,
            "repeatDays" to task.repeatDays,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            routinesCol.add(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, routinesCol.path)
            throw e
        }
    }

    suspend fun updateRoutineCompletion(routineId: String, completed: Boolean) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("routines").document(routineId)
        val updates = mutableMapOf<String, Any>(
            "isCompletedToday" to completed,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (completed) {
            updates["lastCompletedAt"] = FieldValue.serverTimestamp()
        }
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteRoutine(routineId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("routines").document(routineId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // 3. Verification Log
    suspend fun logVerification(
        routineId: String,
        type: String,
        isApproved: Boolean,
        aiFeedback: String,
        score: Long
    ) {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("verifications")
        val payload = mapOf(
            "userId" to uid,
            "routineId" to routineId,
            "type" to type,
            "isApproved" to isApproved,
            "aiFeedback" to aiFeedback,
            "score" to score,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            colRef.add(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    // 4. Notes Observation and Management
    fun observeNotes(): Flow<List<UserNote>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/notes"
        emitAll(
            db.collection("users").document(uid).collection("notes")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(UserNote::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addNote(title: String, content: String, category: String, priority: String) {
        val uid = requireUserId()
        val notesCol = db.collection("users").document(uid).collection("notes")
        val payload = mapOf(
            "userId" to uid,
            "title" to title,
            "content" to content,
            "category" to category,
            "priority" to priority,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            notesCol.add(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, notesCol.path)
            throw e
        }
    }

    suspend fun deleteNote(noteId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun logFocusSession(minutes: Int) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "totalFocusMinutes" to FieldValue.increment(minutes.toLong()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
        }
    }

    suspend fun updateUserProfileDetails(displayName: String, phoneNumber: String) {
        val uid = requireUserId()
        val userDoc = db.collection("users").document(uid)
        try {
            userDoc.update(
                mapOf(
                    "displayName" to displayName,
                    "phoneNumber" to phoneNumber,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userDoc.path)
            throw e
        }
    }

    suspend fun submitPaymentRequest(request: PaymentRequest) {
        val uid = requireUserId()
        val requestsCol = db.collection("payment_requests")
        val payload = mapOf(
            "userId" to uid,
            "userEmail" to request.userEmail,
            "userDisplayName" to request.userDisplayName,
            "userPhone" to request.userPhone,
            "planId" to request.planId,
            "planTitle" to request.planTitle,
            "planDurationDays" to request.planDurationDays,
            "amountToman" to request.amountToman,
            "senderCardLast4" to request.senderCardLast4,
            "senderCardHolder" to request.senderCardHolder,
            "trackingCode" to request.trackingCode,
            "receiptImageBase64" to request.receiptImageBase64,
            "status" to "pending",
            "adminNote" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            requestsCol.add(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, requestsCol.path)
            throw e
        }
    }

    fun observeUserPaymentRequests(userId: String): Flow<List<PaymentRequest>> = flow {
        val path = "payment_requests"
        emitAll(
            db.collection("payment_requests")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(PaymentRequest::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    // ADMIN OPERATIONS
    fun observeAllUsers(): Flow<List<UserProfile>> = flow {
        val path = "users"
        emitAll(
            db.collection("users")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(UserProfile::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeAllPaymentRequests(): Flow<List<PaymentRequest>> = flow {
        val path = "payment_requests"
        emitAll(
            db.collection("payment_requests")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(PaymentRequest::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun approvePaymentRequest(
        requestId: String,
        targetUserId: String,
        durationDays: Long,
        planTitle: String
    ) {
        val reqDoc = db.collection("payment_requests").document(requestId)
        val userDoc = db.collection("users").document(targetUserId)

        try {
            reqDoc.update(
                mapOf(
                    "status" to "approved",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val userSnap = userDoc.get().await()
            val currentEndDate = userSnap.getTimestamp("trialEndDate")?.toDate() ?: Date()
            val baseTime = if (currentEndDate.after(Date())) currentEndDate else Date()
            val cal = Calendar.getInstance().apply {
                time = baseTime
                add(Calendar.DAY_OF_YEAR, durationDays.toInt())
            }
            val newTrialEnd = Timestamp(cal.time)

            userDoc.update(
                mapOf(
                    "trialEndDate" to newTrialEnd,
                    "isSubscribed" to true,
                    "subscriptionPlan" to planTitle,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, reqDoc.path)
            throw e
        }
    }

    suspend fun rejectPaymentRequest(requestId: String, note: String) {
        val reqDoc = db.collection("payment_requests").document(requestId)
        try {
            reqDoc.update(
                mapOf(
                    "status" to "rejected",
                    "adminNote" to note,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, reqDoc.path)
            throw e
        }
    }

    suspend fun adminExtendUserSubscription(targetUserId: String, extraDays: Long) {
        val userDoc = db.collection("users").document(targetUserId)
        try {
            val userSnap = userDoc.get().await()
            val currentEndDate = userSnap.getTimestamp("trialEndDate")?.toDate() ?: Date()
            val baseTime = if (currentEndDate.after(Date())) currentEndDate else Date()
            val cal = Calendar.getInstance().apply {
                time = baseTime
                add(Calendar.DAY_OF_YEAR, extraDays.toInt())
            }
            userDoc.update(
                mapOf(
                    "trialEndDate" to Timestamp(cal.time),
                    "isSubscribed" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userDoc.path)
            throw e
        }
    }

    suspend fun updateUserRole(targetUserId: String, newRole: String) {
        val userDoc = db.collection("users").document(targetUserId)
        try {
            userDoc.update(
                mapOf(
                    "role" to newRole,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userDoc.path)
            throw e
        }
    }

    suspend fun updateUserBadges(targetUserId: String, badges: List<String>) {
        val userDoc = db.collection("users").document(targetUserId)
        try {
            userDoc.update(
                mapOf(
                    "unlockedBadges" to badges,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userDoc.path)
            throw e
        }
    }

    suspend fun updateUserStreak(targetUserId: String, newStreak: Long) {
        val userDoc = db.collection("users").document(targetUserId)
        try {
            userDoc.update(
                mapOf(
                    "streakCount" to newStreak.coerceAtLeast(0L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userDoc.path)
            throw e
        }
    }

    suspend fun updateUserAdminNote(targetUserId: String, note: String) {
        val userDoc = db.collection("users").document(targetUserId)
        try {
            userDoc.update(
                mapOf(
                    "adminNote" to note,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userDoc.path)
            throw e
        }
    }

    suspend fun toggleUserBan(targetUserId: String, isBanned: Boolean) {
        val userDoc = db.collection("users").document(targetUserId)
        try {
            userDoc.update(
                mapOf(
                    "isBanned" to isBanned,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userDoc.path)
            throw e
        }
    }

    suspend fun adminAdjustSubscription(targetUserId: String, deltaDays: Long) {
        val userDoc = db.collection("users").document(targetUserId)
        try {
            val userSnap = userDoc.get().await()
            val currentEndDate = userSnap.getTimestamp("trialEndDate")?.toDate() ?: Date()
            val cal = Calendar.getInstance().apply {
                time = currentEndDate
                add(Calendar.DAY_OF_YEAR, deltaDays.toInt())
            }
            val newEndTime = cal.time
            val isStillSubscribed = newEndTime.after(Date())
            userDoc.update(
                mapOf(
                    "trialEndDate" to Timestamp(newEndTime),
                    "isSubscribed" to isStillSubscribed,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userDoc.path)
            throw e
        }
    }

    suspend fun adminCreateUser(
        displayName: String,
        email: String,
        phone: String,
        role: String,
        durationDays: Long
    ): UserProfile {
        val newUserId = "user_manual_${System.currentTimeMillis().toString().takeLast(8)}"
        val userDoc = db.collection("users").document(newUserId)
        val now = Date()
        val calendar = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_YEAR, durationDays.toInt().coerceAtLeast(1))
        }
        val trialEnd = Timestamp(calendar.time)
        val trialStart = Timestamp(now)

        val newProfileMap = mapOf(
            "userId" to newUserId,
            "email" to email,
            "phoneNumber" to phone,
            "displayName" to displayName,
            "role" to role,
            "unlockedBadges" to emptyList<String>(),
            "adminNote" to "",
            "isBanned" to false,
            "trialStartDate" to trialStart,
            "trialEndDate" to trialEnd,
            "streakCount" to 1L,
            "isSubscribed" to (durationDays > 14),
            "subscriptionPlan" to if (durationDays > 14) "ثبت مستقیم مدیریت" else "",
            "lastActiveDate" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(now),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            userDoc.set(newProfileMap).await()
            seedDefaultRoutines(newUserId)
            return UserProfile(
                id = newUserId,
                userId = newUserId,
                email = email,
                phoneNumber = phone,
                displayName = displayName,
                role = role,
                unlockedBadges = emptyList(),
                adminNote = "",
                isBanned = false,
                trialStartDate = trialStart,
                trialEndDate = trialEnd,
                streakCount = 1L,
                isSubscribed = (durationDays > 14),
                subscriptionPlan = if (durationDays > 14) "ثبت مستقیم مدیریت" else ""
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, userDoc.path)
            throw e
        }
    }
}
