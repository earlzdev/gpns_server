package ru.earl.feature.chat.groups

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.earl.feature.chat.NewCommonGroupLastMsg
import ru.earl.feature.chat.WebSocketConnectionHandler
import ru.earl.feature.chat.OnlineController
import ru.earl.models.commonGroup.Groups
import ru.earl.models.commonGroupMessages.CommonGroupMessages
import ru.earl.models.commonGroupMessages.CommonGroupMessagesDto
import ru.earl.models.userDetails.UserDetails

interface GroupsMessagingService {

    suspend fun fetchAllMessagesInCommonGroup(call: ApplicationCall)

    suspend fun sendMessageInCommonGroup(call: ApplicationCall)
}

class GroupsMessagingServiceImpl() : GroupsMessagingService, OnlineController() {

    override suspend fun fetchAllMessagesInCommonGroup(call: ApplicationCall) {
        authenticate(call)?.apply {
            call.respond(HttpStatusCode.OK, CommonGroupMessages.fetchAllMessages())
        }
    }

    override suspend fun sendMessageInCommonGroup(call: ApplicationCall) {
        authenticate(call)?.apply {
            val msgReceived = call.receive<GroupsNewMessageReceive>()
            val authorAvatar = UserDetails.fetchUserDetailsById(this)?.image ?: ""
            CommonGroupMessages.insertNewMessage(
                CommonGroupMessagesDto(
                    msgReceived.messageId,
                    msgReceived.authorName,
                    msgReceived.timestamp,
                    msgReceived.messageText
                )
            )
            Groups.updateLastMsg(NewCommonGroupLastMsg(
                msgReceived.authorName,
                authorAvatar,
                msgReceived.messageText,
                msgReceived.timestamp
            ))
            val updatable = LastMessageForUpdateInGroup(
                "0",
                msgReceived.authorName,
                authorAvatar,
                msgReceived.messageText,
                msgReceived.timestamp
            )
            val messageEntity = CommonGroupMessagesDto(
                    "",
                    msgReceived.authorName,
                    msgReceived.timestamp,
                    msgReceived.messageText
                )
            CommonGroupMessages.insertNewMessage(messageEntity)
            WebSocketConnectionHandler.roomObserversClients.values.forEach {
                it.socket.send(Frame.Text(Json.encodeToString(updatable)))
            }
            WebSocketConnectionHandler.groupMessagingClients.values.filter { it.groupId == "0" }.forEach {
                it.socket.send(Frame.Text(Json.encodeToString(messageEntity)))
            }
        }
    }
}