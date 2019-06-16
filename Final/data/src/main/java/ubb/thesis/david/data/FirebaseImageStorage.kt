package ubb.thesis.david.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.SingleSubject
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.domain.ImageStorage
import java.io.File

class FirebaseImageStorage : ImageStorage {

    private val storage = FirebaseStorage.getInstance()

    override fun storeImage(userId: String, imageId: String, filePath: String): Completable {
        val uploadTask = CompletableSubject.create()

        val reference = storage.getReference("$userId/images/$imageId")

        val fileUri = Uri.fromFile(File(filePath))
        reference.putFile(fileUri)
                .addOnSuccessListener {
                    info(TAG_LOG, "Successfully uploaded image with id $imageId")
                    uploadTask.onComplete()
                }.addOnFailureListener { error ->
                    debug(TAG_LOG, "Failed to upload image with id $imageId")
                    uploadTask.onError(error)
                }

        return uploadTask
    }

    override fun downloadImage(userId: String, imageId: String, targetPath: String): Completable {
        val downloadTask = CompletableSubject.create()
        val targetFile = File(targetPath)

        storage.getReference("$userId/images/$imageId").getFile(targetFile)
                .addOnSuccessListener {
                    logEvent("Saved image $imageId successfully to $targetPath")
                    downloadTask.onComplete()
                }.addOnFailureListener { error ->
                    logEvent("Failed to save image $imageId with error ${error.message}")
                    downloadTask.onError(error)
                }

        return downloadTask
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

    private fun logEvent(message: String) = debug(TAG_LOG, message)

    companion object {
        private const val TAG_LOG = "FirebaseImageStorageLogger"
    }

}