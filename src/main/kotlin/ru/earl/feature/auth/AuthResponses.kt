package ru.earl.feature.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponses(
    val token: String
)

@Serializable
data class TokenResponse(
    @SerialName("token") val token: String
)

