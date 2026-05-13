package com.ncert7.aitutorandlab.ui.screens.simulation_agent.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Custom WebViewClient to track when simulation page finishes loading
 */
class SimulationWebViewClient(
    private val onPageFinished: () -> Unit
) : WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageFinished()
    }
}

/** WebView component for rendering simulation HTML  */
@Composable
fun SimulationWebView(
    url: String,
    modifier: Modifier = Modifier,
    onParamsChanged: (Map<String, Any>) -> Unit = {},
    onPageFinished: () -> Unit = {}
) {
    AndroidView(
        factory = { context ->
            @SuppressLint("SetJavaScriptEnabled")
            val webView = WebView(context).apply {
                settings.apply {
                    // Enable JavaScript for simulations (safe - internal simulations only)
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
                webViewClient = SimulationWebViewClient(onPageFinished)

                // Add JavaScript interface for receiving parameter changes from the simulation
                addJavascriptInterface(
                    SimulationJavaScriptInterface(onParamsChanged),
                    "SimulationAndroidInterface"
                )

                loadUrl(url)
            }
            webView
        },
        modifier = modifier.fillMaxSize()
    )
}
