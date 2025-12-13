package com.example.simplehomeassistant.data.repository

import com.example.simplehomeassistant.data.local.AppDatabase
import com.example.simplehomeassistant.data.model.*
import com.example.simplehomeassistant.data.remote.HomeAssistantApi
import com.example.simplehomeassistant.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class HomeAssistantRepository(private val database: AppDatabase) {

    private val configDao = database.configurationDao()
    private val entityDao = database.selectedEntityDao()
    private val tabDao = database.tabDao()

    private var currentApi: HomeAssistantApi? = null
    private var currentConfig: Configuration? = null

    // Configuration operations
    fun getAllConfigurations(): Flow<List<Configuration>> = configDao.getAllConfigurations()

    fun getActiveConfiguration(): Flow<Configuration?> = configDao.getActiveConfiguration()

    suspend fun insertConfiguration(configuration: Configuration): Long {
        return configDao.insertConfiguration(configuration)
    }

    suspend fun updateConfiguration(configuration: Configuration) {
        configDao.updateConfiguration(configuration)
    }

    suspend fun deleteConfiguration(configuration: Configuration) {
        entityDao.deleteAllForConfiguration(configuration.id)
        configDao.deleteConfiguration(configuration)
    }

    suspend fun setActiveConfiguration(id: Long) {
        configDao.deactivateAllConfigurations()
        configDao.setActiveConfiguration(id)
        currentConfig = configDao.getConfigurationById(id)
        currentConfig?.let { config ->
            currentApi = RetrofitClient.createApi(config.internalUrl)
        }
    }

    suspend fun switchToExternalUrl() {
        currentConfig?.let { config ->
            currentApi = RetrofitClient.createApi(config.externalUrl)
        }
    }

    suspend fun switchToInternalUrl() {
        currentConfig?.let { config ->
            currentApi = RetrofitClient.createApi(config.internalUrl)
        }
    }

    // Entity operations
    fun getSelectedEntitiesForConfig(configId: Long): Flow<List<SelectedEntity>> {
        return entityDao.getSelectedEntitiesForConfig(configId)
    }

    suspend fun addSelectedEntity(configId: Long, entityId: String) {
        val entities = entityDao.getSelectedEntitiesForConfig(configId).firstOrNull() ?: emptyList()
        val maxOrder = entities.maxOfOrNull { it.displayOrder } ?: 0
        entityDao.insertSelectedEntity(
            SelectedEntity(
                configurationId = configId,
                entityId = entityId,
                displayOrder = maxOrder + 1
            )
        )
    }

    suspend fun removeSelectedEntity(configId: Long, entityId: String) {
        entityDao.deleteByEntityId(configId, entityId)
    }

    // Tab operations
    fun getTabsForConfig(configId: Long): Flow<List<Tab>> {
        return tabDao.getTabsForConfig(configId)
    }

    suspend fun createTab(configId: Long, name: String): Long {
        val existingTabs = tabDao.getTabsForConfig(configId).firstOrNull() ?: emptyList()
        val maxOrder = existingTabs.maxOfOrNull { it.displayOrder } ?: 0
        return tabDao.insertTab(
            Tab(
                configurationId = configId,
                name = name,
                displayOrder = maxOrder + 1
            )
        )
    }

    suspend fun updateTab(tab: Tab) {
        tabDao.updateTab(tab)
    }

    suspend fun deleteTab(tabId: Long) {
        // Delete all entity assignments first
        tabDao.deleteAllEntitiesFromTab(tabId)
        // Then delete the tab
        tabDao.deleteTabById(tabId)
    }

    suspend fun getEntityIdsForTab(tabId: Long): List<String> {
        return tabDao.getEntityIdsForTab(tabId).map { it.entityId }
    }

    suspend fun assignEntityToTab(tabId: Long, entityId: String) {
        tabDao.insertEntityTab(EntityTab(tabId, entityId))
    }

    suspend fun removeEntityFromTab(tabId: Long, entityId: String) {
        tabDao.deleteEntityFromTab(tabId, entityId)
    }

    suspend fun getTabsForEntity(entityId: String): List<Long> {
        // This would need a query in TabDao to get all tabs for an entity
        // For now, we'll skip this as it's not critical
        return emptyList()
    }

    // Home Assistant API operations
    private fun getAuthHeader(): String {
        return "Bearer ${currentConfig?.apiToken ?: ""}"
    }

    suspend fun fetchAllStates(): Result<List<HAEntity>> {
        return try {
            val api = currentApi ?: return Result.failure(Exception("No active configuration"))
            val config =
                currentConfig ?: return Result.failure(Exception("No configuration loaded"))

            val response = api.getStates(getAuthHeader())
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                val errorMsg = when (response.code()) {
                    401, 403 -> "Authentication failed. Check your API token."
                    404 -> "Home Assistant API not found. Check your URL."
                    else -> "Server error: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Cannot resolve hostname. Check your URL or network connection."))
        } catch (e: java.net.ConnectException) {
            Result.failure(Exception("Cannot connect to server. Check if Home Assistant is running."))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Connection timeout. Server is not responding."))
        } catch (e: Exception) {
            Result.failure(Exception("Connection error: ${e.message ?: "Unknown error"}"))
        }
    }

    suspend fun fetchEntityState(entityId: String): Result<HAEntity> {
        return try {
            val api = currentApi ?: return Result.failure(Exception("No active configuration"))
            val response = api.getEntityState(getAuthHeader(), entityId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun turnOnEntity(entity: HAEntity): Result<Unit> {
        return try {
            val api = currentApi ?: return Result.failure(Exception("No active configuration"))
            val request = ServiceCallRequest(entityId = entity.entityId)
            val response = api.turnOn(getAuthHeader(), entity.domain, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun turnOffEntity(entity: HAEntity): Result<Unit> {
        return try {
            val api = currentApi ?: return Result.failure(Exception("No active configuration"))
            val request = ServiceCallRequest(entityId = entity.entityId)
            val response = api.turnOff(getAuthHeader(), entity.domain, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleEntity(entity: HAEntity): Result<Unit> {
        return try {
            val api = currentApi ?: return Result.failure(Exception("No active configuration"))
            val request = ServiceCallRequest(entityId = entity.entityId)
            val response = api.toggle(getAuthHeader(), entity.domain, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setLightBrightness(entity: HAEntity, brightness: Int): Result<Unit> {
        return try {
            val api = currentApi ?: return Result.failure(Exception("No active configuration"))
            val request = ServiceCallRequest(
                entityId = entity.entityId,
                brightness = brightness
            )
            val response = api.setLightAttributes(getAuthHeader(), request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setClimateTemperature(entity: HAEntity, temperature: Float): Result<Unit> {
        return try {
            val api = currentApi ?: return Result.failure(Exception("No active configuration"))
            val request = ServiceCallRequest(
                entityId = entity.entityId,
                temperature = temperature
            )
            val response = api.setTemperature(getAuthHeader(), request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setClimateMode(entity: HAEntity, hvacMode: String): Result<Unit> {
        return try {
            val api = currentApi ?: return Result.failure(Exception("No active configuration"))
            val request = ServiceCallRequest(
                entityId = entity.entityId,
                hvacMode = hvacMode
            )
            val response = api.setHvacMode(getAuthHeader(), request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
