package com.example.data.repository

import android.content.Context
import com.example.R
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
        )
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
            throw e
        }

        if (snapshot.exists()) {
            val profile = snapshot.toObject(UserProfile::class.java)
            if (profile != null) return profile
        }

        // Calculate 14 days trial end date
        val now = Date()
        val calendar = Calendar.getInstance()
        calendar.time = now
        calendar.add(Calendar.DAY_OF_YEAR, 14)
        val trialEnd = Timestamp(calendar.time)
        val trialStart = Timestamp(now)

        val newProfileMap = mapOf(
            "userId" to userId,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "displayName" to displayName,
            "trialStartDate" to trialStart,
            "trialEndDate" to trialEnd,
            "streakCount" to 1L,
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
                trialStartDate = trialStart,
                trialEndDate = trialEnd,
                streakCount = 1L
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
