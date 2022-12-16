package ru.earl.feature.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SocketModelDto (
    @SerialName("action") val action: String,
    @SerialName("value") val value: String
)
