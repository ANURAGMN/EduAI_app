package com.anurag.eduai.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.anurag.eduai.ui.screens.home.components.HomeScreenTopBar

@Preview
@Composable
fun HomeScreen() {
    Column {
        HomeScreenTopBar()
        // Rest of your screen
    }
}