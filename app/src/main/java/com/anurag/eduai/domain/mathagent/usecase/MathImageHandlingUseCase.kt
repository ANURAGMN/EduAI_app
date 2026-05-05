package com.anurag.eduai.domain.mathagent.usecase

import android.util.Base64
import com.anurag.eduai.debug.DebugLogger
import java.io.File
import javax.inject.Inject

/**
 * Use case for handling image operations in math agent
 * Handles encoding, decoding, and validation of images
 */
class MathImageHandlingUseCase @Inject constructor() {

    /**
     * Converts an image file to Base64 string
     */
    fun fileToBase64(file: File): String? {
        return try {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            DebugLogger.errorLog("MathImageHandlingUseCase", "Failed to convert image to base64: ${e.message}")
            null
        }
    }

    /**
     * Validates image file
     */
    fun isValidImageFile(file: File): Boolean {
        return try {
            file.exists() &&
            file.isFile &&
            file.length() > 0 &&
            (file.extension in listOf("jpg", "jpeg", "png", "webp"))
        } catch (e: Exception) {
            DebugLogger.errorLog("MathImageHandlingUseCase", "Image validation error: ${e.message}")
            false
        }
    }

    /**
     * Creates image URI string
     */
    fun createImageDataUri(base64: String): String {
        return "data:image/jpeg;base64,$base64"
    }

    /**
     * Clears/validates image selection
     */
    fun clearImage(imageUri: String?): String? {
        return null // Returns null to clear selection
    }
}
