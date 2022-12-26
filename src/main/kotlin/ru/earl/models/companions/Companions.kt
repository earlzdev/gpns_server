package ru.earl.models.companions

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Companions : Table("companions") {

   private val username = Companions.varchar("username", 50)
   private val userImage = Companions.varchar("user_image", 2500)
   private val from = Companions.varchar("from", 300)
   private val to = Companions.varchar("to", 300)
   private val schedule = Companions.varchar("schedule", 150)
   private val actualTripTime = Companions.varchar("actual_trip_time", 150)
   private val ableToPay = Companions.varchar("able_to_pay", 50)
   private  val comment = Companions.varchar("comment", 1000)

   fun insertNewCompanion(compForm: CompanionDto) {
      try {
          transaction {
             Companions.insert {
                it[username] = compForm.username
                it[userImage] = compForm.userImage
                it[from] = compForm.from
                it[to] = compForm.to
                it[schedule] = compForm.schedule
                it[actualTripTime] = compForm.actualTripTime
                it[ableToPay] = compForm.ableToPay
                it[comment] = compForm.comment
             }
          }
      } catch (e: Exception) {
         e.printStackTrace()
      }
   }

   fun fetchAllCompanions() : List<CompanionDto> {
      return try {
         transaction {
            val queryList = Companions.selectAll().toList()
            val readyList = mutableListOf<CompanionDto>()
            for (i in queryList.indices) {
               readyList.add(
                  CompanionDto(
                  queryList[i][username],
                  queryList[i][userImage],
                  queryList[i][from],
                  queryList[i][to],
                  queryList[i][schedule],
                  queryList[i][actualTripTime],
                  queryList[i][ableToPay],
                  queryList[i][comment]
               ))
            }
            readyList
         }
      } catch (e: Exception) {
         e.printStackTrace()
         emptyList()
      }
   }

   fun deleteCompanionForm(user: String) {
      try {
          transaction {
             Companions.deleteWhere { username eq user }
          }
      } catch (e: Exception) {
         e.printStackTrace()
      }
   }
}