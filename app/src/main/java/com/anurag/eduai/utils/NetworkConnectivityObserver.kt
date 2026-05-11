package com.anurag.eduai.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Observes network connectivity status and provides it as a Flow.
 */
class NetworkConnectivityObserver private constructor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    companion object {
        @Volatile
        private var INSTANCE: NetworkConnectivityObserver? = null

        fun getInstance(context: Context): NetworkConnectivityObserver {
            return INSTANCE ?: synchronized(this) {
                val instance = NetworkConnectivityObserver(context)
                INSTANCE = instance
                instance
            }
        }

        /**
         * Simple check if device is currently online
         */
        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }

    /**
     * Flow that emits true when connected to internet, false otherwise
     */
    val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Set initial value
        val initialNetwork = connectivityManager.activeNetwork
        val initialCapabilities = connectivityManager.getNetworkCapabilities(initialNetwork)
        val isInitiallyOnline = initialCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        trySend(isInitiallyOnline)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    fun register() {
        // Callback registration happens inside the flow builder when collected
    }

    fun destroy() {
        INSTANCE = null
    }
}
