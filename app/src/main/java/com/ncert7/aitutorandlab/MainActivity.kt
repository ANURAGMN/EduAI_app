package com.ncert7.aitutorandlab

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.google.android.gms.ads.MobileAds
import com.ncert7.aitutorandlab.service.logging.ErrorLoggerInitializer
import com.ncert7.aitutorandlab.service.logging.FirestoreErrorLogger
import com.ncert7.aitutorandlab.ui.navigation.LoginNavigator
import com.ncert7.aitutorandlab.ui.theme.AdaptiveTheme
import com.ncert7.aitutorandlab.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var firestoreErrorLogger: FirestoreErrorLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this)

        // Initialize Firestore Error Logger
        // This enables error logging to Firebase in both debug and release modes
        ErrorLoggerInitializer.initialize(firestoreErrorLogger)

        setContent {
            AdaptiveTheme {
                AppTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding() // This adds padding for status bar
                    ) {
                        LoginNavigator()
                    }
                }
            }
        }
    }
}