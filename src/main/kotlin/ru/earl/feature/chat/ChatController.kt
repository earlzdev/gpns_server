package ru.earl.feature.chat

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.reflect.*
import io.ktor.websocket.*
import io.ktor.websocket.serialization.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.earl.models.roomOccupancy.RoomOccupancy
import ru.earl.models.rooms.Room
import ru.earl.models.rooms.RoomDto
import ru.earl.models.roomsMessages.RoomsMessages
import ru.earl.models.roomsMessages.RoomsMessagesDto
import ru.earl.models.roomsUsers.RoomsUsers
import ru.earl.models.userDetails.UserDetails
import ru.earl.models.users.User
import java.text.SimpleDateFormat

class ChatController {

    private val roomObserversClients = HashMap<String, ChatSocketMember>()
    private val messagingClients = HashMap<String, RoomMember>()

    suspend fun initChatSocket(call: ApplicationCall, socket: WebSocketSession) {
        val userId = authenticate(call)
        if (roomObserversClients.containsKey(userId)) {
            println("client $userId is already existed in rooms bservrers")
            call.respond(HttpStatusCode.BadRequest, "User is already connected")
        } else if (userId != null){
            println("client $userId connected to rooms observrrss")
            roomObserversClients[userId] = ChatSocketMember(
                userId,
                socket
            )
        } else {
            call.respond(HttpStatusCode.BadRequest, "No such userId")
        }
    }

    suspend fun initMessaging(call: ApplicationCall, socket: WebSocketSession) {
        val userId = authenticate(call)
        val roomId = call.parameters["roomId"] ?: ""
        val username = User.fetchUserById(userId!!)?.username ?: ""
        if (messagingClients.containsKey(username)) {
            println("client $username is already existed in messaging clients")
            call.respond(HttpStatusCode.BadRequest, "User is already connected")
        } else {
            println("client $username connected to chat")
            RoomOccupancy.insertUserIntoRoom(roomId)
            messagingClients[username] = RoomMember(
                username,
                socket,
                roomId
            )
        }
    }

    suspend fun parseAction(action: String, call: ApplicationCall) {
        val userId = authenticate(call)
        val parsedAction = Json.decodeFromString<ChatSocketActionReceiveRemote>(action)
        if (userId != null) {
            when(parsedAction.action) {
                ADD_ROOM_KEY -> addRoomToDb(userId, parsedAction.value)
//                JOIN_ROOM_KEY -> joinRoom(parsedAction.value, userId)
//                SEND_MESSAGE_KEY -> sendMessage(parsedAction.value)
//                LEAVE_ROOM_KEY -> leaveRoom(parsedAction.value)
            }
        }
    }

    suspend fun sendMessage(messageJson: String) {
        val message = Json.decodeFromString<MessageReceive>(messageJson)
        val userIds = RoomsUsers.fetchUsersIdsInRoom(message.roomId)
        val userNames = mutableListOf<String>()
        for (id in userIds) { userNames.add(User.fetchUserById(id)?.username ?: "") }
        val messageEntity = RoomsMessagesDto(
            message.messageId,
            message.roomId,
            message.authorId,
            message.timestamp,
            message.messageText,
            message.messageDate,
            message.read
        )
        val updatableMessage = LastMessageForUpdate(
            message.roomId,
            User.fetchUserById(message.authorId)?.username ?: "",
            UserDetails.fetchUserDetailsById(message.authorId)?.image ?: "",
            message.timestamp,
            message.messageText,
            MSG_UNREAD_KEY
        )
        RoomsMessages.insertMessageIntoDb(messageEntity)
        Room.updateLastMessage(message.roomId, message.messageText, User.fetchUserById(message.authorId)?.username ?: "")
        messagingClients.values.forEach { member ->
            if (member.roomId == message.roomId) {
                if (RoomOccupancy.checkRoomOccupancy(message.roomId) == 2) {
                    messageEntity.read = 1
                    RoomsMessages.markAsRead(message.roomId)
                    val encodedMessage = Json.encodeToString(messageEntity)
                    member.socket.send(Frame.Text(encodedMessage))
                } else {
                    messageEntity.read = 0
                    val encodedMessage = Json.encodeToString(messageEntity)
                    member.socket.send(Frame.Text(encodedMessage))
                }
            }
        }
        roomObserversClients.values.forEach { observer ->
            val observerUsername = User.fetchUserById(observer.userId)?.username
            if (messagingClients.containsKey(observerUsername)) {
                updatableMessage.read = MSG_READ_KEY
                val encodedMessageForUpdate = Json.encodeToString(updatableMessage)
                observer.socket.send(encodedMessageForUpdate)
            } else {
                updatableMessage.read = MSG_UNREAD_KEY
                val encodedMessageForUpdate = Json.encodeToString(updatableMessage)
                observer.socket.send(encodedMessageForUpdate)
            }
        }
    }

