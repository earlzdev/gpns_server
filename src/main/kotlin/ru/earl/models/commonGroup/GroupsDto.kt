package ru.earl.models.commonGroup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupsDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("image") val image: String,
    @SerialName("last_message") val lastMsgText: String,
    @SerialName("last_message_author") val lastMsgAuthor: String,
    @SerialName("last_message_timestamp") val lastMsgTimestamp: String,
    @SerialName("last_message_author_image") val lastMsgAuthorImage: String,
)
