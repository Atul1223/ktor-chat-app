package com.example.ktor_chat.data.remote

import com.example.ktor_chat.domain.model.Message
import com.example.ktor_chat.data.remote.dto.MessageDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.lang.Exception

class MessageServiceImpl(
    private val client: HttpClient
): MessageService {
    override suspend fun getAllMessages(): List<Message> {
        return try {
            val messages: List<MessageDto> = client.get(MessageService.EndPoints.GetAllMessages.url).body()
            messages.map {
                it.toMessage()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}