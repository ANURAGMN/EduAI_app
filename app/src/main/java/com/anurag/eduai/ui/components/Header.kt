package com.anurag.eduai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.BrandPrimary
import com.anurag.eduai.ui.theme.TextOnPrimary
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(title: String = "Class 7", subtitle: String = "NCERT Curriculum") {
    LargeTopAppBar(
        title = {
            Column {
                Text(
                    text =title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = TextOnPrimary.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Normal
                )
            }
        },
        navigationIcon = {
            IconButton( onClick = {}) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextOnPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = TextOnPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextOnPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = AccentBlue
        ),
        modifier = Modifier.background(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(
                    AccentBlue,
                    BrandPrimary
                )
            )
        )
    )
}
@Preview
@Composable
fun HeaderPreview() {
    Header()
}