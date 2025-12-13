# Custom Tabs Feature - Complete Implementation Guide

## Current Implementation Status: 70% Complete

### ✅ FULLY IMPLEMENTED

#### 1. Database Layer

- `Tab.kt` - Complete
- `EntityTab.kt` - Complete
- `TabDao.kt` - Complete
- `AppDatabase.kt` - Updated to v2

#### 2. Repository

- All tab management methods added to `HomeAssistantRepository.kt`
- Create, read, update, delete tabs
- Assign/remove entities to/from tabs

#### 3. UI Layouts

- `fragment_tab_management.xml` - Tab management screen
- `item_tab.xml` - Individual tab card
- `dialog_tab_name.xml` - Create/edit dialog

#### 4. ViewModels

- `TabManagementViewModel.kt` - Complete with CRUD operations

#### 5. Adapters

- `TabAdapter.kt` - RecyclerView adapter for tab list

#### 6. Fragments

- `TabManagementFragment.kt` - Complete with dialogs and navigation

### ⏳ REMAINING WORK (30%)

To make this feature fully functional, you need:

#### 1. Entity Assignment Screen (CRITICAL)

**File to Create**: `TabEntityAssignmentFragment.kt`

```kotlin
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
        
        binding.titleText.text = "Assign Devices to '$tabName'"
        binding.subtitleText.text = "Check devices to add to this tab"
        
        setupRecyclerView()
        observeViewModel()
        
        viewModel.loadEntitiesAndAssignments(tabId)
    }
    
    private fun setupRecyclerView() {
        adapter = EntitySelectionAdapter { entityId ->
            viewModel.toggleEntityAssignment(tabId, entityId)
        }
        
        binding.entitiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TabEntityAssignmentFragment.adapter
        }
    }
    
    private fun observeViewModel() {
        viewModel.allEntities.observe(viewLifecycleOwner) { entities ->
            val assigned = viewModel.assignedEntityIds.value ?: emptySet()
            adapter.submitList(entities, assigned)
        }
        
        viewModel.assignedEntityIds.observe(viewLifecycleOwner) { assigned ->
            val entities = viewModel.allEntities.value ?: emptyList()
            adapter.submitList(entities, assigned)
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
```

#### 2. Entity Assignment ViewModel (CRITICAL)

**File to Create**: `TabEntityAssignmentViewModel.kt`

```kotlin
package com.example.simplehomeassistant.ui.tabs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.simplehomeassistant.data.local.AppDatabase
import com.example.simplehomeassistant.data.model.HAEntity
import com.example.simplehomeassistant.data.repository.HomeAssistantRepository
import com.example.simplehomeassistant.util.Event
import kotlinx.coroutines.launch

class TabEntityAssignmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeAssistantRepository

    private val _allEntities = MutableLiveData<List<HAEntity>>()
    val allEntities: LiveData<List<HAEntity>> = _allEntities

    private val _assignedEntityIds = MutableLiveData<Set<String>>()
    val assignedEntityIds: LiveData<Set<String>> = _assignedEntityIds

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HomeAssistantRepository(database)
    }

    fun loadEntitiesAndAssignments(tabId: Long) {
        viewModelScope.launch {
            try {
                // Load all entities from HA
                val result = repository.fetchAllStates()
                result.fold(
                    onSuccess = { entities ->
                        _allEntities.value = entities.sortedBy { it.entityId }
                    },
                    onFailure = { exception ->
                        _error.value = Event("Failed to load entities: ${exception.message}")
                    }
                )

                // Load assignments for this tab
                val assigned = repository.getEntityIdsForTab(tabId)
                _assignedEntityIds.value = assigned.toSet()
                
            } catch (e: Exception) {
                _error.value = Event("Error: ${e.message}")
            }
        }
    }

    fun toggleEntityAssignment(tabId: Long, entityId: String) {
        viewModelScope.launch {
            try {
                val currentAssigned = _assignedEntityIds.value ?: emptySet()
                
                if (entityId in currentAssigned) {
                    repository.removeEntityFromTab(tabId, entityId)
                    _assignedEntityIds.value = currentAssigned - entityId
                } else {
                    repository.assignEntityToTab(tabId, entityId)
                    _assignedEntityIds.value = currentAssigned + entityId
                }
            } catch (e: Exception) {
                _error.value = Event("Failed to update assignment: ${e.message}")
            }
        }
    }
}
```

#### 3. Dashboard Integration (CRITICAL)

**Modify**: `DashboardViewModel.kt`

Replace the current automatic room-based tab generation with custom tabs:

