package ubb.thesis.david.data.adapters

import androidx.work.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import ubb.thesis.david.data.utils.asDataMapping
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.extractLandmarkData
import ubb.thesis.david.data.utils.extractSessionData
import ubb.thesis.david.data.workers.DeleteWorker
import ubb.thesis.david.data.workers.DeleteWorker.Companion.ARG_PHOTO_ID
import ubb.thesis.david.data.workers.DeleteWorker.Companion.ARG_USER_ID
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.entities.Backup
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.entities.Session
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FirebaseDataAdapter : CloudDataSource {

    private val storage = FirebaseFirestore.getInstance()

    override fun getUserSessions(userId: String): Maybe<List<Session>> {
        val retrieveSessionsTask: MaybeSubject<List<Session>> = MaybeSubject.create()

        storage.collection("$ROOT/$userId/$COLL_SESSIONS").get()
                .addOnSuccessListener { querySnapshot ->
                    val sessions = querySnapshot.documents.map { it.extractSessionData() }
                    logEvent("Retrieved sessions $sessions successfully.")
                    retrieveSessionsTask.onSuccess(sessions)
                }
                .addOnFailureListener { error ->
                    logEvent("Failed to retrieve sessions with error ${error.message}")
                    retrieveSessionsTask.onError(error)
                }

        return retrieveSessionsTask
    }

    override fun getSessionDetails(userId: String, sessionId: String): Maybe<Map<Landmark, Discovery?>> {
        val retrieveDetailsTask: MaybeSubject<Map<Landmark, Discovery?>> = MaybeSubject.create()

        storage.collection("$ROOT/$userId/$COLL_SESSIONS/$sessionId/$COLL_LANDMARKS").get()
                .addOnSuccessListener { querySnapshot ->
                    val data = querySnapshot.documents.map { it.extractLandmarkData() }.toMap()
                    logEvent("Retrieved cloud backup successfully: $data")
                    retrieveDetailsTask.onSuccess(data)
                }.addOnFailureListener { error ->
                    logEvent("Failed to retrieve session details with error ${error.message}")
                    retrieveDetailsTask.onError(error)
                }

        return retrieveDetailsTask
    }

    override fun createSession(session: Session, landmarks: List<Landmark>): Single<String> {
        val creationTask = SingleSubject.create<String>()

        val newSessionRef = storage.collection("$ROOT/${session.userId}/$COLL_SESSIONS").document()
        val landmarksRef = newSessionRef.collection(COLL_LANDMARKS)

        storage.runTransaction { transaction ->
            transaction.set(newSessionRef, session.asDataMapping())

            for (landmark in landmarks) {
                val documentRef = landmarksRef.document(landmark.id)
                transaction.set(documentRef, landmark.asDataMapping())
            }

        }.addOnSuccessListener {
            logEvent("Session $session has been created successfully")
            creationTask.onSuccess(newSessionRef.id)
        }.addOnFailureListener { ex ->
            logEvent("Session creation has failed with the following error: ${ex.message}")
            creationTask.onError(ex)
        }

        return creationTask
    }

    override fun updateSessionBackup(backup: Backup): Completable {
        val updateTask = CompletableSubject.create()

        val sessionRef = storage.document("$ROOT/${backup.session.userId}/$COLL_SESSIONS/${backup.session.sessionId}")
        val landmarksRef = sessionRef.collection(COLL_LANDMARKS)

        storage.runTransaction { transaction ->
            transaction.set(sessionRef, backup.session.asDataMapping())

            for (landmarkData in backup.landmarks) {
                val documentRef = landmarksRef.document(landmarkData.key.id)
                val data = landmarkData.key.asDataMapping().also { map ->
                    map["foundAt"] = landmarkData.value?.time
                    map["photoId"] = landmarkData.value?.photoId
                }
                transaction.set(documentRef, data, SetOptions.merge())
            }
        }.addOnSuccessListener {
            logEvent("Updated session successfully.")
            updateTask.onComplete()
        }.addOnFailureListener { error ->
            logEvent("Failed to update session data with error ${error.message}")
            updateTask.onError(error)
        }

        return updateTask
    }

    override fun wipeSessionBackup(userId: String): Completable {
        val wipeTask = CompletableSubject.create()

        storage.collection("$ROOT/$userId/$COLL_SESSIONS").whereEqualTo("timeFinished", null).get()
                .addOnSuccessListener { snapshot ->

                    storage.runTransaction { transaction ->
                        snapshot.documents.forEach { document ->
                            val wipeRelatedDataBarrier = CountDownLatch(1)

                            wipeRelatedSessionData(userId, document, wipeRelatedDataBarrier)
                            wipeRelatedDataBarrier.await()

                            transaction.delete(document.reference)
                        }
                    }.addOnSuccessListener {
                        logEvent("Wiped session data of unfinished sessions successfully!")
                        wipeTask.onComplete()
                    }.addOnFailureListener { error ->
                        logEvent("Failed to wipe unfinished sessions with the following error: ${error.message}")
                        wipeTask.onError(error)
                    }
                }.addOnFailureListener { error ->
                    logEvent("Failed to query unfinished sessions with the following error: ${error.message}")
                    wipeTask.onError(error)
                }

        return wipeTask
    }

    private fun wipeRelatedSessionData(userId: String, session: DocumentSnapshot, countDownLatch: CountDownLatch) {
        session.reference.collection(COLL_LANDMARKS).get()
                .addOnSuccessListener { querySnapshot ->
                    val workManager = WorkManager.getInstance()

                    querySnapshot.documents.filter { it["photoId"] != null }.forEach { landmarkSnapshot ->
                        val photoId = landmarkSnapshot.extractLandmarkData().first.id
                        workManager.enqueue(createDeleteTask(userId, photoId))
                    }

                    querySnapshot.documents.forEach { document ->
                        document.reference.delete()
                    }
                    countDownLatch.countDown()
                }
    }

    private fun createDeleteTask(userId: String, photoId: String): OneTimeWorkRequest {
        val imageData = workDataOf(ARG_USER_ID to userId,
                                   ARG_PHOTO_ID to photoId)

        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        return OneTimeWorkRequestBuilder<DeleteWorker>()
                .setInputData(imageData)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR,
                                    OneTimeWorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                                    TimeUnit.MILLISECONDS)
                .build()
    }

    private fun logEvent(message: String) = debug(TAG_LOG, message)

    companion object {
        private const val TAG_LOG = "FirebaseDataSourceLogger"
        private const val ROOT = "users"
        private const val COLL_SESSIONS = "sessions"
        private const val COLL_LANDMARKS = "landmarks"
    }

}