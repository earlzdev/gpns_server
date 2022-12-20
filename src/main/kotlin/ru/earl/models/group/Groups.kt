package ru.earl.models.group

import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.earl.feature.chat.GroupLastMessage

object Groups : Table("groups") {

    private val groupId = Groups.varchar("id", 36)
    private val title = Groups.varchar("title", 50)
    private val image = Groups.varchar("image", 2500)
    private val lastMsgText = Groups.varchar("last_message", 1000)
    private val lastMsgAuthor = Groups.varchar("last_message_author", 50)
    private val lastMsgTimestamp = Groups.varchar("last_message_timestamp", 150)
    private val lastMsgAuthorImage = Groups.varchar("last_message_author_image", 2500)
    private val companionGroup = Groups.integer("companion_group")
    private val messagesCount = Groups.integer("messages_count")
    private val lastMsgRead = Groups.integer("last_message_read")

    fun insertNewGroup(group: GroupsDto) {
        try {
            transaction {
                Groups.insert {
                    it[groupId] = group.id
                    it[title] = group.title
                    it[image] = group.image
                    it[lastMsgText] = group.lastMsgText
                    it[lastMsgAuthor] = group.lastMsgAuthor
                    it[lastMsgTimestamp] = group.lastMsgTimestamp
                    it[lastMsgAuthorImage] = group.lastMsgAuthorImage
                    it[companionGroup] = group.companionGroup
                    it[messagesCount] = group.messagesCount
                    it[lastMsgRead] = group.lastMsgRead
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateLastMsg(newLastMsg: GroupLastMessage) {
        try {
            transaction {
                Groups.update({ groupId eq newLastMsg.groupsId }) {
                    it[lastMsgAuthor] = newLastMsg.authorName
                    it[lastMsgText] = newLastMsg.msgText
                    it[lastMsgTimestamp] = newLastMsg.timestamp
                    it[lastMsgAuthorImage] = newLastMsg.authorImage
                    it[lastMsgRead] = newLastMsg.lastMessageRead
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkCommonGroupAvailability() : Boolean {
        return try {
            transaction { Groups.select { groupId eq "common" }.single() != null }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun setLastMessageAsRead(group_id: String) {
        try {
            transaction {
                Groups.update({ groupId eq group_id }) {
                    it[lastMsgRead] = 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchGroupByGroupId(group_id: String) : GroupsDto? {
        return try {
            transaction {
                val query = Groups.select { groupId.eq(group_id) }.single()
                GroupsDto(
                    query[groupId],
                    query[title],
                    query[image],
                    query[lastMsgText],
                    query[lastMsgAuthor],
                    query[lastMsgTimestamp],
                    query[lastMsgAuthorImage],
                    query[companionGroup],
                    query[messagesCount],
                    query[lastMsgRead]
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun increaseGroupMessagesCounter(group_id: String) {
        try {
            transaction {
                Groups.update({ groupId eq group_id }) {
                    it[messagesCount] = messagesCount + 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}