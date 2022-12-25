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
    val driverComment: String
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
    val comment: String
)