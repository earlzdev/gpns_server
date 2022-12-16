package ru.earl.models.commonGroupMessages

data class CommonGroupMessagesDto (
    val messageId: String,
    val authorName: String,
    val timestamp: String,
    val messageText: String
)