package com.example.simplehomeassistant.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.simplehomeassistant.data.local.AppDatabase
import com.example.simplehomeassistant.data.local.ConfigurationDao
import com.example.simplehomeassistant.data.model.Configuration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ConfigurationDao
 * Tests configuration persistence and active configuration management
 */
@RunWith(AndroidJUnit4::class)
class ConfigurationDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var configDao: ConfigurationDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
        configDao = database.configurationDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertConfiguration_shouldStoreInDatabase() = runBlocking {
        // Arrange
        val config = Configuration(
            id = 0,
            name = "Home",
            internalUrl = "http://192.168.1.100:8123",
            externalUrl = "http://home.example.com:8123",
            apiToken = "test_token_123",
            isActive = true
        )

        // Act
        val id = configDao.insertConfiguration(config)
        val allConfigs = configDao.getAllConfigurations().first()

        // Assert
        assertTrue("Configuration ID should be greater than 0", id > 0)
        assertEquals("Should have 1 configuration", 1, allConfigs.size)
        assertEquals("Home", allConfigs[0].name)
    }

    @Test
    fun insertMultipleConfigurations_shouldStoreAll() = runBlocking {
        // Arrange
        val home =
            Configuration(0, "Home", "http://home:8123", "http://home.com:8123", "token1", true)
        val cabin =
            Configuration(0, "Cabin", "http://cabin:8123", "http://cabin.com:8123", "token2", false)
        val office = Configuration(
            0,
            "Office",
            "http://office:8123",
            "http://office.com:8123",
            "token3",
            false
        )

        // Act
        configDao.insertConfiguration(home)
        configDao.insertConfiguration(cabin)
        configDao.insertConfiguration(office)

        val allConfigs = configDao.getAllConfigurations().first()

        // Assert
        assertEquals(3, allConfigs.size)
        assertTrue(allConfigs.any { it.name == "Home" })
        assertTrue(allConfigs.any { it.name == "Cabin" })
        assertTrue(allConfigs.any { it.name == "Office" })
    }

    @Test
    fun getActiveConfiguration_shouldReturnActiveOne() = runBlocking {
        // Arrange
        val inactive = Configuration(0, "Inactive", "url1", "url2", "token1", false)
        val active = Configuration(0, "Active", "url3", "url4", "token2", true)

        configDao.insertConfiguration(inactive)
        configDao.insertConfiguration(active)

        // Act
        val activeConfig = configDao.getActiveConfiguration().first()

        // Assert
        assertNotNull(activeConfig)
        assertEquals("Active", activeConfig?.name)
        assertTrue(activeConfig?.isActive == true)
    }

    @Test
    fun getActiveConfiguration_whenNoneActive_shouldReturnNull() = runBlocking {
        // Arrange
        val config = Configuration(0, "Inactive", "url", "url", "token", false)
        configDao.insertConfiguration(config)

        // Act
        val activeConfig = configDao.getActiveConfiguration().first()

        // Assert
        assertNull("Should return null when no active configuration", activeConfig)
    }

    @Test
    fun setActiveConfiguration_shouldUpdateActiveStatus() = runBlocking {
        // Arrange
        val config1 = Configuration(0, "Config1", "url1", "url1", "token1", true)
        val config2 = Configuration(0, "Config2", "url2", "url2", "token2", false)

        val id1 = configDao.insertConfiguration(config1)
        val id2 = configDao.insertConfiguration(config2)

        // Act
        configDao.setActiveConfiguration(id2)

        val allConfigs = configDao.getAllConfigurations().first()
        val config1After = allConfigs.find { it.id == id1 }
        val config2After = allConfigs.find { it.id == id2 }

        // Assert
        assertFalse("Config1 should no longer be active", config1After?.isActive == true)
        assertTrue("Config2 should now be active", config2After?.isActive == true)
    }

    @Test
    fun updateConfiguration_shouldModifyExistingConfig() = runBlocking {
        // Arrange
        val original = Configuration(0, "Original", "url", "url", "token", true)
        val id = configDao.insertConfiguration(original)

        // Act
        val updated = Configuration(id, "Updated Name", "new_url", "new_url", "new_token", true)
        configDao.updateConfiguration(updated)

        val config = configDao.getAllConfigurations().first().find { it.id == id }

        // Assert
        assertEquals("Updated Name", config?.name)
        assertEquals("new_url", config?.internalUrl)
        assertEquals("new_token", config?.apiToken)
    }

    @Test
    fun deleteConfiguration_shouldRemoveFromDatabase() = runBlocking {
        // Arrange
        val config = Configuration(0, "To Delete", "url", "url", "token", false)
        val id = configDao.insertConfiguration(config)
        val storedConfig = configDao.getAllConfigurations().first().find { it.id == id }!!

        // Act
        configDao.deleteConfiguration(storedConfig)
        val allConfigs = configDao.getAllConfigurations().first()

        // Assert
        assertEquals(0, allConfigs.size)
    }

    @Test
    fun deleteConfiguration_shouldNotAffectOtherConfigs() = runBlocking {
        // Arrange
        val keep1 = Configuration(0, "Keep1", "url1", "url1", "token1", false)
        val delete = Configuration(0, "Delete", "url2", "url2", "token2", false)
        val keep2 = Configuration(0, "Keep2", "url3", "url3", "token3", false)

        configDao.insertConfiguration(keep1)
        val deleteId = configDao.insertConfiguration(delete)
        configDao.insertConfiguration(keep2)
        val configToDelete = configDao.getAllConfigurations().first().find { it.id == deleteId }!!

        // Act
        configDao.deleteConfiguration(configToDelete)
        val remaining = configDao.getAllConfigurations().first()

        // Assert
        assertEquals(2, remaining.size)
        assertTrue(remaining.any { it.name == "Keep1" })
        assertTrue(remaining.any { it.name == "Keep2" })
        assertFalse(remaining.any { it.name == "Delete" })
    }

    @Test
    fun onlyOneConfigurationShouldBeActive() = runBlocking {
        // Arrange
        val config1 = Configuration(0, "Config1", "url1", "url1", "token1", true)
        val config2 = Configuration(0, "Config2", "url2", "url2", "token2", false)
        val config3 = Configuration(0, "Config3", "url3", "url3", "token3", false)

        val id1 = configDao.insertConfiguration(config1)
        val id2 = configDao.insertConfiguration(config2)
        configDao.insertConfiguration(config3)

        // Act
        configDao.setActiveConfiguration(id2)
        val allConfigs = configDao.getAllConfigurations().first()

        // Assert
        val activeConfigs = allConfigs.filter { it.isActive }
        assertEquals("Only one configuration should be active", 1, activeConfigs.size)
        assertEquals("Config2", activeConfigs[0].name)
    }

    @Test
    fun configurationFlow_shouldEmitUpdates() = runBlocking {
        // Arrange
        val config = Configuration(0, "Test", "url", "url", "token", true)

        // Act
        val id = configDao.insertConfiguration(config)
        val configs1 = configDao.getAllConfigurations().first()

        configDao.updateConfiguration(Configuration(id, "Updated", "url", "url", "token", true))
        val configs2 = configDao.getAllConfigurations().first()

        // Assert
        assertEquals("Test", configs1[0].name)
        assertEquals("Updated", configs2[0].name)
    }

    @Test
    fun apiToken_shouldBeStoredSecurely() = runBlocking {
        // Arrange
        val longToken =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhYmNkZWYxMjM0NTYiLCJpYXQiOjE2NzAwMDAwMDAsImV4cCI6MTk4NTM2MDAwMH0.test_signature"
        val config = Configuration(0, "Secure", "url", "url", longToken, true)

        // Act
        val id = configDao.insertConfiguration(config)
        val stored = configDao.getAllConfigurations().first().find { it.id == id }

        // Assert
        assertEquals("Long token should be stored completely", longToken, stored?.apiToken)
    }
}
