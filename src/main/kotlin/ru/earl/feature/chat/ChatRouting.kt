package ru.earl.feature.chat

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import ru.earl.feature.chat.groups.GroupServiceImpl
import ru.earl.feature.chat.rooms.MainServiceImpl
import ru.earl.feature.chat.rooms.MessagingServiceImpl
import ru.earl.feature.chat.rooms.RoomsServiceImpl

fun Application.configureChatRouting() {
    
    val chatController = ChatController(
        MessagingServiceImpl(),
        WebSocketServiceImpl(),
        MainServiceImpl(),
        RoomsServiceImpl(),
        GroupServiceImpl()
    )
    
    routing {
        authenticate {
            webSocket("/chat") {
                chatController.initRoomsObservingSocket(call, this)
                try {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            // todo
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    chatController.closeRoomsObservingSocket(call)
                }
            }
            webSocket("/messaging") {
                chatController.initRoomMessagingSocket(call, this)
                try {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            chatController.sendMessage(frame.readText())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    chatController.closeRoomMessagingSocket(call)
                }
            }
            webSocket("/group") {
                chatController.initGroupMessagingWebSocket(call, this)
                try {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            // todo
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    chatController.closeGroupWebSocketSession(call)
                }
            }
            get("/fetchRooms") {
                chatController.fetchRoomsForUser(call)
            }
            get("/users") {
                chatController.fetchUsersListForUser(call)
            }
            get("/fetchUserInfo") {
                chatController.fetchUserInfo(call)
                chatController.insertCommonGroup()
            }
            post("/fetchMessagesForRoom") {
                chatController.fetchAllMessages(call)
            }
            post("/markAuthoredMessagesAsRead") {
                chatController.markAuthoredMessageAsRead(call)
            }
            post("/deleteRoom") {
                chatController.deleteRoom(call)
            }
            post("/markMessagesAsRead") {
                chatController.markMessagesAsRead(call)
            }
            post("/updateLastMsgReadState") {
                chatController.updateLastMessageReadState(call)
            }
            post("/typingMessageRequest") {
                chatController.sendTypingMessageRequest(call)
            }
            post("/addRoom") {
                chatController.addNewRoomToDb(call)
            }
        }
    }
}