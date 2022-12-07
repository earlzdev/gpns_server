package ru.earl.models.roomOccupancy

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction

object RoomOccupancy : Table("room_occupancy") {

    private val roomId = RoomOccupancy.varchar("roomId", 36)
    private val full = RoomOccupancy.integer("full")

    fun initNewRoomOccupancy(room_id: String) {
        try {
            transaction {
                RoomOccupancy.insert {
                    it[roomId] = room_id
                    it[full] = 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteRoomOccupancy(room_id: String) {
        try {
            transaction {
                RoomOccupancy.deleteWhere { roomId.eq(room_id) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun insertUserIntoRoom(room_id: String) {
        try {
            transaction {
                RoomOccupancy.update({ roomId eq  room_id }) {
                    it[full] = full + 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeUserFromRoom(room_id: String) {
        try {
            transaction {
                RoomOccupancy.update ({ roomId eq room_id }) {
                    it[full] = full - 1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkRoomOccupancy(room_id: String) : Int? {
        return try {
            transaction {
                val result = RoomOccupancy.select { roomId.eq(room_id) }.single()
                val roomOccupancy = RoomOccupancyDto(
                    result[roomId],
                    result[full]
                )
                roomOccupancy.full
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}