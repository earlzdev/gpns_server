package ru.earl.feature.chat

import java.util.HashMap

object WebSocketConnectionHandler {

    val roomObserversClients = HashMap<String, ChatSocketMember>()
    val messagingClients = HashMap<String, RoomMember>()
}