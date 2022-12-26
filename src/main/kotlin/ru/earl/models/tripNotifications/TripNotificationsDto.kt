package ru.earl.models.tripNotifications

import kotlinx.serialization.Serializable

@Serializable
data class TripNotificationsDto(
    val id: String,
    val authorName: String,
    val receiverName: String,
    val authorTripRole: String,
    val receiverTripRole: String,
    val isInvite: Int,
    val timestamp: String,
)
