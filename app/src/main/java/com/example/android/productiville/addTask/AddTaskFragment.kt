package com.example.android.productiville.addTask

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TimePicker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.example.android.productiville.R
import com.example.android.productiville.adapters.AddSubTaskAdapter
import com.example.android.productiville.adapters.OnSubTaskClickListener
import com.example.android.productiville.adapters.SwipeToDeleteCallbackAddEdit
import com.example.android.productiville.databinding.AddTaskFragmentBinding
import com.example.android.productiville.utils.Constants
import com.example.android.productiville.utils.setSafeOnClickListener
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.api.client.util.DateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AddTaskFragment: Fragment() {

    private val viewModel: AddTaskViewModel by lazy {
        val activity = requireNotNull(this.activity) {

        }
        ViewModelProviders.of(
            this, AddTaskViewModelFactory(arguments?.getString("token")!!, activity.application)
        )
            .get(AddTaskViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = DataBindingUtil.inflate<AddTaskFragmentBinding>(
            inflater, R.layout.add_task_fragment, container, false)

        binding.setLifecycleOwner(this)

        binding.viewModel = viewModel

        initializeClickListeners(binding)

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

        initializeObservers(binding, subTasksAdapter)

        return binding.root
    }

    private fun initializeObservers(binding: AddTaskFragmentBinding, subTaskAdapter: AddSubTaskAdapter) {
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
                binding.dayPicker.visibility = View.GONE
                subTaskAdapter.subTasks = it
            } else {
                binding.addSomeSubTasks.visibility = View.VISIBLE
                binding.dayPicker.visibility = View.VISIBLE
            }
        })

        viewModel.recurrenceEndDate.observe(this, Observer {
            if (it == 0L) {
                binding.recurrenceEndDate.text = "(forever)"
            } else {
                setRecurrenceEndDateText(binding, ZonedDateTime.parse(DateTime(it).toString()))
            }
        })

        viewModel.navToWeekly.observe(this, Observer {
            if (it == true) {
                val bundle = Bundle()
                bundle.putAll(arguments)
                findNavController().navigate(R.id.action_addTaskFragment_to_weeklyFragment, bundle)
                viewModel.navigateToWeeklyDone()
            }
        })
    }

    private fun initializeClickListeners(binding: AddTaskFragmentBinding) {

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

        binding.dayPicker.dayPressedListener = object: MaterialDayPicker.DayPressedListener {
            override fun onDayPressed(weekday: MaterialDayPicker.Weekday, isSelected: Boolean) {
                if(binding.dayPicker.selectedDays.isEmpty()) {
                    binding.repeatUntil.visibility = View.GONE
                    binding.recurrenceEndDate.visibility = View.GONE
                    binding.orFor.visibility = View.GONE
                    binding.countPicker.visibility = View.GONE
                    binding.occurrences.visibility = View.GONE
                    binding.addSubTaskButton.visibility = View.VISIBLE
                    binding.subTasks.visibility = View.VISIBLE
                    binding.addSomeSubTasks.visibility = View.VISIBLE
                } else {
                    binding.repeatUntil.visibility = View.VISIBLE
                    binding.recurrenceEndDate.visibility = View.VISIBLE
                    binding.orFor.visibility = View.VISIBLE
                    binding.countPicker.visibility = View.VISIBLE
                    binding.occurrences.visibility = View.VISIBLE
                    binding.addSubTaskButton.visibility = View.GONE
                    binding.subTasks.visibility = View.GONE
                    binding.addSomeSubTasks.visibility = View.GONE
                }
            }

        }

        binding.recurrenceEndDate.setSafeOnClickListener {
            showDatePicker(it)
        }

        binding.cancelButton.setSafeOnClickListener {
            viewModel.navigateToWeekly()
        }

        binding.addSubTaskButton.setSafeOnClickListener {
            viewModel.addNewSubTask()
        }

        binding.createTaskButton.setSafeOnClickListener {
            it.isClickable = false
            viewModel.setTitle(binding.taskTitle.text.toString())
            viewModel.setSelectedDays(binding.dayPicker.selectedDays)
            viewModel.addToCalendar()
        }

        val spinner = binding.countPicker

        ArrayAdapter.createFromResource(
            context!!,
            R.array.spinnerArray,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_item)
            spinner.setAdapter(adapter)
        }

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.setCount("")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.setCount(parent!!.getItemAtPosition(position) as String)
            }
        }
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
                R.id.recurrenceEndDate -> {
                    viewModel.setRecurrenceEndDate(fixedEpoch)
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

    private fun setStartDateText(binding: AddTaskFragmentBinding, currentTime: ZonedDateTime) {
        binding.startDate.text =
            currentTime.format(DateTimeFormatter.RFC_1123_DATE_TIME).toString().substring(0, 16)
    }

    private fun setEndDateText(binding: AddTaskFragmentBinding, currentTime: ZonedDateTime) {
        binding.endDate.text =
            currentTime.plusMinutes(30).format(DateTimeFormatter.RFC_1123_DATE_TIME).toString()
                .substring(0, 16)
    }

    private fun setStartTimeText(binding: AddTaskFragmentBinding, hoursAndMinutes: Pair<Int, Int>) {
        var minutesFormatted = "0${hoursAndMinutes.second}"
        minutesFormatted = minutesFormatted.substring(minutesFormatted.length - 2, minutesFormatted.length)
        binding.startTime.text = "${hoursAndMinutes.first}:$minutesFormatted"
    }

    private fun setEndTimeText(binding: AddTaskFragmentBinding, hoursAndMinutes: Pair<Int, Int>) {
        var minutesFormatted = "0${hoursAndMinutes.second}"
        minutesFormatted = minutesFormatted.substring(minutesFormatted.length - 2, minutesFormatted.length)
        binding.endTime.text = "${hoursAndMinutes.first}:$minutesFormatted"
    }

    private fun setRecurrenceEndDateText(binding: AddTaskFragmentBinding, selectedTime: ZonedDateTime) {
        binding.recurrenceEndDate.text =
            selectedTime.format(DateTimeFormatter.RFC_1123_DATE_TIME).toString().substring(0, 16)
    }
}