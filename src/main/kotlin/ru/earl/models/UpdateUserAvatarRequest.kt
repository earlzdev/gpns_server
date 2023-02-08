package ru.earl.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserAvatarRequest(
    val newImageString: String
)