package com.example.android.productiville.calendarApiService

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostEvent(
    @Json(name = "id")
    val id: String,

    @Json(name = "summary")
    var name: String = "no title",

    @Json(name = "start")
    val start: PostEventDateTime,

    @Json(name = "end")
    val end: PostEventDateTime,

    @Json(name = "recurrence")
    val rrule: List<String> = listOf()
): Parcelable {
    companion object {
        fun eventAsPostEvent(event: Event): PostEvent {
            return PostEvent(
                id = event.id,
                name = event.name,
                start = PostEventDateTime(event.start.dateTime),
                end = PostEventDateTime(event.end.dateTime)
            )
        }

        fun eventAsPostEventRrule(event: Event, rrule: String): PostEvent {
            return PostEvent(
                id = event.id,
                name = event.name,
                start = PostEventDateTime(event.start.dateTime),
                end = PostEventDateTime(event.end.dateTime),
                rrule = listOf(rrule)
            )
        }
    }
}