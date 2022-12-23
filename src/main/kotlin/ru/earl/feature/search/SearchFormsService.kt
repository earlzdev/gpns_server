package ru.earl.feature.search

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
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
        TODO("Not yet implemented")
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
}