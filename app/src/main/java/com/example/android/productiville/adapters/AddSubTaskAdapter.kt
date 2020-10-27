package com.example.android.productiville.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.android.productiville.R
import com.example.android.productiville.subTasks.SubTask
import com.example.android.productiville.subTasks.SubTaskViewHolder
import com.google.android.material.snackbar.Snackbar

class AddSubTaskAdapter(
    val activity: Activity,
    private val deleteItem: OnSubTaskClickListener
): ListAdapter<SubTask, SubTaskViewHolder>(SubTaskDiffCallback()) {

    var subTasks: MutableList<SubTask> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun submitList(list: MutableList<SubTask>?) {
        subTasks = list!!
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return subTasks.size
    }

    override fun getItem(position: Int): SubTask {
        return subTasks[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubTaskViewHolder {
        return SubTaskViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SubTaskViewHolder, position: Int) {
        val subTask = getItem(position)

        holder.bind(subTask)
    }

    fun deletePosition(position: Int) {
        val mRecentlyDeletedItem = subTasks[position]
        subTasks.removeAt(position)
        notifyItemRemoved(position)
        showUndoSnackbar(mRecentlyDeletedItem, position)
    }

    private fun showUndoSnackbar(item: SubTask, position: Int) {
        val view: View = activity.findViewById(R.id.snackbarSwipeEnabler)
        val snackbar: Snackbar = Snackbar.make(
            view, "Subtask Deleted",
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction("undo") { undoDelete(item, position) }
        snackbar.addCallback(object: Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                if(event == DISMISS_EVENT_CONSECUTIVE ||
                    event == DISMISS_EVENT_SWIPE ||
                    event == DISMISS_EVENT_TIMEOUT) {
                    deleteItem.clickListener(item)
                }
            }
        })

        snackbar.show()
    }

    private fun undoDelete(item: SubTask, position: Int) {
        subTasks.add(position, item)
        notifyItemInserted(position)
    }
}

class SubTaskDiffCallback: DiffUtil.ItemCallback<SubTask>() {
    override fun areItemsTheSame(oldItem: SubTask, newItem: SubTask): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: SubTask, newItem: SubTask): Boolean {
        return oldItem.subTaskId == newItem.subTaskId
    }
}

class OnSubTaskClickListener(val clickListener: (subTask: SubTask) -> Unit) {
    fun onClick(subTask: SubTask) = clickListener(subTask)
}