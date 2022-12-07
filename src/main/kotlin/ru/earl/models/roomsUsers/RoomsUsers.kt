package ru.earl.models.roomsUsers

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object RoomsUsers : Table("rooms_users") {

    private val room_id = RoomsUsers.varchar("room_id", 36)
    private val user_id = RoomsUsers.varchar("user_id", 36)

    fun insertUserForRoom(roomId: String, userId: String) {
        try {
            transaction {
                RoomsUsers.insert {
                    it[room_id] = roomId
                    it[user_id] = userId
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchRoomsIdsForUser(userId: String) : List<String> {
        return try {
            transaction {
                val resultRowList = RoomsUsers.select { user_id.eq(userId) }.toList()
                val readyTokensList = mutableListOf<String>()
                for (i in resultRowList.indices) {
                    val room = RoomsUsersDto(
                        resultRowList[i][room_id].toString(),
                        resultRowList[i][user_id].toString(),
                    )
                    readyTokensList.add(room.roomId)
                }
                readyTokensList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun fetchUsersIdsInRoom(roomId: String) : List<String> {
        return try {
            transaction {
                val resultRow = RoomsUsers.select { room_id.eq(roomId) }.toList()
                val readyUserIdsList = mutableListOf<String>()
                for (i in resultRow.indices) { readyUserIdsList.add(resultRow[i][user_id].toString()) }
                readyUserIdsList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun deleteUsersInRoom(roomId: String) {
        try {
            transaction {
                RoomsUsers.deleteWhere { room_id.eq(roomId) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}