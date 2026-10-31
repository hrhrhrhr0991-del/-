package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class RoutineTask(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val type: String = "standard", // "bed_inspection", "wake_voice", "standard"
    val targetTime: String = "07:00",
    val isCompletedToday: Boolean = false,
    val lastCompletedAt: Timestamp? = null,
    val strictLockEnabled: Boolean = false,
    val description: String = "",
    val category: String = "صبحگاهی",
    val priority: String = "high",
    val repeatDays: String = "هر روز",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