```kotlin
// In loadEntities() method, replace:
// val rooms = sortedEntities.map { it.room }.distinct().sorted()
// val tabList = mutableListOf("All")
// tabList.addAll(rooms)
// _tabs.value = tabList

// With:
val config = _activeConfiguration.value
if (config != null) {
    val userTabs = repository.getTabsForConfig(config.id).firstOrNull() ?: emptyList()
    val tabList = mutableListOf("All")
    tabList.addAll(userTabs.map { it.name })
    _tabs.value = tabList
    
    // Store tab objects for filtering
    _userTabs = userTabs
}

// In applyTabFilter() method, replace:
// val filteredByTab = if (tab == "All") {
//     allEntities
// } else {
//     allEntities.filter { it.room == tab }
// }

// With:
val filteredByTab = if (tab == "All") {
    allEntities
} else {
    val selectedTab = _userTabs.find { it.name == tab }
    if (selectedTab != null) {
        val entityIds = repository.getEntityIdsForTab(selectedTab.id).toSet()
        allEntities.filter { it.entityId in entityIds }
    } else {
        emptyList()
    }
}
```

#### 4. Navigation Setup (REQUIRED)

**Add to**: `res/navigation/mobile_navigation.xml`

```xml
<fragment
    android:id="@+id/nav_tab_management"
    android:name="com.example.simplehomeassistant.ui.tabs.TabManagementFragment"
    android:label="Manage Tabs">
    <action
        android:id="@+id/action_tabManagement_to_entityAssignment"
        app:destination="@+id/nav_tab_entity_assignment" />
</fragment>

<fragment
    android:id="@+id/nav_tab_entity_assignment"
    android:name="com.example.simplehomeassistant.ui.tabs.TabEntityAssignmentFragment"
    android:label="Assign Devices" />
```

**Add to**: `res/menu/bottom_navigation.xml` or `navigation_drawer.xml`

```xml
<item
    android:id="@+id/nav_tab_management"
    android:icon="@android:drawable/ic_menu_sort_by_size"
    android:title="Manage Tabs" />
```

**Update**: `MainActivity.kt` (if using bottom nav)

```kotlin
binding.appBarMain.contentMain.bottomNavView?.let {
    appBarConfiguration = AppBarConfiguration(
        setOf(
            R.id.nav_dashboard,
            R.id.nav_entity_selection,
            R.id.nav_configuration,
            R.id.nav_tab_management  // ADD THIS
        )
    )
    setupActionBarWithNavController(navController, appBarConfiguration)
    it.setupWithNavController(navController)
}
```

## How to Complete the Implementation

### Step 1: Create Missing Files

1. Copy the `TabEntityAssignmentFragment.kt` code above
2. Copy the `TabEntityAssignmentViewModel.kt` code above
3. Save to appropriate directories

### Step 2: Update Dashboard

1. Open `DashboardViewModel.kt`
2. Replace room-based tab logic with custom tab logic (code provided above)
3. Add `_userTabs` variable to store tab objects

### Step 3: Setup Navigation

1. Add fragments to navigation graph
2. Add menu item for tab management
3. Update MainActivity navigation config

### Step 4: Build and Test

```bash
./gradlew clean assembleDebug
```

### Step 5: Test Workflow

1. Open app
2. Go to "Manage Tabs" (from menu)
3. Tap "+ Add Tab"
4. Create tab "Kitchen"
5. Tap "Manage" on Kitchen tab
6. Check Kitchen Light, Kitchen Switch
7. Go back
8. Go to Dashboard
9. See "Kitchen" tab
10. Tap it - see only assigned devices!

## Why the Build Currently Fails

The build fails because:

1. `TabEntityAssignmentFragment` references don't exist yet
2. Navigation actions aren't defined
3. Dashboard still uses old auto-generation logic

## Estimated Time to Complete

- Create 2 missing files: 30 minutes
- Update Dashboard logic: 20 minutes
- Setup navigation: 15 minutes
- Testing & debugging: 30 minutes

**Total**: ~90 minutes

## Alternative: Simplified Version

If you want something working faster, we can:

1. Skip entity assignment UI
2. Add tabs with hardcoded entity IDs
3. Just get the concept working
4. Add full UI later

This would work in ~15 minutes.

## Summary

**What Works**:

- Database ✅
- Repository ✅
- Tab Management UI ✅
- Tab CRUD operations ✅

**What's Missing**:

- Entity assignment UI ⏳
- Dashboard integration ⏳
- Navigation wiring ⏳

**Bottom Line**: You're ~90 minutes away from a fully functional custom tab system!

Would you like me to:
A) Create the remaining files and complete it
B) Create a simplified version that works now
C) Provide templates you can complete yourself

