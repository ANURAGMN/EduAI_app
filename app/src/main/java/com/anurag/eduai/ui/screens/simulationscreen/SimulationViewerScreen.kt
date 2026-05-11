package com.anurag.eduai.ui.screens.simulationscreen

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.anurag.eduai.R
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextOnPrimary

/**
 * SimulationViewerScreen displays a single simulation in a WebView
 *
 * @param simulationId The ID of the simulation to display
 * @param htmlFileName The HTML file name to load from assets
 * @param simulationTitle The title of the simulation
 * @param onBackClick Callback function to be invoked when the back button is clicked
 */
@Composable
fun SimulationViewerScreen(
    simulationId: String,
    htmlFileName: String,
    simulationTitle: String,
    onBackClick: () -> Unit = {},
    onSimulationLoaded: ((simulationId: String) -> Unit)? = null  // Called when WebView finishes loading
) {
    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.SIMULATIONVIEWER)

    val dimens = LocalDimensions.current
    val context = LocalContext.current
    var isFullscreen by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HeaderGradientStart)
    ) {
        // Header (hidden in fullscreen)
        if (!isFullscreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.spaceSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = TextOnPrimary,
                        modifier = Modifier.size(dimens.iconMedium)
                    )
                }

                Text(
                    text = simulationTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextOnPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Reset button
                IconButton(onClick = { webView?.reload() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.reset),
                        tint = TextOnPrimary,
                        modifier = Modifier.size(dimens.iconMedium)
                    )
                }

                // Fullscreen toggle button
                IconButton(onClick = { isFullscreen = !isFullscreen }) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullscreen)
                            stringResource(R.string.exit_fullscreen)
                        else
                            stringResource(R.string.fullscreen),
                        tint = TextOnPrimary,
                        modifier = Modifier.size(dimens.iconMedium)
                    )
                }
            }
        }

        // WebView
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.White)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            setSupportZoom(true)
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                super.onPageFinished(view, url)
                                // Notify caller so progress can be tracked (e.g. markSimulationUrlCompleted)
                                onSimulationLoaded?.invoke(simulationId)
                            }
                        }
                        webChromeClient = WebChromeClient()

                        // Load HTML file from assets
                        loadUrl("file:///android_asset/$htmlFileName")

                        webView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Floating controls in fullscreen mode
            if (isFullscreen) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(dimens.spaceSmall)
                ) {
                    // Reset button
                    IconButton(
                        onClick = { webView?.reload() },
                        modifier = Modifier.background(
                            color = HeaderGradientStart.copy(alpha = 0.8f),
                            shape = MaterialTheme.shapes.small
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.reset),
                            tint = TextOnPrimary
                        )
                    }

                    // Exit fullscreen button
                    IconButton(
                        onClick = { isFullscreen = false },
                        modifier = Modifier.background(
                            color = HeaderGradientStart.copy(alpha = 0.8f),
                            shape = MaterialTheme.shapes.small
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = stringResource(R.string.exit_fullscreen),
                            tint = TextOnPrimary
                        )
                    }
                }
            }
        }
    }

    // Cleanup WebView when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
        }
    }
}
