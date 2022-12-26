package ru.earl.models.tripNotifications

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object TripNotification : Table("trip_notifications") {

    private val notificationId = TripNotification.varchar("id", 36)
    private val authorUsername = TripNotification.varchar("authorUsername", 50)
    private val receiverUsername = TripNotification.varchar("receiverUsername", 50)
    private val authorTripRole = TripNotification.varchar("authorTripRole", 20)
    private val receiverTripRole = TripNotification.varchar("receiverTripRole", 20)
    private val invite = TripNotification.integer("invite")
    private val timestamp = TripNotification.varchar("timestamp", 100)

    fun insertNewNotification(notification: TripNotificationsDto) {
        try {
            transaction {
                TripNotification.insert {
                    it[notificationId] = notification.id
                    it[authorUsername] = notification.authorName
                    it[receiverUsername] = notification.receiverName
                    it[authorTripRole] = notification.authorTripRole
                    it[receiverTripRole] = notification.receiverTripRole
                    it[invite] = notification.isInvite
                    it[timestamp] = notification.timestamp
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
                            queryListForAuthor[i][invite],
                            queryListForAuthor[i][timestamp],
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
                            queryListForReceiver[i][invite],
                            queryListForReceiver[i][timestamp],
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
}