package ru.earl.models.rooms

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomDto(
    @SerialName("roomId") val roomId: String,
    @SerialName("image") val image: String,
    @SerialName("author_name") val author_name: String,
    @SerialName("contact_name") val contact_name: String,
    @SerialName("last_message") val last_message: String,
    @SerialName("last_message_author") val last_message_author: String,
    @SerialName("deletable") val deletable: String
)
