package ubb.thesis.david.data.background

import android.content.Context
import android.net.Uri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.storage.FirebaseStorage
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import java.io.File
import java.util.concurrent.CountDownLatch

class UploadWorker(appContext: Context,
                   workerParameters: WorkerParameters) : Worker(appContext, workerParameters) {

    private lateinit var delayedResult: Result

    override fun doWork(): Result {
        val latch = CountDownLatch(1)

        val userId = inputData.getString("userId")
        val photoId = inputData.getString("photoId")
        val photoPath = inputData.getString("photoPath")

        val storage = FirebaseStorage.getInstance()
        val imageRef = storage.getReference("$userId/images/$photoId")
        val fileUri = Uri.fromFile(File(photoPath))

        info(TAG_LOG, "Image upload of $photoId has been enqueued!")
        imageRef.putFile(fileUri)
                .addOnSuccessListener {
                    info(TAG_LOG, "Image $photoId from $photoPath uploaded successfully!")
                    delayedResult = Result.success()
                    latch.countDown()
                }.addOnFailureListener { error ->
                    debug(TAG_LOG, "Failed to upload image $photoId with error ${error.message}")
                    error.printStackTrace()
                    delayedResult = Result.retry()
                    latch.countDown()
                }.addOnCanceledListener {
                    debug(TAG_LOG, "Image upload of $photoId has been canceled.")
                    latch.countDown()
                    delayedResult = Result.failure()
                }

        latch.await()

        return Result.success()

    }

    companion object {
        private const val TAG_LOG = "UploadWorkerLogger"
    }

}