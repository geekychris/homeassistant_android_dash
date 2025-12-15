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
import kotlinx.coroutines.flow.firstOrNull

class TabEntityAssignmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeAssistantRepository

    private val _allEntities = MutableLiveData<List<HAEntity>>()
    private val _fullEntityList = MutableLiveData<List<HAEntity>>()
    val allEntities: LiveData<List<HAEntity>> = _allEntities

    private val _searchQuery = MutableLiveData<String>("")
    private val _hideUnavailable = MutableLiveData<Boolean>(false)

    private val _assignedEntityIds = MutableLiveData<Set<String>>()
    val assignedEntityIds: LiveData<Set<String>> = _assignedEntityIds

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HomeAssistantRepository(database)
    }

    fun loadEntitiesAndAssignments(tabId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Get active configuration and set it
                val config = repository.getActiveConfiguration().firstOrNull()
                if (config != null) {
                    repository.setActiveConfiguration(config.id)
                }

                // Load all entities from Home Assistant
                val result = repository.fetchAllStates()
                result.fold(
                    onSuccess = { entities ->
                        // Sort alphabetically by name
                        val sortedEntities = entities.sortedBy { it.name.lowercase() }
                        _fullEntityList.value = sortedEntities
                        _allEntities.value = sortedEntities
                    },
                    onFailure = { exception ->
                        _error.value = Event("Failed to load entities: ${exception.message}")
                    }
                )

                // Load current assignments for this tab
                val assigned = repository.getEntityIdsForTab(tabId)
                _assignedEntityIds.value = assigned.toSet()

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = Event("Error: ${e.message}")
                _isLoading.value = false
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

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applySearchFilter()
    }

    fun setHideUnavailable(hide: Boolean) {
        _hideUnavailable.value = hide
        applySearchFilter()
    }

    private fun applySearchFilter() {
        val fullList = _fullEntityList.value ?: emptyList()
        val query = _searchQuery.value?.lowercase() ?: ""
        val hideUnavailable = _hideUnavailable.value ?: false

        var filtered = fullList

        // Apply unavailable filter
        if (hideUnavailable) {
            filtered = filtered.filter { entity ->
                !entity.state.equals("unavailable", ignoreCase = true)
            }
        }

        // Apply keyword search filter - supports multiple keywords
        if (query.isNotEmpty()) {
            // Split query by spaces to get individual keywords
            val keywords = query.trim().split("""\s+""".toRegex()).filter { it.isNotEmpty() }

            filtered = filtered.filter { entity ->
                val searchableText = "${entity.name} ${entity.entityId} ${entity.room}".lowercase()
                // All keywords must be present in the searchable text
                keywords.all { keyword -> searchableText.contains(keyword) }
            }
        }

        _allEntities.value = filtered
    }
}
