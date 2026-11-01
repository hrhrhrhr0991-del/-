package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class BroadcastPost(
    @DocumentId val id: String = "",
    val title: String = "",
    val content: String = "",
    val authorName: String = "مدیریت اراده",
    val authorRole: String = "مدیر کل ارشد",
    val isVerified: Boolean = true,
    val tag: String = "اعلان مهم",
    val likesCount: Long = 0L,
    val likedUserIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)
