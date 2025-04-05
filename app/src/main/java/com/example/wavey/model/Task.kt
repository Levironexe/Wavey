package com.example.wavey.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Task(
    @DocumentId val id: String = "",
    val title: String = "",
    val details: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val dueDate: Timestamp? = Timestamp.now(),
    val tags: List<String> = emptyList(),
    val completed: Boolean = false,
    val userId: String = ""
)
