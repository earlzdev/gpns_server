package ru.earl.models.companionGroupUsers

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object CompanionGroupUsers : Table("companion_group_users") {

    private val compGroup_id = CompanionGroupUsers.varchar("comp_group_id", 36)
    private val user_id = CompanionGroupUsers.varchar("user_id", 36)


    fun insertNewUserIntoCompanionGroup(compId: String, userId: String) {
        try {
            transaction {
                CompanionGroupUsers.insert {
                    it[compGroup_id] = compId
                    it[user_id] = userId
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteUserFromCompGroup(compId: String, userId: String) {
        try {
            transaction {
                CompanionGroupUsers.deleteWhere { user_id eq userId }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchUsersIdsOnCompanionGroup(compId: String) : List<String> {
        return try {
            transaction {
                val query = CompanionGroupUsers.select { compGroup_id eq compId }.toList()
                val readyList = mutableListOf<String>()
                for (i in query.indices) {
                    readyList.add(query[i][user_id])
                }
                readyList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun fetchCompanionGroupIdForUserById(userId: String) : String {
        return try {
            transaction {
                val query = CompanionGroupUsers.select { user_id eq userId }.single()
                query[compGroup_id]
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}