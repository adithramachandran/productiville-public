package com.example.android.productiville.subTasks

import android.annotation.SuppressLint
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.android.productiville.R
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.android.synthetic.main.sub_task.view.*

@BindingAdapter("setSubTaskName")
fun EditText.setSubTaskName(subTask: SubTask?) {
    subTask?.let {
        setText(subTask.name)
        if(subTask.isComplete) {
            setTextColor(ContextCompat.getColor(context, R.color.greyedOutTextSubTask))
        } else {
            setTextColor(ContextCompat.getColor(context, R.color.normalTextSubTask))
        }
    }
}

@BindingAdapter("checkBoxStatus")
fun MaterialCheckBox.setCheckedStatus(subTask: SubTask?) {
    subTask?.let {
        isChecked = subTask.isComplete
    }
}