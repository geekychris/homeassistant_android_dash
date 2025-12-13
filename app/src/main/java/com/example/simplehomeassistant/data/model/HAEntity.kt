package com.example.simplehomeassistant.data.model

import com.google.gson.annotations.SerializedName

data class HAEntity(
    @SerializedName("entity_id")
    val entityId: String,

    @SerializedName("state")
    val state: String,

    @SerializedName("attributes")
    val attributes: HAEntityAttributes,

    @SerializedName("last_changed")
    val lastChanged: String,

    @SerializedName("last_updated")
    val lastUpdated: String
) {
    val domain: String
        get() = entityId.split(".").firstOrNull() ?: ""

    val name: String
        get() = attributes.friendlyName ?: entityId

    val room: String
        get() = attributes.area ?: "Unassigned"

    fun isControllable(): Boolean {
        return domain in listOf(
            "switch",
            "light",
            "climate",
            "fan",
            "cover",
            "lock",
            "media_player"
        )
    }
}

data class HAEntityAttributes(
    @SerializedName("friendly_name")
    val friendlyName: String? = null,

    @SerializedName("area")
    val area: String? = null,

    // Light specific
    @SerializedName("brightness")
    val brightness: Int? = null,

    @SerializedName("rgb_color")
    val rgbColor: List<Int>? = null,

    @SerializedName("color_temp")
    val colorTemp: Int? = null,

    // Climate specific
    @SerializedName("temperature")
    val temperature: Float? = null,

    @SerializedName("current_temperature")
    val currentTemperature: Float? = null,

    @SerializedName("target_temp_high")
    val targetTempHigh: Float? = null,

    @SerializedName("target_temp_low")
    val targetTempLow: Float? = null,

    @SerializedName("hvac_mode")
    val hvacMode: String? = null,

    @SerializedName("hvac_modes")
    val hvacModes: List<String>? = null,

    @SerializedName("preset_mode")
    val presetMode: String? = null,

    @SerializedName("preset_modes")
    val presetModes: List<String>? = null,

    // Sensor specific
    @SerializedName("unit_of_measurement")
    val unitOfMeasurement: String? = null,

    @SerializedName("device_class")
    val deviceClass: String? = null,

    // Generic
    @SerializedName("icon")
    val icon: String? = null,

    @SerializedName("supported_features")
    val supportedFeatures: Int? = null
)

data class ServiceCallRequest(
    @SerializedName("entity_id")
    val entityId: String,

    @SerializedName("brightness")
    val brightness: Int? = null,

    @SerializedName("rgb_color")
    val rgbColor: List<Int>? = null,

    @SerializedName("temperature")
    val temperature: Float? = null,

    @SerializedName("hvac_mode")
    val hvacMode: String? = null,

    @SerializedName("preset_mode")
    val presetMode: String? = null
)
