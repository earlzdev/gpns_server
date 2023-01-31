package ru.earl.models.drivers

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Drivers : Table("drivers") {

    private val username = Drivers.varchar("username", 50)
    private val userImage = Drivers.varchar("user_image", 2500)
    private val driveFrom = Drivers.varchar("from", 100)
    private val driveTo = Drivers.varchar("to", 100)
    private val catchCompanionFrom = Drivers.varchar("can_catch_comp_from", 200)
    private val alsoCanDriveTo = Drivers.varchar("also_can_drive_to", 200)
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
    private val active = Drivers.integer("active")

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
                    it[active] = driverForm.active
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
                            resultQuery[i][active]
                        ))
                }
                readyList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun fetchDriverForm(name: String) : DriverDto? {
        return try {
            transaction {
                val all = Drivers.selectAll().toList()
                val ready = mutableListOf<DriverDto>()
                 for (i in all.indices) {
                    ready.add(
                        DriverDto(
                            all[i][username],
                            all[i][userImage],
                            all[i][driveFrom],
                            all[i][driveTo],
                            all[i][catchCompanionFrom],
                            all[i][alsoCanDriveTo],
                            all[i][schedule],
                            all[i][ableToDriveInTurn],
                            all[i][actualTripTime],
                            all[i][car],
                            all[i][carModel],
                            all[i][carColor],
                            all[i][passengersCount],
                            all[i][carGovNumber],
                            all[i][tripPrice],
                            all[i][driverComment],
                            all[i][active]
                        )
                    )
                }
                println("all drivers -> $ready")
                println("name -> $name")
                val query = Drivers.select { username.eq(name) }.single()
                DriverDto(
                    query[username],
                    query[userImage],
                    query[driveFrom],
                    query[driveTo],
                    query[catchCompanionFrom],
                    query[alsoCanDriveTo],
                    query[schedule],
                    query[ableToDriveInTurn],
                    query[actualTripTime],
                    query[car],
                    query[carModel],
                    query[carColor],
                    query[passengersCount],
                    query[carGovNumber],
                    query[tripPrice],
                    query[driverComment],
                    query[active]
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteDriverForm(user: String) {
        try {
            transaction {
                Drivers.deleteWhere { username eq user }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}