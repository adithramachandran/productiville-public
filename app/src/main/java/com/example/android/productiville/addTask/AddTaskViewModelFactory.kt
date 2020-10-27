package com.example.android.productiville.addTask

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AddTaskViewModelFactory(private val token: String, private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddTaskViewModel(token, application) as T
    }
}