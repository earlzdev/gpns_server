package ru.earl.feature.profile

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import ru.earl.feature.chat.OnlineController
import ru.earl.models.UpdateUserAvatarRequest
import ru.earl.models.userDetails.UserDetails

interface ProfileController {

    suspend fun updateUserAvatar(call: ApplicationCall)

    class Base() : ProfileController, OnlineController() {
        override suspend fun updateUserAvatar(call: ApplicationCall) {
            try {
                authenticate(call)?.apply {
                    val newAvatarString = call.receive<UpdateUserAvatarRequest>().newImageString
                    UserDetails.updateUserImage(newAvatarString, this)
//                    Room.
                }
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, e)
            }
        }
    }
}