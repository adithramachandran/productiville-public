package com.example.android.productiville.taskDatabase

import androidx.room.TypeConverter
import com.example.android.productiville.calendarApiService.EventDateTime

class EventDateTimeConverter {
    @TypeConverter
    fun fromEventDateTime(eventDateTime: EventDateTime): String {
        return eventDateTime.dateTime
    }

    @TypeConverter
    fun toEventDateTime(dateTime: String): EventDateTime {
        return EventDateTime(dateTime)
    }
}