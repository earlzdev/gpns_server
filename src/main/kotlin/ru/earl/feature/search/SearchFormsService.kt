package ru.earl.feature.search

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.earl.feature.chat.OnlineController
import ru.earl.feature.chat.SocketActions
import ru.earl.feature.chat.SocketModelDto
import ru.earl.feature.chat.WebSocketConnectionHandler
import ru.earl.feature.chat.groups.GroupIdReceive
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
import ru.earl.models.userDetails.UserDetailsDto
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
    suspend fun fetchAllCompanionsInGroup(call: ApplicationCall)
    suspend fun removeCompanionFromGroup(call: ApplicationCall)
    suspend fun leaveFromCompanionGroup(call: ApplicationCall)
    suspend fun markTripNotificationAsNotActive(call: ApplicationCall)
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
                            )),
                            FORM_IS_ACTIVE
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
                            )),
                            FORM_IS_ACTIVE
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
                        receive.comment,
                        receive.active
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
                        )),
                        receive.active
                    ))
                )
                WebSocketConnectionHandler.searchingSocketClients.values.filter { it.username != receive.username }.forEach {
                    it.socket.send(Frame.Text(Json.encodeToString(socketDtoModel)))
                }
                UserDetails.setUserTripRoleCompanion(this)
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
                    receive.driverComment,
                    receive.active
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
                        )),
                        receive.active
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
                UserDetails.setUserTripRoleDriver(this)
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
                UserDetails.removeUserTripRole(this)
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
                        DELETED_DRIVER_FORM,
                        dateText,
                        ACTIVE
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
                UserDetails.removeUserTripRole(this)
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
                    notificationReceive.type,
                    dateText,
                    ACTIVE
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
                    notificationReceive.type,
                    dateText,
                    ACTIVE
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
            val companionId = this
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
                AGREED,
                dateText,
                ACTIVE
            )
            val group = Groups.fetchGroupByGroupId(driverId)
            val groupDto = SocketModelDto(
                SocketActions.NEW_GROUP.toString(),
                Json.encodeToString(group)
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
            WebSocketConnectionHandler.roomObserversClients.values.find { it.userId == companionId }?.apply {
                this.socket.send(Frame.Text(Json.encodeToString(groupDto)))
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
                AGREED,
                dateText,
                ACTIVE
            )
            val group = Groups.fetchGroupByGroupId(this)
            val groupDto = SocketModelDto(
                SocketActions.NEW_GROUP.toString(),
                Json.encodeToString(group)
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
            WebSocketConnectionHandler.roomObserversClients.values.find { it.userId == companionId }?.apply {
                this.socket.send(Frame.Text(Json.encodeToString(groupDto)))
            }
            call.respond(HttpStatusCode.OK)
        }
    }

    override suspend fun denyCompanionToRideTogether(call: ApplicationCall) {

    }

    override suspend fun fetchAllCompanionsInGroup(call: ApplicationCall) {
        authenticate(call)?.apply {
            try {
                val groupId = call.receive<GroupIdReceive>().groupId
                val userIdsList = GroupUsers.fetchUsersIdsListInGroup(groupId)
                val readyList = mutableListOf<UserDetailsDto>()
                for (i in userIdsList) {
                    readyList.add(UserDetails.fetchUserDetailsById(i)!!)
                }
                call.respond(HttpStatusCode.OK, readyList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun removeCompanionFromGroup(call: ApplicationCall) {
        authenticate(call)?.apply {
            try {
                val dto = call.receive<RemoveCompanionFromGroupDto>()
                val companionId = User.fetchUserByUsername(dto.username)?.userId ?: ""
                val userId = this
                val group = Groups.fetchGroupByGroupId(dto.groupId)
                val currentDate = Date()
                val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                val dateText = dateFormat.format(currentDate)
                val notification = TripNotificationsDto(
                    UUID.randomUUID().toString(),
                    User.fetchUserById(userId)?.username ?: "",
                    User.fetchUserById(companionId)?.username ?: "",
                    DRIVER_ROLE,
                    COMPANION_ROLE,
                    REMOVED_COMPANION_FROM_GROUP,
                    dateText,
                    ACTIVE
                )
                GroupUsers.removeUserFromGroup(companionId, dto.groupId)
                TripNotification.insertNewNotification(notification)
                WebSocketConnectionHandler.searchingSocketClients.values.find { it.userId == companionId }?.apply {
                    this.socket.send(Frame.Text(Json.encodeToString(
                        SocketModelDto(
                            SocketActions.NEW_NOTIFICATION.toString(),
                            Json.encodeToString(notification)
                        )
                    )))
                }
                WebSocketConnectionHandler.roomObserversClients.values.find { it.userId == companionId }?.apply {
                    this.socket.send(Frame.Text(Json.encodeToString(
                        SocketModelDto(
                            SocketActions.REMOVE_DELETED_GROUP.toString(),
                            Json.encodeToString(group)
                        )
                    )))
                }
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.Conflict)
            }
        }
    }

    override suspend fun leaveFromCompanionGroup(call: ApplicationCall) {
        authenticate(call)?.apply {
            val id = this
            val groupId = call.receive<GroupIdReceive>().groupId
            val groupAuthorName = User.fetchUserById(groupId)?.username ?: ""
            val groupUsers = GroupUsers.fetchUsersIdsListInGroup(groupId)
            val currentDate = Date()
            val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val dateText = dateFormat.format(currentDate)
            val notification = TripNotificationsDto(
                UUID.randomUUID().toString(),
                User.fetchUserById(id)?.username ?: "",
                groupAuthorName,
                COMPANION_ROLE,
                DRIVER_ROLE,
                COMPANION_LEAVED_GROUP,
                dateText,
                ACTIVE
            )
            val group = Groups.fetchGroupByGroupId(groupId)
            WebSocketConnectionHandler.roomObserversClients.values.find { it.userId == id }?.apply {
                this.socket.send(Frame.Text(Json.encodeToString(
                    SocketModelDto(
                        SocketActions.REMOVE_DELETED_GROUP.toString(),
                        Json.encodeToString(group)
                    )
                )))
            }
            WebSocketConnectionHandler.searchingSocketClients.values.filter { groupUsers.contains(it.userId) }.forEach {
                it.socket.send(Frame.Text(Json.encodeToString(
                    SocketModelDto(
                        SocketActions.NEW_NOTIFICATION.toString(),
                        Json.encodeToString(notification)
                    )
                )))
            }
            TripNotification.insertNewNotification(notification)
            GroupUsers.removeUserFromGroup(id, groupId)
        }
    }

    override suspend fun markTripNotificationAsNotActive(call: ApplicationCall) {
        authenticate(call)?.apply {
            val notificationId = call.receive<NotificationIdDto>().id
            TripNotification.markTripNotificationAsNotActive(notificationId)
        }
    }

    companion object {

        private const val COMPANION_ROLE = "COMPANION_ROLE"
        private const val DRIVER_ROLE = "DRIVER_ROLE"
        private const val FORM_IS_ACTIVE = 1
        private const val FORM_IN_NOT_ACTIVE = 0
        private const val INVITE = "INVITE"
        private const val DELETED_DRIVER_FORM = "DELETED_DRIVER_FORM"
        private const val AGREED = "AGREED"
        private const val COMPANION_LEAVED_GROUP = "COMPANION_LEAVED_GROUP"
        private const val REMOVED_COMPANION_FROM_GROUP = "REMOVED_COMPANION_FROM_GROUP"
        private const val ACTIVE = 1
    }
}

/*
notification isInvite
0 - отказ - не исп
1 - приглашение
2 - водитель удалил анкету
3 - согласие
4 - отказ - не исп
 */