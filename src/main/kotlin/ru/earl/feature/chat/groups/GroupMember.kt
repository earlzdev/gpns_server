package ru.earl.feature.chat.groups

import io.ktor.websocket.*

data class GroupMember(
    val username: String,
    val socket: WebSocketSession,
    val groupId: String
)
