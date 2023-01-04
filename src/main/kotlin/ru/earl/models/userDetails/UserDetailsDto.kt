package ru.earl.models.userDetails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDetailsDto(
    val userId: String,
    val image: String,
    val username: String,
    val online: Int,
    val lastAuth: String,
    val tripRole: String
)
