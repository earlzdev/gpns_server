package ru.earl.feature.chat

import kotlinx.serialization.Serializable

@Serializable
data class RoomResponse(
    val roomId: String,
    val image: String,
    val title: String,
    val lastMessage: String,
    val lastMessageAuthor: String,
    val deletable: Boolean,
    val unreadMsgCounter: Int,
    val lastMsgRead: Int
)

@Serializable
data class NewRoomTokenResponseRemote (
    val token: String
)

@Serializable
data class LastMessageForUpdate(
    val roomId: String,
    val authorName: String,
    val authorImage: String,
    val timestamp: String,
    val messageText: String,
    var read: Int
)

@Serializable
data class RoomIdResponse(
    val id: String
)

@Serializable
data class MessageIdResponse(
    val messageId: String
)