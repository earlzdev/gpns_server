package ru.earl.models.roomsUsers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomsUsersDto(
    @SerialName("room_id") val roomId: String,
    @SerialName("user_id") val userId: String
)
