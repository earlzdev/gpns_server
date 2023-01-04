package ru.earl.models.groupUsers

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object GroupUsers : Table("group_users") {

    private val group_id = GroupUsers.varchar("group_id", 36)
    private val user_id = GroupUsers.varchar("user_id", 36)

    fun fetchGroupsIdsForUserById(userId: String) : List<String> {
        return try {
            transaction {
                val queryList = GroupUsers.select { user_id eq userId }.toList()
                val readyList = mutableListOf<String>()
                for (i in queryList.indices) {
                    readyList.add(queryList[i][group_id])
                }
                readyList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun insertNewUserForCommonGroup(userId: String, groupId: String) {
        try {
            transaction {
                if (!checkIsUserInCommonGroup(userId)) {
                    GroupUsers.insert {
                        it[group_id] = groupId
                        it[user_id] = userId
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun insertNewUserIntoGroup(groupId: String, userId: String) {
        try {
            transaction {
                GroupUsers.insert {
                    it[group_id] = groupId
                    it[user_id] = userId
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchUsersIdsListInGroup(gropId: String) : List<String> {
        return try {
            transaction {
                val queryList = GroupUsers.select { group_id eq gropId }.toList()
                val readyList = mutableListOf<String>()
                for (i in queryList.indices) {
                    readyList.add(queryList[i][user_id])
                }
                readyList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun checkIsUserInCommonGroup(userId: String) : Boolean {
        return try {
            transaction {
                val queryList = GroupUsers.select { group_id eq "common" }.toList()
                var result = false
                for (i in queryList.indices) {
                    result = queryList[i][user_id] == userId
                }
                return@transaction result
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteAllUserFromGroup(groupId: String) {
        try {
            transaction {
                GroupUsers.deleteWhere { group_id eq groupId }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeUserFromGroup(userId: String, groupId: String) {
        try {
            transaction {
                GroupUsers.deleteWhere { user_id.eq(userId) and group_id.eq(groupId) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}