package com.anurag.eduai.domain.chatbot.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ResourceController @Inject constructor() {
    private var timerJob: Job? = null
    private var conceptMapJob: Job? = null

    fun startTimer(
        scope: CoroutineScope,
        durationSeconds: Int,
        onTick: (Int) -> Unit,
        onFinish: () -> Unit
    ) {
        timerJob?.cancel()
        timerJob = scope.launch {
            for (remaining in durationSeconds downTo 0) {
                onTick(remaining)
                if (remaining > 0) {
                    delay(1000)
                }
            }
            onFinish()
        }
    }

    fun cancel() {
        timerJob?.cancel()
        conceptMapJob?.cancel()
    }
}
