package ru.earl.feature.search

import kotlinx.serialization.Serializable

@Serializable
data class DriverFormReceive(
    val username: String,
    val userImage: String,
    val driveFrom: String,
    val driveTo: String,
    val catchCompanionFrom: String,
    val alsoCanDriveTo: String,
    val schedule: String,
    val ableToDriveInTurn: Int,
    val actualTripTime: String,
    val car: String,
    val carModel: String,
    val carColor: String,
    val passengersCount: Int,
    val carGovNumber: String,
    val tripPrice: Int,
    val driverComment: String,
    val active: Int
)

@Serializable
data class CompanionFormReceive(
    val username: String,
    val userImage: String,
    val from: String,
    val to: String,
    val schedule: String,
    val actualTripTime: String,
    val ableToPay: String,
    val comment: String,
    val active: Int
)

@Serializable
data class TripNotificationsReceive(
    val id: String,
    val authorName: String,
    val receiverName: String,
    val authorTripRole: String,
    val receiverTripRole: String,
    val type: String,
    val timestamp: String,
)

@Serializable
data class TripNotificationAnswer(
    val tripId: String,
    val accept: Int
)

@Serializable
data class UserNameDto(
    val name: String
)

@Serializable
data class RemoveCompanionFromGroupDto(
    val username: String,
    val groupId: String
)