package com.example.wavey.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Note(
    @DocumentId val id: String = "",
    val title: String? = "",
    val details: String? = "",
    val creationDate: Timestamp = Timestamp.now(),
    val userId: String? = ""
)
