package com.anurag.eduai.domain.chatbot.controller

import kotlinx.coroutines.*
import javax.inject.Inject

class TypingAnimationController @Inject constructor() {
    private var typingJob: Job? = null

    fun startTypingAnimation(
        fullText: String,
        scope: CoroutineScope,
        onUpdate: (typingText: String, isComplete: Boolean) -> Unit
    ) {
        typingJob?.cancel()

        typingJob = scope.launch {
            val words = fullText.split(" ")
            var currentText = ""

            words.forEachIndexed { index, word ->
                currentText += if (index == 0) word else " $word"
                onUpdate(currentText, false)
                delay(120L + (word.length * 8L).coerceAtMost(200L))
            }

            onUpdate("", true)
        }
    }

    fun cancel() {
        typingJob?.cancel()
    }
}