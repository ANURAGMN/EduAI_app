package com.anurag.eduai.ui.screens.chatbotscreen.components.text

/**
 * WordPosition - Marks where a word is
 *
 * Example: "Hello world"
 * WordPosition(0, 4)   = "Hello"
 * WordPosition(6, 10)  = "world"
 */
data class WordPosition(
    val start: Int,    // Where word starts
    val end: Int       // Where word ends (inclusive)
)

/**
 * ProcessedText - The final result
 *
 * Contains:
 * - cleanText: Text without asterisks
 * - boldRanges: Which parts should be bold
 * - wordPositions: Where each word is (for highlighting)
 */
data class ProcessedText(
    val cleanText: String,                    // Text without asterisks
    val boldRanges: List<IntRange>,           // Which parts are bold
    val wordPositions: List<WordPosition>     // Where each word is
)

/**
 * TextProcessor - Process text for display
 *
 * Does TWO things:
 * 1. Find *bold* sections
 * 2. Find word boundaries
 */
class TextProcessor {

    /**
     * Process text to extract bold + words
     *
     * Input:  "The *answer* is 42"
     *
     * Returns:
     * - cleanText: "The answer is 42"
     * - boldRanges: [4..10] (answer is bold)
     * - wordPositions: [
     *     WordPosition(0, 2),   (The)
     *     WordPosition(4, 10),  (answer)
     *     WordPosition(12, 13), (is)
     *     WordPosition(15, 16)  (42)
     *   ]
     */
    fun process(text: String): ProcessedText {
        // Step 1: Find all bold sections using regex
        val boldPattern = """\*{1,2}([^*]+?)\*{1,2}""".toRegex()
        val boldMatches = boldPattern.findAll(text).toList()

        // Step 2: Build clean text and track bold ranges
        val cleanText = StringBuilder()
        val boldRanges = mutableListOf<IntRange>()

        var lastPosition = 0
        var cleanLength = 0

        boldMatches.forEach { match ->
            // Add normal text before bold section
            if (match.range.first > lastPosition) {
                val normalText = text.substring(lastPosition, match.range.first)
                cleanText.append(normalText)
                cleanLength += normalText.length
            }

            // Add bold text (without asterisks)
            val boldText = match.groupValues[1]
            val boldStart = cleanLength
            cleanText.append(boldText)
            cleanLength += boldText.length

            // Mark this as bold range
            boldRanges.add(boldStart until cleanLength)

            lastPosition = match.range.last + 1
        }

        // Add remaining text
        if (lastPosition < text.length) {
            val remainingText = text.substring(lastPosition)
            cleanText.append(remainingText)
            cleanLength += remainingText.length
        }

        // If no bold, add entire text
        if (boldMatches.isEmpty()) {
            cleanText.append(text)
            cleanLength = text.length
        }

        // Step 3: Extract word boundaries from clean text
        val cleanStr = cleanText.toString()
        val wordPositions = mutableListOf<WordPosition>()
        var wordStart = -1

        cleanStr.forEachIndexed { index, char ->
            val isWordChar = isValidWordCharacter(char)

            if (isWordChar) {
                if (wordStart == -1) {
                    wordStart = index
                }
            } else {
                if (wordStart != -1) {
                    wordPositions.add(WordPosition(wordStart, index - 1))
                    wordStart = -1
                }
            }
        }

        // Add final word
        if (wordStart != -1) {
            wordPositions.add(WordPosition(wordStart, cleanStr.length - 1))
        }

        return ProcessedText(
            cleanText = cleanStr,
            boldRanges = boldRanges,
            wordPositions = wordPositions
        )
    }

    /**
     * Check if character is part of a word
     * Works with English, Kannada, Hindi, etc.
     */
    private fun isValidWordCharacter(char: Char): Boolean = when {
        // English & common punctuation
        char.isLetterOrDigit() -> true
        char in "''-–—" -> true

        // Kannada script
        char.code in 0x0C80..0x0CFF -> true
        char == '\u200D' -> true  // Zero Width Joiner
        char == '\u0CCD' -> true  // Kannada virama

        else -> false
    }
}