package com.example.simplehomeassistant.ui.entityselection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.simplehomeassistant.data.local.AppDatabase
import com.example.simplehomeassistant.data.model.Configuration
import com.example.simplehomeassistant.data.model.HAEntity
import com.example.simplehomeassistant.data.model.SelectedEntity
import com.example.simplehomeassistant.data.repository.HomeAssistantRepository
import com.example.simplehomeassistant.util.Event
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class EntitySelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeAssistantRepository

    val activeConfiguration: LiveData<Configuration?>

    private val _allEntities = MutableLiveData<Map<String, List<HAEntity>>>()
    val allEntities: LiveData<Map<String, List<HAEntity>>> = _allEntities

    private val _filteredEntities = MutableLiveData<List<HAEntity>>()
    val filteredEntities: LiveData<List<HAEntity>> = _filteredEntities

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _selectedEntityIds = MutableLiveData<Set<String>>()
    val selectedEntityIds: LiveData<Set<String>> = _selectedEntityIds

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HomeAssistantRepository(database)
        activeConfiguration = repository.getActiveConfiguration().asLiveData()

        // DO NOT auto-load entities - let Fragment call loadEntitiesIfNeeded() manually
        // This prevents ANR and network issues on screen open
    }

    fun loadEntitiesIfNeeded() {
        val config = activeConfiguration.value
        if (config != null && _allEntities.value == null) {
            loadEntities(config.id)
        }
    }

    private fun loadEntities(configId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            // Ensure repository knows about the active configuration
            repository.setActiveConfiguration(configId)

            // Fetch all entities from Home Assistant
            val result = repository.fetchAllStates()

            result.fold(
                onSuccess = { entities ->
                    // Group by room
                    val groupedEntities = entities.groupBy { it.room }
                    _allEntities.value = groupedEntities

                    // Apply initial filter
                    applySearchFilter()
                },
                onFailure = { exception ->
                    _error.value = Event(exception.message ?: "Failed to load entities")
                }
            )

            // Load selected entities for this configuration
            val selectedEntities = repository
                .getSelectedEntitiesForConfig(configId)
                .firstOrNull() ?: emptyList()

            _selectedEntityIds.value = selectedEntities.map { it.entityId }.toSet()
            _isLoading.value = false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applySearchFilter()
    }

    private fun applySearchFilter() {
        val allEntitiesMap = _allEntities.value ?: emptyMap()
        val query = _searchQuery.value?.lowercase() ?: ""

        val allEntitiesList = allEntitiesMap.values.flatten()

        val filtered = if (query.isEmpty()) {
            allEntitiesList
        } else {
            allEntitiesList.filter { entity ->
                entity.name.lowercase().contains(query) ||
                        entity.entityId.lowercase().contains(query) ||
                        entity.room.lowercase().contains(query)
            }
        }

        _filteredEntities.value = filtered
    }

    fun toggleEntitySelection(entityId: String) {
        viewModelScope.launch {
            val config = activeConfiguration.value ?: return@launch
            val currentSelected = _selectedEntityIds.value ?: emptySet()

            if (entityId in currentSelected) {
                android.util.Log.d(
                    "EntitySelection",
                    "Removing entity: $entityId from config ${config.id}"
                )
                repository.removeSelectedEntity(config.id, entityId)
                _selectedEntityIds.value = currentSelected - entityId
            } else {
                android.util.Log.d(
                    "EntitySelection",
                    "Adding entity: $entityId to config ${config.id}"
                )
                repository.addSelectedEntity(config.id, entityId)
                _selectedEntityIds.value = currentSelected + entityId
            }

            android.util.Log.d(
                "EntitySelection",
                "Current selected count: ${_selectedEntityIds.value?.size}"
            )
        }
    }

    // clearError() no longer needed with Event pattern
}
