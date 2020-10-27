package com.example.android.productiville.weekly

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.productiville.calendarApiService.CalendarApi
import com.example.android.productiville.calendarApiService.Event
import com.example.android.productiville.calendarApiService.EventDateTime
import com.example.android.productiville.taskDatabase.TaskDatabase
import kotlinx.coroutines.*

class WeeklyViewModel(
    private val token: String,
    application: Application
): AndroidViewModel(application) {

    private val _events = MutableLiveData<MutableList<Event>>()
    val events: LiveData<MutableList<Event>>
        get() = _events

    private val _navToCity = MutableLiveData<Boolean>()
    val navToCity: LiveData<Boolean>
        get() = _navToCity

    private val _navToAddTask = MutableLiveData<Boolean>()
    val navToAddTask: LiveData<Boolean>
        get() = _navToAddTask

    private val _navToEditTask = MutableLiveData<Event>()
    val navToEditTask: LiveData<Event>
        get() = _navToEditTask

    private val _allDone = MutableLiveData<Boolean>()
    val allDone: LiveData<Boolean>
        get() = _allDone

    private val viewModelJob = Job()

    private val coroutineScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    private val database = TaskDatabase.getInstance(application)

    init {
        getCalendarEvents()
    }

    fun getCalendarEvents() {
        coroutineScope.launch {
            database.taskDatabaseDao.deleteExpired(EventDateTime(EventDateTime.currentString()))
            val getEventsDeferred = CalendarApi.retrofitService.getEvents(authToken = "Bearer $token")
            val oldDatabase = database.taskDatabaseDao.getEvents()
            try {
                val listResult = getEventsDeferred.await().items
                println(listResult)
                val databaseList = rebuildDatabase(listResult, oldDatabase)
                _events.postValue(databaseList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun navigateToCity() {
        _navToCity.value = true
    }

    fun navigateToCityDone() {
        _navToCity.value = null
    }

    fun navigateToAddTask() {
        _navToAddTask.value = true
    }

    fun navigateToAddTaskDone() {
        _navToAddTask.value = null
    }

    fun navigateToEditTask(event: Event) {
        _navToEditTask.value = event
    }

    fun navigateToEditTaskDone() {
        _navToEditTask.value = null
    }

    fun setAllDone(bool: Boolean) {
        _allDone.value = bool
    }

    fun updateEventInDatabase(event: Event) {
        coroutineScope.launch {
            database.taskDatabaseDao.updateEvent(event)
        }
    }

    private fun rebuildDatabase(listResult: List<Event>, databaseList: MutableList<Event>): MutableList<Event> {
        inputFromAPI(listResult, databaseList)
        deleteDiscrepancies(listResult, databaseList)
        return database.taskDatabaseDao.getEvents()
    }

    private fun inputFromAPI(listResult: List<Event>, databaseList: MutableList<Event>) {
        for (item in listResult) {
            if(!databaseList.containsID(item)) {
                database.taskDatabaseDao.addEvent(item)
            }
        }
    }

    private fun deleteDiscrepancies(listResult: List<Event>, databaseList: MutableList<Event>) {
        for (item in databaseList) {
            if(!listResult.containsID(item)) {
                database.taskDatabaseDao.deleteEvent(item.id)
            }
        }
    }

    fun deleteEvent(event: Event) {
        coroutineScope.launch {
            database.taskDatabaseDao.deleteEvent(event.id)
            val deleteEvent = CalendarApi.retrofitService.deleteEvent(event.id, "Bearer $token")
            try {
                deleteEvent.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun List<Event>.containsID(event: Event): Boolean {
    return any { it.id == event.id }
}