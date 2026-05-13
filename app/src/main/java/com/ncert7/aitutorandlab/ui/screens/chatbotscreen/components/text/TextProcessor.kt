package com.ncert7.aitutorandlab.ui.screens.chatbotscreen.components.text

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
        //Parse text and track bold sections
        val cleanText = StringBuilder()
        val boldRanges = mutableListOf<IntRange>()

        var i = 0
        var cleanLength = 0
        var boldStart: Int? = null  // Track where bold section starts in clean text

        while (i < text.length) {
            // Check for ** (double asterisk)
            if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
                if (boldStart == null) {
                    // Start bold section
                    boldStart = cleanLength
                    i += 2  // Skip **
                } else {
                    // End bold section
                    if (cleanLength > boldStart) {
                        boldRanges.add(boldStart until cleanLength)
                    }
                    boldStart = null
                    i += 2  // Skip **
                }
            }
            // Check for * (single asterisk) - only if not already in bold
            else if (text[i] == '*' && (i == 0 || text[i - 1] != '*') && (i + 1 >= text.length || text[i + 1] != '*')) {
                if (boldStart == null) {
                    // Start bold section
                    boldStart = cleanLength
                    i += 1  // Skip *
                } else {
                    // End bold section
                    if (cleanLength > boldStart) {
                        boldRanges.add(boldStart until cleanLength)
                    }
                    boldStart = null
                    i += 1  // Skip *
                }
            }
            // Regular character
            else {
                cleanText.append(text[i])
                cleanLength++
                i++
            }
        }

        // If bold section was never closed, close it at the end
        if (boldStart != null && cleanLength > boldStart) {
            boldRanges.add(boldStart until cleanLength)
        }

        // Extract word boundaries from clean text
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