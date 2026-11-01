package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val displayName: String = "",
    val trialStartDate: Timestamp? = null,
    val trialEndDate: Timestamp? = null,
    val streakCount: Long = 0L,
    val lastActiveDate: String = "",
    val personaArchetype: String = "",
    val aiCoachTone: String = "",
    val totalFocusMinutes: Long = 0L,
    val isSubscribed: Boolean = false,
    val subscriptionPlan: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
