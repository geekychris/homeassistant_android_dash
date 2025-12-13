package com.example.simplehomeassistant.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.simplehomeassistant.data.local.AppDatabase
import com.example.simplehomeassistant.data.model.Configuration
import com.example.simplehomeassistant.data.model.HAEntity
import com.example.simplehomeassistant.data.model.Tab
import com.example.simplehomeassistant.data.repository.HomeAssistantRepository
import com.example.simplehomeassistant.util.Event
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeAssistantRepository

    private val _entities = MutableLiveData<Map<String, List<HAEntity>>>()
    val entities: LiveData<Map<String, List<HAEntity>>> = _entities

    private val _allEntities = MutableLiveData<List<HAEntity>>()

    private val _tabs = MutableLiveData<List<String>>()
    val tabs: LiveData<List<String>> = _tabs

    private val _currentTab = MutableLiveData<String>("All")
    val currentTab: LiveData<String> = _currentTab

    private var _userTabs = listOf<Tab>()

    private val _activeConfiguration = MutableLiveData<Configuration?>()
    val activeConfiguration: LiveData<Configuration?> = _activeConfiguration

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    private val _isExternalConnection = MutableLiveData<Boolean>(false)
    val isExternalConnection: LiveData<Boolean> = _isExternalConnection

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HomeAssistantRepository(database)

        // Simply observe the active configuration
        // DO NOT call repository.setActiveConfiguration here - it causes an infinite loop!
        // The configuration is already set when the user activates it
        viewModelScope.launch {
            repository.getActiveConfiguration().collect { config ->
                _activeConfiguration.value = config
                // User must manually tap Refresh to load entities
            }
        }
    }

    fun loadEntities() {
        // Prevent multiple simultaneous loads
        if (_isLoading.value == true) {
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val config = _activeConfiguration.value
                if (config == null) {
                    _error.value = Event("No active configuration")
                    _isLoading.value = false
                    return@launch
                }

                // Ensure repository knows about the active configuration
                repository.setActiveConfiguration(config.id)

                // Get all states from Home Assistant
                val result = repository.fetchAllStates()

                result.fold(
                    onSuccess = { allEntities ->
                        // Get selected entities for this configuration
                        val selectedEntities = repository
                            .getSelectedEntitiesForConfig(config.id)
                            .firstOrNull() ?: emptyList()

                        val selectedEntityIds = selectedEntities.map { it.entityId }.toSet()

                        // Debug logging
                        android.util.Log.d(
                            "DashboardViewModel",
                            "Total entities from HA: ${allEntities.size}"
                        )
                        android.util.Log.d(
                            "DashboardViewModel",
                            "Selected entity count: ${selectedEntityIds.size}"
                        )
                        android.util.Log.d(
                            "DashboardViewModel",
                            "Selected entity IDs: $selectedEntityIds"
                        )

                        // Sort all entities by entity ID for stable order
                        val sortedAllEntities = allEntities.sortedBy { it.entityId }

                        // Store ALL entities (including sensors) so custom tabs can access them
                        _allEntities.value = sortedAllEntities

                        android.util.Log.d(
                            "DashboardViewModel",
                            "Stored ${sortedAllEntities.size} total entities (including sensors)"
                        )

                        // Generate tabs from custom user tabs
                        viewModelScope.launch {
                            val userTabs =
                                repository.getTabsForConfig(config.id).firstOrNull() ?: emptyList()
                            android.util.Log.d(
                                "DashboardViewModel",
                                ">>>>>>> Loaded user tabs for config ${config.id}: ${userTabs.map { "${it.name}(ID:${it.id})" }}"
                            )
                            _userTabs = userTabs

                            val tabList = mutableListOf("All")
                            tabList.addAll(userTabs.map { it.name })
                            android.util.Log.d(
                                "DashboardViewModel",
                                ">>>>>>> Tab list to display: $tabList")
                            _tabs.value = tabList

                            // Apply current tab filter
                            applyTabFilter()
                        }
                    },
                    onFailure = { exception ->
                        _error.value = Event(exception.message ?: "Failed to load entities")
                    }
                )

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = Event("Error loading entities: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    fun selectTab(tabName: String) {
        android.util.Log.d("DashboardViewModel", ">>>>>>> selectTab called with: '$tabName'")
        _currentTab.value = tabName
        applyTabFilter()
    }

    private fun applyTabFilter() {
        val allEntities = _allEntities.value ?: emptyList()
        val tab = _currentTab.value ?: "All"

        viewModelScope.launch {
            android.util.Log.d("DashboardViewModel", "=== Applying tab filter ===")
            android.util.Log.d("DashboardViewModel", "Current tab: '$tab'")
            android.util.Log.d(
                "DashboardViewModel",
                "Available user tabs: ${_userTabs.map { "${it.name}(ID:${it.id})" }}"
            )
            android.util.Log.d(
                "DashboardViewModel",
                "Total entities to filter: ${allEntities.size}"
            )

            val filteredByTab = if (tab == "All") {
                android.util.Log.d("DashboardViewModel", "Showing all controllable entities")
                // On "All" tab, only show controllable entities (not sensors)
                allEntities.filter { it.isControllable() }
            } else {
                // Find the selected tab
                val selectedTab = _userTabs.find { it.name == tab }
                android.util.Log.d(
                    "DashboardViewModel",
                    "Selected tab object: ${selectedTab?.name} (ID: ${selectedTab?.id})"
                )

                if (selectedTab != null) {
                    // Get entity IDs assigned to this tab
                    val entityIds = repository.getEntityIdsForTab(selectedTab.id).toSet()
                    android.util.Log.d(
                        "DashboardViewModel",
                        "Entity IDs assigned to tab: $entityIds"
                    )
                    android.util.Log.d(
                        "DashboardViewModel",
                        "Number of assigned entities: ${entityIds.size}"
                    )
                    val filtered = allEntities.filter { it.entityId in entityIds }
                    android.util.Log.d(
                        "DashboardViewModel",
                        "Filtered result: ${filtered.size} entities match"
                    )
                    filtered
                } else {
                    // Fallback to room-based filtering if tab not found
                    android.util.Log.d(
                        "DashboardViewModel",
                        "Tab '$tab' not found in user tabs, using room filter fallback"
                    )
                    allEntities.filter { it.room == tab }
                }
            }

            // Group by room while maintaining sorted order
            val groupedEntities = filteredByTab.groupBy { it.room }
            _entities.value = groupedEntities
        }
    }

    fun toggleConnectionType() {
        viewModelScope.launch {
            val isExternal = _isExternalConnection.value ?: false
            if (isExternal) {
                repository.switchToInternalUrl()
                _isExternalConnection.value = false
            } else {
                repository.switchToExternalUrl()
                _isExternalConnection.value = true
            }
            loadEntities()
        }
    }

    fun controlEntity(entity: HAEntity, action: EntityAction) {
        viewModelScope.launch {
            val result = when (action) {
                is EntityAction.TurnOn -> repository.turnOnEntity(entity)
                is EntityAction.TurnOff -> repository.turnOffEntity(entity)
                is EntityAction.Toggle -> repository.toggleEntity(entity)
                is EntityAction.SetBrightness -> repository.setLightBrightness(
                    entity,
                    action.brightness
                )

                is EntityAction.SetTemperature -> repository.setClimateTemperature(
                    entity,
                    action.temperature
                )

                is EntityAction.SetClimateMode -> repository.setClimateMode(entity, action.mode)
            }

            result.fold(
                onSuccess = {
                    // Refresh with retry to ensure state is updated
                    refreshWithRetry(entity.entityId, action)
                },
                onFailure = { exception ->
                    _error.value = Event(exception.message ?: "Failed to control entity")
                }
            )
        }
    }

    private suspend fun refreshWithRetry(entityId: String, action: EntityAction) {
        val expectedState = when (action) {
            is EntityAction.TurnOn -> "on"
            is EntityAction.TurnOff -> "off"
            else -> null
        }

        // Store the original state before we started
        val originalEntity = _entities.value?.values?.flatten()?.find { it.entityId == entityId }
        val originalState = originalEntity?.state

        // Try up to 3 times with delays to give server time to update
        repeat(3) { attempt ->
            // Delay before checking (300ms, then 500ms, then 700ms)
            kotlinx.coroutines.delay(300L + (attempt * 200L))

            // Silently check state without updating UI
            val result = repository.fetchAllStates()

            result.fold(
                onSuccess = { allEntities ->
                    val currentEntity = allEntities.find { it.entityId == entityId }
                    val currentState = currentEntity?.state

                    // Check if state changed from original
                    val stateChanged = currentState != originalState

                    // Check if we reached expected state (for on/off)
                    val reachedExpectedState =
                        expectedState == null || currentState == expectedState

                    if (stateChanged && reachedExpectedState) {
                        // State changed to what we expected - update just this entity
                        android.util.Log.d(
                            "DashboardViewModel",
                            "State changed after ${attempt + 1} attempt(s), updating single entity"
                        )
                        currentEntity?.let { updateSingleEntity(it) }
                        return
                    }
                },
                onFailure = {
                    // Ignore errors during polling, will retry
                }
            )
        }

        // If we get here, state didn't change as expected
        // Do a single final update to show current state
        android.util.Log.d("DashboardViewModel", "State verification completed, final refresh")
        val result = repository.fetchAllStates()
        result.fold(
            onSuccess = { allEntities ->
                val currentEntity = allEntities.find { it.entityId == entityId }
                currentEntity?.let { updateSingleEntity(it) }
            },
            onFailure = {
                // If final check fails, do full refresh
                loadEntities()
            }
        )
    }

    private fun updateSingleEntity(updatedEntity: HAEntity) {
        // Update the entity in the list without full refresh
        val currentEntities = _allEntities.value ?: emptyList()
        val updatedList = currentEntities.map { entity ->
            if (entity.entityId == updatedEntity.entityId) {
                updatedEntity
            } else {
                entity
            }
        }
        _allEntities.value = updatedList
        // Reapply the current tab filter with the updated list
        applyTabFilter()
    }

    // clearError() no longer needed with Event - errors auto-clear after being handled
}

sealed class EntityAction {
    object TurnOn : EntityAction()
    object TurnOff : EntityAction()
    object Toggle : EntityAction()
    data class SetBrightness(val brightness: Int) : EntityAction()
    data class SetTemperature(val temperature: Float) : EntityAction()
    data class SetClimateMode(val mode: String) : EntityAction()
}
