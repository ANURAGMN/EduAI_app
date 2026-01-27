package com.anurag.eduai.ui.screens.simlation.component

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.LocalDimensions

@Composable
fun SimulationWebView(
    url: String,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimensions.current
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        setSupportZoom(false)
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript(
                                """
                                (function() {
                                    var style = document.createElement('style');
                                    style.innerHTML = '#status-banner, .status-banner, [id*="status"], [class*="status"], [id*="auto-mode"], [class*="auto-mode"] { display: none !important; }';
                                    document.head.appendChild(style);
                                })();
                                """.trimIndent(),
                                null
                            )
                            isLoading = false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            hasError = true
                            isLoading = false
                        }
                    }

                    loadUrl(url)
                }
            },
            update = { webView ->
                if (webView.url != url) {
                    isLoading = true
                    hasError = false
                    webView.loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(dimens.spaceSmall))
                Text(
                    text = stringResource(R.string.sim_loading_simulation),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (hasError) {
            Text(
                text = stringResource(R.string.sim_error_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}