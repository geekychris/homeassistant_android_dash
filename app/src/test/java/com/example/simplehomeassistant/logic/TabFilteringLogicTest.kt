package com.example.simplehomeassistant.logic

import com.example.simplehomeassistant.data.model.HAEntity
import com.example.simplehomeassistant.data.model.HAEntityAttributes
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for tab filtering logic
 * These test the core logic of how entities are filtered by tabs
 */
class TabFilteringLogicTest {

    private fun createEntity(entityId: String, isControllable: Boolean = true): HAEntity {
        return HAEntity(
            entityId = entityId,
            state = "off",
            attributes = HAEntityAttributes(friendlyName = entityId),
            lastChanged = "2025-01-01",
            lastUpdated = "2025-01-01"
        )
    }

    @Test
    fun `All tab should show only controllable entities`() {
        // Arrange
        val allEntities = listOf(
            createEntity("light.kitchen", isControllable = true),
            createEntity("switch.bedroom", isControllable = true),
            createEntity("sensor.temperature", isControllable = false),
            createEntity("binary_sensor.motion", isControllable = false)
        )

        // Act
        val filteredEntities = allEntities.filter { entity ->
            entity.isControllable()
        }

        // Assert
        assertEquals("All tab should show 2 controllable entities", 2, filteredEntities.size)
        assertTrue(filteredEntities.all { it.isControllable() })
        assertFalse(filteredEntities.any { it.entityId.startsWith("sensor.") })
        assertFalse(filteredEntities.any { it.entityId.startsWith("binary_sensor.") })
    }

    @Test
    fun `custom tab should show assigned entities including sensors`() {
        // Arrange
        val allEntities = listOf(
            createEntity("light.kitchen"),
            createEntity("switch.bedroom"),
            createEntity("sensor.temperature"),
            createEntity("binary_sensor.motion")
        )
        val assignedEntityIds = setOf("light.kitchen", "sensor.temperature")

        // Act
        val filteredEntities = allEntities.filter { it.entityId in assignedEntityIds }

        // Assert
        assertEquals("Custom tab should show 2 assigned entities", 2, filteredEntities.size)
        assertTrue("Should include light", filteredEntities.any { it.entityId == "light.kitchen" })
        assertTrue(
            "Should include sensor",
            filteredEntities.any { it.entityId == "sensor.temperature" })
    }

    @Test
    fun `entities should be sorted alphabetically by entity ID`() {
        // Arrange
        val unsortedEntities = listOf(
            createEntity("switch.zzz"),
            createEntity("light.aaa"),
            createEntity("climate.mmm"),
            createEntity("light.bbb")
        )

        // Act
        val sortedEntities = unsortedEntities.sortedBy { it.entityId }

        // Assert
        assertEquals("climate.mmm", sortedEntities[0].entityId)
        assertEquals("light.aaa", sortedEntities[1].entityId)
        assertEquals("light.bbb", sortedEntities[2].entityId)
        assertEquals("switch.zzz", sortedEntities[3].entityId)
    }

    @Test
    fun `empty tab should return no entities`() {
        // Arrange
        val allEntities = listOf(
            createEntity("light.kitchen"),
            createEntity("switch.bedroom")
        )
        val emptyTabAssignments = emptySet<String>()

        // Act
        val filteredEntities = allEntities.filter { it.entityId in emptyTabAssignments }

        // Assert
        assertEquals(0, filteredEntities.size)
    }

    @Test
    fun `tab with non-existent entities should return empty list`() {
        // Arrange
        val allEntities = listOf(
            createEntity("light.kitchen"),
            createEntity("switch.bedroom")
        )
        val nonExistentIds = setOf("light.garage", "switch.basement")

        // Act
        val filteredEntities = allEntities.filter { it.entityId in nonExistentIds }

        // Assert
        assertEquals(0, filteredEntities.size)
    }

    @Test
    fun `tab with mix of existing and non-existing entities should return only existing`() {
        // Arrange
        val allEntities = listOf(
            createEntity("light.kitchen"),
            createEntity("switch.bedroom")
        )
        val mixedIds = setOf("light.kitchen", "light.garage", "sensor.nonexistent")

        // Act
        val filteredEntities = allEntities.filter { it.entityId in mixedIds }

        // Assert
        assertEquals(1, filteredEntities.size)
        assertEquals("light.kitchen", filteredEntities[0].entityId)
    }

    @Test
    fun `filtering should preserve entity order after sort`() {
        // Arrange
        val allEntities = listOf(
            createEntity("light.kitchen"),
            createEntity("light.bedroom"),
            createEntity("light.living_room"),
            createEntity("switch.garage")
        ).sortedBy { it.entityId }

        val assignedIds = setOf("light.living_room", "light.bedroom")

        // Act
        val filteredEntities = allEntities.filter { it.entityId in assignedIds }

        // Assert
        assertEquals(2, filteredEntities.size)
        // Should maintain alphabetical order
        assertEquals("light.bedroom", filteredEntities[0].entityId)
        assertEquals("light.living_room", filteredEntities[1].entityId)
    }

    @Test
    fun `single entity update should replace in list`() {
        // Arrange
        val entities = listOf(
            createEntity("light.kitchen"),
            createEntity("light.bedroom"),
            createEntity("light.living_room")
        )
        val updatedEntity = HAEntity(
            entityId = "light.bedroom",
            state = "on", // Changed from "off"
            attributes = HAEntityAttributes(friendlyName = "light.bedroom"),
            lastChanged = "2025-01-02",
            lastUpdated = "2025-01-02"
        )

        // Act
        val updatedList = entities.map { entity ->
            if (entity.entityId == updatedEntity.entityId) {
                updatedEntity
            } else {
                entity
            }
        }

        // Assert
        assertEquals(3, updatedList.size)
        assertEquals("on", updatedList.find { it.entityId == "light.bedroom" }?.state)
        assertEquals("off", updatedList.find { it.entityId == "light.kitchen" }?.state)
        assertEquals("off", updatedList.find { it.entityId == "light.living_room" }?.state)
    }

    @Test
    fun `tab filtering with large entity set should be efficient`() {
        // Arrange
        val largeEntitySet = (1..1000).map {
            createEntity("light.entity_$it")
        }
        val assignedIds = (1..50).map { "light.entity_$it" }.toSet()

        // Act
        val startTime = System.currentTimeMillis()
        val filteredEntities = largeEntitySet.filter { it.entityId in assignedIds }
        val duration = System.currentTimeMillis() - startTime

        // Assert
        assertEquals(50, filteredEntities.size)
        assertTrue("Filtering should be fast (< 100ms)", duration < 100)
    }

    @Test
    fun `grouping by room should work correctly`() {
        // Arrange
        val entities = listOf(
            HAEntity(
                "light.kitchen_1",
                "off",
                HAEntityAttributes(area = "Kitchen"),
                "time",
                "time"
            ),
            HAEntity(
                "light.kitchen_2",
                "off",
                HAEntityAttributes(area = "Kitchen"),
                "time",
                "time"
            ),
            HAEntity(
                "light.bedroom_1",
                "off",
                HAEntityAttributes(area = "Bedroom"),
                "time",
                "time"
            ),
            HAEntity(
                "switch.bedroom_1",
                "off",
                HAEntityAttributes(area = "Bedroom"),
                "time",
                "time"
            )
        )

        // Act
        val groupedByRoom = entities.groupBy { it.room }

        // Assert
        assertEquals(2, groupedByRoom.size)
        assertEquals(2, groupedByRoom["Kitchen"]?.size)
        assertEquals(2, groupedByRoom["Bedroom"]?.size)
    }
}
