package com.example.simplehomeassistant.ui.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.simplehomeassistant.data.api.AuthenticationApi
import com.example.simplehomeassistant.data.local.AppDatabase
import com.example.simplehomeassistant.data.model.Configuration
import com.example.simplehomeassistant.data.repository.HomeAssistantRepository
import kotlinx.coroutines.launch

class ConfigurationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeAssistantRepository
    private val authApi = AuthenticationApi()

    val configurations: LiveData<List<Configuration>>
    val activeConfiguration: LiveData<Configuration?>

    private val _saveResult = MutableLiveData<Result<Long>>()
    val saveResult: LiveData<Result<Long>> = _saveResult

    private val _testResult = MutableLiveData<Result<String>>()
    val testResult: LiveData<Result<String>> = _testResult

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
        username: String? = null,
        password: String? = null,
        externalApiToken: String? = null,
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
                    isActive = isActive,
                    useTokenAuth = true,
                    username = username,
                    password = password,
                    externalApiToken = externalApiToken
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

    fun saveConfigurationWithCredentials(
        name: String,
        internalUrl: String,
        externalUrl: String,
        username: String,
        password: String,
        isActive: Boolean = false,
        id: Long = 0
    ) {
        viewModelScope.launch {
            try {
                // Try to exchange credentials for token
                val url = if (internalUrl.isNotEmpty()) internalUrl else externalUrl
                val result = authApi.exchangeCredentials(url, username, password)

                result.fold(
                    onSuccess = { tokenResponse ->
                        // Successfully got token, save configuration
                        val config = Configuration(
                            id = id,
                            name = name,
                            internalUrl = internalUrl,
                            externalUrl = externalUrl,
                            apiToken = tokenResponse.accessToken,
                            isActive = isActive,
                            useTokenAuth = false,
                            username = username,
                            password = password
                        )

                        val resultId = if (id > 0) {
                            repository.updateConfiguration(config)
                            id
                        } else {
                            repository.insertConfiguration(config)
                        }

                        _saveResult.value = Result.success(resultId)
                    },
                    onFailure = { error ->
                        _saveResult.value = Result.failure(
                            Exception("Failed to authenticate: ${error.message}")
                        )
                    }
                )
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

    fun activateConfigurationById(id: Long) {
        viewModelScope.launch {
            repository.setActiveConfiguration(id)
        }
    }

    fun updateUrlPreference(configuration: Configuration, preferInternal: Boolean) {
        viewModelScope.launch {
            val updatedConfig = configuration.copy(preferInternalUrl = preferInternal)
            repository.updateConfiguration(updatedConfig)
            // If this is the active configuration, reload it to use the new URL
            if (configuration.isActive) {
                repository.setActiveConfiguration(configuration.id)
            }
        }
    }

    fun testConnection(url: String, token: String, urlType: String = "Internal") {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Test connection by directly calling the API
                val result = authApi.testConnection(url, token)

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    result.fold(
                        onSuccess = { entityCount ->
                            _testResult.value =
                                Result.success("✓ $urlType URL connected successfully! Found $entityCount entities.")
                        },
                        onFailure = { error ->
                            _testResult.value =
                                Result.failure(Exception("$urlType URL connection failed: ${error.message}"))
                        }
                    )
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _testResult.value =
                        Result.failure(Exception("Error testing $urlType URL: ${e.message}"))
                }
            }
        }
    }
}
