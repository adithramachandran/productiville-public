package com.example.android.productiville.weekly

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WeeklyViewModelFactory(private val token: String, private val application: Application): ViewModelProvider.Factory {
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WeeklyViewModel(token, application) as T
    }
}