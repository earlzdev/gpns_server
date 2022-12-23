package ru.earl.models.drivers

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Drivers : Table("drivers") {

    private val username = Drivers.varchar("username", 50)
    private val userImage = Drivers.varchar("user_image", 2500)
    private val driveFrom = Drivers.varchar("from", 100)
    private val driveTo = Drivers.varchar("to", 100)
    private val catchCompanionFrom = Drivers.varchar("can_catch_comp_from", 100)
    private val alsoCanDriveTo = Drivers.varchar("also_can_drive_to", 100)
    private val schedule = Drivers.varchar("schedule", 100)
    private val ableToDriveInTurn = Drivers.integer("able_to_drive_in_turn")
    private val actualTripTime = Drivers.varchar("actual_trip_time", 100)
    private val car = Drivers.varchar("car", 30)
    private val carModel = Drivers.varchar("car_model", 30)
    private val carColor = Drivers.varchar("car_color", 30)
    private val passengersCount = Drivers.integer("count_of_passengers")
    private val carGovNumber = Drivers.varchar("car_gov_number", 20)
    private val tripPrice = Drivers.integer("trip_price")
    private val driverComment = Drivers.varchar("driver_comment", 1000)

    fun insertNewDriverForm(driverForm: DriverDto) {
        try {
            transaction {
                Drivers.insert {
                    it[username] = driverForm.username
                    it[userImage] = driverForm.userImage
                    it[driveFrom] = driverForm.driveFrom
                    it[driveTo] = driverForm.driveTo
                    it[catchCompanionFrom] = driverForm.catchCompanionFrom
                    it[alsoCanDriveTo] = driverForm.alsoCanDriveTo
                    it[schedule] = driverForm.schedule
                    it[ableToDriveInTurn] = driverForm.ableToDriveInTurn
                    it[actualTripTime] = driverForm.actualTripTime
                    it[car] = driverForm.car
                    it[carModel] = driverForm.carModel
                    it[carColor] = driverForm.carColor
                    it[passengersCount] = driverForm.passengersCount
                    it[carGovNumber] = driverForm.carGovNumber
                    it[tripPrice] = driverForm.tripPrice
                    it[driverComment] = driverForm.driverComment
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchAllDrivers() : List<DriverDto> {
        return try {
            transaction {
                val resultQuery = Drivers.selectAll().toList()
                val readyList = mutableListOf<DriverDto>()
                for (i in resultQuery.indices) {
                    readyList.add(
                        DriverDto(
                            resultQuery[i][username],
                            resultQuery[i][userImage],
                            resultQuery[i][driveFrom],
                            resultQuery[i][driveTo],
                            resultQuery[i][catchCompanionFrom],
                            resultQuery[i][alsoCanDriveTo],
                            resultQuery[i][schedule],
                            resultQuery[i][ableToDriveInTurn],
                            resultQuery[i][actualTripTime],
                            resultQuery[i][car],
                            resultQuery[i][carModel],
                            resultQuery[i][carColor],
                            resultQuery[i][passengersCount],
                            resultQuery[i][carGovNumber],
                            resultQuery[i][tripPrice],
                            resultQuery[i][driverComment],
                        ))
                }
                readyList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}