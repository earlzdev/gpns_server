package ru.earl.feature.search

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.reflect.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.earl.feature.chat.OnlineController
import ru.earl.feature.chat.SocketActions
import ru.earl.feature.chat.SocketModelDto
import ru.earl.feature.chat.WebSocketConnectionHandler
import ru.earl.models.companions.CompanionDto
import ru.earl.models.companions.Companions
import ru.earl.models.drivers.DriverDto
import ru.earl.models.drivers.Drivers
import ru.earl.models.group.Groups
import ru.earl.models.group.GroupsDto
import ru.earl.models.groupUsers.GroupUsers
import ru.earl.models.tripNotifications.TripNotification
import ru.earl.models.tripNotifications.TripNotificationsDto
import ru.earl.models.userDetails.UserDetails
import ru.earl.models.users.User
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

interface SearchFormsService {

    suspend fun initSearchingWebSocket(call: ApplicationCall, socket: WebSocketSession)
    suspend fun closeSearchingWebSocket(call: ApplicationCall)
    suspend fun fetchAllForms(call: ApplicationCall)
    suspend fun insertNewCompanionForm(call: ApplicationCall)
    suspend fun insertNewDriverForm(call: ApplicationCall)
    suspend fun deleteCompanionForm(call: ApplicationCall)
    suspend fun deleteDriverForm(call: ApplicationCall)
    suspend fun inviteCompanion(call: ApplicationCall)
    suspend fun inviteDriver(call: ApplicationCall)
    suspend fun answerTripInvitation(call: ApplicationCall)
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
                val socketDtoModel = SocketModelDto(
                    SocketActions.NEW_SEARCHING_FORM.toString(),
                    Json.encodeToString(TripFormDto(
                        receive.username,
                        receive.userImage,
                        COMPANION_ROLE,
                        receive.from,
                        receive.to,
                        receive.schedule,
                        Json.encodeToString(CompanionFormDetails(
                            receive.actualTripTime,
                            receive.ableToPay,
                            receive.comment
                        ))
                    ))
                )
                WebSocketConnectionHandler.searchingSocketClients.values.forEach {
                    it.socket.send(Frame.Text(Json.encodeToString(socketDtoModel)))
                }
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
            val newGroupIdForDriver = UUID.randomUUID().toString()
            val driverId = User.fetchUserByUsername(receive.username)?.userId ?: ""
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
                Groups.insertNewGroup(GroupsDto(
                    newGroupIdForDriver,
                    "Мои попутчики",
                    "",
                    "Здесь пока пусто...",
                    "",
                    "",
                    "",
                    1,
                    0,
                    1
                ))
                GroupUsers.insertNewUserForGroup(driverId, newGroupIdForDriver)
                val socketDtoModel = SocketModelDto(
                    SocketActions.NEW_SEARCHING_FORM.toString(),
                    Json.encodeToString(TripFormDto(
                        receive.username,
                        receive.userImage,
                        DRIVER_ROLE,
                        receive.driveFrom,
                        receive.driveTo,
                        receive.schedule,
                        Json.encodeToString(DriverFormDetails(
                            receive.catchCompanionFrom,
                            receive.alsoCanDriveTo,
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
                    ))
                )
                WebSocketConnectionHandler.searchingSocketClients.values.forEach {
                    it.socket.send(Frame.Text(Json.encodeToString(socketDtoModel)))
                }
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.Conflict, e)
            }
        }
    }

    override suspend fun deleteCompanionForm(call: ApplicationCall) {
        authenticate(call)?.apply {
            try {
                val username = User.fetchUserById(this)?.username ?: ""
                Companions.deleteCompanionForm(username)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun deleteDriverForm(call: ApplicationCall) {
        authenticate(call)?.apply {
            try {
                val username = User.fetchUserById(this)?.username ?: ""
                Drivers.deleteDriverForm(username)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun inviteCompanion(call: ApplicationCall) {
        authenticate(call)?.apply {
            try {
                val notificationReceive = call.receive<TripNotificationsReceive>()
                val currentDate = Date()
                val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                val dateText = dateFormat.format(currentDate)
                TripNotification.insertNewNotification(
                    TripNotificationsDto(
                        notificationReceive.id,
                        notificationReceive.authorName,
                        notificationReceive.receiverName,
                        notificationReceive.authorTripRole,
                        notificationReceive.receiverTripRole,
                        notificationReceive.isInvite,
                        dateText,
                    )
                )
                // todo send notification by socket
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.Conflict)
            }
        }
    }

    override suspend fun inviteDriver(call: ApplicationCall) {
        authenticate(call)?.apply {
            try {
                val notificationReceive = call.receive<TripNotificationsReceive>()
                val currentDate = Date()
                val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                val dateText = dateFormat.format(currentDate)
                TripNotification.insertNewNotification(
                    TripNotificationsDto(
                        notificationReceive.id,
                        notificationReceive.authorName,
                        notificationReceive.receiverName,
                        notificationReceive.authorTripRole,
                        notificationReceive.receiverTripRole,
                        notificationReceive.isInvite,
                        dateText,
                    )
                )
                // todo send notification by socket
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.Conflict)
            }
        }
    }

    override suspend fun answerTripInvitation(call: ApplicationCall) {
        authenticate(call)?.apply {
            // todo
        }
    }

    override suspend fun initSearchingWebSocket(call: ApplicationCall, socket: WebSocketSession) {
        authenticate(call)?.apply {
            val username = UserDetails.fetchUserDetailsById(this)?.username
            if (WebSocketConnectionHandler.searchingSocketClients.containsKey(username)) {
                call.respond(HttpStatusCode.Conflict, "User is already exists in searching socket")
            } else if (username != null) {
                call.respond(HttpStatusCode.OK, "$username connected to searching")
                WebSocketConnectionHandler.searchingSocketClients[username] = SearchingMember(
                    this, username, socket
                )
            } else {
                call.respond(HttpStatusCode.BadRequest, "No such user")
            }
        }
    }

    override suspend fun closeSearchingWebSocket(call: ApplicationCall) {
        authenticate(call)?.apply {
            val username = UserDetails.fetchUserDetailsById(this)?.username ?: ""
            WebSocketConnectionHandler.searchingSocketClients[username]?.socket?.close()
            if (WebSocketConnectionHandler.searchingSocketClients.containsKey(username)) {
                WebSocketConnectionHandler.searchingSocketClients.remove(username)
                setUserOffline(this)
                println("DISCONNECTED FROM SEARCHING OBSERVERS $username")
                println("observes -> ${WebSocketConnectionHandler.roomObserversClients}")
            } else {
                println("no such name in searching observing")
                println("${WebSocketConnectionHandler.roomObserversClients.values}")
            }
        }
    }

    companion object {

        private const val COMPANION_ROLE = "COMPANION_ROLE"
        private const val DRIVER_ROLE = "DRIVER_ROLE"
    }
}