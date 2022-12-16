package ru.earl.models.commonGroupMessages

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object CommonGroupMessages : Table("common_group_messages") {

    private val messageId = CommonGroupMessages.varchar("message_id", 36)
    private val authorName = CommonGroupMessages.varchar("author_name", 50)
    private val timestamp = CommonGroupMessages.varchar("timestamp", 50)
    private val messageText = CommonGroupMessages.varchar("message_text", 1000)

    fun insertNewMessage(msg: CommonGroupMessagesDto) {
        try {
            transaction {
                CommonGroupMessages.insert {
                    it[messageId] = msg.messageId
                    it[authorName] = msg.authorName
                    it[timestamp] = msg.timestamp
                    it[messageText] = msg.messageText
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchAllMessages() : List<CommonGroupMessagesDto> {
        return try {
            transaction {
                val queryList = CommonGroupMessages.selectAll().toList()
                val readyList = mutableListOf<CommonGroupMessagesDto>()
                for (i in queryList.indices) {
                    readyList.add(
                        CommonGroupMessagesDto(
                            queryList[i][messageId],
                            queryList[i][authorName],
                            queryList[i][timestamp],
                            queryList[i][messageText],
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