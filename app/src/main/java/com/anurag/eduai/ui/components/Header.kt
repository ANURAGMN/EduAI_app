package com.anurag.eduai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.HeaderGradientEnd
import com.anurag.eduai.ui.theme.HeaderGradientStart
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextOnPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Header(
    title: String = "Class 7",
    onBackClick: () -> Unit = {},
    onGoHome:() -> Unit = {},
    onGoSetting:() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior
) {
    val dimens = LocalDimensions.current
   LargeTopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextOnPrimary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = TextOnPrimary,
                    modifier = Modifier.size(dimens.iconMedium)
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
       actions = {
           IconButton(onClick = {}) {
               Icon(
                   imageVector = Icons.Default.Home,
                   contentDescription = stringResource(R.string.home),
                   tint = TextOnPrimary,
                   modifier = Modifier.size(dimens.iconMedium)
                       .clickable(onClick = onGoHome)
               )
           }
           IconButton(onClick = {}) {
               Icon(
                   imageVector = Icons.Default.Settings,
                   contentDescription = stringResource(R.string.settings),
                   tint = TextOnPrimary,
                   modifier = Modifier.size(dimens.iconMedium)
                       .clickable(onClick = onGoSetting)
               )
           }
       },
        scrollBehavior = scrollBehavior
    )
}