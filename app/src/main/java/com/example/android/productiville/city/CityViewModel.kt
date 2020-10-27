package com.example.android.productiville.city

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CityViewModel: ViewModel() {

    private val _navigateToWeekly = MutableLiveData<Boolean>()
    val navigateToWeekly: LiveData<Boolean>
        get() = _navigateToWeekly


    fun onClickWeekly() {
        _navigateToWeekly.value = true
    }

    fun weeklyNavDone() {
        _navigateToWeekly.value = null
    }
}