package com.anurag.eduai.repository

import com.anurag.eduai.data.firebase.User
import com.anurag.eduai.debug.DebugLogger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    suspend fun checkUserExists(userId: String): User? {
        return try {
            val snapshot = usersCollection.document(userId).get().await()

            if (!snapshot.exists()) return null

            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            DebugLogger.errorLog("Firestore", "Error\n $e")
            null
        }
    }

    suspend fun createNewUser(user: User): Boolean {
        return try {
            val data = mapOf(
                "id" to user.id,
                "email" to user.email,
                "displayName" to user.displayName,
                "profilePictureUri" to user.profilePictureUri,
                "schoolName" to user.schoolName,
                "phoneNumber" to user.phoneNumber,
                "studentClass" to user.studentClass,
                "language" to user.language,
                "createdAt" to user.createdAt,
                "updatedAt" to user.lastLogin

            )

            usersCollection.document(user.id).set(data).await()
            true
        } catch (e: Exception) {
            DebugLogger.errorLog("Firestore", "Error\n $e")
            false
        }
    }

    suspend fun updateUserProfile(
        userId: String,
        name: String,
        phone: String,
        school: String,
        studentClass: Int,
        updatedAt: Long
    ): Boolean {
        return try {
            usersCollection.document(userId)
                .update(
                    mapOf(
                        "displayName" to name,
                        "phoneNumber" to phone,
                        "schoolName" to school,
                        "studentClass" to studentClass,
                        "updatedAt" to updatedAt
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }


}
