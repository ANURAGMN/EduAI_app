package com.anurag.eduai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.TextOnPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Header(
    title: String = "Class 7",
    onBackClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior
) {
   LargeTopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnPrimary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextOnPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = Transparent,
            scrolledContainerColor = Transparent
        ),
        modifier = Modifier.background(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(
                    HeaderGradientStart,
                    HeaderGradientEnd
                )
            )
        ),
        scrollBehavior = scrollBehavior
    )
}