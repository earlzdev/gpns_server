package ru.earl.models.companionGroup

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.earl.feature.chat.NewCompGroupLastMsg

object CompanionGroup : Table("companions_group") {

    private val compId = CompanionGroup.varchar("comp_id", 36)
    private val lastMsgText = CompanionGroup.varchar("last_message_text", 1000)
    private val lastMsgTimestamp = CompanionGroup.varchar("last_message_timestamp", 150)
    private val lastMsgAuthor = CompanionGroup.varchar("last_message_author", 100)
    private val lastMsgAuthorImage = CompanionGroup.varchar("last_message_image", 2500)

    fun insertNewCompanionGroup(compGroup: CompanionGroupDto) {
        try {
            transaction {
                CompanionGroup.insert {
                    it[compId] = compGroup.compId
                    it[lastMsgText] = compGroup.lastMsgText
                    it[lastMsgTimestamp] = compGroup.lastMsgTimestamp
                    it[lastMsgAuthor] = compGroup.lastMsgAuthor
                    it[lastMsgAuthorImage] = compGroup.lastMsgAuthorImage
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateLastMsg(newLastMsg: NewCompGroupLastMsg) {
        try {
            transaction {
                CompanionGroup.update({ compId eq newLastMsg.compGroupId }) {
                    it[lastMsgText] = newLastMsg.msgText
                    it[lastMsgAuthor] = newLastMsg.authorName
                    it[lastMsgTimestamp] = newLastMsg.timestamp
                    it[lastMsgAuthorImage] = newLastMsg.authorImage
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchCompanionGroup(groupId: String) : CompanionGroupDto? {
        return try {
            transaction {
                val query = CompanionGroup.select { compId eq groupId }.single()
                CompanionGroupDto(
                    query[compId],
                    query[lastMsgText],
                    query[lastMsgTimestamp],
                    query[lastMsgAuthor],
                    query[lastMsgAuthorImage]
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}