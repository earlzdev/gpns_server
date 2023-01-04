package ru.earl.models.drivers

import kotlinx.serialization.Serializable

@Serializable
data class DriverDto(
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
