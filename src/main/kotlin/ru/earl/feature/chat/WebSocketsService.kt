package ru.earl.feature.chat

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.websocket.*
import ru.earl.feature.chat.groups.GroupMember
import ru.earl.feature.chat.rooms.ChatSocketMember
import ru.earl.feature.chat.rooms.RoomMember
import ru.earl.models.group_occupancy.GroupOccupancy
import ru.earl.models.roomOccupancy.RoomOccupancy
import ru.earl.models.userDetails.UserDetails
import ru.earl.models.users.User

interface WebSocketsService {

    suspend fun initRoomsObservingSocket(call: ApplicationCall, socket: WebSocketSession)
    suspend fun closeRoomsObservingSocket(call: ApplicationCall)
    suspend fun initRoomMessagingSocket(call: ApplicationCall, socket: WebSocketSession)
    suspend fun closeRoomMessagingSocket(call: ApplicationCall)
    suspend fun initGroupMessagingWebSocket(call: ApplicationCall, socket: WebSocketSession)
    suspend fun closeGroupWebSocketSession(call: ApplicationCall)
}

class WebSocketServiceImpl() : WebSocketsService, OnlineController() {

    override suspend fun closeRoomMessagingSocket(call: ApplicationCall) {
        val userId = authenticate(call)
        val username = User.fetchUserById(userId!!)?.username ?: ""
        val user = WebSocketConnectionHandler.messagingClients[username]
        WebSocketConnectionHandler.messagingClients[username]?.socket?.close()
        if (WebSocketConnectionHandler.messagingClients.containsKey(username)) {
            WebSocketConnectionHandler.messagingClients.remove(username)
            RoomOccupancy.removeUserFromRoom(user?.roomId!!)
            println("DISCONNECTED FROM MESSAGING $username")
            println("observes -> $WebSocketConnectionHandler.messagingClients")
        } else {
            println("no such name in messaging")
            println("${WebSocketConnectionHandler.messagingClients.values}")
        }
    }

    override suspend fun initRoomsObservingSocket(call: ApplicationCall, socket: WebSocketSession) {
        val userId = authenticate(call)
        if (WebSocketConnectionHandler.roomObserversClients.containsKey(userId)) {
            println("client $userId is already existed in rooms observers")
            call.respond(HttpStatusCode.BadRequest, "User is already connected")
        } else if (userId != null){
            println("client $userId connected to rooms observers")
            println("socket session -> $socket")
            WebSocketConnectionHandler.roomObserversClients[userId] = ChatSocketMember(
                userId,
                socket
            )
            println("${WebSocketConnectionHandler.roomObserversClients.values}")
        } else {
            call.respond(HttpStatusCode.BadRequest, "No such userId")
        }
    }

    override suspend fun initRoomMessagingSocket(call: ApplicationCall, socket: WebSocketSession) {
        val userId = authenticate(call)
        val roomId = call.parameters["roomId"] ?: ""
        val username = User.fetchUserById(userId!!)?.username ?: ""
        if (WebSocketConnectionHandler.messagingClients.containsKey(username)) {
            println("client $username is already existed in messaging clients")
            call.respond(HttpStatusCode.BadRequest, "User is already connected")
        } else {
            println("client $username connected to chat")
            RoomOccupancy.insertUserIntoRoom(roomId)
            WebSocketConnectionHandler.messagingClients[username] = RoomMember(
                username,
                socket,
                roomId
            )
            println("${WebSocketConnectionHandler.messagingClients.values}")
        }
    }

    override suspend fun closeRoomsObservingSocket(call: ApplicationCall) {
        val userId = authenticate(call) ?: ""
        WebSocketConnectionHandler.roomObserversClients[userId]?.socket?.close()
        if (WebSocketConnectionHandler.roomObserversClients.containsKey(userId)) {
            WebSocketConnectionHandler.roomObserversClients.remove(userId)
            setUserOffline(userId)
            println("DISCONNECTED FROM ROOMS OBSERVERS $userId")
            println("observes -> $WebSocketConnectionHandler.roomObserversClients")
        } else {
            println("no such name in rooms observing")
            println("${WebSocketConnectionHandler.roomObserversClients.values}")
        }
    }

    override suspend fun initGroupMessagingWebSocket(call: ApplicationCall, socket: WebSocketSession) {
        authenticate(call)?.apply {
            val groupId = call.parameters["groupId"] ?: ""
            GroupOccupancy.setUserInGroupOccupancy(groupId)
            val clientUsername = User.fetchUserById(this)?.username ?: ""
            if (WebSocketConnectionHandler.groupMessagingClients.containsKey(clientUsername)) {
                println("client $clientUsername is already existed in group messaging clients")
                call.respond(HttpStatusCode.BadRequest, "User is already connected")
            } else {
                WebSocketConnectionHandler.groupMessagingClients[clientUsername] = GroupMember(
                    clientUsername,
                    socket,
                    groupId
                )
                println("client $clientUsername connected to group")
                println("groups observers -> ${WebSocketConnectionHandler.groupMessagingClients.values}")
            }
        }
    }

    override suspend fun closeGroupWebSocketSession(call: ApplicationCall) {
        authenticate(call)?.apply {
            val username = UserDetails.fetchUserDetailsById(this)?.username ?: ""
            val groupId = WebSocketConnectionHandler.groupMessagingClients.values.find { it.username == username }?.groupId ?: ""
            println("GROUP ID -> $groupId")
            GroupOccupancy.removeUserFromGroupOccupancy(groupId)
            WebSocketConnectionHandler.groupMessagingClients[username]?.socket?.close()
            if (WebSocketConnectionHandler.groupMessagingClients.containsKey(username)) {
                WebSocketConnectionHandler.groupMessagingClients.remove(username)
                println("DISCONNECTED FROM GROUPS MESSAGING $username")
                println("observes -> ${WebSocketConnectionHandler.groupMessagingClients}")
            } else {
                println("no such name in groups messaging clients")
                println("${WebSocketConnectionHandler.groupMessagingClients.values}")
            }
        }
    }
}