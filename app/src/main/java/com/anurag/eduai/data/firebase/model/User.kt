package com.anurag.eduai.data.firebase.model

import android.os.Parcelable
import com.anurag.eduai.data.local.entities.StudentEntity
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "", // from google
    val email: String = "", // from google
    val displayName: String? = "", // from google
    val profilePictureUri: String? = "", // link: from google
    val schoolName: String = "", // input from user
    val phoneNumber: String = "", // input from user
    val studentClass: Int= 7, // input from user
    val language: String = "en", // default english
    val createdAt: Long = System.currentTimeMillis(),
    @PropertyName("updatedAt")
    val lastLogin: Long = System.currentTimeMillis(),
    val jwtToken: String = "", // JWT token from auth provider
    val appName: String = "", // app identifier for tracking which app user comes from

) : Parcelable {
    fun toStudentEntity(): StudentEntity {
        return StudentEntity(
            studentId = id,
            studentName = displayName ?: "",
            email = email,
            phoneNumber = phoneNumber,
            studentSchool = schoolName,
            language = language,
            classLevel = studentClass,
            profilePhotoUrl = profilePictureUri,
            createdAt = createdAt,
            updatedAt = lastLogin,
            isSynced = true // because this came from the server
        )
    }
}