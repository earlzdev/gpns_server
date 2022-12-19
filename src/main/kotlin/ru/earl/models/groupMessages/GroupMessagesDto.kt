package ru.earl.models.groupMessages

import kotlinx.serialization.Serializable

@Serializable
data class GroupMessagesDto (
    val groupId: String,
    val messageId: String,
    val authorName: String,
    val authorImage: String,
    val timestamp: String,
    val messageText: String,
    val read: Int
)