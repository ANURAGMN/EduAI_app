package com.anurag.eduai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.ui.navigation.LoginNavigator
import com.anurag.eduai.ui.screens.chatbotscreen.ChatbotScreen
import com.anurag.eduai.ui.theme.AdaptiveTheme
import com.anurag.eduai.ui.theme.AppTheme
import com.anurag.eduai.ui.viewModel.ChatViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AdaptiveTheme{
                AppTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        LoginNavigator(userViewModel = viewModel())
                    }

                }
            }
        }
        supportActionBar?.hide() // this hide the APP name shown on top of every screen

    }
}