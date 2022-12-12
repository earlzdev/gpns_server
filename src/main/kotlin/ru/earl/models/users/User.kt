package ru.earl.models.users

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object User : Table("users") {

    private val email = User.varchar("email", 25)
    private val username = User.varchar("username", 25)
    private val password = User.varchar("password", 200)
    private val userSalt = User.varchar("salt", 200)
    private val userId = User.varchar("id", 36)

    fun insert(userDTO: UserDto) {
        transaction {
            User.insert {
                it[email] = userDTO.email
                it[username] = userDTO.username
                it[password] = userDTO.password
                it[userSalt] = userDTO.userSalt
                it[userId] = userDTO.userId
            }
        }
    }

    fun fetchUserByEmail(emailInput: String): UserDto? {
        return try {
            transaction {
                val user = User.select { email.eq(emailInput) }.single()
                UserDto(
                    user[email],
                    user[username],
                    user[password],
                    user[userSalt],
                    user[userId]
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fetchUserById(id: String) : UserDto? {
        return try {
            transaction {
                val resultRow = User.select { userId.eq(id) }.single()
                UserDto(
                    resultRow[email],
                    resultRow[username],
                    resultRow[password],
                    resultRow[userSalt],
                    resultRow[userId]
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fetchUserByUsername(name: String) : UserDto? {
        return try {
            transaction {
                val resultRow = User.select { username.eq(name) }.single()
                UserDto(
                    resultRow[email],
                    resultRow[username],
                    resultRow[password],
                    resultRow[userSalt],
                    resultRow[userId]
                )
            }
        } catch (e: Exception) {
            println("exc -> $e")
            e.printStackTrace()
            null
        }
    }

    fun fetchPossibleContacts(id: String) : List<UserDto?> {
        return try {
            transaction {
                val resultRowList = User.select { userId.neq(id) }.toList()
                val usersList = mutableListOf<UserDto>()
                for (item in resultRowList) {
                    val user =  UserDto(
                        item[email],
                        item[username],
                        item[password],
                        item[userSalt],
                        item[userId]
                    )
                    usersList.add(user)
                }
               usersList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}