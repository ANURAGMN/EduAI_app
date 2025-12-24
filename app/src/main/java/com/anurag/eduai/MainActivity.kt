package com.anurag.eduai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.anurag.eduai.ui.screens.login.LoginScreen
import com.anurag.eduai.ui.theme.AppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                LoginScreen()
            }
        }
        supportActionBar?.hide() // this hide the APP name shown on top of every screen

    }
}