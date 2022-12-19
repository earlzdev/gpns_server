package ru.earl.models.groupMessages

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object GroupMessages : Table("group_messages") {

    private val groupId = GroupMessages.varchar("group_id", 36)
    private val messageId = GroupMessages.varchar("message_id", 36)
    private val authorName = GroupMessages.varchar("author_name", 50)
    private val authorImage = GroupMessages.varchar("author_image", 2500)
    private val timestamp = GroupMessages.varchar("timestamp", 100)
    private val messageText = GroupMessages.varchar("message_text", 1000)
    private val read = GroupMessages.integer("read")

    fun insertNewMessage(msg: GroupMessagesDto) {
        try {
            transaction {
                GroupMessages.insert {
                    it[groupId] = msg.groupId
                    it[messageId] = msg.messageId
                    it[authorName] = msg.authorName
                    it[authorImage] = msg.authorImage
                    it[timestamp] = msg.timestamp
                    it[messageText] = msg.messageText
                    it[read] = msg.read
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchMessagesForGroup(group_id: String) : List<GroupMessagesDto> {
        return try {
            transaction {
                val queryList = GroupMessages.select { groupId eq group_id }.toList()
                val readyList = mutableListOf<GroupMessagesDto>()
                for (i in queryList.indices) {
                    readyList.add(
                        GroupMessagesDto(
                            queryList[i][groupId],
                            queryList[i][messageId],
                            queryList[i][authorName],
                            queryList[i][authorImage],
                            queryList[i][timestamp],
                            queryList[i][messageText],
                            queryList[i][read]
                        )
                    )
                }
                readyList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}