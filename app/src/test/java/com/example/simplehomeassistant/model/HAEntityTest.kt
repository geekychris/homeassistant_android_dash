package com.example.simplehomeassistant.model

import com.example.simplehomeassistant.data.model.HAEntity
import com.example.simplehomeassistant.data.model.HAEntityAttributes
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for HAEntity model
 * Tests entity properties, domain extraction, and controllability logic
 */
class HAEntityTest {

    private fun createTestEntity(
        entityId: String,
        state: String = "off",
        friendlyName: String? = null,
        area: String? = null
    ): HAEntity {
        return HAEntity(
            entityId = entityId,
            state = state,
            attributes = HAEntityAttributes(
                friendlyName = friendlyName,
                area = area
            ),
            lastChanged = "2025-01-01T00:00:00",
            lastUpdated = "2025-01-01T00:00:00"
        )
    }

    @Test
    fun `domain should be extracted from entity ID`() {
        // Arrange & Act
        val light = createTestEntity("light.kitchen_main")
        val switch = createTestEntity("switch.bedroom_fan")
        val sensor = createTestEntity("sensor.temperature")

        // Assert
        assertEquals("light", light.domain)
        assertEquals("switch", switch.domain)
        assertEquals("sensor", sensor.domain)
    }

    @Test
    fun `name should use friendly name when available`() {
        // Arrange
        val entity = createTestEntity(
            entityId = "light.kitchen",
            friendlyName = "Kitchen Main Light"
        )

        // Act & Assert
        assertEquals("Kitchen Main Light", entity.name)
    }

    @Test
    fun `name should fallback to entity ID when no friendly name`() {
        // Arrange
        val entity = createTestEntity(
            entityId = "light.kitchen",
            friendlyName = null
        )

        // Act & Assert
        assertEquals("light.kitchen", entity.name)
    }

    @Test
    fun `room should use area from attributes`() {
        // Arrange
        val entity = createTestEntity(
            entityId = "light.main",
            area = "Kitchen"
        )

        // Act & Assert
        assertEquals("Kitchen", entity.room)
    }

    @Test
    fun `room should default to Unassigned when no area`() {
        // Arrange
        val entity = createTestEntity(
            entityId = "light.main",
            area = null
        )

        // Act & Assert
        assertEquals("Unassigned", entity.room)
    }

    @Test
    fun `lights should be controllable`() {
        // Arrange
        val light = createTestEntity("light.kitchen")

        // Act & Assert
        assertTrue("Lights should be controllable", light.isControllable())
    }

    @Test
    fun `switches should be controllable`() {
        // Arrange
        val switch = createTestEntity("switch.bedroom")

        // Act & Assert
        assertTrue("Switches should be controllable", switch.isControllable())
    }

    @Test
    fun `climate devices should be controllable`() {
        // Arrange
        val climate = createTestEntity("climate.thermostat")

        // Act & Assert
        assertTrue("Climate devices should be controllable", climate.isControllable())
    }

    @Test
    fun `fans should be controllable`() {
        // Arrange
        val fan = createTestEntity("fan.ceiling")

        // Act & Assert
        assertTrue("Fans should be controllable", fan.isControllable())
    }

    @Test
    fun `covers should be controllable`() {
        // Arrange
        val cover = createTestEntity("cover.garage")

        // Act & Assert
        assertTrue("Covers should be controllable", cover.isControllable())
    }

    @Test
    fun `locks should be controllable`() {
        // Arrange
        val lock = createTestEntity("lock.front_door")

        // Act & Assert
        assertTrue("Locks should be controllable", lock.isControllable())
    }

    @Test
    fun `media players should be controllable`() {
        // Arrange
        val mediaPlayer = createTestEntity("media_player.living_room")

        // Act & Assert
        assertTrue("Media players should be controllable", mediaPlayer.isControllable())
    }

    @Test
    fun `sensors should not be controllable`() {
        // Arrange
        val sensor = createTestEntity("sensor.temperature")

        // Act & Assert
        assertFalse("Sensors should not be controllable", sensor.isControllable())
    }

    @Test
    fun `binary sensors should not be controllable`() {
        // Arrange
        val binarySensor = createTestEntity("binary_sensor.motion")

        // Act & Assert
        assertFalse("Binary sensors should not be controllable", binarySensor.isControllable())
    }

    @Test
    fun `automations should not be controllable`() {
        // Arrange
        val automation = createTestEntity("automation.morning_routine")

        // Act & Assert
        assertFalse("Automations should not be controllable", automation.isControllable())
    }

    @Test
    fun `entity with invalid domain should not be controllable`() {
        // Arrange
        val invalid = createTestEntity("unknown.device")

        // Act & Assert
        assertFalse("Unknown domains should not be controllable", invalid.isControllable())
    }

    @Test
    fun `state should be accessible`() {
        // Arrange
        val entityOn = createTestEntity("light.kitchen", state = "on")
        val entityOff = createTestEntity("light.bedroom", state = "off")

        // Act & Assert
        assertEquals("on", entityOn.state)
        assertEquals("off", entityOff.state)
    }

    @Test
    fun `multiple entities with same domain should extract correctly`() {
        // Arrange
        val entities = listOf(
            createTestEntity("light.kitchen"),
            createTestEntity("light.bedroom"),
            createTestEntity("light.living_room")
        )

        // Act & Assert
        assertTrue("All should be lights", entities.all { it.domain == "light" })
        assertTrue("All should be controllable", entities.all { it.isControllable() })
    }

    @Test
    fun `entity ID without dot should use whole string as domain`() {
        // Arrange
        val entity = createTestEntity("invalid_entity_id")

        // Act & Assert
        assertEquals("invalid_entity_id", entity.domain)
        assertFalse("Should not be controllable", entity.isControllable())
    }
}
