package com.example.android.productiville.calendarApiService

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.productiville.subTasks.SubTask
import com.squareup.moshi.Json
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "task_database")
data class Event(
    @Json(name = "id")
    @PrimaryKey(autoGenerate = false)
    val id: String,

    @Json(name = "summary")
    @ColumnInfo(name = "task_name")
    var name: String = "no title",

    @Json(name = "start")
    @ColumnInfo(name = "task_start")
    val start: EventDateTime,

    @Json(name = "end")
    @ColumnInfo(name = "task_end")
    val end: EventDateTime,

    @Json(name = "recurringEventId")
    @ColumnInfo(name = "recurrenceId")
    val recurrenceId: String = "",

    @ColumnInfo(name = "is_super_task")
    var isSuperTask: Boolean = true,

    @ColumnInfo(name = "sub_tasks")
    val subTasks: MutableList<SubTask> = mutableListOf()
): Parcelable