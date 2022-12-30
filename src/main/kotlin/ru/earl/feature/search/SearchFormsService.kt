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
import ru.earl.models.groupMessages.GroupMessages
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
    suspend fun fetchAllNotificationsForUser(call: ApplicationCall)
    suspend fun fetchCompanionForm(call: ApplicationCall)
    suspend fun fetchDriverForm(call: ApplicationCall)
    suspend fun acceptDriverToRideTogether(call: ApplicationCall)
    suspend fun denyDriverToRideTogether(call: ApplicationCall)
    suspend fun acceptCompanionToRideTogether(call: ApplicationCall)
    suspend fun denyCompanionToRideTogether(call: ApplicationCall)
}

class SearchFormsServiceImpl : SearchFormsService, OnlineController() {

    override suspend fun fetchAllForms(call: ApplicationCall) {
        authenticate(call)?.apply {
            try {
                val username = UserDetails.fetchUserDetailsById(this)?.username ?: ""
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
                readyFormsList.removeIf { it.username == username }
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
                WebSocketConnectionHandler.searchingSocketClients.values.filter { it.username != receive.username }.forEach {
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
            val userId = User.fetchUserById(this)?.userId ?: ""
            val driverId = User.fetchUserByUsername(receive.username)?.userId ?: ""
            try {
                val group = GroupsDto(
                    userId,
                    "Мои попутчики",
                    "",
                    "Здесь пока пусто...",
                    "",
                    "",
                    "",
                    1,
                    0,
                    1
                )
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
                Groups.insertNewGroup(group)
                GroupUsers.insertNewUserIntoGroup(driverId, userId)
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
                WebSocketConnectionHandler.searchingSocketClients.values.filter { it.username != receive.username }.forEach {
                    it.socket.send(Frame.Text(Json.encodeToString(socketDtoModel)))
                }
                WebSocketConnectionHandler.roomObserversClients.values.filter { it.userId == this }.forEach {
                    it.socket.send(Frame.Text(
                        Json.encodeToString(
                            SocketModelDto(
                                SocketActions.NEW_GROUP.toString(),
                                Json.encodeToString(group)
                            )
                        )
                    ))
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
                val response = SocketModelDto(
                    SocketActions.REMOVE_DELETED_FORM.toString(),
                    Json.encodeToString(DeletedSearchingFormDto(username))
                )
                WebSocketConnectionHandler.searchingSocketClients.values.filter { it.username != username }.forEach {
                    it.socket.send(Frame.Text(Json.encodeToString(response)))
                }
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun deleteDriverForm(call: ApplicationCall) {
        authenticate(call)?.apply {
            try {
                val currentDate = Date()
                val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                val dateText = dateFormat.format(currentDate)
                val username = User.fetchUserById(this)?.username ?: ""
                Drivers.deleteDriverForm(username)
                val response = SocketModelDto(
                    SocketActions.REMOVE_DELETED_FORM.toString(),
                    Json.encodeToString(DeletedSearchingFormDto(username))
                )
                val group = Groups.fetchGroupByGroupId(this)
                val groupUsersList = GroupUsers.fetchUsersIdsListInGroup(this)
                WebSocketConnectionHandler.roomObserversClients.values.filter { groupUsersList.contains(it.userId) }.forEach {
                    it.socket.send(Frame.Text(
                        Json.encodeToString(
                            SocketModelDto(
                                SocketActions.REMOVE_DELETED_GROUP.toString(),
                                Json.encodeToString(group)
                            )
                        )
                    ))
                }
                for (i in groupUsersList.filter { it != this }) {
                    val receiverUsername = UserDetails.fetchUserDetailsById(i)?.username ?: ""
                    val notification = TripNotificationsDto(
                        UUID.randomUUID().toString(),
                        username,
                        receiverUsername,
                        DRIVER_ROLE,
                        COMPANION_ROLE,
                        2,
                        dateText
                    )
                    TripNotification.insertNewNotification(notification)
                    WebSocketConnectionHandler.searchingSocketClients.values.forEach {
                        if (it.username == receiverUsername) {
                            val model = SocketModelDto(
                                SocketActions.NEW_NOTIFICATION.toString(),
                                Json.encodeToString(notification)
                            )
                            it.socket.send(Frame.Text(Json.encodeToString(model)))
                        }
                    }
                }
                Groups.deleteGroup(this)
                GroupUsers.deleteAllUserFromGroup(this)
                GroupMessages.deleteAllGroupMessages(this)
                WebSocketConnectionHandler.searchingSocketClients.values.filter { it.username != username }.forEach {
                    it.socket.send(Frame.Text(Json.encodeToString(response)))
                }
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun inviteCompanion(call: ApplicationCall) {
        authenticate(call)?.apply {
            println("NEW INVITE")
            try {
                val notificationReceive = call.receive<TripNotificationsReceive>()
                val currentDate = Date()
                val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                val dateText = dateFormat.format(currentDate)
                val dto = TripNotificationsDto(
                    notificationReceive.id,
                    notificationReceive.authorName,
                    notificationReceive.receiverName,
                    notificationReceive.authorTripRole,
                    notificationReceive.receiverTripRole,
                    notificationReceive.isInvite,
                    dateText,
                )
                TripNotification.insertNewNotification(dto)
                val socketClient = WebSocketConnectionHandler.searchingSocketClients.values
                    .find { it.username == notificationReceive.receiverName }
                val socketModelDto = SocketModelDto(
                    SocketActions.NEW_NOTIFICATION.toString(),
                    Json.encodeToString(dto)
                )
                socketClient?.socket?.send(Frame.Text(Json.encodeToString(socketModelDto)))
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
                val dto = TripNotificationsDto(
                    notificationReceive.id,
                    notificationReceive.authorName,
                    notificationReceive.receiverName,
                    notificationReceive.authorTripRole,
                    notificationReceive.receiverTripRole,
                    notificationReceive.isInvite,
                    dateText,
                )
                TripNotification.insertNewNotification(dto)
                TripNotification.insertNewNotification(dto)
                val socketClient = WebSocketConnectionHandler.searchingSocketClients.values
                    .find { it.username == notificationReceive.receiverName }
                val socketModelDto = SocketModelDto(
                    SocketActions.NEW_NOTIFICATION.toString(),
                    Json.encodeToString(dto)
                )
                socketClient?.socket?.send(Frame.Text(Json.encodeToString(socketModelDto)))
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.Conflict)
            }
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

    override suspend fun fetchAllNotificationsForUser(call: ApplicationCall) {
        authenticate(call)?.apply {
            val username = User.fetchUserById(this)?.username ?: ""
            val list = TripNotification.fetchAllTripNotificationsForUser(username)
            val sortedList = list.toMutableList().sortedBy {
                val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                sdf.parse(it.timestamp)
            }
            call.respond(HttpStatusCode.OK, sortedList)
        }
    }

    override suspend fun fetchCompanionForm(call: ApplicationCall) {
        authenticate(call)?.apply {
            try {
                val name = call.receive<UserNameDto>().name
                println("name -> $name")
                val form = Companions.fetchCompanionForm(name)
                if (form != null) {
                    call.respond(HttpStatusCode.OK, form)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.Conflict)
            }
        }
    }

    override suspend fun fetchDriverForm(call: ApplicationCall) {
        authenticate(call)?.apply {
            try {
                val name = call.receive<UserNameDto>().name
                println("name -> $name")
                val form = Drivers.fetchDriverForm(name)
                if (form != null) {
                    call.respond(HttpStatusCode.OK, form)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.Conflict)
            }
        }
    }

    override suspend fun acceptDriverToRideTogether(call: ApplicationCall) {
        authenticate(call)?.apply {
            val driverName = call.receive<UserNameDto>().name
            val driverId = User.fetchUserByUsername(driverName)?.userId ?: ""
            val currentDate = Date()
            val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val dateText = dateFormat.format(currentDate)
            GroupUsers.insertNewUserIntoGroup(driverId, this)
            val notification = TripNotificationsDto(
                UUID.randomUUID().toString(),
                UserDetails.fetchUserDetailsById(this)?.username ?: "",
                driverName,
                COMPANION_ROLE,
                DRIVER_ROLE,
                3,
                dateText
            )
            TripNotification.insertNewNotification(notification)
            WebSocketConnectionHandler.searchingSocketClients.values.find { it.username == driverName }?.apply {
                this.socket.send(Frame.Text(
                    Json.encodeToString(
                        SocketModelDto(
                            SocketActions.NEW_NOTIFICATION.toString(),
                            Json.encodeToString(notification)
                        )
                    )
                ))
            }
            call.respond(HttpStatusCode.OK)
        }
    }

    override suspend fun denyDriverToRideTogether(call: ApplicationCall) {

    }

    override suspend fun acceptCompanionToRideTogether(call: ApplicationCall) {
        authenticate(call)?.apply {
            val companionName = call.receive<UserNameDto>().name
            val companionId = User.fetchUserByUsername(companionName)?.userId ?: ""
            val currentDate = Date()
            val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val dateText = dateFormat.format(currentDate)
            GroupUsers.insertNewUserIntoGroup(this, companionId)
            val notification = TripNotificationsDto(
                UUID.randomUUID().toString(),
                UserDetails.fetchUserDetailsById(this)?.username ?: "",
                companionName,
                DRIVER_ROLE,
                COMPANION_ROLE,
                3,
                dateText
            )
            TripNotification.insertNewNotification(notification)
            WebSocketConnectionHandler.searchingSocketClients.values.find { it.username == companionName }?.apply {
                this.socket.send(Frame.Text(
                    Json.encodeToString(
                        SocketModelDto(
                            SocketActions.NEW_NOTIFICATION.toString(),
                            Json.encodeToString(notification)
                        )
                    )
                ))
            }
            call.respond(HttpStatusCode.OK)
        }
    }

    override suspend fun denyCompanionToRideTogether(call: ApplicationCall) {

    }

    companion object {

        private const val COMPANION_ROLE = "COMPANION_ROLE"
        private const val DRIVER_ROLE = "DRIVER_ROLE"
    }
}