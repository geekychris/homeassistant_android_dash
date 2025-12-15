package com.example.simplehomeassistant.ui.events

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.simplehomeassistant.data.local.AppDatabase
import com.example.simplehomeassistant.data.model.Configuration
import com.example.simplehomeassistant.data.model.EntityEvent
import com.example.simplehomeassistant.data.repository.HomeAssistantRepository
import com.example.simplehomeassistant.util.Event
import kotlinx.coroutines.launch
import java.util.*

class EntityEventsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeAssistantRepository

    val activeConfiguration: LiveData<Configuration?>

    private val _allEvents = MutableLiveData<List<EntityEvent>>()

    private val _filteredEvents = MutableLiveData<List<EntityEvent>>()
    val filteredEvents: LiveData<List<EntityEvent>> = _filteredEvents

    private val _searchQuery = MutableLiveData<String>("")

    private val _timeFilter = MutableLiveData<TimeFilter>(TimeFilter.LAST_HOUR)
    val timeFilter: LiveData<TimeFilter> = _timeFilter

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HomeAssistantRepository(database)
        activeConfiguration = repository.getActiveConfiguration().asLiveData()
    }

    fun loadEvents() {
        android.util.Log.d("EntityEventsViewModel", "loadEvents() called")
        viewModelScope.launch {
            try {
                android.util.Log.d(
                    "EntityEventsViewModel",
                    "Inside coroutine, setting loading true"
                )
                _isLoading.value = true

                // Check if there's an active configuration
                val config = activeConfiguration.value
                android.util.Log.d(
                    "EntityEventsViewModel",
                    "Active config: ${config?.name ?: "null"}"
                )
                if (config == null) {
                    android.util.Log.e("EntityEventsViewModel", "No active configuration!")
                    _error.value =
                        Event("No active configuration. Please set one in Configurations.")
                    _isLoading.value = false
                    return@launch
                }

                // Ensure repository knows about the active configuration
                android.util.Log.d("EntityEventsViewModel", "Setting active config ${config.id}")
                repository.setActiveConfiguration(config.id)

                val filter = _timeFilter.value ?: TimeFilter.LAST_HOUR
                val startTime = getStartTimeForFilter(filter)
                val endTime = Date() // Now

                android.util.Log.d(
                    "EntityEventsViewModel",
                    "Fetching history from ${startTime} to ${endTime}"
                )
                val result = repository.fetchEntityHistory(startTime, endTime)

                result.fold(
                    onSuccess = { events ->
                        _allEvents.value = events
                        applyFilters()
                    },
                    onFailure = { exception ->
                        _error.value = Event(exception.message ?: "Failed to load events")
                    }
                )

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = Event("Error loading events: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun setTimeFilter(filter: TimeFilter) {
        _timeFilter.value = filter
        loadEvents() // Reload with new time range
    }

    private fun applyFilters() {
        val allEvents = _allEvents.value ?: emptyList()
        val query = _searchQuery.value?.lowercase() ?: ""

        val filtered = if (query.isEmpty()) {
            allEvents
        } else {
            // Split query by spaces for multi-keyword search
            val keywords = query.trim().split("""\s+""".toRegex()).filter { it.isNotEmpty() }

            allEvents.filter { event ->
                val searchableText = "${event.name} ${event.entityId} ${event.state}".lowercase()
                // All keywords must be present
                keywords.all { keyword -> searchableText.contains(keyword) }
            }
        }

        _filteredEvents.value = filtered
    }

    private fun getStartTimeForFilter(filter: TimeFilter): Date {
        val calendar = Calendar.getInstance()
        when (filter) {
            TimeFilter.LAST_HOUR -> calendar.add(Calendar.HOUR_OF_DAY, -1)
            TimeFilter.LAST_DAY -> calendar.add(Calendar.DAY_OF_MONTH, -1)
            TimeFilter.LAST_WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
        }
        return calendar.time
    }

    enum class TimeFilter {
        LAST_HOUR,
        LAST_DAY,
        LAST_WEEK
    }
}
