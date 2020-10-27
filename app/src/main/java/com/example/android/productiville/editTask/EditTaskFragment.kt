package com.example.android.productiville.editTask

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.productiville.R
import com.example.android.productiville.adapters.AddSubTaskAdapter
import com.example.android.productiville.adapters.OnSubTaskClickListener
import com.example.android.productiville.adapters.SwipeToDeleteCallbackAddEdit
import com.example.android.productiville.calendarApiService.Event
import com.example.android.productiville.databinding.EditTaskFragmentBinding
import com.example.android.productiville.utils.Constants
import com.example.android.productiville.utils.setSafeOnClickListener
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.api.client.util.DateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class EditTaskFragment: Fragment() {

    private val viewModel: EditTaskViewModel by lazy {
        val activity = requireNotNull(this.activity) {

        }
        ViewModelProviders.of(
            this, EditTaskViewModelFactory(
                arguments?.getString("token")!!,
                arguments?.getParcelable<Event>("editEvent")!!,
                activity.application)
        )
            .get(EditTaskViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = DataBindingUtil.inflate<EditTaskFragmentBinding>(
            inflater, R.layout.edit_task_fragment, container, false)

        binding.setLifecycleOwner(this)

        binding.viewModel = viewModel

        binding.taskTitle.setText(viewModel.title.value)

        val subTasksAdapter =
            AddSubTaskAdapter(
                activity!!,
                OnSubTaskClickListener {
                    viewModel.deleteSubTask(it)
                }
            )

        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallbackAddEdit(subTasksAdapter))

        binding.subTasks.apply {
            adapter = subTasksAdapter
            layoutManager = LinearLayoutManager(context)
            itemTouchHelper.attachToRecyclerView(this)
        }

        setOnClickListeners(binding)

        setObservers(binding, subTasksAdapter)

        return binding.root

    }

    private fun setOnClickListeners(binding: EditTaskFragmentBinding) {
        binding.startTime.setSafeOnClickListener {
            showTimePicker(it)
        }

        binding.endTime.setSafeOnClickListener {
            showTimePicker(it)
        }

        binding.startDate.setSafeOnClickListener {
            showDatePicker(it)
        }

        binding.endDate.setSafeOnClickListener {
            showDatePicker(it)
        }

        binding.cancelButton.setSafeOnClickListener {
            viewModel.navigateToWeekly()
        }

        binding.addSubTaskButton.setSafeOnClickListener {
            viewModel.addNewSubTask()
        }

        binding.editTaskButton.setSafeOnClickListener {
            it.isClickable = false
            viewModel.setTitle(binding.taskTitle.text.toString())
            viewModel.addToCalendar()
        }
    }

    private fun setObservers(binding: EditTaskFragmentBinding, subTaskAdapter: AddSubTaskAdapter) {
        viewModel.startDate.observe(this, Observer {
            setStartDateText(binding, ZonedDateTime.parse(DateTime(it).toString()))
        })

        viewModel.endDate.observe(this, Observer {
            setEndDateText(binding, ZonedDateTime.parse(DateTime(it).toString()))
        })

        viewModel.startTime.observe(this, Observer {
            setStartTimeText(binding, it)
        })

        viewModel.endTime.observe(this, Observer {
            setEndTimeText(binding, it)
        })

        viewModel.subTasks.observe(this, Observer {
            if (it.isNotEmpty()) {
                binding.addSomeSubTasks.visibility = View.GONE
                subTaskAdapter.subTasks = it
            } else {
                binding.addSomeSubTasks.visibility = View.VISIBLE
            }
        })

        viewModel.navToWeekly.observe(this, Observer {
            if(it == true) {
                val bundle = Bundle()
                bundle.putString("token", arguments?.getString("token")!!)
                findNavController().navigate(R.id.action_editTaskFragment_to_weeklyFragment, bundle)
                viewModel.navigateToWeeklyDone()
            }
        })
    }

    private fun showDatePicker(view: View) {

        val picker = MaterialDatePicker.Builder.datePicker().build()

        picker.addOnPositiveButtonClickListener {
            val fixedEpoch = it + Constants.MILLIS_IN_DAY
            when(view.id) {
                R.id.startDate -> {
                    viewModel.setStartDate(fixedEpoch)
                }
                R.id.endDate -> {
                    viewModel.setEndDate(fixedEpoch)
                }
            }
        }
        picker.show(fragmentManager!!, picker.toString())
    }

    private fun showTimePicker(view: View) {
        val currentTime = ZonedDateTime.now()
        val listener = { _: TimePicker, hourOfDay: Int, minute: Int ->
            when(view.id) {
                R.id.startTime -> {
                    viewModel.setStartTime(Pair(hourOfDay, minute))
                }
                R.id.endTime -> {
                    viewModel.setEndTime(Pair(hourOfDay, minute))
                }
                else -> println("Called from an unknown view")
            }
        }
        val timePicker= TimePickerDialog(context, listener, currentTime.hour, currentTime.minute, false)
        timePicker.show()
    }

    private fun setStartDateText(binding: EditTaskFragmentBinding, currentTime: ZonedDateTime) {
        binding.startDate.text =
            currentTime.format(DateTimeFormatter.RFC_1123_DATE_TIME).toString().substring(0, 16)
    }

    private fun setEndDateText(binding: EditTaskFragmentBinding, currentTime: ZonedDateTime) {
        binding.endDate.text =
            currentTime.plusMinutes(30).format(DateTimeFormatter.RFC_1123_DATE_TIME).toString()
                .substring(0, 16)
    }

    private fun setStartTimeText(binding: EditTaskFragmentBinding, hoursAndMinutes: Pair<Int, Int>) {
        var minutesFormatted = "0${hoursAndMinutes.second}"
        minutesFormatted = minutesFormatted.substring(minutesFormatted.length - 2, minutesFormatted.length)
        binding.startTime.text = "${hoursAndMinutes.first}:$minutesFormatted"
    }

    private fun setEndTimeText(binding: EditTaskFragmentBinding, hoursAndMinutes: Pair<Int, Int>) {
        var minutesFormatted = "0${hoursAndMinutes.second}"
        minutesFormatted = minutesFormatted.substring(minutesFormatted.length - 2, minutesFormatted.length)
        binding.endTime.text = "${hoursAndMinutes.first}:$minutesFormatted"
    }
}