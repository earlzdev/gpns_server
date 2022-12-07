package ru.earl.feature.chat

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import ru.earl.models.users.User

fun Application.configureChatRouting() {

    val chatController = ChatController()

    routing {
        authenticate {
            webSocket("/chat") {
                chatController.initChatSocket(call, this)
                try {
                    incoming.consumeEach { frame ->
                        if(frame is Frame.Text) {
                            chatController.parseAction(frame.readText(), call)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    val userId = chatController.authenticate(call)
                    chatController.tryChatDisconnect(userId!!)
                }
            }
            webSocket("/messaging") {
                chatController.initMessaging(call, this)
                try {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            chatController.sendMessage(frame.readText())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    val userId = chatController.authenticate(call)
                    val username = User.fetchUserById(userId!!)?.username
                    chatController.tryMessagingDisconnect(username!!)
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
            }
            post("/fetchMessagesForRoom") {
                chatController.fetchAllMessages(call)
            }
            post("/deleteRoom") {
                chatController.deleteRoom(call)
            }
            post("/fetchUnreadMessagesInRoom") {
                chatController.fetchUnreadMessagesInRoom(call)
            }
            post("/markMessagesAsRead") {
                chatController.markMessagesAsRead(call)
            }
        }
    }
}
