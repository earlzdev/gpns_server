package ru.earl.feature.chat

import io.ktor.websocket.*

data class ChatSocketMember(
    val userId: String,
    val socket: WebSocketSession,
)
