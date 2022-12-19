package ru.earl.feature.chat.groups

import kotlinx.serialization.Serializable

@Serializable
data class LastMessageForUpdateInGroup(
    val groupId: String,
    val authorName: String,
    val authorImage: String,
    val messageText: String,
    val timestamp: String
)

@Serializable
data class GroupMessageResponse(
    val groupId: String,
    val messageId: String,
    val authorName: String,
    val authorImage: String,
    val timestamp: String,
    val messageText: String,
    val read: Int
)