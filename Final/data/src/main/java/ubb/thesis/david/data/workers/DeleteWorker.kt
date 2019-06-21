package ubb.thesis.david.data.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.google.firebase.storage.FirebaseStorage
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info

class DeleteWorker(appContext: Context, workerParameters: WorkerParameters) : BaseWorker(appContext, workerParameters) {

    override fun executeTask() {
        val userId = inputData.getString("userId")
        val photoId = inputData.getString("photoId")

        val storage = FirebaseStorage.getInstance()
        val imageRef = storage.getReference("$userId/images/$photoId")

        info(TAG_LOG, "Image deletion of $photoId has been enqueued!")
        imageRef.delete()
                .addOnSuccessListener {
                    info(TAG_LOG, "Image $photoId has been deleted successfully!")
                    onWorkDone(Result.success())
                }.addOnFailureListener { error ->
                    debug(TAG_LOG, "Failed to delete image $photoId with error ${error.message}")
                    error.printStackTrace()
                    onWorkDone(Result.retry())
                }.addOnCanceledListener {
                    debug(TAG_LOG, "Image deletion of $photoId has been canceled.")
                    onWorkDone(Result.failure())
                }
    }

    companion object {
        private const val TAG_LOG = "DeleteWorkerLogger"
    }

}