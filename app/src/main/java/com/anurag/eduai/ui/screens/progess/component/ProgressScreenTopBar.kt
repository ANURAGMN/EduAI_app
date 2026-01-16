package com.anurag.eduai.ui.screens.progess.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.White

@Composable
fun ProgressScreenTopBar(
    onGoHome:() -> Unit = {},
    onGoSetting:() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF11C416),
                        Color(0xFF009358)
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.your_progress),
                    color = White,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                // Navigate to home
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Home Icon",
                    modifier = Modifier.size(20.dp)
                        .clickable(enabled = true, onClick = onGoHome),
                    tint = White,
                )
                Spacer(modifier = Modifier.width(10.dp))
                // navigate to stting
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Setting Icon",
                    modifier = Modifier.size(20.dp)
                        .clickable(enabled = true, onClick = onGoSetting),
                    tint = White
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = stringResource(R.string.last_seven_days),
                color = Color(0xFFD0D0D0),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}