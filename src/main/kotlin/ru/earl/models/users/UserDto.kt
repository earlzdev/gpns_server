package ru.earl.models.users

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto (
    @SerialName("email") val email: String,
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
    @SerialName("salt") val userSalt: String,
    @SerialName("id") val userId: String,
)
