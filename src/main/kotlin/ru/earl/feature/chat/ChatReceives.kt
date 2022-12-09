package ru.earl.feature.chat

import kotlinx.serialization.Serializable

@Serializable
data class NewRoomReceiveRemote(
    val roomId: String,
    val name: String,
    val image: String,
    val author: String,
    val contact: String,
    val lastMessage: String,
    val lastMessageAuthor: String,
    val contactIsOnline: Int,
    val contactLastAuth: String
)

@Serializable
data class ChatSocketActionReceiveRemote(
    val action: String,
    val userId: String,
    val value: String
)

@Serializable
data class MessageReceive(
    val messageId: String,
    val roomId: String,
    val authorId: String,
    val timestamp: String,
    val messageText: String,
    val messageDate: String,
    val read: Int
)

@Serializable
data class RoomTokenReceive(
    val roomId: String
)

@Serializable
data class MessageId(
    val messageId: String
)

@Serializable
data class MarkAuthoredMessageAsReadRequest(
    val roomId: String,
    val authorName: String
)