package ru.earl.models.rooms

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction

object Room : Table("rooms") {

    private val roomId = Room.varchar("roomId", 36)
    private val image = Room.varchar("image", 2500)
    private val author_name = Room.varchar("author_name", 25)
    private val contact_name = Room.varchar("contact_name", 25)
    private val lastMessage = Room.varchar("last_message", 1000)
    private val lastMessageAuthor = Room.varchar("last_message_author", 25)
    private val deletable = Room.varchar("deletable", 10)
    private val unreadMsgCount = Room.integer("unreadMsgCount")
    private val lastMsgRead = Room.integer("lastMsgRead")
    private val contactOnline = Room.integer("contactOnline")
    private val contactLastAuth = Room.varchar("contactLastAuth", 150)
    private val lastMsgTimestamp = Room.varchar("last_msg_timestamp", 50)

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
                    it[unreadMsgCount] = room.unreadMsgCount
                    it[lastMsgRead] = room.isLastMsgRead
                    it[contactOnline] = room.contactOnline
                    it[contactLastAuth] = room.contactLastAuth
                    it[lastMsgTimestamp] = room.lastMsgTimestamp
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
                    resultRow[deletable],
                    resultRow[unreadMsgCount],
                    resultRow[lastMsgRead],
                    resultRow[contactOnline],
                    resultRow[contactLastAuth],
                    resultRow[lastMsgTimestamp]
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

    fun updateLastMessage(room_id: String, newLastMessage: String, author: String, lastMsgTime: String) {
        try {
            transaction {
                Room.update({ roomId eq room_id}) {
                    it[lastMessage] = newLastMessage
                    it[lastMessageAuthor] = author
                    it[lastMsgTimestamp] = lastMsgTime
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun increaseRoomUnreadMessagesCount(room_Id: String) {
         try {
            transaction {
                Room.update({ roomId eq room_Id }) {
                    it[unreadMsgCount] = unreadMsgCount + 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearRoomUnreadMessagesCounter(room_Id: String) {
        try {
            transaction {
                Room.update({ roomId eq room_Id }) {
                    it[unreadMsgCount] = 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateLastMessageReadStateToRead(room_id: String) {
        try {
            transaction {
                Room.update({ roomId eq room_id }) {
                    it[lastMsgRead] = 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateLastMessageReadStateToUnread(room_id: String) {
        try {
            transaction {
                Room.update({ roomId eq room_id }) {
                    it[lastMsgRead] = 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchAllRoomsIdsWithUser(username: String) : List<String> {
        return try {
            transaction {
                val resultRow = Room.select { author_name eq username }.toList()
                val resultRowForContact = Room.select { contact_name eq username }.toList()
                val readyList = mutableListOf<String>()
                for (i in resultRow.indices) {
                    readyList.add(
                        resultRow[i][roomId]
                    )
                }
                for (i in resultRowForContact.indices) {
                    readyList.add(
                        resultRowForContact[i][roomId]
                    )
                }
                readyList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun setUserOnline(username: String) {
        try {
            transaction {
                Room.update({ contact_name eq username }) {
                    it[contactOnline] = 1
                }
                Room.update({ author_name eq username }) {
                    it[contactOnline] = 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUserOffline(username: String) {
        try {
            transaction {
                Room.update({ contact_name eq username }) {
                    it[contactOnline] = 0
                }
                Room.update({ author_name eq username }) {
                    it[contactOnline] = 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateUserLastAuth(room_id: String, lastAuthDate: String) {
        try {
            transaction {
                Room.update({ roomId eq room_id }) {
                    it[contactLastAuth] = lastAuthDate
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}