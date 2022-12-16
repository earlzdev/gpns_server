package ru.earl.models.companionGroupsMessages

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import ru.earl.models.commonGroupMessages.CommonGroupMessages

object CompanionGroupsMessages : Table("companion_groups_messages") {

    private val messageId = CompanionGroupsMessages.varchar("message_id", 36)
    private val authorName = CompanionGroupsMessages.varchar("author_name", 50)
    private val timestamp = CompanionGroupsMessages.varchar("timestamp", 50)
    private val messageText = CompanionGroupsMessages.varchar("message_text", 1000)

    fun insertNewMas(msg: CompanionGroupsMessagesDto) {
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

    fun fetchAllMessages() : List<CompanionGroupsMessagesDto> {
        return try {
            transaction {
                val queryList = CommonGroupMessages.selectAll().toList()
                val readyList = mutableListOf<CompanionGroupsMessagesDto>()
                for (i in queryList.indices) {
                    readyList.add(
                        CompanionGroupsMessagesDto(
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