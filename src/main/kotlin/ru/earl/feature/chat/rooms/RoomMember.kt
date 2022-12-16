package ru.earl.feature.chat.rooms

import io.ktor.websocket.*

data class RoomMember(
    val username: String,
    val socket: WebSocketSession,
    val roomId: String
)
