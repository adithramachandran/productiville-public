package com.example.android.productiville.subTasks

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubTask (
    val subTaskId: String,
    var name: String,
    var isComplete: Boolean
): Parcelable
