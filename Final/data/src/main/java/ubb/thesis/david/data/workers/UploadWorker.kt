package ubb.thesis.david.data.workers

import android.content.Context
import android.net.Uri
import androidx.work.WorkerParameters
import com.google.firebase.storage.FirebaseStorage
import ubb.thesis.david.data.R
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import java.io.File

class UploadWorker(appContext: Context, workerParameters: WorkerParameters) :
    BaseWorker(appContext, workerParameters) {

    override fun executeTask() {
        val userId = inputData.getString(ARG_USER_ID)
        val photoId = inputData.getString(ARG_PHOTO_ID)
        val photoPath = inputData.getString(ARG_PHOTO_PATH)

        val storage = FirebaseStorage.getInstance()
        val imageRef = storage.getReference("$userId/images/$photoId")
        val fileUri = Uri.fromFile(File(photoPath))

        info(TAG_LOG, "Image upload of $photoId has been enqueued!")
        displayProgress(applicationContext.getString(R.string.message_upload_enqueued))

        imageRef.putFile(fileUri)
                .addOnSuccessListener {
                    info(TAG_LOG, "Image $photoId from $photoPath uploaded successfully!")
                    displayProgress(applicationContext.getString(R.string.message_upload_finished))

                    onWorkDone(Result.success())
                }.addOnFailureListener { error ->
                    debug(TAG_LOG, "Failed to upload image $photoId with error ${error.message}")
                    displayProgress(applicationContext.getString(R.string.message_upload_failed))
                    error.printStackTrace()

                    onWorkDone(Result.retry())
                }.addOnCanceledListener {
                    debug(TAG_LOG, "Image upload of $photoId has been canceled.")
                    onWorkDone(Result.failure())
                }
    }

    companion object {
        private const val TAG_LOG = "UploadWorkerLogger"
        const val ARG_USER_ID = "userId"
        const val ARG_PHOTO_ID = "photoId"
        const val ARG_PHOTO_PATH = "photoPath"
    }

}