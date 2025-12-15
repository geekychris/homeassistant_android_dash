package com.example.simplehomeassistant.data.remote

import com.example.simplehomeassistant.data.model.HAEntity
import com.example.simplehomeassistant.data.model.EntityEvent
import com.example.simplehomeassistant.data.model.ServiceCallRequest
import retrofit2.Response
import retrofit2.http.*

interface HomeAssistantApi {

    @GET("api/states")
    suspend fun getStates(
        @Header("Authorization") authorization: String
    ): Response<List<HAEntity>>

    @GET("api/states/{entity_id}")
    suspend fun getEntityState(
        @Header("Authorization") authorization: String,
        @Path("entity_id") entityId: String
    ): Response<HAEntity>

    @POST("api/services/{domain}/turn_on")
    suspend fun turnOn(
        @Header("Authorization") authorization: String,
        @Path("domain") domain: String,
        @Body request: ServiceCallRequest
    ): Response<List<HAEntity>>

    @POST("api/services/{domain}/turn_off")
    suspend fun turnOff(
        @Header("Authorization") authorization: String,
        @Path("domain") domain: String,
        @Body request: ServiceCallRequest
    ): Response<List<HAEntity>>

    @POST("api/services/{domain}/toggle")
    suspend fun toggle(
        @Header("Authorization") authorization: String,
        @Path("domain") domain: String,
        @Body request: ServiceCallRequest
    ): Response<List<HAEntity>>

    @POST("api/services/climate/set_temperature")
    suspend fun setTemperature(
        @Header("Authorization") authorization: String,
        @Body request: ServiceCallRequest
    ): Response<List<HAEntity>>

    @POST("api/services/climate/set_hvac_mode")
    suspend fun setHvacMode(
        @Header("Authorization") authorization: String,
        @Body request: ServiceCallRequest
    ): Response<List<HAEntity>>

    @POST("api/services/light/turn_on")
    suspend fun setLightAttributes(
        @Header("Authorization") authorization: String,
        @Body request: ServiceCallRequest
    ): Response<List<HAEntity>>

    @GET("api/history/period/{timestamp}")
    suspend fun getHistory(
        @Header("Authorization") authorization: String,
        @Path("timestamp") timestamp: String,
        @Query("filter_entity_id") filterEntityId: String? = null,
        @Query("end_time") endTime: String? = null
    ): Response<List<List<EntityEvent>>>

    @GET("api/logbook")
    suspend fun getLogbook(
        @Header("Authorization") authorization: String,
        @Query("start_time") startTime: String,
        @Query("end_time") endTime: String? = null
    ): Response<List<EntityEvent>>
}
