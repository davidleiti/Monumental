package ubb.thesis.david.monumental.view.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.data.adapters.FirebaseLandmarkDetector
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.domain.ImageStorage
import ubb.thesis.david.domain.LandmarkDetector
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.cloud.UploadImage
import ubb.thesis.david.domain.usecases.detection.DetectLandmark
import ubb.thesis.david.domain.usecases.detection.LooseLabelFiltering
import ubb.thesis.david.domain.usecases.detection.StrictLabelFiltering
import ubb.thesis.david.domain.usecases.local.UpdateCachedLandmark
import ubb.thesis.david.monumental.Configuration
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseViewModel
import ubb.thesis.david.monumental.common.SingleLiveEvent
import java.util.*

class SnapshotViewModel(private val beaconManager: BeaconManager,
                        private val imageStorage: ImageStorage,
                        private val landmarkDetector: LandmarkDetector) : BaseViewModel() {

    // Resources
    private val sessionManager: SessionManager = Configuration.provideSessionManager()

    // Observable sources
    private val _landmarkSaved = SingleLiveEvent<Unit>()
    private val _initialLabelingPass = MutableLiveData<Boolean>()
    private val _finalLabelingPass = MutableLiveData<Boolean>()
    private val _detectionPass = MutableLiveData<Boolean>()
    private val _errors = MutableLiveData<Throwable>()

    // Exposed observable properties
    val initialLabelingPassed: LiveData<Boolean> = _initialLabelingPass
    val finalLabelingPassed: LiveData<Boolean> = _finalLabelingPass
    val detectionPassed: LiveData<Boolean> = _detectionPass
    val landmarkSaved: LiveData<Unit> = _landmarkSaved
    val errors: LiveData<Throwable> = _errors

    fun filterLabelInitial(path: String) {
        StrictLabelFiltering(path, landmarkDetector, AsyncTransformerFactory.create<Boolean>())
                .execute()
                .subscribe({ passed ->
                               if (passed)
                                   info(TAG_LOG, "Initial filtering passed!")
                               else
                                   info(TAG_LOG, "Initial filtering has failed!")

                               _initialLabelingPass.value = passed
                           }, { error ->
                               debug(TAG_LOG, "Error encountered while labeling on device, message: ${error.message}")
                               _errors.value = error
                           })
                .also { addDisposable(it) }
    }

    fun detectLandmark(landmark: Landmark, imagePath: String) {
        DetectLandmark(landmark, imagePath, landmarkDetector, AsyncTransformerFactory.create<String>())
                .execute()
                .subscribe({ detection ->
                               if (detection != FirebaseLandmarkDetector.RESULT_NONE_DETECTED)
                                   info(TAG_LOG,
                                        "Recognized the following landmark while analyzing the image: $detection")
                               else
                                   info(TAG_LOG, "Failed to recognize any landmark while analyzing the image")

                               _detectionPass.value = detection != FirebaseLandmarkDetector.RESULT_NONE_DETECTED
                           }, { error ->
                               debug(TAG_LOG, "Error encountered while detecting, message: ${error.message}")
                               _errors.value = error
                           })
                .also { addDisposable(it) }
    }

    fun filterImageFinal(path: String) {
        LooseLabelFiltering(path, landmarkDetector, AsyncTransformerFactory.create<Boolean>())
                .execute()
                .subscribe({ passed ->
                               if (passed)
                                   info(TAG_LOG, "Final filtering has passed!")
                               else
                                   info(TAG_LOG, "Final filter has failed!")

                               _finalLabelingPass.value = passed
                           }, { error ->
                               debug(TAG_LOG, "Error encountered while labeling on cloud, message: ${error.message}")
                               _errors.value = error
                           })
                .also { addDisposable(it) }
    }

    fun saveLandmark(landmark: Landmark, userId: String, photoPath: String, timeDiscovered: Date) {
        val parameters = UpdateCachedLandmark.Params(landmark, userId, landmark.id, timeDiscovered)

        UpdateCachedLandmark(parameters, sessionManager, AsyncTransformerFactory.create())
                .execute()
                .subscribe({
                               info(TAG_LOG, "Updated landmark $landmark data successfully!")
                               beaconManager.removeBeacon(landmark.id, userId)
                               uploadImage(landmark, userId, photoPath)
                               _landmarkSaved.value = Unit
                           }, { error ->
                               debug(TAG_LOG, "Error updating landmark, cause: ${error.message}")
                               _errors.value = error
                           }).also { addDisposable(it) }
    }

    private fun uploadImage(landmark: Landmark, userId: String, photoPath: String) {
        val parameters = UploadImage.Params(userId, landmark.id, photoPath)

        UploadImage(parameters, imageStorage, AsyncTransformerFactory.create())
                .execute()
                .subscribe({}, { error -> _errors.value = error })
                .also { addDisposable(it) }
    }

    companion object {
        private const val TAG_LOG = "SnapshotViewLogger"
    }

}
