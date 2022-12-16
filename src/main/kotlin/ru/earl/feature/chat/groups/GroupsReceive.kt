package ru.earl.feature.chat.groups

import kotlinx.serialization.Serializable

@Serializable
data class GroupsNewMessageReceive(
    val messageId: String,
    val authorName: String,
    val timestamp: String,
    val messageText: String
)