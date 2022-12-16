package ru.earl.feature.chat

import ru.earl.feature.chat.groups.GroupMember
import ru.earl.feature.chat.rooms.ChatSocketMember
import ru.earl.feature.chat.rooms.RoomMember

object WebSocketConnectionHandler {

    val roomObserversClients = HashMap<String, ChatSocketMember>()
    val messagingClients = HashMap<String, RoomMember>()
    val groupMessagingClients = HashMap<String, GroupMember>()
}