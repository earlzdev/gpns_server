package ru.earl.feature.chat.rooms

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.earl.feature.chat.*
import ru.earl.models.roomOccupancy.RoomOccupancy
import ru.earl.models.rooms.Room
import ru.earl.models.rooms.RoomDto
import ru.earl.models.roomsMessages.RoomsMessages
import ru.earl.models.roomsUsers.RoomsUsers
import ru.earl.models.userDetails.UserDetails
import ru.earl.models.users.User

interface RoomsService {

    suspend fun fetchRoomsForUser(call: ApplicationCall)
    suspend fun deleteRoom(call: ApplicationCall)
    suspend fun updateLastMessageReadState(call: ApplicationCall)
    suspend fun markAuthoredMessageAsReadInRoom(call: ApplicationCall)
    suspend fun addNewRoomToDb(call: ApplicationCall)
    suspend fun sendNewRoomToContacts(userId: String, newRoom: RoomDto)

}

class RoomsServiceImpl() : RoomsService, OnlineController() {
    override suspend fun fetchRoomsForUser(call: ApplicationCall) {
        val userId = authenticate(call)
        if (userId != null) {
            val roomsIdsList = RoomsUsers.fetchRoomsIdsForUser(userId)
            val readyRoomsList = mutableListOf<RoomResponse>()
            val username = User.fetchUserById(userId)?.username
            if (roomsIdsList.isEmpty()) {
                call.respond(HttpStatusCode.OK, "No rooms")
            } else {
                for (i in roomsIdsList.indices) {
                    val room = Room.fetchRoomByRoomId(roomsIdsList[i])
                    if (room?.contact_name == username && room != null) {
                        val contactName = room.author_name
                        val contactId = User.fetchUserByUsername(contactName)?.userId
                        val isOnline = UserDetails.checkUserOnline(contactId!!)
                        val roomResponse = RoomResponse(
                            room.roomId,
                            room.image,
                            room.author_name,
                            room.last_message,
                            room.last_message_author,
                            room.deletable.toBoolean(),
                            room.unreadMsgCount,
                            room.isLastMsgRead,
                            isOnline,
                            room.contactLastAuth
                        )
                        readyRoomsList.add(roomResponse)
                    } else {
                        val image = UserDetails.fetchUserDetailsById(userId)?.image
                        val contactName = room?.contact_name
                        val contactId = User.fetchUserByUsername(contactName!!)?.userId
                        val isOnline = UserDetails.checkUserOnline(contactId!!)
                        val roomResponse = RoomResponse(
                            room.roomId ,
                            image ?: "",
                            room.contact_name ,
                            room.last_message,
                            room.last_message_author,
                            room.deletable.toBoolean(),
                            room.unreadMsgCount,
                            room.isLastMsgRead,
                            isOnline,
                            room.contactLastAuth
                        )
                        readyRoomsList.add(roomResponse)
                    }
                    println("ready rooms list $readyRoomsList")
                }
                call.respond(HttpStatusCode.OK, readyRoomsList)
            }
        } else {
            call.respond(HttpStatusCode.BadRequest, "Authenticate problem")
        }
    }

    override suspend fun deleteRoom(call: ApplicationCall) {
        val userId = authenticate(call)
        val roomId = call.receive<RoomTokenReceive>().roomId
        val contactId = RoomsUsers.fetchUsersIdsInRoom(roomId).find { it != userId }
        val deletedRoom = Room.fetchRoomByRoomId(roomId)
        val response = RoomResponse(
            deletedRoom?.roomId ?: "",
            deletedRoom?.image ?: "",
            deletedRoom?.contact_name ?: "",
            deletedRoom?.last_message ?: "",
            deletedRoom?.last_message_author ?: "",
            deletedRoom?.deletable.toBoolean(),
            deletedRoom?.unreadMsgCount ?: 0,
            deletedRoom?.isLastMsgRead ?: 0,
            deletedRoom?.contactOnline ?: 0,
            deletedRoom?.contactLastAuth ?: ""
        )
        val jsonResponse = Json.encodeToString(response)
        val responseDto = SocketModelDto(
            SocketActions.REMOVE_DELETED_BY_ANOTHER_USER_ROOM.toString(),
            jsonResponse
        )
        val responseJson = Json.encodeToString(responseDto)
        WebSocketConnectionHandler.roomObserversClients.values.find { it.userId == userId }?.socket?.send(Frame.Text(responseJson))
        WebSocketConnectionHandler.roomObserversClients.values.find { it.userId == contactId }?.socket?.send(Frame.Text(responseJson))
        Room.removeRoom(roomId)
        RoomsUsers.deleteUsersInRoom(roomId)
        RoomsMessages.deleteAllMessagesInRoom(roomId)
        RoomOccupancy.deleteRoomOccupancy(roomId)
        call.respond(HttpStatusCode.OK)
    }

