package com.anurag.eduai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.ui.navigation.LoginNavigator
import com.anurag.eduai.ui.screens.chatbotscreen.ChatbotScreen
import com.anurag.eduai.ui.screens.login.LoginScreen
import com.anurag.eduai.ui.theme.AppDimensionProvider
import com.anurag.eduai.ui.theme.AppTheme
import com.anurag.eduai.ui.viewModel.ChatViewModel
import com.anurag.eduai.ui.viewModel.UserViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                AppDimensionProvider {
                    ChatbotScreen(
                        chatViewModel = ChatViewModel(application)
                    )
                }
            }
        }
        supportActionBar?.hide() // this hide the APP name shown on top of every screen

    }
}