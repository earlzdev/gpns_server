package ru.earl.feature.chat

import io.ktor.server.application.*
import io.ktor.websocket.*
import ru.earl.feature.chat.groups.GroupService
import ru.earl.feature.chat.groups.GroupServiceImpl
import ru.earl.feature.chat.groups.GroupsMessagingService
import ru.earl.feature.chat.rooms.*
import ru.earl.models.rooms.RoomDto

class ChatController(
    private val messagingServiceImpl: MessagingServiceImpl,
    private val webSocketServiceImpl: WebSocketServiceImpl,
    private val mainServiceImpl: MainServiceImpl,
    private val roomsServiceImpl: RoomsServiceImpl,
    private val groupsService: GroupServiceImpl,
    private val groupsMessagingService: GroupsMessagingService
) : MessagingService, RoomsService, MainService, WebSocketsService, GroupService, GroupsMessagingService {

    override suspend fun fetchUsersListForUser(call: ApplicationCall) {
        mainServiceImpl.fetchUsersListForUser(call)
    }

    override suspend fun fetchUserInfo(call: ApplicationCall) {
        mainServiceImpl.fetchUserInfo(call)
    }

    override suspend fun sendMessage(messageJson: String) {
        messagingServiceImpl.sendMessage(messageJson)
    }

    override suspend fun markMessagesAsRead(call: ApplicationCall) {
        messagingServiceImpl.markMessagesAsRead(call)
    }

    override suspend fun sendTypingMessageRequest(call: ApplicationCall) {
        messagingServiceImpl.sendTypingMessageRequest(call)
    }

    override suspend fun markAuthoredMessageAsRead(call: ApplicationCall) {
        messagingServiceImpl.markAuthoredMessageAsRead(call)
    }

    override suspend fun sendUpdatableMessage(receivedMessage: MessageReceive) {
        messagingServiceImpl.sendUpdatableMessage(receivedMessage)
    }

    override suspend fun fetchAllMessages(call: ApplicationCall) {
        messagingServiceImpl.fetchAllMessages(call)
    }

    override suspend fun fetchRoomsForUser(call: ApplicationCall) {
        roomsServiceImpl.fetchRoomsForUser(call)
    }

    override suspend fun deleteRoom(call: ApplicationCall) {
        roomsServiceImpl.deleteRoom(call)
    }

    override suspend fun updateLastMessageReadState(call: ApplicationCall) {
        roomsServiceImpl.updateLastMessageReadState(call)
    }

    override suspend fun markAuthoredMessageAsReadInRoom(call: ApplicationCall) {
        roomsServiceImpl.markAuthoredMessageAsReadInRoom(call)
    }

    override suspend fun addNewRoomToDb(call: ApplicationCall) {
        roomsServiceImpl.addNewRoomToDb(call)
    }

    override suspend fun sendNewRoomToContacts(userId: String, newRoom: RoomDto) {
        roomsServiceImpl.sendNewRoomToContacts(userId, newRoom)
    }

    override suspend fun initRoomsObservingSocket(call: ApplicationCall, socket: WebSocketSession) {
        webSocketServiceImpl.initRoomsObservingSocket(call, socket)
    }

    override suspend fun closeRoomsObservingSocket(call: ApplicationCall) {
        webSocketServiceImpl.closeRoomsObservingSocket(call)
    }

    override suspend fun initRoomMessagingSocket(call: ApplicationCall, socket: WebSocketSession) {
        webSocketServiceImpl.initRoomMessagingSocket(call, socket)
    }

    override suspend fun closeRoomMessagingSocket(call: ApplicationCall) {
        webSocketServiceImpl.closeRoomMessagingSocket(call)
    }

    override suspend fun insertCommonGroup(call: ApplicationCall) {
        groupsService.insertCommonGroup(call)
    }

    override suspend fun initGroupMessagingWebSocket(call: ApplicationCall, socket: WebSocketSession) {
        webSocketServiceImpl.initGroupMessagingWebSocket(call, socket)
    }

    override suspend fun closeGroupWebSocketSession(call: ApplicationCall) {
        webSocketServiceImpl.closeGroupWebSocketSession(call)
    }

    override suspend fun fetchGroups(call: ApplicationCall) {
        groupsService.fetchGroups(call)
    }

    override suspend fun fetchAllMessagesInGroup(call: ApplicationCall) {
        groupsMessagingService.fetchAllMessagesInGroup(call)
    }

    override suspend fun sendMessageInGroup(messageJson: String) {
        groupsMessagingService.sendMessageInGroup(messageJson)
    }

    override suspend fun sendUpdateTypingMessageInGroup(call: ApplicationCall) {
        groupsMessagingService.sendUpdateTypingMessageInGroup(call)
    }
}