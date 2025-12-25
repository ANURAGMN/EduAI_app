package com.anurag.eduai.repository

import com.anurag.eduai.data.firebase.User
import com.anurag.eduai.debug.DebugLogger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    suspend fun checkUserExists(userId: String): Boolean {
        val snapshot = usersCollection.document(userId).get().await()
        return snapshot.exists()
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
                "studentClass" to user.studentClass
            )

            usersCollection.document(user.id).set(data).await()
            true
        } catch (e: Exception) {
            DebugLogger.errorLog("Firestore", "Error\n $e")
            false
        }
    }

}
