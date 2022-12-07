package ru.earl.models.rooms

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object Room : Table("rooms") {

    private val roomId = Room.varchar("roomId", 36)
    private val image = Room.varchar("image", 2500)
    private val author_name = Room.varchar("author_name", 25)
    private val contact_name = Room.varchar("contact_name", 25)
    private val lastMessage = Room.varchar("last_message", 1000)
    private val lastMessageAuthor = Room.varchar("last_message_author", 25)
    private val deletable = Room.varchar("deletable", 10)

    fun insertRoom(room: RoomDto) {
        try {
            transaction {
                Room.insert {
                    it[roomId] = room.roomId
                    it[image] = room.image
                    it[author_name] = room.author_name
                    it[contact_name] = room.contact_name
                    it[lastMessage] = room.last_message
                    it[lastMessageAuthor] = room.last_message_author
                    it[deletable] = room.deletable
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchRoomByRoomId(room_id: String) : RoomDto? {
        return try {
            transaction {
                val resultRow = Room.select { roomId.eq(room_id) }.single()
                RoomDto(
                    resultRow[roomId],
                    resultRow[image],
                    resultRow[author_name],
                    resultRow[contact_name],
                    resultRow[lastMessage],
                    resultRow[lastMessageAuthor],
                    resultRow[deletable]
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun removeRoom(room_id: String) {
        try {
            transaction {
                Room.deleteWhere { roomId.eq(room_id) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateLastMessage(room_id: String, newLastMessage: String, author: String) {
        try {
            transaction {
                Room.update({ roomId eq room_id}) {
                    it[lastMessage] = newLastMessage
                    it[lastMessageAuthor] = author
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}