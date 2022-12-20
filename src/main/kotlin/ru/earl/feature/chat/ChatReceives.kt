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
data class MarkAuthoredMessageAsReadRequest(
    val roomId: String,
    val authorName: String
)

@Serializable
data class TypingMessageDto(
    val roomId: String,
    val username: String,
    val typing: Int
)

@Serializable
data class GroupLastMessage(
    val groupsId: String,
    val authorName: String,
    val authorImage: String,
    val msgText: String,
    val timestamp: String,
    val lastMessageRead: Int
)