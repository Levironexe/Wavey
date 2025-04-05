package com.example.wavey.model

data class Category(
    val name: String,
    val tag: String,
    val taskCount: Int,
    val iconResId: Int? = null
)
