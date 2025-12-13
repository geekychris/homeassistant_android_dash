package com.example.simplehomeassistant.ui.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.simplehomeassistant.data.local.AppDatabase
import com.example.simplehomeassistant.data.model.Configuration
import com.example.simplehomeassistant.data.repository.HomeAssistantRepository
import kotlinx.coroutines.launch

class ConfigurationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeAssistantRepository

    val configurations: LiveData<List<Configuration>>
    val activeConfiguration: LiveData<Configuration?>

    private val _saveResult = MutableLiveData<Result<Long>>()
    val saveResult: LiveData<Result<Long>> = _saveResult

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HomeAssistantRepository(database)
        configurations = repository.getAllConfigurations().asLiveData()
        activeConfiguration = repository.getActiveConfiguration().asLiveData()
    }

    fun saveConfiguration(
        name: String,
        internalUrl: String,
        externalUrl: String,
        apiToken: String,
        isActive: Boolean = false,
        id: Long = 0
    ) {
        viewModelScope.launch {
            try {
                val config = Configuration(
                    id = id,
                    name = name,
                    internalUrl = internalUrl,
                    externalUrl = externalUrl,
                    apiToken = apiToken,
                    isActive = isActive
                )

                val resultId = if (id > 0) {
                    repository.updateConfiguration(config)
                    id
                } else {
                    repository.insertConfiguration(config)
                }

                _saveResult.value = Result.success(resultId)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }

    fun deleteConfiguration(configuration: Configuration) {
        viewModelScope.launch {
            repository.deleteConfiguration(configuration)
        }
    }

    fun setActiveConfiguration(configuration: Configuration) {
        viewModelScope.launch {
            repository.setActiveConfiguration(configuration.id)
        }
    }
}
