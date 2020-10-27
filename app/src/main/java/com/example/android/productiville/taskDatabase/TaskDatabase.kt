package com.example.android.productiville.taskDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.android.productiville.calendarApiService.Event
import com.example.android.productiville.subTasks.SubTaskConverter

@Database(entities = [Event::class], version = 2, exportSchema = false)
@TypeConverters(EventDateTimeConverter::class, SubTaskConverter::class)
abstract class TaskDatabase: RoomDatabase() {

    abstract val taskDatabaseDao: TaskDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            var instance = INSTANCE
            if(instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
            }

            return instance
        }
    }
}