package ru.earl.models.groupUsers

import kotlinx.serialization.Serializable

@Serializable
data class GroupUsersDto (
    val compGroupId: String,
    val userId: String
)

