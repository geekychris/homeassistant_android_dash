package com.example.simplehomeassistant.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for entity utility functions
 * These run fast on your computer (no Android device needed)
 */
class EntityUtilsTest {

    @Test
    fun `entity ID should extract domain correctly`() {
        // Arrange
        val entityId = "light.kitchen_main"

        // Act
        val domain = entityId.substringBefore(".")

        // Assert
        assertEquals("light", domain)
    }

    @Test
    fun `controllable entity types should be recognized`() {
        // Arrange
        val controllableTypes = listOf("light", "switch", "climate", "cover", "fan", "lock")

        // Act & Assert
        controllableTypes.forEach { type ->
            val entityId = "$type.test_entity"
            assertTrue("$type should be controllable", isControllable(entityId))
        }
    }

    @Test
    fun `sensor entity types should not be controllable`() {
        // Arrange
        val sensorTypes = listOf("sensor", "binary_sensor")

        // Act & Assert
        sensorTypes.forEach { type ->
            val entityId = "$type.test_entity"
            assertFalse("$type should not be controllable", isControllable(entityId))
        }
    }

    @Test
    fun `friendly name should be extracted from attributes`() {
        // This is an example - you'd need to implement the actual parsing logic
        val json = """{"friendly_name": "Kitchen Light"}"""
        val friendlyName = extractFriendlyName(json)
        assertEquals("Kitchen Light", friendlyName)
    }

    // Helper functions (these would typically be in your actual code)
    private fun isControllable(entityId: String): Boolean {
        val controllableDomains =
            setOf("light", "switch", "climate", "cover", "fan", "lock", "media_player")
        val domain = entityId.substringBefore(".")
        return domain in controllableDomains
    }

    private fun extractFriendlyName(json: String): String {
        // Simplified example - actual implementation would parse JSON properly
        return json.substringAfter("\"friendly_name\": \"").substringBefore("\"")
    }
}