    suspend fun fetchUnreadMessagesInRoom(call: ApplicationCall) {
        val userId = authenticate(call)
    }

    suspend fun markMessagesAsRead(call: ApplicationCall) {
        authenticate(call)
        val roomId = call.receive<RoomTokenReceive>().roomId
        val unreadList = RoomsMessages.fetchUnreadMessagesForRoom(roomId)
        for (message in unreadList) {
            messagingClients.values.forEach {
                if (it.roomId == message.roomId) {
                    val jsonMessage = Json.encodeToString(MessageIdResponse(message.messageId))
                    it.socket.send(Frame.Text(jsonMessage))
                }
            }
        }
        RoomsMessages.markAsRead(roomId)
        call.respond(HttpStatusCode.OK)
    }

    suspend fun fetchAllMessages(call: ApplicationCall) {
        authenticate(call)
        val roomToken = call.receive<RoomTokenReceive>().roomId
        val messagesList = RoomsMessages.fetchMessagesByRoomToken(roomToken)
        val sortedList = messagesList.toMutableList().sortedBy {
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
            sdf.parse(it.messageDate)
        }
        call.respond(sortedList)
    }

    suspend fun fetchUsersListForUser(call: ApplicationCall) {
        val userId = authenticate(call)
        if (userId != null) {
            val usersList = UserDetails.fetchAllUsersListForUserById(userId)
            call.respond(HttpStatusCode.OK, usersList)
        } else {
            call.respond(HttpStatusCode.BadRequest, "Authenticate problem")
        }
    }

    private suspend fun addRoomToDb(userId: String, jsonAction: String) {
        val newRoomReceiveRemote = Json.decodeFromString<NewRoomReceiveRemote>(jsonAction)
        val newRoom = RoomDto(
            newRoomReceiveRemote.roomId,
            newRoomReceiveRemote.image,
            newRoomReceiveRemote.author,
            newRoomReceiveRemote.contact,
            newRoomReceiveRemote.lastMessage,
            newRoomReceiveRemote.lastMessageAuthor,
            DEFAULT_DELETABLE_ROOM_VALUE
        )
        val contactId = User.fetchUserByUsername(newRoomReceiveRemote.contact)?.userId
        RoomsUsers.insertUserForRoom(newRoomReceiveRemote.roomId, userId)
        RoomsUsers.insertUserForRoom(newRoomReceiveRemote.roomId, contactId!!)
        Room.insertRoom(newRoom)
        RoomOccupancy.initNewRoomOccupancy(newRoomReceiveRemote.roomId)
        sendNewRoomToContacts(userId, newRoom)
    }

