package com.anurag.eduai.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ProfileImageStorage {

    /**
     * Saves selected image into app-internal storage
     * Returns absolute file path
     */
    fun saveProfileImage(
        context: Context,
        imageUri: Uri,
        studentId: String
    ): String {
        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw IllegalStateException("Cannot open input stream")

        val profileDir = File(context.filesDir, "profile")
        if (!profileDir.exists()) profileDir.mkdirs()

        val file = File(profileDir, "profile_$studentId.jpg")

        FileOutputStream(file).use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }
}