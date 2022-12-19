package ru.earl.feature.chat.groups

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import ru.earl.feature.chat.OnlineController
import ru.earl.models.group.Groups
import ru.earl.models.group.GroupsDto
import ru.earl.models.groupUsers.GroupUsers

interface GroupService {

    suspend fun insertCommonGroup(call: ApplicationCall)

    suspend fun fetchGroups(call: ApplicationCall)
}

class GroupServiceImpl() : GroupService, OnlineController() {

    override suspend fun insertCommonGroup(call: ApplicationCall) {
        authenticate(call)?.apply {
            if (!Groups.checkCommonGroupAvailability()) {
                val commonGroup = GroupsDto(
                    COMMON_GROUP_ID,
                    "Общий чат",
                    "",
                    "",
                    "",
                    "",
                    "",
                    0,
                    0
                )
                Groups.insertNewGroup(commonGroup)
                GroupUsers.insertNewUserForGroup(this, COMMON_GROUP_ID)
            }
        }
    }

    override suspend fun fetchGroups(call: ApplicationCall) {
        authenticate(call)?.apply {
            val groupsIdsList = GroupUsers.fetchGroupsIdsForUserById(this)
            val readyList = mutableListOf<GroupsDto?>()
            for (i in groupsIdsList.indices) {
                readyList.add(Groups.fetchGroupByGroupId(groupsIdsList[i]))
            }
            println("READY GROUPS LIST -> $readyList")
            call.respond(HttpStatusCode.OK, readyList)
        }
    }

    companion object {
        private const val COMMON_GROUP_ID = "common"
    }
}