    private suspend fun sendNewRoomToContacts(userId: String, newRoom: RoomDto) {
        val contactId = User.fetchUserByUsername(newRoom.contact_name)?.userId
        val authorRoomResponse = RoomResponse(
            newRoom.roomId,
            newRoom.image,
            title = newRoom.contact_name,
            newRoom.last_message,
            newRoom.last_message_author,
            newRoom.deletable.toBoolean()
        )
        val contactRoomResponse = RoomResponse(
            newRoom.roomId,
            newRoom.image,
            title = newRoom.author_name,
            newRoom.last_message,
            newRoom.last_message_author,
            newRoom.deletable.toBoolean()
        )
        try {
            val jsonAuthorRoom = Json.encodeToString(authorRoomResponse)
            val jsonContactRoom = Json.encodeToString(contactRoomResponse)
            roomObserversClients.values.forEach { member ->
                if (member.userId == userId) {
                    member.socket.send(Frame.Text(jsonAuthorRoom))
                } else if (member.userId == contactId) {
                    member.socket.send(Frame.Text(jsonContactRoom))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchUserInfo(call: ApplicationCall) {
        val userId = authenticate(call)
        val userInfo = UserDetails.fetchUserDetailsById(userId!!)
        call.respond(HttpStatusCode.OK, userInfo!!)
    }

    suspend fun fetchRoomsForUser(call: ApplicationCall) {
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
                        val roomResponse = RoomResponse(
                            room.roomId,
                            room.image,
                            room.author_name,
                            room.last_message,
                            room.last_message_author,
                            room.deletable.toBoolean()
                        )
                        readyRoomsList.add(roomResponse)
                    } else {
                        val image = UserDetails.fetchUserDetailsById(userId)?.image
                        val roomResponse = RoomResponse(
                            room?.roomId ?: "",
                            image ?: "",
                            room?.contact_name ?: "",
                            room?.last_message ?: "",
                            room?.last_message_author ?: "",
                            room?.deletable.toBoolean()
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

    suspend fun deleteRoom(call: ApplicationCall) {
        authenticate(call)
        val roomId = call.receive<RoomTokenReceive>().roomId
        Room.removeRoom(roomId)
        RoomsUsers.deleteUsersInRoom(roomId)
        RoomsMessages.deleteAllMessagesInRoom(roomId)
        RoomOccupancy.deleteRoomOccupancy(roomId)
        call.respond(HttpStatusCode.OK)
    }

    fun authenticate(call: ApplicationCall): String? {
        val principal = call.principal<JWTPrincipal>()
        return principal?.getClaim(USER_ID, String::class)
    }

    suspend fun tryChatDisconnect(userId: String) {
        roomObserversClients[userId]?.socket?.close()
        if (roomObserversClients.containsKey(userId)) {
            roomObserversClients.remove(userId)
            println("DISCONNECTED FROM ROOMS OBSERVERS $userId")
            println("observes -> $roomObserversClients")
        }
    }

    suspend fun tryMessagingDisconnect(username: String) {
        val user = messagingClients[username]
        messagingClients[username]?.socket?.close()
        if (messagingClients.containsKey(username)) {
            messagingClients.remove(username)
            RoomOccupancy.removeUserFromRoom(user?.roomId!!)
            println("DISCONNECTED FROM MESSAGING $username")
            println("observes -> $messagingClients")
        }
    }

    companion object {

        private const val ADD_ROOM_KEY = "addRoom"
        private const val USER_ID = "userId"
        private const val DEFAULT_DELETABLE_ROOM_VALUE = "true"
        private const val MSG_READ_KEY = 1
        private const val MSG_UNREAD_KEY = 0
    }
}

/*
юзер открывает приложение - записываем в базу время последней аутентификации
юзер закрывает приложение - записываем в базу время разрыва соединения

при выгрузке данных с базы другими юзерами, сравниваем время последней аутентификации и время последнего разрыва соединения
если первое больше, то юзер в сети, если нет, но показываем время выхода
 */

/*
юзер отсылает сообщение, которое по дефолту unread
на сервере происходит проверка, есть ли контакт, которому адресовано message, в messaging clients, и если есть, ему парситься новое прочитанное сообщение,
а если нет - отсылается unread
юзер получает unread сообщение, при получении которого обновляется счетчик для каждой комнаты
при подключении к комнате, счетчик обнуляется, recycler скроллится к первому непрочитанному сообщению
 */