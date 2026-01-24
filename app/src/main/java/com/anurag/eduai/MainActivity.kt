package com.anurag.eduai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.ui.navigation.LoginNavigator
import com.anurag.eduai.ui.theme.AdaptiveTheme
import com.anurag.eduai.ui.theme.AppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AdaptiveTheme {
                AppTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding() // This adds padding for status bar
                    ) {
                        LoginNavigator(userViewModel = viewModel())
                    }
                }
            }
        }
    }
}