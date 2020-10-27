package com.example.android.productiville.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.example.android.productiville.R
import com.example.android.productiville.calendarApiService.Event
import com.example.android.productiville.databinding.TaskItemBinding

class TaskItemViewHolder(private var viewDataBinding: TaskItemBinding):
    RecyclerView.ViewHolder(viewDataBinding.root) {
    fun bind(event: Event) {
        viewDataBinding.event = event

        viewDataBinding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): TaskItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = TaskItemBinding.inflate(layoutInflater, parent, false)
            return TaskItemViewHolder(binding)
        }
    }
}