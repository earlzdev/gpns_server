package ru.earl.models.userDetails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDetailsDto(
    @SerialName("userId") val userId: String,
    @SerialName("image") val image: String,
    @SerialName("username") val username: String,
    @SerialName("online") val online: Int,
    @SerialName("lastAuth") val lastAuth: String
)
