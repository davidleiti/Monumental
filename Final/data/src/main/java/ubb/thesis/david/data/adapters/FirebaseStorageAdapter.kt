package ubb.thesis.david.data.adapters

import androidx.work.*
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.SingleSubject
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.workers.DownloadWorker
import ubb.thesis.david.data.workers.DownloadWorker.Companion.ARG_TARGET_PATH
import ubb.thesis.david.data.workers.UploadWorker
import ubb.thesis.david.data.workers.UploadWorker.Companion.ARG_PHOTO_PATH
import ubb.thesis.david.domain.ImageStorage
import java.util.concurrent.TimeUnit

class FirebaseStorageAdapter : ImageStorage {

    private val storage = FirebaseStorage.getInstance()
    private val defaultConstraints: Constraints by lazy { createDefaultConstraints() }

    override fun storeImage(userId: String, imageId: String, filePath: String): Completable {
        val imageData = workDataOf(UploadWorker.ARG_USER_ID to userId,
                                   UploadWorker.ARG_PHOTO_ID to imageId,
                                   ARG_PHOTO_PATH to filePath)

        WorkManager.getInstance().enqueue(createDefaultRequest<UploadWorker>(imageData, defaultConstraints))
        return Completable.complete()
    }

    override fun downloadImage(userId: String, imageId: String, targetPath: String): Completable {
        val imageData = workDataOf(DownloadWorker.ARG_USER_ID to userId,
                                   DownloadWorker.ARG_PHOTO_ID to imageId,
                                   ARG_TARGET_PATH to targetPath)

        WorkManager.getInstance().enqueue(createDefaultRequest<DownloadWorker>(imageData, defaultConstraints))
        return Completable.complete()
    }

    override fun deleteImage(userId: String, imageId: String): Completable {
        val deleteImageTask = CompletableSubject.create()

        storage.getReference("$userId/images/$imageId").delete()
                .addOnSuccessListener {
                    logEvent("Deleted image $imageId successfully.")
                    deleteImageTask.onComplete()
                }.addOnFailureListener { error ->
                    logEvent("Failed to delete image $imageId with error ${error.message}")
                    deleteImageTask.onError(error)
                }

        return deleteImageTask
    }

    override fun getImageUrl(userId: String, imageId: String): Single<String> {
        val getUrlTask = SingleSubject.create<String>()

        storage.getReference("$userId/images/$imageId").downloadUrl
                .addOnSuccessListener { uri ->
                    logEvent("Retrieved download url of image $imageId successfully.")
                    getUrlTask.onSuccess(uri.toString())
                }.addOnFailureListener { error ->
                    logEvent("Failed to retrieve download url of image $imageId with error ${error.message}")
                    getUrlTask.onError(error)
                }

        return getUrlTask
    }

    private fun createDefaultConstraints(): Constraints =
        Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

    private inline fun <reified T : Worker> createDefaultRequest(data: Data, constraints: Constraints) =
        OneTimeWorkRequestBuilder<T>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR,
                                    OneTimeWorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                                    TimeUnit.MILLISECONDS)
                .build()

    private fun logEvent(message: String) = debug(
            TAG_LOG, message)

    companion object {
        private const val TAG_LOG = "FirebaseImageStorageLogger"
    }

}