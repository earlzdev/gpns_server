package ru.earl.feature.search

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

fun Application.configureSearchRouting() {

    val controller = SearchController(
        SearchFormsServiceImpl()
    )

    routing {
        authenticate {
            webSocket("/searching") {
                controller.initSearchingWebSocket(call, this)
                try {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            // todo
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    controller.closeSearchingWebSocket(call)
                }
            }
            post("/sendNewDriverForm") {
                controller.insertNewDriverForm(call)
            }
            post("/sendNewCompanionForm") {
                controller.insertNewCompanionForm(call)
            }
            get("/fetchAllCompForms") {
                controller.fetchAllForms(call)
            }
            post("/inviteDriver") {
                controller.inviteDriver(call)
            }
            post("/inviteCompanion") {
                controller.inviteCompanion(call)
            }
            post("/deleteCompanionForm") {
                controller.deleteCompanionForm(call)
            }
            post("/deleteDriverForm") {
                controller.deleteDriverForm(call)
            }
            post("/fetchAllNotifications") {
                controller.fetchAllNotificationsForUser(call)
            }
            post("/fetchCompanionForm") {
                controller.fetchCompanionForm(call)
            }
            post("/fetchDriverForm") {
                controller.fetchDriverForm(call)
            }
            post("/acceptCompanionToDrive") {
                controller.acceptCompanionToRideTogether(call)
            }
            post("/denyCompanionToDrive") {
                controller.denyCompanionToRideTogether(call)
            }
            post("/acceptDriverToDrive") {
                controller.acceptDriverToRideTogether(call)
            }
            post("/denyDriverToDrive") {
                controller.denyDriverToRideTogether(call)
            }
            post("/fetchAllCompanionsInGroup") {
                controller.fetchAllCompanionsInGroup(call)
            }
            post("/removeCompanionFromGroup") {
                controller.removeCompanionFromGroup(call)
            }
        }
    }
}
