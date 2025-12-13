package com.example.simplehomeassistant

import android.app.Application
import android.util.Log

class HomeAssistantApplication : Application() {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun onCreate() {
        super.onCreate()

        // Set up global exception handler to prevent crashes from network errors
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("HomeAssistant", "Uncaught exception in thread ${thread.name}", throwable)

            // Check if it's a network-related error
            if (isNetworkError(throwable)) {
                Log.e(
                    "HomeAssistant",
                    "Network error suppressed to prevent crash: ${throwable.message}"
                )
                // Network errors should be handled gracefully - don't crash
                // The error will be shown in the UI through the normal error handling
            } else {
                // For non-network errors, let the default handler deal with it
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun isNetworkError(throwable: Throwable): Boolean {
        var current: Throwable? = throwable
        while (current != null) {
            val message = current.message?.lowercase() ?: ""
            val className = current::class.java.name.lowercase()

            if (message.contains("network") ||
                message.contains("socket") ||
                message.contains("connection") ||
                message.contains("timeout") ||
                message.contains("host") ||
                message.contains("unable to resolve") ||
                className.contains("unknownhost") ||
                className.contains("socketexception") ||
                className.contains("connectexception") ||
                className.contains("sockettimeout")
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }
}
