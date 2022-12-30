package ru.earl.feature.search

import kotlinx.serialization.Serializable

@Serializable
data class TripFormDto(
    val username: String,
    val userImage: String,
    val companionRole: String,
    val from: String,
    val to: String,
    val schedule: String,
    val details: String
)

@Serializable
data class CompanionFormDetails(
    val actualTripTime: String,
    val ableToPay: String,
    val comment: String
) : TripFormDetails

@Serializable
data class DriverFormDetails(
    val catchCompanionFrom: String,
    val alsoCanDriveTo: String,
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

@Serializable
data class DeletedSearchingFormDto(
    val username: String
)

interface TripFormDetails