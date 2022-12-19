package ru.earl.feature.chat.groups

import kotlinx.serialization.Serializable

@Serializable
data class GroupsNewMessageReceive(
    val groupId: String,
    val messageId: String,
    val authorName: String,
    val authorImage: String,
    val timestamp: String,
    val messageText: String,
    val read: Int
)
@Serializable
data class GroupIdReceive(
    val groupId: String
)

@Serializable
data class TypingStatusInGroupRequest(
    val groupId: String,
    val username: String,
    val status: Int
)