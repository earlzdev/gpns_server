package ru.earl.feature.chat.groups

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import ru.earl.feature.chat.OnlineController
import ru.earl.models.commonGroup.Groups
import ru.earl.models.commonGroup.GroupsDto
import ru.earl.models.companionGroup.CompanionGroup
import ru.earl.models.companionGroupUsers.CompanionGroupUsers

interface GroupService {

    suspend fun insertCommonGroup()

    suspend fun fetchCompanionGroup(call: ApplicationCall)

    suspend fun fetchCommonGroup(call: ApplicationCall)
}

class GroupServiceImpl() : GroupService, OnlineController() {

    override suspend fun insertCommonGroup() {
        if (!Groups.checkCommonGroupAvailability()) {
            val commonGroup = GroupsDto(
                0,
                "Общий чат",
                "",
                "",
                "",
                "",
                ""
            )
            Groups.insertNewGroup(commonGroup)
        }
    }

    override suspend fun fetchCompanionGroup(call: ApplicationCall) {
        authenticate(call)?.apply {
            val companionGroupId = CompanionGroupUsers.fetchCompanionGroupIdForUserById(this)
            CompanionGroup.fetchCompanionGroup(companionGroupId)?.apply {
                call.respond(HttpStatusCode.OK, this)
            }
        }
    }

    override suspend fun fetchCommonGroup(call: ApplicationCall) {
        Groups.fetchCommonGroup()?.apply {
            call.respond(HttpStatusCode.OK, this)
        }
    }
}