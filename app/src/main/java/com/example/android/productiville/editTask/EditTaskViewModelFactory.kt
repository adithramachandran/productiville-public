package com.example.android.productiville.editTask

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.productiville.calendarApiService.Event

class EditTaskViewModelFactory(private val token: String, private val editEvent: Event, private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return EditTaskViewModel(token, editEvent, application) as T
    }
}