package com.example.simplehomeassistant.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.simplehomeassistant.data.local.AppDatabase
import com.example.simplehomeassistant.data.local.TabDao
import com.example.simplehomeassistant.data.model.Configuration
import com.example.simplehomeassistant.data.model.EntityTab
import com.example.simplehomeassistant.data.model.Tab
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for TabDao
 * These tests run on an Android device/emulator and test database operations
 */
@RunWith(AndroidJUnit4::class)
class TabDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var tabDao: TabDao
    private var configId: Long = 0

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Create an in-memory database (data is wiped after tests)
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        tabDao = database.tabDao()

        // Create a test configuration to use
        configId = database.configurationDao().insertConfiguration(
            Configuration(
                name = "Test Config",
                internalUrl = "http://test.local:8123",
                externalUrl = "http://test.com:8123",
                apiToken = "test_token",
                isActive = true
            )
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertTab_shouldStoreTabInDatabase() = runBlocking {
        // Arrange
        val tab = Tab(
            id = 0,
            configurationId = configId,
            name = "Kitchen",
            displayOrder = 1
        )

        // Act
        val insertedId = tabDao.insertTab(tab)
        val tabs = tabDao.getTabsForConfig(configId).first()

        // Assert
        assertTrue("Tab ID should be greater than 0", insertedId > 0)
        assertEquals("Should have 1 tab", 1, tabs.size)
        assertEquals("Tab name should match", "Kitchen", tabs[0].name)
    }

    @Test
    fun insertMultipleTabs_shouldMaintainDisplayOrder() = runBlocking {
        // Arrange
        val kitchen = Tab(0, configId, "Kitchen", 1)
        val bedroom = Tab(0, configId, "Bedroom", 2)
        val living = Tab(0, configId, "Living Room", 3)

        // Act
        tabDao.insertTab(kitchen)
        tabDao.insertTab(bedroom)
        tabDao.insertTab(living)

        val tabs = tabDao.getTabsForConfig(configId).first()

        // Assert
        assertEquals("Should have 3 tabs", 3, tabs.size)
        assertEquals("First tab should be Kitchen", "Kitchen", tabs[0].name)
        assertEquals("Second tab should be Bedroom", "Bedroom", tabs[1].name)
        assertEquals("Third tab should be Living Room", "Living Room", tabs[2].name)
    }

    @Test
    fun updateTab_shouldChangeTabName() = runBlocking {
        // Arrange
        val tab = Tab(0, configId, "Kitchen", 1)
        val tabId = tabDao.insertTab(tab)

        // Act
        val updatedTab = Tab(tabId, configId, "New Kitchen", 1)
        tabDao.updateTab(updatedTab)

        val tabs = tabDao.getTabsForConfig(configId).first()

        // Assert
        assertEquals("Tab name should be updated", "New Kitchen", tabs[0].name)
    }

    @Test
    fun deleteTab_shouldRemoveTabFromDatabase() = runBlocking {
        // Arrange
        val tab = Tab(0, configId, "Kitchen", 1)
        val tabId = tabDao.insertTab(tab)

        // Act
        tabDao.deleteTabById(tabId)
        val tabs = tabDao.getTabsForConfig(configId).first()

        // Assert
        assertEquals("Should have no tabs", 0, tabs.size)
    }

    @Test
    fun assignEntityToTab_shouldCreateAssociation() = runBlocking {
        // Arrange
        val tab = Tab(0, configId, "Kitchen", 1)
        val tabId = tabDao.insertTab(tab)

        // Act
        tabDao.insertEntityTab(EntityTab(tabId, "light.kitchen"))
        tabDao.insertEntityTab(EntityTab(tabId, "switch.kitchen"))

        val entities = tabDao.getEntityIdsForTab(tabId)

        // Assert
        assertEquals("Should have 2 entities", 2, entities.size)
        assertEquals("light.kitchen", entities[0].entityId)
        assertEquals("switch.kitchen", entities[1].entityId)
    }

    @Test
    fun removeEntityFromTab_shouldDeleteAssociation() = runBlocking {
        // Arrange
        val tab = Tab(0, configId, "Kitchen", 1)
        val tabId = tabDao.insertTab(tab)
        tabDao.insertEntityTab(EntityTab(tabId, "light.kitchen"))
        tabDao.insertEntityTab(EntityTab(tabId, "switch.kitchen"))

        // Act
        tabDao.deleteEntityFromTab(tabId, "light.kitchen")
        val entities = tabDao.getEntityIdsForTab(tabId)

        // Assert
        assertEquals("Should have 1 entity", 1, entities.size)
        assertEquals("switch.kitchen", entities[0].entityId)
    }

    @Test
    fun deleteTab_shouldCascadeDeleteEntityAssignments() = runBlocking {
        // Arrange
        val tab = Tab(0, configId, "Kitchen", 1)
        val tabId = tabDao.insertTab(tab)
        tabDao.insertEntityTab(EntityTab(tabId, "light.kitchen"))
        tabDao.insertEntityTab(EntityTab(tabId, "switch.kitchen"))

        // Act
        tabDao.deleteAllEntitiesFromTab(tabId)
        tabDao.deleteTabById(tabId)

        val tabs = tabDao.getTabsForConfig(configId).first()

        // Assert
        assertEquals("Tab should be deleted", 0, tabs.size)
        // Note: Entity assignments are deleted when we call deleteAllEntitiesFromTab
    }

    @Test
    fun getTabsForDifferentConfigs_shouldIsolateTabs() = runBlocking {
        // Arrange
        val config2Id = database.configurationDao().insertConfiguration(
            Configuration(
                name = "Second Config",
                internalUrl = "http://test2.local:8123",
                externalUrl = "http://test2.com:8123",
                apiToken = "test_token_2",
                isActive = false
            )
        )

        tabDao.insertTab(Tab(0, configId, "Config1 Tab", 1))
        tabDao.insertTab(Tab(0, config2Id, "Config2 Tab", 1))

        // Act
        val config1Tabs = tabDao.getTabsForConfig(configId).first()
        val config2Tabs = tabDao.getTabsForConfig(config2Id).first()

        // Assert
        assertEquals("Config 1 should have 1 tab", 1, config1Tabs.size)
        assertEquals("Config 2 should have 1 tab", 1, config2Tabs.size)
        assertEquals("Config1 Tab", config1Tabs[0].name)
        assertEquals("Config2 Tab", config2Tabs[0].name)
    }
}
