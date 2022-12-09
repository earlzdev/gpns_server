package ru.earl.models.usersOnline

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsersOnlineDto(
    @SerialName("user_id") val userId: String,
    @SerialName("last_authentication") val lastAuthentication: String,
    @SerialName("is_online") val isOnline: Int
    )
