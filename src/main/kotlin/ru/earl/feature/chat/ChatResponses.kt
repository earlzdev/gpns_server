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
    val lastMsgRead: Int,
    val contactIsOnline: Int,
    val contactLastAuth: String
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

@Serializable
data class SetUserOnlineInRoom(
    val online: Int,
    val username: String,
    val roomId: String,
    val lastAuthDate: String
)

@Serializable
data class SetUserOnlineInMessaging(
    val online: Int,
    val lastAuth: String
)
