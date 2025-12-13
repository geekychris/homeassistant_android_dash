package com.example.simplehomeassistant.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC // Changed from BODY to reduce log spam
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS) // Reduced from 30 to fail faster
            .readTimeout(15, TimeUnit.SECONDS)    // Reduced from 30
            .writeTimeout(15, TimeUnit.SECONDS)   // Reduced from 30
            .build()
    }

    fun createApi(baseUrl: String): HomeAssistantApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(HomeAssistantApi::class.java)
    }
}
