package ru.earl.models.commonGroup

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.earl.feature.chat.NewCommonGroupLastMsg

object Groups : Table("groups") {

    private val groupId = Groups.integer("id")
    private val title = Groups.varchar("title", 50)
    private val image = Groups.varchar("image", 2500)
    private val lastMsgText = Groups.varchar("last_message", 1000)
    private val lastMsgAuthor = Groups.varchar("last_message_author", 50)
    private val lastMsgTimestamp = Groups.varchar("last_message_timestamp", 150)
    private val lastMsgAuthorImage = Groups.varchar("last_message_author_image", 2500)

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
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateLastMsg(newLastMsg: NewCommonGroupLastMsg) {
        try {
            transaction {
                Groups.update {
                    it[lastMsgAuthor] = newLastMsg.authorName
                    it[lastMsgText] = newLastMsg.msgText
                    it[lastMsgTimestamp] = newLastMsg.timestamp
                    it[lastMsgAuthorImage] = newLastMsg.authorImage
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkCommonGroupAvailability() : Boolean {
        return try {
            transaction { Groups.select { groupId eq 0 }.single() != null }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun fetchCommonGroup() : GroupsDto? {
        return try {
            transaction {
                val query = Groups.select { groupId.eq(0) }.single()
                GroupsDto(
                    query[groupId],
                    query[title],
                    query[image],
                    query[lastMsgText],
                    query[lastMsgAuthor],
                    query[lastMsgTimestamp],
                    query[lastMsgAuthorImage]
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}