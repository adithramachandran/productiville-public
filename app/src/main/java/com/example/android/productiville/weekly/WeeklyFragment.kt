package com.example.android.productiville.weekly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.productiville.R
import com.example.android.productiville.adapters.OnClickListener
import com.example.android.productiville.adapters.SwipeToDeleteCallbackWeekly
import com.example.android.productiville.adapters.WeeklyViewAdapter
import com.example.android.productiville.calendarApiService.Event
import com.example.android.productiville.databinding.WeeklyFragmentBinding
import com.example.android.productiville.utils.setSafeOnClickListener


class WeeklyFragment: Fragment() {

    private val viewModel: WeeklyViewModel by lazy {
        val activity = requireNotNull(this.activity) {

        }
        ViewModelProviders.of(
            this, WeeklyViewModelFactory(arguments?.getString("token")!!, activity.application))
            .get(WeeklyViewModel::class.java)
    }

    private var viewModelAdapter: WeeklyViewAdapter? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.events.observe(viewLifecycleOwner, Observer<MutableList<Event>> { events ->
            events?.apply {
                viewModelAdapter?.tasks = events
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<WeeklyFragmentBinding>(
                inflater,
                R.layout.weekly_fragment,
                container,
                false)

        binding.setLifecycleOwner(this)

        binding.viewModel = viewModel

        val swipeToRefresh = binding.refreshWeekly

        swipeToRefresh.setOnRefreshListener {
            viewModel.getCalendarEvents()
        }

        viewModelAdapter = WeeklyViewAdapter(activity!!,
            OnClickListener { event ->
                deleteFromDatabase(event)
        },
            OnClickListener { event ->
                viewModel.navigateToEditTask(event)
        },
            OnClickListener { event ->
                viewModel.updateEventInDatabase(event)
            }
        )

        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallbackWeekly(viewModelAdapter!!))

        binding.taskList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewModelAdapter
            itemTouchHelper.attachToRecyclerView(this)
        }

        binding.navFromWeekly.setSafeOnClickListener {
            viewModel.navigateToCity()
        }

        binding.addTaskButton.setSafeOnClickListener {
            viewModel.navigateToAddTask()
        }

        viewModel.events.observe(viewLifecycleOwner, Observer {
            it?.let {
                if(it.isEmpty()) viewModel.setAllDone(true) else viewModel.setAllDone(false)
                viewModelAdapter?.submitList(it)
                swipeToRefresh.isRefreshing = false
            }
        })

        viewModel.navToCity.observe(this, Observer {
            if(it == true) {
                viewModelAdapter?.closeAllEvents()
                val bundle = Bundle()
                bundle.putAll(arguments)
                findNavController().navigate(R.id.action_weeklyFragment_to_cityFragment, bundle)
                viewModel.navigateToCityDone()
            }
        })

        viewModel.navToAddTask.observe(this, Observer {
            if(it == true) {
                viewModelAdapter?.closeAllEvents()
                val bundle = Bundle()
                bundle.putAll(arguments)
                findNavController().navigate(R.id.action_weeklyFragment_to_addTaskFragment, bundle)
                viewModel.navigateToAddTaskDone()
            }
        })

        viewModel.navToEditTask.observe(this, Observer { event: Event? ->
            if(event != null) {
                viewModelAdapter?.closeAllEvents()
                val bundle = Bundle()
                bundle.putAll(arguments)
                bundle.putParcelable("editEvent", event)
                findNavController().navigate(R.id.action_weeklyFragment_to_editTaskFragment, bundle)
                viewModel.navigateToEditTaskDone()
            }
        })

        viewModel.allDone.observe(this, Observer {
            binding.allDoneText.visibility = if(it) View.VISIBLE else View.GONE
        })

        return binding.root
    }

    private fun deleteFromDatabase(task: Event) {
        viewModel.deleteEvent(task)
    }
}