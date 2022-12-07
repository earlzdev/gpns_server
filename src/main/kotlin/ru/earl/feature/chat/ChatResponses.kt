package ru.earl.feature.chat

import kotlinx.serialization.Serializable

@Serializable
data class RoomResponse(
    val roomId: String,
    val image: String,
    val title: String,
    val lastMessage: String,
    val lastMessageAuthor: String,
    val deletable: Boolean
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

data class TestResponse(
    val valueFirst: String
)

@Serializable
data class MessageIdResponse(
    val messageId: String
)