package ru.earl.feature.chat

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.earl.models.rooms.Room
import ru.earl.models.roomsUsers.RoomsUsers
import ru.earl.models.userDetails.UserDetails
import ru.earl.models.users.User
import ru.earl.models.usersOnline.UsersOnline
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


open class OnlineController {

    protected suspend fun authenticate(call: ApplicationCall): String? {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.getClaim(USER_ID, String::class)
        val checkOnline = UsersOnline.checkOnline(userId ?: "")
        if (!checkOnline) {
            UsersOnline.setUserOnline(userId ?: "")
            UserDetails.setUserOnline(userId ?: "")
            setUserOnline(userId ?: "")
        }
        return userId
    }

    private suspend fun setUserOnline(userId: String) {
        val currentDate = Date()
        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val dateText = dateFormat.format(currentDate)
        val username = User.fetchUserById(userId)?.username
        UsersOnline.setUserOnline(userId)
        UserDetails.setUserOnline(userId)
        Room.setUserOnline(username!!)
        // send by socket
        val rooms = RoomsUsers.fetchRoomsIdsForUser(userId)
        for (room in rooms) {
            WebSocketConnectionHandler.messagingClients.values.filter { it.roomId == room }.forEach {
                val response = Json.encodeToString(SetUserOnlineInMessaging(USER_ONLINE, it.username))
                val responseDto = SocketModelDto(
                    SocketActions.UPDATE_USER_ONLINE_STATUS_IN_CHAT.toString(),
                    response
                )
                it.socket.send(Frame.Text(Json.encodeToString(responseDto)))
            }
        }
        val roomsIdsListWithUser = Room.fetchAllRoomsIdsWithUser(username)
        for (roomId in roomsIdsListWithUser) {
            RoomsUsers.fetchUsersIdsInRoom(roomId).forEach {
                if (WebSocketConnectionHandler.roomObserversClients.containsKey(it)) {
                    val response = Json.encodeToString(SetUserOnlineInRoom(USER_ONLINE, username, roomId, dateText))
                    val responseDto = SocketModelDto(
                        SocketActions.UPDATE_USER_ONLINE_IN_ROOM.toString(),
                        response
                    )
                    WebSocketConnectionHandler.roomObserversClients[it]?.socket?.send(Frame.Text(Json.encodeToString(responseDto)))
                }
            }
        }
    }

    protected suspend fun setUserOffline(userId: String) {
        val currentDate = Date()
        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val dateText = dateFormat.format(currentDate)
        val username = User.fetchUserById(userId)?.username
        UsersOnline.setUserOffline(userId, dateText)
        UserDetails.setUserOffline(userId, dateText)
        Room.setUserOffline(username!!)
        val rooms = RoomsUsers.fetchRoomsIdsForUser(userId)
        for (room in rooms) {
            WebSocketConnectionHandler.messagingClients.values.filter { it.roomId == room }.forEach {
                val response = Json.encodeToString(SetUserOnlineInMessaging(USER_OFFLINE, dateText))
                val responseDto = SocketModelDto(
                    SocketActions.UPDATE_USER_ONLINE_STATUS_IN_CHAT.toString(),
                    response
                )
                it.socket.send(Frame.Text(Json.encodeToString(responseDto)))
            }
        }
        val roomsIdsListWithUser = Room.fetchAllRoomsIdsWithUser(username)
        for (roomId in roomsIdsListWithUser) {
            Room.updateUserLastAuth(roomId, dateText)
            RoomsUsers.fetchUsersIdsInRoom(roomId).forEach {
                if (WebSocketConnectionHandler.roomObserversClients.containsKey(it)) {
                    val response = Json.encodeToString(SetUserOnlineInRoom(USER_OFFLINE, username, roomId, dateText))
                    val responseDto = SocketModelDto(
                        SocketActions.UPDATE_USER_ONLINE_IN_ROOM.toString(),
                        response
                    )
                    WebSocketConnectionHandler.roomObserversClients[it]?.socket?.send(Frame.Text(Json.encodeToString(responseDto)))
                }
            }
        }
    }

    companion object {

        private const val USER_ID = "userId"
        private const val USER_OFFLINE = 0
        private const val USER_ONLINE = 1
    }
}