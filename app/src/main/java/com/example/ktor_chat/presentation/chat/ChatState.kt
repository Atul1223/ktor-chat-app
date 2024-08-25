package com.example.ktor_chat.presentation.chat

import com.example.ktor_chat.domain.model.Message

data class ChatState(
    val messages: List<Message> = emptyList(),
    val loading: Boolean = false,
)
