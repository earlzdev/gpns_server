package ru.earl.models.group

import kotlinx.serialization.Serializable

@Serializable
data class GroupsDto(
    val id: String,
    val title: String,
    val image: String,
    val lastMsgText: String,
    val lastMsgAuthor: String,
    val lastMsgTimestamp: String,
    val lastMsgAuthorImage: String,
    val companionGroup: Int,
    val messagesCount: Int,
    val lastMsgRead: Int
)
