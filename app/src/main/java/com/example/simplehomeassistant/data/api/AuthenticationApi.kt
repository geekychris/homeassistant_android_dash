package com.example.simplehomeassistant.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Authentication API for Home Assistant
 * Handles username/password to token exchange
 */
class AuthenticationApi {

    data class TokenResponse(
        val accessToken: String,
        val refreshToken: String,
        val expiresIn: Int
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Exchange username/password for access token using Home Assistant's login_flow API
     * This is a multi-step process:
     * 1. Initialize login flow
     * 2. Submit credentials
     * 3. Exchange authorization code for token
     */
    suspend fun exchangeCredentials(
        baseUrl: String,
        username: String,
        password: String
    ): Result<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            val clientId = "https://habitat-app.local"

            // Step 1: Initialize login flow with homeassistant provider
            // The handler parameter should be a JSON array with provider name
            val initRequestBody = JSONObject().apply {
                put("client_id", clientId)
                put("handler", JSONArray().apply { put("homeassistant") })
                put("redirect_uri", "$clientId/auth/callback")
            }.toString()

            android.util.Log.d("AuthAPI", "Init request: $initRequestBody")

            val initRequest = Request.Builder()
                .url("$baseUrl/auth/login_flow")
                .post(initRequestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val initResponse = client.newCall(initRequest).execute()
            val initBody = initResponse.body?.string() ?: "{}"

            if (!initResponse.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Failed to initialize login flow (${initResponse.code}): $initBody")
                )
            }

            val initJson = JSONObject(initBody)
            val flowId = initJson.getString("flow_id")
            val type = initJson.optString("type")

            // Check if we need to proceed with username/password
            if (type != "form") {
                return@withContext Result.failure(
                    Exception("Unexpected flow type: $type. Expected 'form' for username/password login.")
                )
            }

            // Step 2: Submit username and password
            val credentialsRequest = Request.Builder()
                .url("$baseUrl/auth/login_flow/$flowId")
                .post(
                    JSONObject().apply {
                        put("client_id", clientId)
                        put("username", username)
                        put("password", password)
                    }.toString().toRequestBody("application/json".toMediaType())
                )
                .build()

            val credentialsResponse = client.newCall(credentialsRequest).execute()
            if (!credentialsResponse.isSuccessful) {
                val errorBody = credentialsResponse.body?.string() ?: "Unknown error"
                return@withContext Result.failure(
                    Exception("Authentication failed (${credentialsResponse.code}): $errorBody")
                )
            }

            val credentialsJson = JSONObject(credentialsResponse.body?.string() ?: "{}")

            // Check if we got an authorization code
            if (credentialsJson.getString("type") != "create_entry") {
                return@withContext Result.failure(
                    Exception("Unexpected response type: ${credentialsJson.optString("type")}")
                )
            }

            val authCode = credentialsJson.getJSONObject("result").getString("code")

            // Step 3: Exchange authorization code for access token
            val tokenRequest = Request.Builder()
                .url("$baseUrl/auth/token")
                .post(
                    FormBody.Builder()
                        .add("grant_type", "authorization_code")
                        .add("code", authCode)
                        .add("client_id", clientId)
                        .build()
                )
                .build()

            val tokenResponse = client.newCall(tokenRequest).execute()
            if (!tokenResponse.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Token exchange failed: ${tokenResponse.code}")
                )
            }

            val tokenJson = JSONObject(tokenResponse.body?.string() ?: "{}")
            val token = TokenResponse(
                accessToken = tokenJson.getString("access_token"),
                refreshToken = tokenJson.optString("refresh_token", ""),
                expiresIn = tokenJson.getInt("expires_in")
            )

            Result.success(token)
        } catch (e: Exception) {
            Result.failure(Exception("Authentication error: ${e.message}", e))
        }
    }

    /**
     * Refresh an expired access token
     */
    suspend fun refreshAccessToken(
        baseUrl: String,
        refreshToken: String
    ): Result<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            val clientId = "habitat_android_${UUID.randomUUID()}"

            val formBody = FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .add("client_id", clientId)
                .build()

            val request = Request.Builder()
                .url("$baseUrl/auth/token")
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "{}")
                val tokenResponse = TokenResponse(
                    accessToken = json.getString("access_token"),
                    refreshToken = json.optString("refresh_token", refreshToken),
                    expiresIn = json.getInt("expires_in")
                )
                Result.success(tokenResponse)
            } else {
                Result.failure(Exception("Token refresh failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Refresh error: ${e.message}", e))
        }
    }

    suspend fun testConnection(url: String, token: String): Result<Int> {
        return try {
            // Clean the URL: trim whitespace and remove trailing slash
            val cleanUrl = url.trim().trimEnd('/')
            val cleanToken = token.trim()

            android.util.Log.d("TestConnection", "Testing URL: [$cleanUrl]")
            android.util.Log.d("TestConnection", "Token length: ${cleanToken.length}")

            val request = Request.Builder()
                .url("$cleanUrl/api/states")
                .get()
                .addHeader("Authorization", "Bearer $cleanToken")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            android.util.Log.d("TestConnection", "Response code: ${response.code}")
            android.util.Log.d("TestConnection", "Response length: ${responseBody.length}")
            android.util.Log.d("TestConnection", "Response starts: ${responseBody.take(50)}")

            if (response.isSuccessful) {
                try {
                    // Parse JSON array to count entities
                    val jsonArray = JSONArray(responseBody)
                    Result.success(jsonArray.length())
                } catch (e: Exception) {
                    // If parsing fails, provide more context
                    val preview = responseBody.take(200).replace("\n", "\\n")
                    android.util.Log.e("TestConnection", "JSON parse error", e)
                    Result.failure(Exception("Invalid JSON response. Got: $preview..."))
                }
            } else {
                // Include response body for debugging
                val errorPreview = responseBody.take(200).replace("\n", "\\n")
                Result.failure(Exception("HTTP ${response.code}: ${response.message}. Response: $errorPreview"))
            }
        } catch (e: Exception) {
            android.util.Log.e("TestConnection", "Connection failed", e)
            Result.failure(Exception("Connection error: ${e.message}", e))
        }
    }
}
