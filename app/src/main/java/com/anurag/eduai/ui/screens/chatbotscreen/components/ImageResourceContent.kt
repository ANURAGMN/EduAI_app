package com.anurag.eduai.ui.screens.chatbotscreen.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Simple Image Content using WebView - No external dependencies needed!
 */
@Composable
fun ImageResourceContent(
    imageUrl: String,
    description: String?,
    modifier: Modifier = Modifier
) {
    var zoomLevel by remember { mutableIntStateOf(100) }

    Box(modifier = modifier.fillMaxSize()) {
        // WebView to display the image
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.apply {
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        setSupportZoom(true)
                    }

                    // Load HTML with the image
                    loadDataWithBaseURL(
                        null,
                        """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <style>
                                body {
                                    margin: 0;
                                    padding: 0;
                                    display: flex;
                                    justify-content: center;
                                    align-items: center;
                                    min-height: 100vh;
                                    background-color: #ffffff;
                                }
                                img {
                                    max-width: 100%;
                                    height: auto;
                                    display: block;
                                }
                            </style>
                        </head>
                        <body>
                            <img src="$imageUrl" alt="Image">
                        </body>
                        </html>
                        """.trimIndent(),
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            update = { webView ->
                // Update zoom when zoomLevel changes
                webView.setInitialScale(zoomLevel)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Zoom controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zoom In
            IconButton(
                onClick = {
                    zoomLevel = (zoomLevel + 25).coerceAtMost(300)
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom In",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Zoom Out
            IconButton(
                onClick = {
                    zoomLevel = (zoomLevel - 25).coerceAtLeast(50)
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = "Zoom Out",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

    }
}