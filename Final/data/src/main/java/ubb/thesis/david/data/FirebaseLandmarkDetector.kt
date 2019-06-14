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
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import ubb.thesis.david.domain.LandmarkDetector
import ubb.thesis.david.domain.entities.Landmark
import java.io.File
import java.io.IOException

class FirebaseLandmarkDetector(private val context: Context) : LandmarkDetector {

    private val onDeviceImageLabeler: FirebaseVisionImageLabeler
    private val cloudImageLabeler: FirebaseVisionImageLabeler
    private val landmarkDetector: FirebaseVisionCloudLandmarkDetector

    private var onDeviceLabelingSource: Subject<Boolean> = PublishSubject.create()
    private var cloudLabelingSource: Subject<Boolean> = PublishSubject.create()
    private var detectionSource: Subject<String> = PublishSubject.create()

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

    override fun filterImageOnDevice(imagePath: String): Observable<Boolean> {
        createFirebaseImage(imagePath)?.let { image ->
            onDeviceImageLabeler.processImage(image)
                    .addOnSuccessListener { labels ->
                        onDeviceLabelingSource.onNext(
                                passesFilters(
                                        labels = labels.map { it.text },
                                        filters = LABELS_FILTER + LABELS_GENERIC))
                    }
                    .addOnFailureListener {
                        onDeviceLabelingSource.onError(it)
                        onDeviceLabelingSource = BehaviorSubject.create()
                    }
        } ?: run {
            detectionSource.onError(Throwable(ERROR_CREATE_IMAGE))
        }

        return onDeviceLabelingSource.take(1)
    }

    override fun filterImageCloud(imagePath: String): Observable<Boolean> {
        createFirebaseImage(imagePath)?.let { image ->
            cloudImageLabeler.processImage(image)
                    .addOnSuccessListener { labels ->
                        cloudLabelingSource.onNext(passesFilters(labels.map { it.text }, LABELS_FILTER))
                    }
                    .addOnFailureListener {
                        onDeviceLabelingSource.onError(it)
                        onDeviceLabelingSource = BehaviorSubject.create()
                    }
        } ?: run {
            detectionSource.onError(Throwable(ERROR_CREATE_IMAGE))
        }

        return cloudLabelingSource.take(1)
    }

    override fun detectLandmark(targetLandmark: Landmark, imagePath: String): Observable<String> {
        createFirebaseImage(imagePath)?.let { image ->
            landmarkDetector.detectInImage(image)
                    .addOnSuccessListener { landmarks ->
                        detectionSource.onNext(verifyDetectedLandmark(targetLandmark, landmarks))
                    }
                    .addOnFailureListener {
                        detectionSource.onError(it)
                        detectionSource = BehaviorSubject.create()
                    }
        } ?: run {
            detectionSource.onError(Throwable(ERROR_CREATE_IMAGE))
        }

        return detectionSource.take(1)
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
        private const val ERROR_CREATE_IMAGE = "Failed to create image at path, see log stacktrace for details"

        private const val DETECTION_DISTANCE_THRESHOLD = 50
        private val LABELS_FILTER = arrayOf(
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
                "Statue",
                "Building",
                "Architecture",
                "Tower"
        )

        private val LABELS_GENERIC = arrayOf(
                "Building",
                "Architecture",
                "Tower"
        )
    }
}

private fun FirebaseVisionCloudLandmark.toLocation(): Location =
    Location("").also {
        it.longitude = locations[0].longitude
        it.latitude = locations[0].latitude
    }