package com.example.simplehomeassistant.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplehomeassistant.databinding.FragmentEntityEventsBinding
import com.google.android.material.snackbar.Snackbar

class EntityEventsFragment : Fragment() {

    private var _binding: FragmentEntityEventsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntityEventsViewModel by viewModels()
    private lateinit var adapter: EntityEventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntityEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        setupSearchView()
        observeViewModel()

        // Load initial events
        viewModel.loadEvents()
    }

    private fun setupRecyclerView() {
        adapter = EntityEventsAdapter()

        binding.eventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@EntityEventsFragment.adapter
        }
    }

    private fun setupFilters() {
        binding.chipLastHour.setOnClickListener {
            viewModel.setTimeFilter(EntityEventsViewModel.TimeFilter.LAST_HOUR)
        }

        binding.chipLastDay.setOnClickListener {
            viewModel.setTimeFilter(EntityEventsViewModel.TimeFilter.LAST_DAY)
        }

        binding.chipLastWeek.setOnClickListener {
            viewModel.setTimeFilter(EntityEventsViewModel.TimeFilter.LAST_WEEK)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.activeConfiguration.observe(viewLifecycleOwner) { config ->
            if (config == null) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.eventsRecyclerView.visibility = View.GONE
                binding.emptyStateText.text =
                    "No active configuration.\nPlease set one in Configurations."
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.filteredEvents.observe(viewLifecycleOwner) { events ->
            if (events.isEmpty() && viewModel.isLoading.value != true) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.eventsRecyclerView.visibility = View.GONE
                binding.emptyStateText.text = "No events found.\nTry adjusting the time filter."
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.eventsRecyclerView.visibility = View.VISIBLE
                adapter.submitList(events)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { errorMessage ->
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.timeFilter.observe(viewLifecycleOwner) { filter ->
            // Update chip selection based on current filter
            when (filter) {
                EntityEventsViewModel.TimeFilter.LAST_HOUR -> binding.chipLastHour.isChecked = true
                EntityEventsViewModel.TimeFilter.LAST_DAY -> binding.chipLastDay.isChecked = true
                EntityEventsViewModel.TimeFilter.LAST_WEEK -> binding.chipLastWeek.isChecked = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
