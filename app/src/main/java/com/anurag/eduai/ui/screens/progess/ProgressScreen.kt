package com.anurag.eduai.ui.screens.progess

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent
import com.anurag.eduai.ui.screens.progess.component.ProgressScreenTopBar
import com.anurag.eduai.ui.screens.progess.component.ShareButton
import com.anurag.eduai.ui.screens.progess.component.SkillsProgressSection
import com.anurag.eduai.ui.screens.progess.component.StatusCardGrid
import com.anurag.eduai.ui.screens.progess.component.WeeklyActivitySection
import com.anurag.eduai.ui.theme.BackgroundSecondary

@Composable
fun ProgressScreen(

)
{
    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.PROGRESS)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSecondary)
            .verticalScroll(rememberScrollState())
    ) {
        ProgressScreenTopBar()

        Spacer(modifier = Modifier.padding(15.dp))
        Column(
            modifier = Modifier
                .background(BackgroundSecondary)
                .padding(15.dp)
        ) {
            StatusCardGrid()
            WeeklyActivitySection()

            Spacer(modifier = Modifier.height(25.dp))

            SkillsProgressSection()

            Spacer(modifier = Modifier.height(20.dp))

            ShareButton()

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

}