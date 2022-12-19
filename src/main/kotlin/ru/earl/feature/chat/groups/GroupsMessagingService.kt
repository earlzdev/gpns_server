package ru.earl.feature.chat.groups

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.earl.feature.chat.*
import ru.earl.models.group.Groups
import ru.earl.models.groupMessages.GroupMessages
import ru.earl.models.groupMessages.GroupMessagesDto
import ru.earl.models.userDetails.UserDetails
import ru.earl.models.users.User
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

interface GroupsMessagingService {

    suspend fun fetchAllMessagesInGroup(call: ApplicationCall)

    suspend fun sendMessageInGroup(messageJson: String)

    suspend fun sendUpdateTypingMessageInGroup(call: ApplicationCall)
}

class GroupsMessagingServiceImpl() : GroupsMessagingService, OnlineController() {

    override suspend fun fetchAllMessagesInGroup(call: ApplicationCall) {
        authenticate(call)?.apply {
            val groupId = call.receive<GroupIdReceive>().groupId
            val messagesList = GroupMessages.fetchMessagesForGroup(groupId)
            val sortedList = messagesList.toMutableList().sortedBy {
                val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                sdf.parse(it.timestamp)
            }
            call.respond(HttpStatusCode.OK, sortedList)
        }
    }

    override suspend fun sendMessageInGroup(messageJson: String) {
        val msgReceived = Json.decodeFromString<GroupsNewMessageReceive>(messageJson)
        val currentDate = Date()
        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val dateText = dateFormat.format(currentDate)
        GroupMessages.insertNewMessage(
            GroupMessagesDto(
                msgReceived.groupId,
                msgReceived.messageId,
                msgReceived.authorName,
                msgReceived.authorImage,
                dateText,
                msgReceived.messageText,
                msgReceived.read
            )
        )
        Groups.updateLastMsg(GroupLastMessage(
            msgReceived.groupId,
            msgReceived.authorName,
            msgReceived.authorImage,
            msgReceived.messageText,
            dateText
        ))
        val updatable = LastMessageForUpdateInGroup(
            msgReceived.groupId,
            msgReceived.authorName,
            msgReceived.authorImage,
            msgReceived.messageText,
            dateText
        )
        val messageEntity = GroupMessageResponse(
            msgReceived.groupId,
            msgReceived.messageId,
            msgReceived.authorName,
            msgReceived.authorImage,
            dateText,
            msgReceived.messageText,
            MESSAGE_UNREAD_KEY
        )
        val messageDto = SocketModelDto(
            SocketActions.NEW_MESSAGE_IN_GROUP.toString(),
            Json.encodeToString(messageEntity)
        )
        val updatableMessageDto = SocketModelDto(
            SocketActions.NEW_UPDATABLE_MESSAGE_IN_GROUP.toString(),
            Json.encodeToString(updatable)
        )
        WebSocketConnectionHandler.roomObserversClients.values.forEach {
            it.socket.send(Frame.Text(Json.encodeToString(updatableMessageDto)))
        }
        WebSocketConnectionHandler.groupMessagingClients.values.filter { it.groupId == msgReceived.groupId }.forEach {
            it.socket.send(Frame.Text(Json.encodeToString(messageDto)))
        }
    }

    override suspend fun sendUpdateTypingMessageInGroup(call: ApplicationCall) {
        authenticate(call)?.apply {
            val request = call.receive<TypingStatusInGroupRequest>()
            WebSocketConnectionHandler.groupMessagingClients.values.filter { it.groupId == request.groupId }.forEach {
                val response = SocketModelDto(
                    SocketActions.UPDATE_TYPING_MESSAGE_STATUS_IN_GROUP.toString(),
                    Json.encodeToString(request)
                )
                it.socket.send(Frame.Text(Json.encodeToString(response)))
            }
        }
    }

    companion object {
        private const val COMMON_GROUP_ID = "common"
        private const val MESSAGE_UNREAD_KEY = 0
    }
}