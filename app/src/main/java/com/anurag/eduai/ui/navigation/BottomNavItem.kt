package com.anurag.eduai.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Progress : BottomNavItem("progress", Icons.AutoMirrored.Filled.ShowChart, "Progress")
    object Setting : BottomNavItem("setting", Icons.Default.Settings, "Setting")
}