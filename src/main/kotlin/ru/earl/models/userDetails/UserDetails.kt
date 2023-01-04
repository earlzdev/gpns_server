package ru.earl.models.userDetails

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.earl.models.users.User

object UserDetails : Table("users_details") {

    private val userId = UserDetails.varchar("user_id", 36)
    private val image = UserDetails.varchar("image", 2500)
    private val username = UserDetails.varchar("username", 25)
    private val online = UserDetails.integer("online")
    private val lastAuth = UserDetails.varchar("last_auth", 150)
    private val tripRole = UserDetails.varchar("trip_role", 20)

    fun fetchAllUsersListForUserById(id: String) : List<UserDetailsDto?> {
        return try {
            transaction {
                val resultRowList = UserDetails.select { userId.neq(id) }.toList()
                val readyList = mutableListOf<UserDetailsDto>()
                for (item in resultRowList) {
                    val details = UserDetailsDto(
                        item[userId],
                        item[image],
                        item[username],
                        item[online],
                        item[lastAuth],
                        item[tripRole]
                    )
                    readyList.add(details)
                }
                readyList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun insertUserDetails(user: UserDetailsDto) {
        try {
            transaction {
                UserDetails.insert {
                    it[userId] = user.userId
                    it[image] = user.image
                    it[username] = user.username
                    it[online] = user.online
                    it[lastAuth] = user.lastAuth
                    it[tripRole] = user.tripRole
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchUserDetailsById(id: String) : UserDetailsDto? {
        return try {
            transaction {
                val resultRow = UserDetails.select { userId.eq(id) }.single()
                UserDetailsDto(
                    resultRow[userId],
                    resultRow[image],
                    resultRow[username],
                    resultRow[online],
                    resultRow[lastAuth],
                    resultRow[tripRole]
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun setUserOnline(user_id: String) {
        try {
            transaction {
                UserDetails.update({ userId eq user_id }) {
                    it[online] = 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUserOffline(user_id: String, date: String) {
        try {
            transaction {
                UserDetails.update({ userId eq user_id }) {
                    it[online] = 0
                    it[lastAuth] = date
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUserTripRoleDriver(user_id: String) {
        try {
            transaction {
                UserDetails.update({ userId eq user_id }) {
                    it[tripRole] = "DRIVER"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUserTripRoleCompanion(user_id: String) {
        try {
            transaction {
                UserDetails.update({ userId eq user_id }) {
                    it[tripRole] = "Companion"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeUserTripRole(user_id: String) {
        try {
            transaction {
                UserDetails.update({ userId eq user_id }) {
                    it[tripRole] = ""
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkUserOnline(user_id: String) : Int {
        return try {
            transaction {
                val query = UserDetails.select { userId eq user_id }.single()
                val user = UserDetailsDto(
                    query[userId],
                    query[image],
                    query[username],
                    query[online],
                    query[lastAuth],
                    query[tripRole]
                )
                println("user in room -> $user")
                if (user.online == 1) {
                    return@transaction  1
                } else {
                    return@transaction 0
                }
            }
        } catch (e: Exception) {
            println("exception -> $e")
            e.printStackTrace()
            0
        }
    }
}