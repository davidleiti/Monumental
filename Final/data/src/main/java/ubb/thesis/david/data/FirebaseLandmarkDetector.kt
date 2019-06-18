package ubb.thesis.david.data

import android.content.Context
import android.location.Location
import android.net.Uri
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.toLocation
import ubb.thesis.david.domain.LandmarkDetector
import ubb.thesis.david.domain.entities.Landmark
import java.io.File
import java.io.IOException

class FirebaseLandmarkDetector(private val context: Context) : LandmarkDetector {

    private val onDeviceImageLabeler: FirebaseVisionImageLabeler
    private val cloudImageLabeler: FirebaseVisionImageLabeler
    private val landmarkDetector: FirebaseVisionCloudLandmarkDetector

    init {
        val onDeviceLabelerOptions = FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.6f)
                .build()
        val cloudLabelerOptions = FirebaseVisionCloudImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.85f)
                .build()
        val detectorOptions = FirebaseVisionCloudDetectorOptions.Builder()
                .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                .setMaxResults(10)
                .build()

        onDeviceImageLabeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(onDeviceLabelerOptions)
        cloudImageLabeler = FirebaseVision.getInstance().getCloudImageLabeler(cloudLabelerOptions)
        landmarkDetector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector(detectorOptions)
    }

    override fun filterImageLocal(imagePath: String): Single<Boolean> {
        val labelingTask = SingleSubject.create<Boolean>()

        createFirebaseImage(imagePath)?.let { image ->
            onDeviceImageLabeler.processImage(image)
                    .addOnSuccessListener { labels ->
                        debug(TAG_LOG, "Labels predicted: ${labels.map { it.text + '(' + it.confidence + ')' }}")
                        labelingTask.onSuccess(passesFilters(labels = labels.map { it.text },
                                                             filters = LABELS_SPECIFIC + LABELS_GENERIC))
                    }
                    .addOnFailureListener { error ->
                        debug(TAG_LOG, "Failed to label image with error ${error.message}")
                        labelingTask.onError(error)
                    }
        } ?: run {
            labelingTask.onError(Throwable(ERROR_CREATE_IMAGE))
        }

        return labelingTask
    }

    override fun filterImageCloud(imagePath: String): Single<Boolean> {
        val labelingTask = SingleSubject.create<Boolean>()

        createFirebaseImage(imagePath)?.let { image ->
            cloudImageLabeler.processImage(image)
                    .addOnSuccessListener { labels ->
                        debug(TAG_LOG, "Labels predicted: ${labels.map { it.text + '(' + it.confidence + ')' }}")
                        labelingTask.onSuccess(passesFilters(labels.map { it.text }, LABELS_SPECIFIC))
                    }
                    .addOnFailureListener { error ->
                        debug(TAG_LOG, "Failed to detect landmark with error ${error.message}")
                        labelingTask.onError(error)
                    }
        } ?: run {
            labelingTask.onError(Throwable(ERROR_CREATE_IMAGE))
        }

        return labelingTask
    }

    override fun detectLandmark(targetLandmark: Landmark, imagePath: String): Single<String> {
        val detectionTask = SingleSubject.create<String>()

        createFirebaseImage(imagePath)?.let { image ->
            landmarkDetector.detectInImage(image)
                    .addOnSuccessListener { landmarks ->
                        debug(TAG_LOG,
                              "Landmarks predicted: ${landmarks.map { it.landmark + '(' + it.confidence + ')' }}")
                        detectionTask.onSuccess(verifyDetectedLandmark(targetLandmark, landmarks))
                    }
                    .addOnFailureListener { error ->
                        debug(TAG_LOG, "Failed to label image with error ${error.message}")
                        detectionTask.onError(error)
                    }
        } ?: run {
            detectionTask.onError(Throwable(ERROR_CREATE_IMAGE))
        }

        return detectionTask
    }

    private fun verifyDetectedLandmark(target: Landmark, landmarks: List<FirebaseVisionCloudLandmark>): String {
        if (landmarks.isEmpty())
            return NONE_DETECTED

        val sorted = landmarks.sortedBy { it.confidence }

        val targetLocation = Location("").apply {
            latitude = target.lat
            longitude = target.lng
        }

        return if (sorted[0].toLocation().distanceTo(targetLocation) <= DETECTION_DISTANCE_THRESHOLD)
            sorted[0].landmark
        else NONE_DETECTED
    }

    private fun passesFilters(labels: List<String>, filters: Array<String>): Boolean {
        for (label in labels)
            if (label in filters) {
                return true
            }
        return false
    }

    private fun createFirebaseImage(path: String): FirebaseVisionImage? = try {
        FirebaseVisionImage.fromFilePath(context, Uri.fromFile(File(path)))
    } catch (ex: IOException) {
        ex.printStackTrace()
        null
    }

    companion object {
        const val NONE_DETECTED = "No landmark detected"

        private const val TAG_LOG = "FirebaseLandmarkDetectorLogger"
        private const val ERROR_CREATE_IMAGE = "Failed to create image at path, see log stacktrace for details"
        private const val DETECTION_DISTANCE_THRESHOLD = 50

        private val LABELS_SPECIFIC = arrayOf(
                "Monument",
                "Landmark",
                "Tourist Attraction",
                "Historic Site",
                "Church",
                "Cathedral",
                "Castle",
                "Palace",
                "Memorial",
                "Basilica",
                "Statue"
        )

        private val LABELS_GENERIC = arrayOf(
                "Building",
                "Architecture",
                "Tower"
        )
    }
}