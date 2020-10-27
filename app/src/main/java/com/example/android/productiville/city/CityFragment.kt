package com.example.android.productiville.city

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.android.productiville.R
import com.example.android.productiville.databinding.CityFragmentBinding

class CityFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil
            .inflate<CityFragmentBinding>(
                inflater,
                R.layout.city_fragment,
                container,
                false)

        val viewModelFactory = CityViewModelFactory()

        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(CityViewModel::class.java)

        binding.viewModel = viewModel

        binding.setLifecycleOwner(this)

        viewModel.navigateToWeekly.observe(this, Observer {
            if(it == true) {
                val bundle = Bundle()
                bundle.putAll(arguments)
                findNavController().navigate(R.id.action_cityFragment_to_weeklyFragment, bundle)
                viewModel.weeklyNavDone()
            }
        })

        return binding.root
    }
}