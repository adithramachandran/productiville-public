package com.example.android.productiville.addTask

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.example.android.productiville.calendarApiService.CalendarApi
import com.example.android.productiville.calendarApiService.Event
import com.example.android.productiville.calendarApiService.EventDateTime
import com.example.android.productiville.calendarApiService.PostEvent
import com.example.android.productiville.subTasks.SubTask
import com.example.android.productiville.taskDatabase.TaskDatabase
import com.google.api.client.util.DateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.internal.waitMillis
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.ZonedDateTime
import java.util.*

class AddTaskViewModel(private val token: String, application: Application): AndroidViewModel(application) {

    private val id = UUID.randomUUID().toString().replace("-", "")

    private val currentTime: ZonedDateTime = ZonedDateTime.parse(DateTime(System.currentTimeMillis()).toString())

    private val roundedTime: ZonedDateTime = currentTime.plusMinutes((15 - (currentTime.minute % 15)).toLong())

    private val roundedEndTime = roundedTime.plusMinutes(30)

    private val database = TaskDatabase.getInstance(application)

    private val _navToWeekly = MutableLiveData<Boolean>()
    val navToWeekly: LiveData<Boolean>
        get() = _navToWeekly

    private val _startDate = MutableLiveData<Long>()
    val startDate: LiveData<Long>
        get() = _startDate

    private val _endDate = MutableLiveData<Long>()
    val endDate: LiveData<Long>
        get() = _endDate

    private val _startTime = MutableLiveData<Pair<Int, Int>>()
    val startTime: LiveData<Pair<Int, Int>>
        get() = _startTime

    private val _endTime = MutableLiveData<Pair<Int, Int>>()
    val endTime: LiveData<Pair<Int, Int>>
        get() = _endTime

    private val _recurrenceEndDate = MutableLiveData<Long>()
    val recurrenceEndDate: LiveData<Long>
        get() = _recurrenceEndDate

    private val _subTasks = MutableLiveData<MutableList<SubTask>>()
    val subTasks: LiveData<MutableList<SubTask>>
        get() = _subTasks

    private val _count = MutableLiveData<String>()

    private val _title = MutableLiveData<String>()

    private val _selectedDays = MutableLiveData<List<MaterialDayPicker.Weekday>>()

    private val viewModelJob = Job()

    private val coroutineScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    init {
        val roundedSystemTime = System.currentTimeMillis() + System.currentTimeMillis() % 900000
        setStartDate(roundedSystemTime)
        setEndDate(roundedSystemTime + 1800000)
        setStartTime(Pair(roundedTime.hour, roundedTime.minute))
        setEndTime(Pair(roundedEndTime.hour, roundedEndTime.minute))
        setSelectedDays(listOf())
        setTitle("")
        setCount("")
        setRecurrenceEndDate(0)
        _subTasks.value = mutableListOf()
    }

    fun navigateToWeekly() {
        _navToWeekly.value = true
    }

    fun navigateToWeeklyDone() {
        _navToWeekly.value = null
    }

    fun setStartDate(epoch: Long) {
        _startDate.value = epoch
    }

    fun setEndDate(epoch: Long) {
        _endDate.value = epoch
    }

    fun setStartTime(pair: Pair<Int, Int>) {
        _startTime.value = pair
    }

    fun setEndTime(pair: Pair<Int, Int>) {
        _endTime.value = pair
    }

    fun setRecurrenceEndDate(epoch: Long) {
        _recurrenceEndDate.value = epoch
    }

    fun setTitle(title: String) {
        _title.value = title
    }

    fun setSelectedDays(days: List<MaterialDayPicker.Weekday>) {
        _selectedDays.value = days
    }

    fun setCount(count: String) {
        _count.value = count
    }

    fun addNewSubTask() {
        val intermediateList = _subTasks.value
        intermediateList?.add(SubTask("$id-${intermediateList.size}", "new subtask", false))
        _subTasks.value = intermediateList
    }

    fun deleteSubTask(subTask: SubTask) {
        val intermediateList = _subTasks.value
        intermediateList?.remove(subTask)
        _subTasks.value = intermediateList
    }

    fun addToCalendar() {
        val startDateTime =
            ZonedDateTime.parse(DateTime(_startDate.value!!).toString())
                .withHour(_startTime.value!!.first)
                .withMinute(_startTime.value!!.second)
                .withSecond(1)
                .withNano(0)
        val endDateTime =
            ZonedDateTime.parse(DateTime(_endDate.value!!).toString())
                .withHour(_endTime.value!!.first)
                .withMinute(_endTime.value!!.second)
                .withSecond(1)
                .withNano(0)
        val recurrenceEndDateTime =
            ZonedDateTime.parse(DateTime(_recurrenceEndDate.value!!).toString())
                .withSecond(1)
                .withNano(0)
        val title = if(_title.value!! == "") "no title" else _title.value

        if (startDateTime.isBefore(endDateTime)) {
            if (_selectedDays.value!!.isEmpty()) {
                coroutineScope.launch {
                    val newEvent = Event(
                        id = id,
                        name = title!!,
                        start = EventDateTime(startDateTime.toString()),
                        end = EventDateTime(endDateTime.toString()),
                        subTasks = _subTasks.value!!
                    )
                    database.taskDatabaseDao.addEvent(newEvent)
                    CalendarApi.retrofitService.insertEvent(
                        PostEvent.eventAsPostEvent(newEvent),
                        "Bearer $token"
                    ).enqueue(object: Callback<Void> {
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(getApplication(), "Add Task Failed", Toast.LENGTH_SHORT).show()
                        }

                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            _navToWeekly.postValue(true)
                        }
                    })
                }
            } else {
                if (_count.value == "" && _endDate.value == 0L) {
                    Toast.makeText(
                        getApplication(),
                        "Select either end date or count",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    coroutineScope.launch {
                        var rrule =
                            "RRULE:FREQ=WEEKLY;${if (_count.value != "0") "COUNT=${_count.value}"
                            else if(_recurrenceEndDate.value!! == 0L) "COUNT=730" 
                            else "UNTIL=$recurrenceEndDateTime"};BYDAY="
                        for(index in 0.._selectedDays.value!!.size - 1) {
                            rrule += _selectedDays.value!![index].name.substring(0,2)
                            if(index < _selectedDays.value!!.size - 1) {
                                rrule += ","
                            }
                        }
                        val newEvent = Event(
                            id = id,
                            name = title!!,
                            start = EventDateTime(startDateTime.toString()),
                            end = EventDateTime(endDateTime.toString())
                        )
                        database.taskDatabaseDao.addEvent(newEvent)
                        CalendarApi.retrofitService.insertEvent(PostEvent.eventAsPostEventRrule(newEvent, rrule), "Bearer $token")

                    }
                }
            }
        } else {
            Toast.makeText(getApplication(), "Start date must be before end date", Toast.LENGTH_LONG).show()
        }
    }
}