package com.example.android.productiville.subTasks

import androidx.room.TypeConverter

class SubTaskConverter {

    private fun fromSubTask(subTask: SubTask): String {
        return "startsuperTaskId${subTask.subTaskId}name${subTask.name}isComplete${subTask.isComplete}isCompleteEndend"
    }

    private fun toSubTask(subTask: String): SubTask {
        return SubTask(
            subTask.substringAfter("superTaskId").substringBefore("name"),
            subTask.substringAfter("name").substringBefore("isComplete"),
            subTask.substringAfter("isComplete").substringBefore("isCompleteEnd").toBoolean()
        )
    }

    @TypeConverter
    fun fromSubTaskSet(subTaskSet: MutableList<SubTask>): String {
        var result = ""
        for (subTask in subTaskSet) {
            result += fromSubTask(subTask)
        }
        return result
    }

    @TypeConverter
    fun toSubTaskSet(subTaskSet: String): MutableList<SubTask> {
        val result: MutableList<SubTask> = mutableListOf<SubTask>()
        var fromDatabase = subTaskSet
        while(fromDatabase.isNotEmpty()) {
            result.add(toSubTask(fromDatabase.substringAfter("start").substringBefore("end")))
            fromDatabase = fromDatabase.substringAfter("end")
        }
        return result
    }
}