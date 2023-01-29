package ru.earl.feature.chat

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CurrentDateTimeGiver {

    fun getCurrentDateAsString(): String {
        val currentDate = Date()
        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(currentDate)
    }
}