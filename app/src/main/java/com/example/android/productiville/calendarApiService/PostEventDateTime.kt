package com.example.android.productiville.calendarApiService

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.time.ZonedDateTime

@Parcelize
data class PostEventDateTime(
    @Json(name = "dateTime")
    var dateTime: String,
    @Json(name = "timeZone")
    val timezone: String = ZonedDateTime.parse(dateTime).zone.toString()
) : Parcelable, Comparable<EventDateTime> {
    override fun compareTo(other: EventDateTime): Int {
        return ZonedDateTime.parse(dateTime).compareTo(ZonedDateTime.parse(other.dateTime))
    }
}