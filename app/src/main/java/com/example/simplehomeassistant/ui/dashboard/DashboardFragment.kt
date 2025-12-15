package com.example.simplehomeassistant.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simplehomeassistant.databinding.FragmentDashboardBinding
import com.google.android.material.snackbar.Snackbar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var adapter: EntityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        // Load entities on view creation if configuration exists
        if (viewModel.activeConfiguration.value != null) {
            android.util.Log.d(
                "DashboardFragment",
                "Config exists on view created, loading entities..."
            )
            viewModel.loadEntities()
        } else {
            android.util.Log.d("DashboardFragment", "No active config on view created")
        }

        // Also observe for future configuration changes
        viewModel.activeConfiguration.observe(viewLifecycleOwner) { config ->
            android.util.Log.d("DashboardFragment", "Config changed: ${config?.name}")
            if (config != null && viewModel.entities.value.isNullOrEmpty()) {
                // Configuration exists and no entities loaded yet - auto refresh
                android.util.Log.d("DashboardFragment", "Loading entities due to config change...")
                viewModel.loadEntities()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = EntityAdapter { entity, action ->
            viewModel.controlEntity(entity, action)
        }

        binding.entitiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DashboardFragment.adapter
        }

        // Add swipe-to-delete support for custom tabs only
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                // Only allow swipe in custom tabs, not "All" tab
                val currentTab = viewModel.currentTab.value
                return if (currentTab != null && currentTab != "All") {
                    super.getSwipeDirs(recyclerView, viewHolder)
                } else {
                    0 // Disable swipe
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val entity = adapter.getEntityAt(position)

                if (entity != null) {
                    val entityId = entity.entityId
                    val entityName = entity.name
                    val tabName = viewModel.currentTab.value

                    viewModel.removeDeviceFromCurrentTab(entityId)

                    Snackbar.make(
                        binding.root,
                        "$entityName removed from $tabName",
                        Snackbar.LENGTH_LONG
                    ).setAction("UNDO") {
                        // Re-add the entity to the tab
                        viewModel.addDeviceToCurrentTab(entityId)
                    }.show()
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.entitiesRecyclerView)
    }

    private fun setupListeners() {
        binding.refreshButton.setOnClickListener {
            viewModel.loadEntities()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadEntities()
        }

        binding.internalChip.setOnClickListener {
            if (viewModel.isExternalConnection.value == true) {
                viewModel.toggleConnectionType()
            }
        }

        binding.externalChip.setOnClickListener {
            if (viewModel.isExternalConnection.value == false) {
                viewModel.toggleConnectionType()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.entities.observe(viewLifecycleOwner) { entitiesMap ->
            if (entitiesMap.isEmpty()) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.entitiesRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.entitiesRecyclerView.visibility = View.VISIBLE

                // Flatten the map into a list with room headers
                val flatList = mutableListOf<Any>()
                entitiesMap.forEach { (room, entities) ->
                    flatList.add(room) // Room header
                    flatList.addAll(entities)
                }

                // For now, just show all entities without headers
                val allEntities = entitiesMap.values.flatten()
                adapter.submitList(allEntities)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading && adapter.itemCount == 0) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { errorMessage ->
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
            }
        }

        // Observe tabs and setup TabLayout
        viewModel.tabs.observe(viewLifecycleOwner) { tabs ->
            // Remember current selection
            val currentSelectedTab = viewModel.currentTab.value ?: "All"

            binding.tabLayout.removeAllTabs()
            tabs.forEachIndexed { index, tabName ->
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tabName))

                // Restore selection if this is the current tab
                if (tabName == currentSelectedTab) {
                    binding.tabLayout.selectTab(binding.tabLayout.getTabAt(index))
                }
            }
        }

        // Handle tab selection
        binding.tabLayout.addOnTabSelectedListener(object :
            com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                tab?.text?.toString()?.let { tabName ->
                    viewModel.selectTab(tabName)
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        viewModel.activeConfiguration.observe(viewLifecycleOwner) { config ->
            if (config == null) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.entitiesRecyclerView.visibility = View.GONE
                binding.emptyStateText.text =
                    "No configuration set.\nPlease add a configuration in Configurations tab."
                binding.connectionChipGroup.visibility = View.GONE
                binding.refreshButton.visibility = View.GONE
            } else {
                binding.connectionChipGroup.visibility = View.VISIBLE
                binding.refreshButton.visibility = View.VISIBLE
                // Show helpful message if entities haven't been loaded yet
                if (viewModel.entities.value?.isEmpty() != false) {
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.emptyStateText.text =
                        "Tap Refresh to load entities\nfrom ${config.name}"
                }
            }
        }

        viewModel.isExternalConnection.observe(viewLifecycleOwner) { isExternal ->
            binding.internalChip.isChecked = !isExternal
            binding.externalChip.isChecked = isExternal
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
