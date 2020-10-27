package com.example.android.productiville.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.android.productiville.R
import com.example.android.productiville.calendarApiService.Event
import com.example.android.productiville.subTasks.SubTask
import com.example.android.productiville.subTasks.SubTaskViewHolder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.sub_task.view.*
import kotlinx.android.synthetic.main.task_item.view.*


class WeeklyViewAdapter(
    val activity: Activity,
    val deleteItem: OnClickListener,
    private val navToEdit: OnClickListener,
    private val updateEventInDatabase: OnClickListener
): ListAdapter<Event, TaskItemViewHolder>(TaskItemDiffCallback()){

    private var currentOpenTask: Pair<TaskItemViewHolder, Event>? = null

    var tasks: MutableList<Event> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskItemViewHolder {
        return TaskItemViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    override fun getItem(position: Int): Event {
        return tasks[position]
    }

    override fun submitList(list: MutableList<Event>?) {
        tasks = list!!
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TaskItemViewHolder, position: Int) {

        val task = getItem(position)

        holder.itemView.setOnClickListener {
            currentOpenTask?.let {
                hideEditPanel(it.first, it.second)
            }
            showEditPanel(holder)
            currentOpenTask = Pair(holder, task)
        }
        holder.itemView.closeEditPanelButton.setOnClickListener {
            hideEditPanel(holder, task)
            currentOpenTask = null
        }
        holder.itemView.editTaskButton.setOnClickListener {
            navToEdit.clickListener(task)
        }
        holder.itemView.addSubTaskButton.setOnClickListener {
            val subTask = SubTask("${task.id}-${task.subTasks.size}", "new subtask", false)
            task.subTasks.add(subTask)
            val viewHolder = SubTaskViewHolder.from(holder.itemView.subTasks)
            viewHolder.bind(subTask)
            holder.itemView.subTasks.addView(viewHolder.itemView)
        }
        for(subTask in task.subTasks) {
            val viewHolder = SubTaskViewHolder.from(holder.itemView.subTasks)
            viewHolder.bind(subTask)
            holder.itemView.subTasks.addView(viewHolder.itemView)
        }
        holder.bind(task)
    }

    private fun showEditPanel(holder: TaskItemViewHolder) {
        holder.itemView.editTaskButton.visibility = View.VISIBLE
        holder.itemView.addSubTaskButton.visibility = View.VISIBLE
        holder.itemView.closeEditPanelButton.visibility = View.VISIBLE
        holder.itemView.expandableLayout.visibility = View.VISIBLE
        holder.itemView.taskName.visibility = View.GONE
        holder.itemView.startTime.visibility = View.GONE
        holder.itemView.dayOfWeek.visibility = View.GONE
    }

    private fun hideEditPanel(holder: TaskItemViewHolder, event: Event) {
        holder.itemView.editTaskButton.visibility = View.GONE
        holder.itemView.addSubTaskButton.visibility = View.GONE
        holder.itemView.closeEditPanelButton.visibility = View.GONE
        holder.itemView.expandableLayout.visibility = View.GONE
        holder.itemView.taskName.visibility = View.VISIBLE
        holder.itemView.startTime.visibility = View.VISIBLE
        holder.itemView.dayOfWeek.visibility = View.VISIBLE
        updateEventInDatabase.clickListener(event)
    }

    fun deletePosition(position: Int) {
        val mRecentlyDeletedItem = tasks[position]
        tasks.removeAt(position)
        notifyItemRemoved(position)
        showUndoSnackbar(mRecentlyDeletedItem, position)
    }

    private fun showUndoSnackbar(item: Event, position: Int) {
        val view: View = activity.findViewById(R.id.snackbarSwipeEnabler)
        val snackbar: Snackbar = Snackbar.make(
            view, "Task Deleted",
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

    private fun undoDelete(item: Event, position: Int) {
        tasks.add(position, item)
        notifyItemInserted(position)
    }

    fun closeAllEvents() {
        currentOpenTask?.let {
            hideEditPanel(it.first, it.second)
        }
    }
}

class TaskItemDiffCallback: DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }
}

class OnClickListener(val clickListener: (event: Event) -> Unit) {
    fun onClick(event: Event) = clickListener(event)
}