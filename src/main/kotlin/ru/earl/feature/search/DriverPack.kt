package ru.earl.feature.search

import kotlinx.serialization.Serializable

@Serializable
data class TripFormDto(
    val username: String,
    val userImage: String,
    val companionRole: String,
    val details: TripFormDetails
)

data class CompanionFormDetails(
    val from: String,
    val to: String,
    val schedule: String,
    val actualTripTime: String,
    val ableToPay: String,
    val comment: String
) : TripFormDetails

data class DriverFormDetails(
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
) : TripFormDetails

interface TripFormDetails