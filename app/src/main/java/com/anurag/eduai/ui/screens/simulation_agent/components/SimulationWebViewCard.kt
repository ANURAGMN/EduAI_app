package com.anurag.eduai.ui.screens.simulation_agent.components

import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.anurag.eduai.R

@Composable
fun SimulationWebViewCard(
    visible: Boolean,
    simulationUrls: List<String>,
    onClose: () -> Unit,
    onParamsChanged: (Map<String, Any>) -> Unit = {},
    blurBackground: Boolean = false,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && simulationUrls.isNotEmpty(),
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = modifier.fillMaxSize().zIndex(1f)
    ) { Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) }

    AnimatedVisibility(
        visible = visible && simulationUrls.isNotEmpty(),
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(animationSpec = tween(300)),
        exit = scaleOut(targetScale = 0.8f, animationSpec = tween(200)) +
                fadeOut(animationSpec = tween(200)),
        modifier = modifier.fillMaxSize().zIndex(2f)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (simulationUrls.size) {
                        1 -> SimulationWebView(url = simulationUrls[0], onParamsChanged = onParamsChanged)
                        2 -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = stringResource(R.string.sim_before_label),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    SimulationWebView(url = simulationUrls[0], onParamsChanged = {})
                                }
                                Text(
                                    text = stringResource(R.string.sim_after_label),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    SimulationWebView(url = simulationUrls[1], onParamsChanged = onParamsChanged)
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        shape = MaterialTheme.shapes.small,
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.sim_close_simulation),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulationWebView(
    url: String,
    onParamsChanged: (Map<String, Any>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
                webViewClient = WebViewClient()
                addJavascriptInterface(
                    SimulationJavaScriptInterface(onParamsChanged),
                    "SimulationAndroidInterface"
                )
                loadUrl(url)
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

/**
 * JavaScript Interface for communication between HTML simulation and Android.
 *
 * The simulation HTML should call:
 *   SimulationAndroidInterface.onParametersChanged(JSON.stringify(params))
 *
 * IMPORTANT: @JavascriptInterface callbacks run on a background thread.
 * We post back to the main thread before invoking the Compose state callback.
 */
class SimulationJavaScriptInterface(
    private val onParamsChanged: (Map<String, Any>) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @android.webkit.JavascriptInterface
    fun onParametersChanged(paramsJson: String) {
        try {
            val gson = com.google.gson.Gson()
            val mapType = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            val params: Map<String, Any> = gson.fromJson(paramsJson, mapType)
            // Post to main thread — required for safe Compose state updates
            mainHandler.post { onParamsChanged(params) }
        } catch (e: Exception) {
            android.util.Log.e("SimulationJS", "Error parsing parameters: ${e.message}")
        }
    }
}