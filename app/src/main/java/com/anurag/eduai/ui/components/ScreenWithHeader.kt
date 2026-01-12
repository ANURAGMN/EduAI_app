package com.anurag.eduai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.anurag.eduai.ui.theme.BackgroundPrimary
import com.anurag.eduai.ui.theme.LocalDimensions


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenWithHeader(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit = {},
    extraContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable () -> Unit, // mostly a lazy list of the subjects, chapters, etc.
    ) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val dimens =LocalDimensions.current
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Header(
                title = title,
                subtitle= subtitle,
                onBackClick = onBackClick,
                scrollBehavior = scrollBehavior,
                extraContent = extraContent,
                )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundPrimary)
                .padding(paddingValues)
        ) {
            content()
        }
    }
}