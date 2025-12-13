package com.example.simplehomeassistant.ui.tabs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.simplehomeassistant.data.local.AppDatabase
import com.example.simplehomeassistant.data.model.Configuration
import com.example.simplehomeassistant.data.model.Tab
import com.example.simplehomeassistant.data.repository.HomeAssistantRepository
import com.example.simplehomeassistant.util.Event
import kotlinx.coroutines.launch

class TabManagementViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeAssistantRepository
    val activeConfiguration: LiveData<Configuration?>

    private val _tabs = MutableLiveData<List<Tab>>()
    val tabs: LiveData<List<Tab>> = _tabs

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> = _error

    private val _success = MutableLiveData<Event<String>>()
    val success: LiveData<Event<String>> = _success

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HomeAssistantRepository(database)
        activeConfiguration = repository.getActiveConfiguration().asLiveData()

        // Load tabs when configuration changes
        viewModelScope.launch {
            repository.getActiveConfiguration().collect { config ->
                config?.let {
                    repository.getTabsForConfig(it.id).collect { tabList ->
                        _tabs.value = tabList
                    }
                }
            }
        }
    }

    fun createTab(name: String) {
        if (name.isBlank()) {
            _error.value = Event("Tab name cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                val config = activeConfiguration.value
                if (config == null) {
                    _error.value = Event("No active configuration")
                    return@launch
                }

                repository.createTab(config.id, name)
                _success.value = Event("Tab '$name' created")
            } catch (e: Exception) {
                _error.value = Event("Failed to create tab: ${e.message}")
            }
        }
    }

    fun updateTab(tab: Tab) {
        if (tab.name.isBlank()) {
            _error.value = Event("Tab name cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                repository.updateTab(tab)
                _success.value = Event("Tab updated")
            } catch (e: Exception) {
                _error.value = Event("Failed to update tab: ${e.message}")
            }
        }
    }

    fun deleteTab(tab: Tab) {
        viewModelScope.launch {
            try {
                repository.deleteTab(tab.id)
                _success.value = Event("Tab '${tab.name}' deleted")
            } catch (e: Exception) {
                _error.value = Event("Failed to delete tab: ${e.message}")
            }
        }
    }

    suspend fun getEntityCountForTab(tabId: Long): Int {
        return try {
            repository.getEntityIdsForTab(tabId).size
        } catch (e: Exception) {
            0
        }
    }
}
