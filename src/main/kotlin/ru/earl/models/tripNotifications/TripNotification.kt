package ru.earl.models.tripNotifications

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object TripNotification : Table("trip_notifications") {

    private val notificationId = TripNotification.varchar("idd", 36)
    private val authorUsername = TripNotification.varchar("author_name", 50)
    private val receiverUsername = TripNotification.varchar("receiver_name", 50)
    private val authorTripRole = TripNotification.varchar("author_trip_role", 20)
    private val receiverTripRole = TripNotification.varchar("receiver_trip_role", 20)
    private val type = TripNotification.varchar("typee", 50)
    private val timestamp = TripNotification.varchar("timestamp", 100)
    private val active = TripNotification.integer("active")

    fun insertNewNotification(notification: TripNotificationsDto) {
        try {
            transaction {
                TripNotification.insert {
                    it[notificationId] = notification.id
                    it[authorUsername] = notification.authorName
                    it[receiverUsername] = notification.receiverName
                    it[authorTripRole] = notification.authorTripRole
                    it[receiverTripRole] = notification.receiverTripRole
                    it[type] = notification.type
                    it[timestamp] = notification.timestamp
                    it[active] = notification.active
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchAllTripNotificationsForUser(username: String) : List<TripNotificationsDto> {
        return try {
            transaction {
                val queryListForAuthor = TripNotification.select { authorUsername eq username }.toList()
                val queryListForReceiver = TripNotification.select { receiverUsername eq username }.toList()
                val readyList = mutableListOf<TripNotificationsDto>()
                for (i in queryListForAuthor.indices) {
                    readyList.add(
                        TripNotificationsDto(
                            queryListForAuthor[i][notificationId],
                            queryListForAuthor[i][authorUsername],
                            queryListForAuthor[i][receiverUsername],
                            queryListForAuthor[i][authorTripRole],
                            queryListForAuthor[i][receiverTripRole],
                            queryListForAuthor[i][type],
                            queryListForAuthor[i][timestamp],
                            queryListForAuthor[i][active]
                        )
                    )
                }
                for (i in queryListForReceiver.indices) {
                    readyList.add(
                        TripNotificationsDto(
                            queryListForReceiver[i][notificationId],
                            queryListForReceiver[i][authorUsername],
                            queryListForReceiver[i][receiverUsername],
                            queryListForReceiver[i][authorTripRole],
                            queryListForReceiver[i][receiverTripRole],
                            queryListForReceiver[i][type],
                            queryListForReceiver[i][timestamp],
                            queryListForReceiver[i][active],
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

    fun markTripNotificationAsNotActive(id: String) {
        try {
            transaction {
                TripNotification.update({ notificationId.eq(id) }) {
                    it[active] = 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}