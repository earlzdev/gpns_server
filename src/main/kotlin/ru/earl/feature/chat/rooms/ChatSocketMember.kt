package ru.earl.feature.chat.rooms

import io.ktor.websocket.*

data class ChatSocketMember(
    val userId: String,
    val socket: WebSocketSession,
)
