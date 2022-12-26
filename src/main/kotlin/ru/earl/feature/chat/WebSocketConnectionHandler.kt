package ru.earl.feature.chat

import ru.earl.feature.chat.groups.GroupMember
import ru.earl.feature.chat.rooms.ChatSocketMember
import ru.earl.feature.chat.rooms.RoomMember
import ru.earl.feature.search.SearchingMember

object WebSocketConnectionHandler {

    val roomObserversClients = HashMap<String, ChatSocketMember>()
    val messagingClients = HashMap<String, RoomMember>()
    val groupMessagingClients = HashMap<String, GroupMember>()
    val searchingSocketClients = HashMap<String, SearchingMember>()
}