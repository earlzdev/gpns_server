package ru.earl.feature.chat.rooms

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import ru.earl.feature.chat.OnlineController
import ru.earl.models.roomsUsers.RoomsUsers
import ru.earl.models.userDetails.UserDetails
import ru.earl.models.userDetails.UserDetailsDto

interface MainService {

    suspend fun fetchUsersListForUser(call: ApplicationCall)
    suspend fun fetchUserInfo(call: ApplicationCall)
}

class MainServiceImpl : MainService, OnlineController() {

    override suspend fun fetchUsersListForUser(call: ApplicationCall) {
        authenticate(call)?.apply {
            val allRoomIdsInWhichUserConsists = RoomsUsers.fetchRoomsIdsForUser(this)
            val usersIdsTheUserHaveDialog = mutableListOf<String>()
            allRoomIdsInWhichUserConsists.forEach {
                usersIdsTheUserHaveDialog.add(RoomsUsers.fetchUsersIdsInRoom(it).find { it != this } ?: "")
            }
            val allAppUsersListIds = UserDetails.fetchAllUsersListForUserById(this).map { it?.userId }
            val readyListOfUserIds = mutableListOf<String>()
            allAppUsersListIds.forEach {
                if (!usersIdsTheUserHaveDialog.contains(it)) {
                    readyListOfUserIds.add(it!!)
                }
            }
            val readyList = mutableListOf<UserDetailsDto?>()
            readyListOfUserIds.forEach {
                readyList.add(UserDetails.fetchUserDetailsById(it))
            }
            call.respond(HttpStatusCode.OK, readyList)
        }
    }

    override suspend fun fetchUserInfo(call: ApplicationCall) {
        val userId = authenticate(call)
        val userInfo = UserDetails.fetchUserDetailsById(userId!!)
        call.respond(HttpStatusCode.OK, userInfo!!)
    }
}