package ubb.thesis.david.data

import androidx.work.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import ubb.thesis.david.data.background.DeleteWorker
import ubb.thesis.david.data.utils.asDataMapping
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.extractLandmarkData
import ubb.thesis.david.data.utils.extractSessionData
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.entities.Backup
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.entities.Session
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FirebaseDataSource : CloudDataSource {

    private val storage = FirebaseFirestore.getInstance()
    private val imageStorage = FirebaseStorage.getInstance()

    override fun getUserSessions(userId: String): Maybe<List<Session>> {
        val sessionsRetrieved: MaybeSubject<List<Session>> = MaybeSubject.create()

        storage.collection("$ROOT/$userId/sessions").get()
                .addOnSuccessListener { querySnapshot ->
                    val sessions = querySnapshot.documents.map { it.extractSessionData() }
                    sessionsRetrieved.onSuccess(sessions)
                }
                .addOnFailureListener { error ->
                    debug(TAG_LOG, "Failed to retrieve sessions with error ${error.message}")
                    sessionsRetrieved.onError(error)
                }

        return sessionsRetrieved
    }

    override fun getSessionDetails(userId: String, sessionId: String): Maybe<Map<Landmark, Discovery?>> {
        val dataRetrieved: MaybeSubject<Map<Landmark, Discovery?>> = MaybeSubject.create()

        storage.collection("$ROOT/$userId/sessions/$sessionId/landmarks").get()
                .addOnSuccessListener { querySnapshot ->
                    val data = querySnapshot.documents.map { it.extractLandmarkData() }.toMap()
                    logEvent("Retrieved cloud backup successfully: $data")
                    dataRetrieved.onSuccess(data)
                }.addOnFailureListener { error ->
                    logEvent("Failed to retrieve session details with error ${error.message}")
                    dataRetrieved.onError(error)
                }

        return dataRetrieved
    }

    override fun createSession(session: Session, landmarks: List<Landmark>): Single<String> {
        val creationCompleted = SingleSubject.create<String>()

        val newSessionRef = storage.collection("$ROOT/${session.userId}/sessions").document()
        val landmarksRef = newSessionRef.collection("landmarks")

        storage.runTransaction { transaction ->
            transaction.set(newSessionRef, session.asDataMapping())

            for (landmark in landmarks) {
                val documentRef = landmarksRef.document(landmark.id)
                transaction.set(documentRef, landmark.asDataMapping())
            }

        }.addOnSuccessListener {
            logEvent("Session $session has been created successfully")
            creationCompleted.onSuccess(newSessionRef.id)
        }.addOnFailureListener { ex ->
            logEvent("Session creation has failed with the following error: ${ex.message}")
            creationCompleted.onError(ex)
        }

        return creationCompleted
    }

    override fun updateSessionBackup(backup: Backup): Completable {
        val updateCompleted = CompletableSubject.create()

        val sessionRef = storage.document("$ROOT/${backup.session.userId}/sessions/${backup.session.sessionId}")
        val landmarksRef = sessionRef.collection("landmarks")

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
            updateCompleted.onComplete()
        }.addOnFailureListener { error ->
            logEvent("Failed to update session data with error ${error.message}")
            updateCompleted.onError(error)
        }

        return updateCompleted
    }

    override fun wipeSessionBackup(userId: String): Completable {
        val wipeCompleted = CompletableSubject.create()

        storage.collection("$ROOT/$userId/sessions").whereEqualTo("timeFinished", null).get()
                .addOnSuccessListener { snapshot ->

                    storage.runTransaction { transaction ->
                        snapshot.documents.forEach { document ->
                            val wipeImagesBlock = CountDownLatch(1)
                            deleteSessionImages(userId, document, wipeImagesBlock)
                            wipeImagesBlock.await()

                            transaction.delete(document.reference)
                        }
                    }.addOnSuccessListener {
                        logEvent("Wiped session data of unfinished sessions successfully!")
                        wipeCompleted.onComplete()
                    }.addOnFailureListener { error ->
                        logEvent("Failed to wipe unfinished sessions with the following error: ${error.message}")
                        wipeCompleted.onError(error)
                    }
                }.addOnFailureListener { error ->
                    logEvent("Failed to query unfinished sessions with the following error: ${error.message}")
                    wipeCompleted.onError(error)
                }

        return wipeCompleted
    }

    private fun deleteSessionImages(userId: String, session: DocumentSnapshot, countDownLatch: CountDownLatch) {
        session.reference.collection("landmarks").get()
                .addOnSuccessListener { querySnapshot ->
                    val workManager = WorkManager.getInstance()

                    querySnapshot.documents.filter { it["photoId"] != null }.forEach { landmarkSnapshot ->
                        val photoId = landmarkSnapshot.extractLandmarkData().first.id
                        workManager.enqueue(createDeleteTask(userId, photoId))
                    }
                    countDownLatch.countDown()
                }
    }

    private fun createDeleteTask(userId: String, photoId: String): OneTimeWorkRequest {
        val imageData = workDataOf("userId" to userId,
                                   "photoId" to photoId)

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
    }

}