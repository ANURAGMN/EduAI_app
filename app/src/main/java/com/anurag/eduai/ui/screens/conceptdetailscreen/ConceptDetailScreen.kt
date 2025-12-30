package com.anurag.eduai.ui.screens.conceptdetailscreen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.anurag.eduai.service.analytics.ScreenName
import com.anurag.eduai.service.analytics.TrackScreenEvent

@Composable
fun ConceptDetailScreen (){
    // Analytics Tracking
    TrackScreenEvent(screenName = ScreenName.CONCEPT_DETAIL)

    Text("Concept Detail Screen")
    //fetch concept details here
}