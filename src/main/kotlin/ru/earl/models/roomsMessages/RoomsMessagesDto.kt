package ru.earl.models.roomsMessages

import kotlinx.serialization.Serializable

@Serializable
data class RoomsMessagesDto(
    val messageId: String,
    val roomId: String,
    val authorId: String,
    val timestamp: String,
    val messageText: String,
    val messageDate: String,
    var read: Int
)
