package ru.earl.models.companions

import kotlinx.serialization.Serializable

@Serializable
data class CompanionDto(
    val username: String,
    val userImage: String,
    val from: String,
    val to: String,
    val schedule: String,
    val actualTripTime: String,
    val ableToPay: String,
    val comment: String
)