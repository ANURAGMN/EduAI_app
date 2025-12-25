package com.anurag.eduai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.ui.navigation.LoginNavigator
import com.anurag.eduai.ui.screens.login.LoginScreen
import com.anurag.eduai.ui.theme.AppTheme
import com.anurag.eduai.ui.viewModel.UserViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                LoginNavigator(
                    userViewModel = viewModel()
                )
            }
        }
        supportActionBar?.hide() // this hide the APP name shown on top of every screen

    }
}