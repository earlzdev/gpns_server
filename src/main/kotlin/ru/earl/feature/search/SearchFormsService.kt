package ru.earl.feature.search

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.earl.feature.chat.OnlineController
import ru.earl.models.companions.CompanionDto
import ru.earl.models.companions.Companions
import ru.earl.models.drivers.DriverDto
import ru.earl.models.drivers.Drivers

interface SearchFormsService {

    suspend fun fetchAllForms(call: ApplicationCall)
    suspend fun insertNewCompanionForm(call: ApplicationCall)
    suspend fun insertNewDriverForm(call: ApplicationCall)
    suspend fun deleteCompanionForm(call: ApplicationCall)
    suspend fun deleteDriverForm(call: ApplicationCall)
}

class SearchFormsServiceImpl : SearchFormsService, OnlineController() {

    override suspend fun fetchAllForms(call: ApplicationCall) {
        authenticate(call)?.apply {
            try {
                val companions = Companions.fetchAllCompanions()
                val drivers = Drivers.fetchAllDrivers()
                val readyFormsList = mutableListOf<TripFormDto>()
                for (i in companions.indices) {
                    readyFormsList.add(
                        TripFormDto(
                            companions[i].username,
                            companions[i].userImage,
                            COMPANION_ROLE,
                            companions[i].from,
                            companions[i].to,
                            companions[i].schedule,
                            Json.encodeToString(CompanionFormDetails(
                                companions[i].actualTripTime,
                                companions[i].ableToPay,
                                companions[i].comment,
                            ))
                        )
                    )
                }
                for (i in drivers.indices) {
                    readyFormsList.add(
                        TripFormDto(
                            drivers[i].username,
                            drivers[i].userImage,
                            DRIVER_ROLE,
                            drivers[i].driveFrom,
                            drivers[i].driveTo,
                            drivers[i].schedule,
                            Json.encodeToString(DriverFormDetails(
                                drivers[i].catchCompanionFrom,
                                drivers[i].alsoCanDriveTo,
                                drivers[i].ableToDriveInTurn,
                                drivers[i].actualTripTime,
                                drivers[i].car,
                                drivers[i].carModel,
                                drivers[i].carColor,
                                drivers[i].passengersCount,
                                drivers[i].carGovNumber,
                                drivers[i].tripPrice,
                                drivers[i].driverComment,
                            ))
                        )
                    )
                }
                call.respond(HttpStatusCode.OK, readyFormsList)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.Conflict)
            }
        }
    }

    override suspend fun insertNewCompanionForm(call: ApplicationCall) {
        authenticate(call)?.apply {
            val receive = call.receive<CompanionFormReceive>()
            try {
                Companions.insertNewCompanion(
                    CompanionDto(
                        receive.username,
                        receive.userImage,
                        receive.from,
                        receive.to,
                        receive.schedule,
                        receive.actualTripTime,
                        receive.ableToPay,
                        receive.comment
                    )
                )
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.Conflict, e)
            }
        }
    }

    override suspend fun insertNewDriverForm(call: ApplicationCall) {
        authenticate(call)?.apply {
            val receive = call.receive<DriverFormReceive>()
            try {
                Drivers.insertNewDriverForm(DriverDto(
                    receive.username,
                    receive.userImage,
                    receive.driveFrom,
                    receive.driveTo,
                    receive.catchCompanionFrom,
                    receive.alsoCanDriveTo,
                    receive.schedule,
                    receive.ableToDriveInTurn,
                    receive.actualTripTime,
                    receive.car,
                    receive.carModel,
                    receive.carColor,
                    receive.passengersCount,
                    receive.carGovNumber,
                    receive.tripPrice,
                    receive.driverComment
                ))
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.Conflict, e)
            }
        }
    }

    override suspend fun deleteCompanionForm(call: ApplicationCall) {
        // todo
    }

    override suspend fun deleteDriverForm(call: ApplicationCall) {
        // todo
    }

    companion object {

        private const val COMPANION_ROLE = "COMPANION_ROLE"
        private const val DRIVER_ROLE = "DRIVER_ROLE"
    }
}