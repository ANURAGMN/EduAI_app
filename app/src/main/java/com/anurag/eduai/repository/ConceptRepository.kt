package com.anurag.eduai.repository

import com.anurag.eduai.data.local.SharedPreferenceUtils
import com.anurag.eduai.data.local.dao.ConceptDao
import com.anurag.eduai.sync.FirebaseConceptMapper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ConceptRepository(
    private val firestore: FirebaseFirestore,
    private val conceptDao: ConceptDao,
    private val sharedPreferenceUtils: SharedPreferenceUtils
) {
    private val collection = firestore.collection("concepts")

    /**
     * Manual sync Logic:
     * Fetches all the concept records from Firestore
     * rewrite all the concept of localDB
     */
    suspend fun syncManually() = withContext(Dispatchers.IO) {
        val snapshot = collection.get().await()
        val mapped = snapshot.documents.map(FirebaseConceptMapper::map)

        conceptDao.insertConcepts(mapped)
        sharedPreferenceUtils.updateLastSyncTime()
    }
    /**
     * Weekly sync Logic:
     * only fetches the records that are updated after last sync timestamp.
     */
    suspend fun syncWeekly() = withContext(Dispatchers.IO) {
        val lastSync = sharedPreferenceUtils.getLastSyncTime()

        val snapshot = collection
            .whereGreaterThan("updatedAt", lastSync)
            .get()
            .await()

        val updates = snapshot.documents.map(FirebaseConceptMapper::map)

        conceptDao.insertConcepts(updates)
        sharedPreferenceUtils.updateLastSyncTime()
    }

    /**
     * Fetch concepts from localDB
     */
    fun getConceptsForChapter(chapterId: String) =
        conceptDao.getConceptsForChapter(chapterId)
}