    override suspend fun updateLastMessageReadState(call: ApplicationCall) {
        val id = call.receive<RoomTokenReceive>().roomId
        Room.updateLastMessageReadStateToRead(id)
        call.respond(HttpStatusCode.OK)
    }

    override suspend fun markAuthoredMessageAsReadInRoom(call: ApplicationCall) {
        authenticate(call)
        val receive = call.receive<MarkAuthoredMessageAsReadRequest>()
        val response = RoomIdResponse(receive.roomId)
        val jsonResponse = Json.encodeToString(response)
        val authorId = User.fetchUserByUsername(receive.authorName)?.userId
        val author = WebSocketConnectionHandler.roomObserversClients.values.find { it.userId == authorId }
        val dtoModel = SocketModelDto(
            SocketActions.UPDATE_LAST_MESSAGE_READ_STATE.toString(),
            jsonResponse
        )
        author?.socket?.send(Frame.Text(Json.encodeToString(dtoModel)))
        call.respond(HttpStatusCode.OK)
    }

    override suspend fun addNewRoomToDb(call: ApplicationCall) {
        val userId = authenticate(call) ?: ""
        val newRoomReceiveRemote = call.receive<NewRoomReceiveRemote>()
        val newRoom = RoomDto(
            newRoomReceiveRemote.roomId,
            newRoomReceiveRemote.image,
            newRoomReceiveRemote.author,
            newRoomReceiveRemote.contact,
            newRoomReceiveRemote.lastMessage,
            newRoomReceiveRemote.lastMessageAuthor,
            DEFAULT_DELETABLE_ROOM_VALUE,
            FIRST_UNREAD_MSG_IN_ROOM,
            0,
            newRoomReceiveRemote.contactIsOnline,
            newRoomReceiveRemote.contactLastAuth
        )
        val contactId = User.fetchUserByUsername(newRoomReceiveRemote.contact)?.userId
        RoomOccupancy.initNewRoomOccupancy(newRoomReceiveRemote.roomId)
        RoomsUsers.insertUserForRoom(newRoomReceiveRemote.roomId, userId)
        RoomsUsers.insertUserForRoom(newRoomReceiveRemote.roomId, contactId!!)
        Room.insertRoom(newRoom)
        sendNewRoomToContacts(userId, newRoom)
        call.respond(HttpStatusCode.OK)
    }

    override suspend fun sendNewRoomToContacts(userId: String, newRoom: RoomDto) {
        val contactId = User.fetchUserByUsername(newRoom.contact_name)?.userId
        val authorRoomResponse = RoomResponse(
            newRoom.roomId,
            newRoom.image,
            title = newRoom.contact_name,
            newRoom.last_message,
            newRoom.last_message_author,
            newRoom.deletable.toBoolean(),
            FIRST_UNREAD_MSG_IN_ROOM,
            MSG_UNREAD_KEY,
            newRoom.contactOnline,
            newRoom.contactLastAuth
        )
        val contactRoomResponse = RoomResponse(
            newRoom.roomId,
            newRoom.image,
            title = newRoom.author_name,
            newRoom.last_message,
            newRoom.last_message_author,
            newRoom.deletable.toBoolean(),
            FIRST_UNREAD_MSG_IN_ROOM,
            MSG_UNREAD_KEY,
            newRoom.contactOnline,
            newRoom.contactLastAuth
        )
        try {
            val jsonAuthorRoom = Json.encodeToString(authorRoomResponse)
            val authorRoomDto = SocketModelDto(
                SocketActions.NEW_ROOM.toString(),
                jsonAuthorRoom
            )
            val jsonContactRoom = Json.encodeToString(contactRoomResponse)
            val contactRoomDto = SocketModelDto(
                SocketActions.NEW_ROOM.toString(),
                jsonContactRoom
            )
            WebSocketConnectionHandler.roomObserversClients.values.forEach { member ->
                if (member.userId == userId) {
                    member.socket.send(Frame.Text(Json.encodeToString(authorRoomDto)))
                } else if (member.userId == contactId) {
                    member.socket.send(Frame.Text(Json.encodeToString(contactRoomDto)))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {

        private const val DEFAULT_DELETABLE_ROOM_VALUE = "true"
        private const val MSG_UNREAD_KEY = 0
        private const val FIRST_UNREAD_MSG_IN_ROOM = 0
    }
}