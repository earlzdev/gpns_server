package ru.earl.models.userDetails

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object UserDetails : Table("users_details") {

    private val userId = UserDetails.varchar("userId", 36)
    private val image = UserDetails.varchar("image", 2500)
    private val username = UserDetails.varchar("username", 25)
    private val online = UserDetails.varchar("online", 30)

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
                        item[online]
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
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}