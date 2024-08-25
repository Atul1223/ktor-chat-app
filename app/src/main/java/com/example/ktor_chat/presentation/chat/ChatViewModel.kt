package com.example.ktor_chat.presentation.chat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ktor_chat.util.Resource
import com.example.ktor_chat.data.remote.ChatSocketService
import com.example.ktor_chat.data.remote.MessageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val  messageService: MessageService,
    private val chatSocketService: ChatSocketService,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _chatText = mutableStateOf("")
    val chatText: State<String> = _chatText

    private val _state = mutableStateOf(ChatState())
    val state: State<ChatState> = _state

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun connectToChat() {
        getAllMessages()
        savedStateHandle.get<String>("username")?.let {
            viewModelScope.launch {
                val result = chatSocketService.initSession(it)
                when(result) {
                    is Resource.Success -> {
                        chatSocketService.observeMessages()
                            .onEach {message ->
                                val newMessageList = state.value.messages.toMutableList().apply {
                                    add(0,message)
                                }
                                _state.value = state.value.copy(
                                    messages = newMessageList
                                )
                            }.launchIn(viewModelScope)
                    }

                    is Resource.Error -> {
                        _toastEvent.emit(result.message ?: "Unknown Error")
                    }
                }
            }
        }
    }

    fun onChatTextChange(message: String) {
        _chatText.value = message
    }

    fun disconnect() {
        viewModelScope.launch {
            chatSocketService.closeSession()
        }
    }

    fun sendMessage() {
        viewModelScope.launch {
            if(_chatText.value.isNotBlank()) {
                chatSocketService.sendMessage(_chatText.value)
            }
        }
    }

    fun getAllMessages() {
        viewModelScope.launch {
            _state.value = state.value.copy(loading = true)
            val result = messageService.getAllMessages()
            _state.value = state.value.copy(loading = false, messages = result)
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}