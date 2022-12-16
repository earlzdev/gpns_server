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