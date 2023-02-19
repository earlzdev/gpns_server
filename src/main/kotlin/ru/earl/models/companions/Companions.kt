package ru.earl.models.companions

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Companions : Table("companions") {

   private val username = Companions.varchar("username", 50)
   private val userImage = Companions.varchar("user_image", 2500)
   private val from = Companions.varchar("fromm", 300)
   private val to = Companions.varchar("too", 300)
   private val schedule = Companions.varchar("schedule", 150)
   private val actualTripTime = Companions.varchar("actual_trip_time", 150)
   private val ableToPay = Companions.varchar("able_to_pay", 50)
   private  val comment = Companions.varchar("comment", 1000)
   private val active = Companions.integer("active")

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
               it[active] = compForm.active
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
                     queryList[i][comment],
                     queryList[i][active]
                  ))
            }
            readyList
         }
      } catch (e: Exception) {
         e.printStackTrace()
         emptyList()
      }
   }

   fun fetchCompanionForm(name: String) : CompanionDto? {
      return try {
         transaction {
            val query = Companions.select { username eq name }.single()
            CompanionDto(
               query[username],
               query[userImage],
               query[from],
               query[to],
               query[schedule],
               query[actualTripTime],
               query[ableToPay],
               query[comment],
               query[active]
            )
         }
      } catch (e: Exception) {
         e.printStackTrace()
         null
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