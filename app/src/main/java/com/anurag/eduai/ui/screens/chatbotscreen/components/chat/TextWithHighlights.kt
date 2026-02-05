package com.anurag.eduai.ui.screens.chatbotscreen.components.chat

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.eduai.ui.screens.chatbotscreen.utility.TextProcessor
import com.anurag.eduai.ui.theme.AccentBlue
import com.anurag.eduai.ui.theme.TextPrimary
import com.anurag.eduai.ui.viewModel.TextToSpeech

/**
 * TextWithHighlights - CLEAN AND SIMPLE
 *
 * FLOW:
 * - Always processes fullText
 * - During typing: Shows typingText (partial) with bold styling
 * - TTS can highlight IMMEDIATELY as it starts speaking
 */
@Composable
fun TextWithHighlights(
    modifier: Modifier = Modifier,
    text: String,
    isTyping: Boolean = false,
    fullText: String = text, // Complete text for processing
    ttsController: TextToSpeech = viewModel(),
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    val currentWordIndex by ttsController.currentWordIndex.collectAsState()
    val ttsState by ttsController.state.collectAsState()

    // Process full text ONCE - cached until fullText changes
    val processor = remember { TextProcessor() }
    val processed = remember(fullText) {
        processor.process(fullText)
    }

    // TTS highlights when speaking this text
    val shouldHighlight = ttsState.isSpeaking &&
            ttsController.currentSpeakingText == fullText

    // Build the displayed text - NO remember here so it updates on every text change
    val styledText = buildAnnotatedString {
        // Determine what text to display
        val displayText = if (isTyping && text.isNotEmpty()) {
            // During typing: show partial text (clean it from asterisks)
            processor.process(text).cleanText
        } else {
            // After typing: show full clean text
            processed.cleanText
        }

        // Append the text
        append(displayText)

        // Apply bold styling based on fullText processing
        // We need to map bold ranges to the current displayText length
        if (isTyping) {
            // During typing: process the partial text to get its bold ranges
            val partialProcessed = processor.process(text)
            partialProcessed.boldRanges.forEach { range ->
                val start = range.first
                val end = minOf(range.last + 1, displayText.length)

                if (start < displayText.length) {
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary),
                        start,
                        end
                    )
                }
            }
        } else {
            // After typing: use full text bold ranges
            processed.boldRanges.forEach { range ->
                val start = range.first
                val end = minOf(range.last + 1, displayText.length)

                if (start < displayText.length) {
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary),
                        start,
                        end
                    )
                }
            }
        }

        // Apply TTS word highlighting
        // This works on the full text word positions
        if (shouldHighlight && currentWordIndex in processed.wordPositions.indices) {
            val word = processed.wordPositions[currentWordIndex]
            val start = word.start
            val end = minOf(word.end + 1, displayText.length)

            // Only highlight if word is visible in current display
            if (start < displayText.length) {
                addStyle(
                    SpanStyle(
                        background = AccentBlue,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    start,
                    end
                )
            }
        }
    }

    Text(
        text = styledText,
        fontSize = 40.sp,
        lineHeight = 60.sp,
        color = TextPrimary,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier,
        onTextLayout = onTextLayout
    )
}