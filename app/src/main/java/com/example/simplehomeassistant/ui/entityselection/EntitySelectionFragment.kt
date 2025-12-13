package com.example.simplehomeassistant.ui.entityselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplehomeassistant.databinding.FragmentEntitySelectionBinding
import com.google.android.material.snackbar.Snackbar

class EntitySelectionFragment : Fragment() {

    private var _binding: FragmentEntitySelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntitySelectionViewModel by viewModels()
    private lateinit var adapter: EntitySelectionAdapter

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

        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        // Load entities when fragment is ready
        viewModel.loadEntitiesIfNeeded()
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
            viewModel.toggleEntitySelection(entityId)
        }

        binding.entitiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@EntitySelectionFragment.adapter
        }
    }

    private fun observeViewModel() {
        // Observe filtered entities from search
        viewModel.filteredEntities.observe(viewLifecycleOwner) { entities ->
            val selectedIds = viewModel.selectedEntityIds.value ?: emptySet()

            if (entities.isEmpty() && viewModel.allEntities.value?.isEmpty() != false) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.entitiesRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.entitiesRecyclerView.visibility = View.VISIBLE
                adapter.submitList(entities, selectedIds)
            }
        }

        viewModel.selectedEntityIds.observe(viewLifecycleOwner) { selectedIds ->
            val entities = viewModel.filteredEntities.value ?: emptyList()
            if (entities.isNotEmpty()) {
                adapter.submitList(entities, selectedIds)
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

        viewModel.activeConfiguration.observe(viewLifecycleOwner) { config ->
            if (config == null) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.entitiesRecyclerView.visibility = View.GONE
                binding.emptyStateText.text =
                    "No active configuration.\nPlease set up a configuration first."
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
