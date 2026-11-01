package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class PaymentRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userDisplayName: String = "",
    val userPhone: String = "",
    val planId: String = "",
    val planTitle: String = "",
    val planDurationDays: Long = 30L,
    val amountToman: Long = 0L,
    val senderCardLast4: String = "",
    val senderCardHolder: String = "",
    val trackingCode: String = "",
    val receiptImageBase64: String = "",
    val status: String = "pending", // "pending", "approved", "rejected"
    val adminNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
