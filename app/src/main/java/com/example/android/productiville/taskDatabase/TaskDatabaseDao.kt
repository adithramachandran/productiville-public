package com.example.android.productiville.taskDatabase

import androidx.room.*
import com.example.android.productiville.calendarApiService.Event
import com.example.android.productiville.calendarApiService.EventDateTime

@Dao
interface TaskDatabaseDao {

    @Insert
    fun addEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAll(vararg events: Event)

    @Update
    fun updateEvent(event: Event): Int

    @Query("delete from task_database where id=:id")
    fun deleteEvent(id: String)

    @Query("delete from task_database where task_end<=:eventDateTime")
    fun deleteExpired(eventDateTime: EventDateTime)

    @Query("select * from task_database order by task_start")
    fun getEvents(): MutableList<Event>

    @Query("delete from task_database")
    fun deleteAll()
}