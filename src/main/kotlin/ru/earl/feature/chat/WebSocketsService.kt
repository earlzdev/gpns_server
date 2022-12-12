package ru.earl.feature.chat

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.websocket.*
import ru.earl.models.roomOccupancy.RoomOccupancy
import ru.earl.models.users.User

interface WebSocketsService {

    suspend fun initRoomsObservingSocket(call: ApplicationCall, socket: WebSocketSession)
    suspend fun closeRoomsObservingSocket(call: ApplicationCall)
    suspend fun initRoomMessagingSocket(call: ApplicationCall, socket: WebSocketSession)
    suspend fun closeRoomMessagingSocket(call: ApplicationCall)
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
}