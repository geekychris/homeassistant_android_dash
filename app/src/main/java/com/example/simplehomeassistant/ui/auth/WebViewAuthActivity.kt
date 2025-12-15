package com.example.simplehomeassistant.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.simplehomeassistant.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

/**
 * WebView-based authentication activity
 * Matches the approach used by official Home Assistant companion app
 */
class WebViewAuthActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: Toolbar

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private lateinit var baseUrl: String
    private lateinit var codeVerifier: String
    private lateinit var codeChallenge: String
    private val clientId = "https://home-assistant.io/android"
    private val redirectUri = "homeassistant://auth-callback"

    companion object {
        const val EXTRA_BASE_URL = "base_url"
        const val EXTRA_ACCESS_TOKEN = "access_token"
        const val EXTRA_REFRESH_TOKEN = "refresh_token"
        const val TAG = "WebViewAuth"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview_auth)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        toolbar = findViewById(R.id.toolbar)

        baseUrl = intent.getStringExtra(EXTRA_BASE_URL) ?: run {
            finish()
            return
        }

        // Setup toolbar with URL being authenticated
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Login to ${Uri.parse(baseUrl).host ?: "Home Assistant"}"
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Generate PKCE parameters
        codeVerifier = generateCodeVerifier()
        codeChallenge = generateCodeChallenge(codeVerifier)

        setupWebView()
        startAuthFlow()
    }

    private fun setupWebView() {
        webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: return false
                    Log.d(TAG, "URL: $url")

                    // Check if this is the redirect with authorization code
                    if (url.startsWith(redirectUri)) {
                        handleAuthCallback(url)
                        return true
                    }

                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun startAuthFlow() {
        // Build the authorization URL
        val state = generateRandomString(32)
        val authUrl = Uri.parse("$baseUrl/auth/authorize").buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()
            .toString()

        Log.d(TAG, "Loading auth URL: $authUrl")
        progressBar.visibility = View.VISIBLE
        webView.loadUrl(authUrl)
    }

    private fun handleAuthCallback(url: String) {
        val uri = Uri.parse(url)
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")

        if (code != null) {
            Log.d(TAG, "Got authorization code")
            exchangeCodeForToken(code)
        } else {
            val error = uri.getQueryParameter("error")
            Log.e(TAG, "Authorization failed: $error")
            Snackbar.make(
                findViewById(android.R.id.content),
                "Authorization failed: $error",
                Snackbar.LENGTH_LONG
            ).show()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun exchangeCodeForToken(code: String) {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val formBody = FormBody.Builder()
                    .add("grant_type", "authorization_code")
                    .add("code", code)
                    .add("client_id", clientId)
                    .add("redirect_uri", redirectUri)
                    .add("code_verifier", codeVerifier)
                    .build()

                val request = Request.Builder()
                    .url("$baseUrl/auth/token")
                    .post(formBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: "{}"

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val json = JSONObject(responseBody)
                        val accessToken = json.getString("access_token")
                        val refreshToken = json.optString("refresh_token", "")

                        Log.d(TAG, "Token exchange successful")

                        // Return tokens to calling activity
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_ACCESS_TOKEN, accessToken)
                            putExtra(EXTRA_REFRESH_TOKEN, refreshToken)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Log.e(TAG, "Token exchange failed: ${response.code} - $responseBody")
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Token exchange failed: ${response.code}",
                            Snackbar.LENGTH_LONG
                        ).show()
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Token exchange error", e)
                withContext(Dispatchers.Main) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Error: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        }
    }

    // PKCE helper functions
    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun generateRandomString(length: Int): String {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING).take(length)
    }
}
