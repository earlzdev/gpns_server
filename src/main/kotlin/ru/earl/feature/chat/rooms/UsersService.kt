package ru.earl.feature.chat.rooms

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import ru.earl.feature.chat.OnlineController
import ru.earl.models.userDetails.UserDetails

interface MainService {

    suspend fun fetchUsersListForUser(call: ApplicationCall)
    suspend fun fetchUserInfo(call: ApplicationCall)
}

class MainServiceImpl() : MainService, OnlineController() {

    override suspend fun fetchUsersListForUser(call: ApplicationCall) {
        val userId = authenticate(call)
        if (userId != null) {
            val usersList = UserDetails.fetchAllUsersListForUserById(userId)
            call.respond(HttpStatusCode.OK, usersList)
        } else {
            call.respond(HttpStatusCode.BadRequest, "Authenticate problem")
        }
    }

    override suspend fun fetchUserInfo(call: ApplicationCall) {
        val userId = authenticate(call)
        val userInfo = UserDetails.fetchUserDetailsById(userId!!)
        call.respond(HttpStatusCode.OK, userInfo!!)
    }
}