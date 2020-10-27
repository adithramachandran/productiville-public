package com.example.android.productiville.calendarApiService

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EventResponse(
    @Json(name = "items")
    val items: List<Event>
) : Parcelable

//fun EventResponse.asDatabaseModel(): Array<Event> {
//    return items.map {
//        Event(
//            id = it.id,
//            name = it.name,
//            start = it.start,
//            end = it.end,
//            recurrenceId = it.recurrenceId
//        )
//    }.toTypedArray()
//}