package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class PeerMessage(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderHandle: String = "",
    val senderIsVerified: Boolean = false,
    val recipientId: String = "",
    val recipientHandle: String = "",
    val messageText: String = "",
    val messageType: String = "motivation", // motivation, progress, streak, cheer
    val progressPercent: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null
)
