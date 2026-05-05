package com.anurag.eduai.ui.screens.mathagentscreen.dataclass

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Message model for Math Agent screen
 * Similar to ChatMessageModel but with math-specific fields
 */
data class MathMessageModel(
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val node: String? = null, // Current state/node in the math agent workflow
    val imageUrl: String? = null, // For user-submitted images in math problems
    val isError: Boolean = false,
    val canRetry: Boolean = false
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
