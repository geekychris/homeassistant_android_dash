package com.example.simplehomeassistant.ui.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplehomeassistant.databinding.FragmentEntitySelectionBinding
import com.example.simplehomeassistant.ui.entityselection.EntitySelectionAdapter
import com.google.android.material.snackbar.Snackbar

class TabEntityAssignmentFragment : Fragment() {

    private var _binding: FragmentEntitySelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TabEntityAssignmentViewModel by viewModels()
    private lateinit var adapter: EntitySelectionAdapter

    private var tabId: Long = 0
    private var tabName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabId = arguments?.getLong("tabId") ?: 0
        tabName = arguments?.getString("tabName") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntitySelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Update titles
        binding.titleText.text = "Assign Devices to '$tabName'"
        binding.subtitleText.text = "Check devices to add to this tab. Changes save automatically."

        // Setup search view
        setupSearchView()

        setupRecyclerView()
        observeViewModel()

        viewModel.loadEntitiesAndAssignments(tabId)
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

    private fun setupRecyclerView() {
        adapter = EntitySelectionAdapter { entityId ->
            viewModel.toggleEntityAssignment(tabId, entityId)

            // Show immediate feedback
            val entity = viewModel.allEntities.value?.find { it.entityId == entityId }
            val isAssigned = viewModel.assignedEntityIds.value?.contains(entityId) ?: false
            if (entity != null) {
                val message = if (isAssigned) {
                    "${entity.name} added to $tabName"
                } else {
                    "${entity.name} removed from $tabName"
                }
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.entitiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TabEntityAssignmentFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.allEntities.observe(viewLifecycleOwner) { entities ->
            val assigned = viewModel.assignedEntityIds.value ?: emptySet()

            if (entities.isEmpty()) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.entitiesRecyclerView.visibility = View.GONE
                binding.emptyStateText.text =
                    "No entities available.\nPlease refresh from dashboard first."
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.entitiesRecyclerView.visibility = View.VISIBLE
                adapter.submitList(entities, assigned)
            }
        }

        viewModel.assignedEntityIds.observe(viewLifecycleOwner) { assigned ->
            val entities = viewModel.allEntities.value ?: emptyList()
            if (entities.isNotEmpty()) {
                adapter.submitList(entities, assigned)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
