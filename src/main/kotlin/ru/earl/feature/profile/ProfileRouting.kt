package ru.earl.feature.profile

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureProfileRouting() {

    val controller = ProfileController.Base()

    routing {
        authenticate {
            post("/updateUserAvatar") {
                controller.updateUserAvatar(call)
            }
        }
    }
}