package com.example.ktor_chat.domain.model

data class Message(
    val text: String,
    val formattedTime: String,
    val userName: String
)
