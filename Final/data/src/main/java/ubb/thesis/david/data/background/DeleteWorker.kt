package ubb.thesis.david.data.background

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.storage.FirebaseStorage
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import java.util.concurrent.CountDownLatch

class DeleteWorker(appContext: Context, workerParameters: WorkerParameters) : Worker(appContext, workerParameters) {

    private lateinit var delayedResult: Result

    override fun doWork(): Result {
        val latch = CountDownLatch(1)

        val userId = inputData.getString("userId")
        val photoId = inputData.getString("photoId")

        val storage = FirebaseStorage.getInstance()
        val imageRef = storage.getReference("$userId/images/$photoId")

        info(TAG_LOG, "Image deletion of $photoId has been enqueued!")
        imageRef.delete()
                .addOnSuccessListener {
                    info(TAG_LOG, "Image $photoId has been deleted successfully!")
                    delayedResult = Result.success()
                    latch.countDown()
                }.addOnFailureListener { error ->
                    debug(TAG_LOG, "Failed to delete image $photoId with error ${error.message}")
                    error.printStackTrace()
                    delayedResult = Result.retry()
                    latch.countDown()
                }.addOnCanceledListener {
                    debug(TAG_LOG, "Image deletion of $photoId has been canceled.")
                    latch.countDown()
                    delayedResult = Result.failure()
                }

        latch.await()

        return Result.success()
    }

    companion object {
        private const val TAG_LOG = "DeleteWorkerLogger"
    }

}