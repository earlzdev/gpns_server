package ru.earl.feature.chat.rooms

import io.ktor.websocket.*
import ru.earl.models.users.User

data class ChatSocketMember(
    val userId: String,
    val socket: WebSocketSession,
) {
    fun provideUsername() = User.fetchUserById(userId)?.username
}
