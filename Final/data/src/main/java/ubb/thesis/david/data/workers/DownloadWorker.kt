package ubb.thesis.david.data.workers

import android.content.Context
import android.net.Uri
import androidx.work.WorkerParameters
import com.google.firebase.storage.FirebaseStorage
import ubb.thesis.david.data.R
import ubb.thesis.david.data.utils.FileUtils
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import java.io.File

class DownloadWorker(appContext: Context, workerParameters: WorkerParameters) :
    BaseWorker(appContext, workerParameters) {

    override fun executeTask() {
        val userId = inputData.getString("userId")
        val photoId = inputData.getString("photoId")
        val targetPath = inputData.getString("targetPath")

        val storage = FirebaseStorage.getInstance()
        val imageRef = storage.getReference("$userId/images/$photoId")
        val fileUri = Uri.fromFile(File(targetPath))

        info(TAG_LOG, "Image download of $photoId has been enqueued!")
        displayProgress(applicationContext.getString(R.string.download_enqueued))

        imageRef.getFile(fileUri)
                .addOnSuccessListener {
                    info(TAG_LOG, "Image $photoId has been downloaded successfully to $targetPath!")
                    displayProgress(applicationContext.getString(R.string.download_finished))

                    performMediaScan(targetPath!!)
                    onWorkDone(Result.success())
                }.addOnFailureListener { error ->
                    debug(TAG_LOG, "Failed to download image $photoId with error ${error.message}")
                    displayProgress(applicationContext.getString(R.string.download_failed))
                    error.printStackTrace()

                    onWorkDone(Result.retry())
                }.addOnCanceledListener {
                    debug(TAG_LOG, "Image download of $photoId has been canceled.")
                    onWorkDone(Result.failure())
                }
    }

    private fun performMediaScan(filePath: String) =
        FileUtils.performMediaScan(applicationContext, filePath)

    companion object {
        private const val TAG_LOG = "DownloadWorkerLogger"
    }

}