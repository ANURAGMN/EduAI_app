package com.ncert7.aitutorandlab.ui.screens.simulation_agent.components

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import com.ncert7.aitutorandlab.debug.DebugLogger
import org.json.JSONObject

/**
 * JavaScript Bridge Interface for WebView
 * Allows HTML simulations to communicate with Android app
 * Methods annotated with @JavascriptInterface are exposed to JavaScript
 */
class SimulationJavaScriptInterface(
    private val onParamsChanged: (Map<String, Any>) -> Unit
) {

    companion object {
        private const val TAG = "SimulationJSInterface"
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Called from JavaScript when simulation parameters change
     * Example: user adjusts angle in pendulum simulation
     * @param paramsJson JSON string containing changed parameters
     */
    @JavascriptInterface
    fun onParametersChanged(paramsJson: String) {
        try {
            DebugLogger.debugLog(TAG, "Parameters changed from JS: $paramsJson")

            val jsonObject = JSONObject(paramsJson)
            val paramsMap = mutableMapOf<String, Any>()

            // Convert JSON object to Map<String, Any>
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonObject.get(key)
                paramsMap[key] = when (value) {
                    JSONObject.NULL -> Unit
                    else -> value
                }
            }

            // Post callback to main thread
            mainHandler.post {
                onParamsChanged(paramsMap)
                DebugLogger.debugLog(TAG, "Converted params to Map: $paramsMap")
            }

        } catch (e: Exception) {
            DebugLogger.errorLog(TAG, "Error parsing parameters JSON: ${e.message}")
        }
    }

    /**
     * Called from JavaScript to log debug messages
     * @param message Log message from simulation
     */
    @JavascriptInterface
    fun logDebug(message: String) {
        DebugLogger.debugLog(TAG, "JS Log: $message")
    }

    /**
     * Called from JavaScript to report errors
     * @param error Error message from simulation
     */
    @JavascriptInterface
    fun logError(error: String) {
        DebugLogger.errorLog(TAG, "JS Error: $error")
    }
}
