package com.example.android.productiville.calendarApiService

import android.os.Parcelable
import com.google.api.client.util.DateTime
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.time.ZonedDateTime

@Parcelize
data class EventDateTime(
    @Json(name = "dateTime")
    var dateTime: String
) : Parcelable, Comparable<EventDateTime> {
    override fun compareTo(other: EventDateTime): Int {
        return ZonedDateTime.parse(dateTime).compareTo(ZonedDateTime.parse(other.dateTime))
    }
    companion object {
        fun currentString(): String {
            return DateTime(System.currentTimeMillis()).toString()
        }
    }
}