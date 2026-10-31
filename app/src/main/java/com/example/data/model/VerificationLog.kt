package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class VerificationLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val routineId: String = "",
    val type: String = "bed", // "bed", "voice"
    val isApproved: Boolean = false,
    val aiFeedback: String = "",
    val score: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)
