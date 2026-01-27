package com.anurag.eduai.ui.screens.simlation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.anurag.eduai.R
import com.anurag.eduai.ui.theme.IconPrimary
import com.anurag.eduai.ui.theme.LocalDimensions
import com.anurag.eduai.ui.theme.TextPrimary

@Composable
fun SimListeningOverlay(
    text: String,
    amplitude: Float = 0f,
    onStopClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val dimens = LocalDimensions.current

    LaunchedEffect(text) {
        if (text.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(
                RoundedCornerShape(
                    topStart = dimens.cornerRadiusLarge,
                    topEnd = dimens.cornerRadiusLarge
                )
            )
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SimVoiceWaveAnimation(
                amplitude = amplitude,
                isListening = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimens.spaceMedium)
            )

            Spacer(modifier = Modifier.height(dimens.spaceSmall))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.spaceMedium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.listening),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                } else {
                    Spacer(modifier = Modifier.width(dimens.spaceExtraSmall))
                }

                IconButton(
                    onClick = onStopClick,
                    modifier = Modifier
                        .size(dimens.iconLarge)
                        .padding(dimens.spaceExtraSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.sim_stop_listening),
                        tint = IconPrimary,
                        modifier = Modifier.size(dimens.iconMedium)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.spaceExtraSmall))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .heightIn(
                        min = dimens.containerMinHeight / 3,
                        max = dimens.containerMinHeight
                    )
                    .padding(
                        horizontal = dimens.messagePadding,
                        vertical = dimens.spaceSmall
                    )
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .fillMaxWidth()
                ) {
                    if (text.isNotEmpty()) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            color = TextPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimens.spaceMedium))
        }
    }
}