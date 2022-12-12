package ru.earl.feature.chat

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.earl.models.roomOccupancy.RoomOccupancy
import ru.earl.models.rooms.Room
import ru.earl.models.roomsMessages.RoomsMessages
import ru.earl.models.roomsMessages.RoomsMessagesDto
import ru.earl.models.roomsUsers.RoomsUsers
import ru.earl.models.userDetails.UserDetails
import ru.earl.models.users.User
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

interface MessagingService {

    suspend fun sendMessage(messageJson: String)
    suspend fun markMessagesAsRead(call: ApplicationCall)
    suspend fun sendTypingMessageRequest(call: ApplicationCall)
    suspend fun markAuthoredMessageAsRead(call: ApplicationCall)
    suspend fun sendUpdatableMessage(receivedMessage: MessageReceive)
    suspend fun fetchAllMessages(call: ApplicationCall)
}

class MessagingServiceImpl() : MessagingService, OnlineController() {
    override suspend fun sendMessage(messageJson: String) {
        val receivedMessage = Json.decodeFromString<MessageReceive>(messageJson)
        val userIds = RoomsUsers.fetchUsersIdsInRoom(receivedMessage.roomId)
        val userNames = mutableListOf<String>()
        val currentDate = Date()
        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val dateText = dateFormat.format(currentDate)
        for (id in userIds) { userNames.add(User.fetchUserById(id)?.username ?: "") }
        val messageEntity = RoomsMessagesDto(
            receivedMessage.messageId,
            receivedMessage.roomId,
            receivedMessage.authorId,
            receivedMessage.timestamp,
            receivedMessage.messageText,
            dateText,
            receivedMessage.read
        )
        RoomsMessages.insertMessageIntoDb(messageEntity)
        Room.updateLastMessage(receivedMessage.roomId, receivedMessage.messageText, User.fetchUserById(receivedMessage.authorId)?.username ?: "")
        WebSocketConnectionHandler.messagingClients.values.forEach { member ->
            if (member.roomId == receivedMessage.roomId) {
                if (RoomOccupancy.checkRoomOccupancy(receivedMessage.roomId) == 2) {
                    messageEntity.read = 1
                    RoomsMessages.markAsRead(receivedMessage.roomId)
                    val encodedMessage = Json.encodeToString(messageEntity)
                    member.socket.send(Frame.Text(encodedMessage))
                } else {
                    messageEntity.read = 0
                    val encodedMessage = Json.encodeToString(messageEntity)
                    member.socket.send(Frame.Text(encodedMessage))
                }
            }
        }
        sendUpdatableMessage(receivedMessage)
    }

    override suspend fun markMessagesAsRead(call: ApplicationCall) {
        val roomId = call.receive<RoomTokenReceive>().roomId
        val unreadList = RoomsMessages.fetchUnreadMessagesForRoom(roomId)
        for (message in unreadList) {
            WebSocketConnectionHandler.messagingClients.values.forEach {
                if (it.roomId == message.roomId) {
                    val jsonMessage = Json.encodeToString(MessageIdResponse(message.messageId))
                    it.socket.send(Frame.Text(jsonMessage))
                }
            }
        }
        Room.clearRoomUnreadMessagesCounter(roomId)
        RoomsMessages.markAsRead(roomId)
        call.respond(HttpStatusCode.OK)
    }

    override suspend fun markAuthoredMessageAsRead(call: ApplicationCall) {
        authenticate(call)
        val receive = call.receive<MarkAuthoredMessageAsReadRequest>()
        val response = RoomIdResponse(receive.roomId)
        val jsonResponse = Json.encodeToString(response)
        val authorId = User.fetchUserByUsername(receive.authorName)?.userId
        val author = WebSocketConnectionHandler.roomObserversClients.values.find { it.userId == authorId }
        author?.socket?.send(Frame.Text(jsonResponse))
        call.respond(HttpStatusCode.OK)
    }

    override suspend fun sendTypingMessageRequest(call: ApplicationCall) {
        authenticate(call)
        val response = call.receive<TypingMessageDto>()
        WebSocketConnectionHandler.messagingClients.values.find { it.roomId == response.roomId && it.username != response.username }.apply {
            this?.socket?.send(Frame.Text(Json.encodeToString(response)))
        }
        call.respond(HttpStatusCode.OK)
    }

    override suspend fun sendUpdatableMessage(receivedMessage: MessageReceive) {
        val updatableMessage = LastMessageForUpdate(
            receivedMessage.roomId,
            User.fetchUserById(receivedMessage.authorId)?.username ?: "",
            UserDetails.fetchUserDetailsById(receivedMessage.authorId)?.image ?: "",
            receivedMessage.timestamp,
            receivedMessage.messageText,
            MSG_UNREAD_KEY
        )
        Room.updateLastMessage(receivedMessage.roomId, receivedMessage.messageText, User.fetchUserById(receivedMessage.authorId)?.username ?: "")
        val authorId = receivedMessage.authorId
        val contactId = RoomsUsers.fetchUsersIdsInRoom(receivedMessage.roomId).find { it != authorId }
        val roomOccupancy = RoomOccupancy.checkRoomOccupancy(updatableMessage.roomId)
        if (roomOccupancy == 2) {
            updatableMessage.read = MSG_READ_KEY
            val encodedMessageForUpdate = Json.encodeToString(updatableMessage)
            WebSocketConnectionHandler.roomObserversClients.values.find { it.userId == authorId }?.socket?.send(Frame.Text(encodedMessageForUpdate))
            WebSocketConnectionHandler.roomObserversClients.values.find { it.userId == contactId }?.socket?.send(Frame.Text(encodedMessageForUpdate))
        } else if (roomOccupancy != 2) {
            updatableMessage.read = MSG_UNREAD_KEY
            Room.increaseRoomUnreadMessagesCount(receivedMessage.roomId)
            Room.updateLastMessageReadStateToUnread(receivedMessage.roomId)
            val encodedMessageForUpdate = Json.encodeToString(updatableMessage)
            WebSocketConnectionHandler.roomObserversClients.values.find { it.userId == authorId }?.socket?.send(Frame.Text(encodedMessageForUpdate))
            WebSocketConnectionHandler.roomObserversClients.values.find { it.userId == contactId }?.socket?.send(Frame.Text(encodedMessageForUpdate))
        }
    }

    override suspend fun fetchAllMessages(call: ApplicationCall) {
        val roomToken = call.receive<RoomTokenReceive>().roomId
        val messagesList = RoomsMessages.fetchMessagesByRoomToken(roomToken)
        val sortedList = messagesList.toMutableList().sortedBy {
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
            sdf.parse(it.messageDate)
        }
        call.respond(sortedList)
    }

    companion object {

        private const val MSG_READ_KEY = 1
        private const val MSG_UNREAD_KEY = 0
    }
}