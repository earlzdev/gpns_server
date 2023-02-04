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
import ru.earl.models.group_occupancy.GroupOccupancy
import ru.earl.models.userDetails.UserDetails
import ru.earl.models.users.User
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

interface GroupsMessagingService {

    suspend fun fetchAllMessagesInGroup(call: ApplicationCall)
    suspend fun sendMessageInGroup(messageJson: String)
    suspend fun sendUpdateTypingMessageInGroup(call: ApplicationCall)
    suspend fun markMessagesAsReadInGroup(call: ApplicationCall)
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
            dateText,
            msgReceived.read
        ))
        val unreadUpdatableMessage = LastMessageForUpdateInGroup(
            msgReceived.groupId,
            msgReceived.authorName,
            msgReceived.authorImage,
            msgReceived.messageText,
            dateText,
            MESSAGE_UNREAD_KEY
        )
        val readUpdatableMessage = LastMessageForUpdateInGroup(
            msgReceived.groupId,
            msgReceived.authorName,
            msgReceived.authorImage,
            msgReceived.messageText,
            dateText,
            MESSAGE_READ_KEY
        )
        val readMessageEntity = GroupMessageResponse(
            msgReceived.groupId,
            msgReceived.messageId,
            msgReceived.authorName,
            msgReceived.authorImage,
            dateText,
            msgReceived.messageText,
            MESSAGE_READ_KEY
        )
        val unreadMessageEntity = GroupMessageResponse(
            msgReceived.groupId,
            msgReceived.messageId,
            msgReceived.authorName,
            msgReceived.authorImage,
            dateText,
            msgReceived.messageText,
            MESSAGE_UNREAD_KEY
        )
        val readMessageDto = SocketModelDto(
            SocketActions.NEW_MESSAGE_IN_GROUP.toString(),
            Json.encodeToString(readMessageEntity)
        )
        val unreadMessageDto = SocketModelDto(
            SocketActions.NEW_MESSAGE_IN_GROUP.toString(),
            Json.encodeToString(unreadMessageEntity)
        )
        val unreadUpdatableMessageDto = SocketModelDto(
            SocketActions.NEW_UPDATABLE_MESSAGE_IN_GROUP.toString(),
            Json.encodeToString(unreadUpdatableMessage)
        )
        val readUpdatableMessageDto = SocketModelDto(
            SocketActions.NEW_UPDATABLE_MESSAGE_IN_GROUP.toString(),
            Json.encodeToString(readUpdatableMessage)
        )
        val inGroupUsers =
            WebSocketConnectionHandler.groupMessagingClients.values.filter { it.groupId == msgReceived.groupId }
        val inGroupUserUsernames = inGroupUsers.map { it.username }
        val notInGroupUsers =
            WebSocketConnectionHandler.roomObserversClients.values.filter { !inGroupUserUsernames.contains(it.provideUsername()) }
        val inGroup =
            WebSocketConnectionHandler.roomObserversClients.values.filter { inGroupUserUsernames.contains(it.provideUsername()) }
        if (GroupOccupancy.isSomebodyInGroup(msgReceived.groupId)) {
            inGroup.forEach {
                println("sent read in group")
                it.socket.send(Frame.Text(Json.encodeToString(readUpdatableMessageDto)))
            }
        } else {
            inGroup.forEach {
                println("sent unread in group")
                it.socket.send(Frame.Text(Json.encodeToString(unreadUpdatableMessageDto)))
            }
        }
        notInGroupUsers.forEach {
            println("sent unread not in group users")
            it.socket.send(Frame.Text(Json.encodeToString(unreadUpdatableMessageDto)))
        }
        WebSocketConnectionHandler.groupMessagingClients.values.filter { it.groupId == msgReceived.groupId }.forEach {
            if (!GroupOccupancy.isSomebodyInGroup(msgReceived.groupId)) {
                it.socket.send(Frame.Text(Json.encodeToString(unreadMessageDto)))
            } else {
                it.socket.send(Frame.Text(Json.encodeToString(readMessageDto)))
            }
        }
        Groups.increaseGroupMessagesCounter(msgReceived.groupId)
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
            call.respond(HttpStatusCode.OK)
        }
    }

    override suspend fun markMessagesAsReadInGroup(call: ApplicationCall) {
        val groupId = call.receive<GroupIdReceive>().groupId
        val lastMessageAuthorUsername = Groups.fetchGroupByGroupId(groupId)?.lastMsgAuthor ?: ""
        val lastMessageAuthorId = User.fetchUserByUsername(lastMessageAuthorUsername)?.userId
        GroupMessages.markMessagesAsReadInGroup(groupId)
        Groups.setLastMessageAsRead(groupId)
        val markMessagesAsReadResponse = SocketModelDto(
            SocketActions.MARK_MESSAGES_AS_READ_IN_GROUP.toString(),
            Json.encodeToString(MarkMessagesAsReadInGroupResponse(groupId))
        )
        val markAuthoredMessagesAsReadInGroup = SocketModelDto(
            SocketActions.MARK_AUTHORED_MESSAGES_AS_READ_IN_GROUP.toString(),
            Json.encodeToString(MarkAuthoredMessagesAsReadInGroup(groupId))
        )
        WebSocketConnectionHandler.groupMessagingClients.values.filter { it.groupId == groupId }.forEach {
            it.socket.send(Frame.Text(Json.encodeToString(markMessagesAsReadResponse)))
        }
        WebSocketConnectionHandler.roomObserversClients.values.find { it.userId == lastMessageAuthorId }
            ?.socket?.send(Frame.Text(Json.encodeToString(markAuthoredMessagesAsReadInGroup)))
        call.respond(HttpStatusCode.OK)
    }

    companion object {
        private const val MESSAGE_UNREAD_KEY = 0
        private const val MESSAGE_READ_KEY = 1
    }
}