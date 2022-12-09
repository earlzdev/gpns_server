package ru.earl.models.usersOnline

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object UsersOnline : Table("users_online") {

    private val userId = UsersOnline.varchar("user_id", 36)
    private val lastAuthentication = UsersOnline.varchar("last_authentication", 50)
    private val isOnline = UsersOnline.integer("is_online")

    fun insertNewUser(user: UsersOnlineDto) {
        try {
            transaction {
                UsersOnline.insert {
                    it[userId] = user.userId
                    it[lastAuthentication] = user.lastAuthentication
                    it[isOnline] = user.isOnline
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUserOnline(user_id: String) {
        try {
            transaction {
                if (user_id != "") {
                    UsersOnline.update ({ userId eq user_id }) {
                        it[isOnline] = 1
                    }
                }
            }
        } catch (e: Exception) {

        }
    }

    fun setUserOffline(user_id: String, lastAuth: String) {
        try {
            transaction {
                if (user_id != "") {
                    UsersOnline.update ({ userId eq user_id }) {
                        it[isOnline] = 0
                        it[lastAuthentication] = lastAuth
                    }
                }
            }
        } catch (e: Exception) {

        }
    }

    fun checkOnline(user_id: String) : Boolean {
        return try {
            transaction {
                val query = UsersOnline.select { userId.eq(user_id) }.single()
                val online = UsersOnlineDto(
                    query[userId],
                    query[lastAuthentication],
                    query[isOnline]
                )
                if (online.isOnline == 1) return@transaction true else false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteUser(user_id: String) {
        try {
            transaction {
                UsersOnline.deleteWhere { userId eq user_id }
            }
        } catch (e: Exception) {

        }
    }
}