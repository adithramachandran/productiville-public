package com.example.android.productiville.editTask

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.ZonedDateTime

class EditTaskViewModel(private val token: String, private val editEvent: Event, application: Application)
    : AndroidViewModel(application) {

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

    private val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title

    private val _subTasks = MutableLiveData<MutableList<SubTask>>()
    val subTasks: LiveData<MutableList<SubTask>>
        get() = _subTasks

    private val viewModelJob = Job()

    private val coroutineScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    init {
        val startDateTime = ZonedDateTime.parse(editEvent.start.dateTime)
        val endDateTime = ZonedDateTime.parse(editEvent.end.dateTime)
        setStartDate(startDateTime.toEpochSecond() * 1000L)
        setEndDate(endDateTime.toEpochSecond() * 1000L)
        setStartTime(Pair(startDateTime.hour, startDateTime.minute))
        setEndTime(Pair(endDateTime.hour, endDateTime.minute))
        setTitle(editEvent.name)
        _subTasks.value = editEvent.subTasks
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

    fun setTitle(title: String) {
        _title.value = title
    }

    fun addNewSubTask() {
        val intermediateList = _subTasks.value
        intermediateList?.add(SubTask("${editEvent.id}-${intermediateList.size}", "new subtask", false))
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
        val title = _title.value!!

        if (startDateTime.isBefore(endDateTime)) {
            coroutineScope.launch {
                val editedEvent = Event(
                    id = editEvent.id,
                    name = title,
                    start = EventDateTime(startDateTime.toString()),
                    end = EventDateTime(endDateTime.toString()),
                    recurrenceId = editEvent.recurrenceId,
                    isSuperTask = editEvent.isSuperTask,
                    subTasks = editEvent.subTasks
                )
                database.taskDatabaseDao.updateEvent(editedEvent)
                CalendarApi.retrofitService.updateEvent(
                    editEvent.id,
                    PostEvent.eventAsPostEvent(editedEvent),
                    "Bearer $token"
                ).enqueue(object: Callback<Void> {
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(getApplication(), "Event edit failed", Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        _navToWeekly.postValue(true)
                    }

                })
            }
        } else {
            Toast.makeText(getApplication(), "Start date must be before end date", Toast.LENGTH_LONG).show()
        }
    }
}