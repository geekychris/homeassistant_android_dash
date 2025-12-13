package com.example.simplehomeassistant.ui.tabs

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplehomeassistant.R
import com.example.simplehomeassistant.databinding.FragmentTabManagementBinding
import com.example.simplehomeassistant.data.model.Tab
import com.google.android.material.snackbar.Snackbar

class TabManagementFragment : Fragment() {

    private var _binding: FragmentTabManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TabManagementViewModel by viewModels()
    private lateinit var adapter: TabAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = TabAdapter(
            onEditClick = { tab ->
                showEditTabDialog(tab)
            },
            onDeleteClick = { tab ->
                showDeleteConfirmation(tab)
            },
            onManageEntitiesClick = { tab ->
                navigateToEntityAssignment(tab)
            },
            getEntityCount = { tabId ->
                viewModel.getEntityCountForTab(tabId)
            }
        )

        binding.tabsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TabManagementFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.addTabButton.setOnClickListener {
            showCreateTabDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.tabs.observe(viewLifecycleOwner) { tabs ->
            if (tabs.isEmpty()) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.tabsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.tabsRecyclerView.visibility = View.VISIBLE
                adapter.submitList(tabs)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { errorMessage ->
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.success.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { successMessage ->
                Snackbar.make(binding.root, successMessage, Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.activeConfiguration.observe(viewLifecycleOwner) { config ->
            if (config == null) {
                Snackbar.make(
                    binding.root,
                    "No active configuration. Please set one first.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showCreateTabDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tab_name, null)
        val tabNameInput = dialogView.findViewById<EditText>(R.id.tabNameInput)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = tabNameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.createTab(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditTabDialog(tab: Tab) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tab_name, null)
        val tabNameInput = dialogView.findViewById<EditText>(R.id.tabNameInput)
        tabNameInput.setText(tab.name)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Tab")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = tabNameInput.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.updateTab(tab.copy(name = newName))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(tab: Tab) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Tab")
            .setMessage("Delete '${tab.name}'? Devices won't be deleted, just removed from this tab.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTab(tab)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToEntityAssignment(tab: Tab) {
        // Navigate to entity assignment screen
        val bundle = Bundle().apply {
            putLong("tabId", tab.id)
            putString("tabName", tab.name)
        }
        findNavController().navigate(
            R.id.action_tabManagement_to_entityAssignment,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
