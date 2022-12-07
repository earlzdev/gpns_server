package ru.earl.models.roomsMessages

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import ru.earl.models.rooms.Room

object RoomsMessages : Table("rooms_messages") {

    private val messageToken = RoomsMessages.varchar("message_id", 36)
    private val roomToken = RoomsMessages.varchar("room_id", 36)
    private val authorToken = RoomsMessages.varchar("author_id", 36)
    private val timestamp = RoomsMessages.varchar("timestamp", 25)
    private val textMessage = RoomsMessages.varchar("message", 1000)
    private val messageDate = RoomsMessages.varchar("messageDate", 50)
    private val read = RoomsMessages.integer("read")

    fun insertMessageIntoDb(message : RoomsMessagesDto) {
        transaction {
            RoomsMessages.insert {
                it[messageToken] = message.messageId
                it[roomToken] = message.roomId
                it[authorToken] = message.authorId
                it[timestamp] = message.timestamp
                it[textMessage] =  message.messageText
                it[messageDate] = message.messageDate
                it[read] = message.read
            }
        }
    }

    fun fetchMessagesByRoomToken(token: String) : List<RoomsMessagesDto> {
        return try {
            transaction {
                val query = RoomsMessages.select { roomToken.eq(token) }.toList()
                val messagesList = mutableListOf<RoomsMessagesDto>()
                for (i in query.indices) {
                    val message = RoomsMessagesDto(
                        query[i][messageToken],
                        query[i][roomToken],
                        query[i][authorToken],
                        query[i][timestamp],
                        query[i][textMessage],
                        query[i][messageDate],
                        query[i][read]
                    )
                    messagesList.add(message)
                }
                messagesList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun deleteAllMessagesInRoom(room_id: String) {
        try {
            transaction {
                RoomsMessages.deleteWhere { roomToken.eq(room_id) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchMessageById(id: String) : RoomsMessagesDto? {
        return try {
            transaction {
                val query = RoomsMessages.select { messageToken.eq(id) }.single()
                RoomsMessagesDto(
                    query[messageToken],
                    query[roomToken],
                    query[authorToken],
                    query[timestamp],
                    query[textMessage],
                    query[messageDate],
                    query[read]
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun markAsRead(roomId: String) {
        try {
            transaction {
                RoomsMessages.update ({ roomToken eq roomId }) {
                    it[read] = 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchUnreadMessagesForRoom(roomId: String) : List<RoomsMessagesDto> {
        return try {
            transaction {
                val resultRowList = RoomsMessages.select { roomToken.eq(roomId) and read.eq(0) }.toList()
                val readyList = mutableListOf<RoomsMessagesDto>()
                for (i in resultRowList.indices) {
                    readyList.add(
                        RoomsMessagesDto(
                        resultRowList[i][messageToken],
                            resultRowList[i][roomToken],
                            resultRowList[i][authorToken],
                            resultRowList[i][timestamp],
                            resultRowList[i][textMessage],
                            resultRowList[i][messageDate],
                            resultRowList[i][read],
                    ))
                }
                readyList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

