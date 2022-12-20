package ru.earl.models.group_occupancy

import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object GroupOccupancy : Table("group_occupancy") {

    private val groupId = GroupOccupancy.varchar("group_id", 36)
    private val groupOccupancy = GroupOccupancy.integer("group_occupancy")

    fun insertNewGroupOccupancy(group_id: String) {
        try {
            transaction {
                GroupOccupancy.insert {
                    it[groupId] = group_id
                    it[groupOccupancy] = 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isSomebodyInGroup(group_id: String) : Boolean {
        return try {
            transaction {
                val query = GroupOccupancy.select { groupId eq group_id }.single()
                return@transaction query[groupOccupancy] != 0 && query[groupOccupancy] != 1
            }
        } catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    fun setUserInGroupOccupancy(group_id: String) {
        try {
            transaction {
                GroupOccupancy.update({ groupId eq group_id }) {
                    it[groupOccupancy] = groupOccupancy + 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeUserFromGroupOccupancy(group_id: String) {
        try {
            transaction {
                GroupOccupancy.update({ groupId eq group_id }) {
                    it[groupOccupancy] = groupOccupancy - 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}