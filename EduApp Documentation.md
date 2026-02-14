
# Sync Logic

`location:` `/sync/`

### **Files**
```kotlin
sync
|- FirebaseChapterMapper.kt
|- FirebaseConceptMapper.kt
|- FirebaseSubjectMapper.kt
|- FirebaseSyncmanager.kt
|- WeeklySyncWorker.kt
```
- **FirebaseChapterMapper**
- contains only one function: 
	- map()
		- input param: document: DocumentSnapshot from firebase
		- it takes a firestore document and pulls out chapter_id, subject_id, unit_name, conceptCount from it. then it creates a ChapterEntity object and return it. if any required field is missing it will throw error
		- used in: `FirebaseSyncManager.kt` `/sync/FirebaseSyncManager.kt`

- **FirebaseConceptMapper**
- contains one function and one sealed class:
	- map()
		- input param: document: DocumentSnapshot from firebase
		- it reads the document and extract concept fields like summary, detail, concept_name, chapter_id, type, simulation_url etc. it combines summary and detail into one string. it also check the type field using ConceptType sealed class to know if its SIMULATION or STUDY. then returns a ConceptEntity object. throws error if required fields are missing
		- used in: `FirebaseSyncManager.kt` `/sync/FirebaseSyncManager.kt`
	- ConceptType (sealed class)
		- has two object types Simulation and Study
		- has a companion function from() which takes a raw string and returns the matching ConceptType. if string dont match it throws error
		- used in: `FirebaseConceptMapper.kt` `/sync/FirebaseConceptMapper.kt` (used internally in map function)

- **FirebaseSubjectMapper**
- contains only one function:
	- map()
		- input param: document: DocumentSnapshot from firebase
		- it reads the document and extract subject_id, class_id, subjectCount from it. it also capitalise the first letter of subject name. then it creates a SubjectEntity and return it. throws error if subject_id is not found
		- used in: `FirebaseSyncManager.kt` `/sync/FirebaseSyncManager.kt`

- **FirebaseSyncManager**
- constructor params: firestore (FirebaseFirestore), subjectDao (SubjectDao), chapterDao (ChapterDao), conceptDao (ConceptDao)
- contains three functions:
	- syncAllContent()
		- input param: none
		- return type: SyncResult
		- it fetches all documents from the "Concept" collection in firestore. then it loops through each document and uses the three mapper classes (FirebaseSubjectMapper, FirebaseChapterMapper, FirebaseConceptMapper) to create entities. it collects unique subjects and chapters and all concepts. after that it inserts them into local room database in order subjects then chapters then concepts. returns SyncResult with success or failure
		- used in: `UserViewModel.kt` `/ui/viewModel/UserViewModel.kt`, `WeeklySyncWorker.kt` `/sync/WeeklySyncWorker.kt`
	- syncSubjectContent()
		- input param: subjectId: String
		- return type: SyncResult
		- it fetches all concepts from firestore where subject_id matches the given subjectId. then it maps chapters and concepts from the documents and insert them into local database. returns SyncResult
		- used in: `UserViewModel.kt` `/ui/viewModel/UserViewModel.kt`
	- syncChapterContent()
		- input param: chapterId: String
		- return type: SyncResult
		- it fetches all concepts from firestore where chapter_id matches the given chapterId. maps each document to ConceptEntity and inserts into local database. returns SyncResult
		- used in: not used anywhere currently outside of FirebaseSyncManager itself

- **SyncResult** (data class, defined in FirebaseSyncManager.kt)
	- fields: success (Boolean), message (String)
	- simple data class to hold result of sync operation weather it passed or failed

- **WeeklySyncWorker**
- extends CoroutineWorker
- constructor params: appContext (Context), workerParams (WorkerParameters)
- contains one function:
	- doWork()
		- input param: none (overrides parent)
		- return type: Result
		- it gets the local database instance and creates a FirebaseSyncManager with the daos. then calls syncAllContent() to sync everything. after sync it also writes a test entry to "worker_test" collection in firestore with current time and device model. if something goes wrong it returns Result.retry() so workmanager will try again later
		- used in: `EduAiApplication.kt` `/EduAiApplication.kt` (scheduled as periodic work request)