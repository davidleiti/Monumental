package ubb.thesis.david.monumental.view.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.data.FirebaseLandmarkDetector
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.domain.LandmarkDetector
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.cloud.DetectLandmark
import ubb.thesis.david.domain.usecases.cloud.FilterImageCloud
import ubb.thesis.david.domain.usecases.local.FilterImageLocal
import ubb.thesis.david.domain.usecases.local.UpdateCachedLandmark
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseViewModel
import java.util.*

class SnapshotViewModel(private val sessionManager: SessionManager,
                        private val beaconManager: BeaconManager,
                        private val landmarkDetector: LandmarkDetector) : BaseViewModel() {

    // Observable sources
    private val _initialLabelingPass = MutableLiveData<Boolean>()
    private val _finalLabelingPass = MutableLiveData<Boolean>()
    private val _detectionPass = MutableLiveData<Boolean>()
    private val _onLandmarkSaved = MutableLiveData<Unit>()
    private val _errors = MutableLiveData<Throwable>()

    // Exposed observable properties
    val initialLabelingPassed: LiveData<Boolean> = _initialLabelingPass
    val finalLabelingPassed: LiveData<Boolean> = _finalLabelingPass
    val detectionPassed: LiveData<Boolean> = _detectionPass
    val onLandmarkSaved: LiveData<Unit> = _onLandmarkSaved
    val errors: LiveData<Throwable> = _errors

    fun filterLabelInitial(path: String) {
        FilterImageLocal(path, landmarkDetector, AsyncTransformerFactory.create<Boolean>())
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
                               if (detection != FirebaseLandmarkDetector.NONE_DETECTED)
                                   info(TAG_LOG,
                                        "Recognized the following landmark while analyzing the image: $detection")
                               else
                                   info(TAG_LOG, "Failed to recognize any landmark while analyzing the image")

                               _detectionPass.value = detection != FirebaseLandmarkDetector.NONE_DETECTED
                           }, { error ->
                               debug(TAG_LOG, "Error encountered while detecting, message: ${error.message}")
                               _errors.value = error
                           })
                .also { addDisposable(it) }
    }

    fun filterImageFinal(path: String) {
        FilterImageCloud(path, landmarkDetector, AsyncTransformerFactory.create<Boolean>())
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
        val parameters = UpdateCachedLandmark.Params(landmark, userId, photoPath, timeDiscovered)
        UpdateCachedLandmark(parameters, sessionManager, AsyncTransformerFactory.create())
                .execute()
                .subscribe({
                               info(TAG_LOG, "Updated landmark $landmark data successfully!")
                               beaconManager.removeBeacon(landmark.id, userId)
                               _onLandmarkSaved.value = Unit
                           }, {
                               debug(TAG_LOG, "Error updating landmark, cause: ${it.message}")
                               _errors.value
                           }).also { addDisposable(it) }
    }

    companion object {
        private const val TAG_LOG = "SnapshotLogger"
    }

}
