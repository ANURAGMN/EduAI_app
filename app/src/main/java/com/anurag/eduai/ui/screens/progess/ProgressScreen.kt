package com.anurag.eduai.ui.screens.progess

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anurag.eduai.ui.screens.progess.component.ProgressScreenTopBar
import com.anurag.eduai.ui.screens.progess.component.StatusCardGrid
import com.anurag.eduai.ui.theme.BackgroundSecondary

@Composable
fun ProgressScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSecondary)
    ) {
        ProgressScreenTopBar()

        Spacer(modifier = Modifier.padding(15.dp))
        Column(
            modifier = Modifier
                .background(BackgroundSecondary)
                .padding(15.dp)
        ) {
            StatusCardGrid()
        }
    }

}