package com.example.android.productiville.adapters

import android.widget.*
import androidx.databinding.BindingAdapter
import com.example.android.productiville.calendarApiService.Event
import java.time.ZonedDateTime

@BindingAdapter("taskName")
fun TextView.setTaskName(event: Event?) {
    event?.let {
        text = event.name
    }
}

@BindingAdapter("taskDay")
fun TextView.setTaskDay(event: Event?) {
    event?.let {
        val startTime = ZonedDateTime.parse(event.start.dateTime)
        var dayOfWeek = startTime.dayOfWeek.toString()
        dayOfWeek = dayOfWeek.substring(0, 1) + dayOfWeek.substring(1).toLowerCase()
        text =  dayOfWeek
    }
}

@BindingAdapter("startTime")
fun TextView.setStartTime(event: Event?) {
    event?.let {
        val startTime = ZonedDateTime.parse(event.start.dateTime)
            .withNano(0).toString()
            .substringAfter("T").substringBefore("Z").substring(0,5)
        text = startTime
    }
}