package com.example.android.productiville.subTasks

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.android.productiville.R
import com.example.android.productiville.databinding.SubTaskBinding
import kotlinx.android.synthetic.main.sub_task.view.*

class SubTaskViewHolder(private var viewDataBinding: SubTaskBinding):
        RecyclerView.ViewHolder(viewDataBinding.root) {
    fun bind(subTask: SubTask) {
        viewDataBinding.subTask = subTask
        val imm = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        itemView.subTaskName.setOnKeyListener { _, keyCode, _ ->
            if(keyCode == KeyEvent.KEYCODE_ENTER) {
                subTask.name = itemView.subTaskName.text.toString()
                imm.hideSoftInputFromWindow(itemView.subTaskName.applicationWindowToken, 0)
                itemView.subTaskName.clearFocus()
                true
            } else {
                false
            }
        }

        itemView.materialCheckBox.setOnCheckedChangeListener { _, isChecked ->
            subTask.isComplete = isChecked
            if(isChecked) {
                itemView.subTaskName.setTextColor(ContextCompat.getColor(itemView.context, R.color.greyedOutTextSubTask))
            } else {
                itemView.subTaskName.setTextColor(ContextCompat.getColor(itemView.context, R.color.normalTextSubTask))
            }
        }

        viewDataBinding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): SubTaskViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = SubTaskBinding.inflate(layoutInflater, parent, false)
            return SubTaskViewHolder(binding)
        }
    }
}