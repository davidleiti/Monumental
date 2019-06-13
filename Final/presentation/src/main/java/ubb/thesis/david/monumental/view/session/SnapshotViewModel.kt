package ubb.thesis.david.monumental.view.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.data.FirebaseLandmarkDetector
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.domain.LandmarkDetector
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.DetectLandmark
import ubb.thesis.david.domain.usecases.FilterImageCloud
import ubb.thesis.david.domain.usecases.FilterImageOnDevice
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseViewModel

class SnapshotViewModel(private val landmarkDetector: LandmarkDetector) : BaseViewModel() {

    private val _initialLabelingPass = MutableLiveData<Boolean>()
    private val _finalLabelingPass = MutableLiveData<Boolean>()
    private val _detectionPass = MutableLiveData<Boolean>()
    private val _errors = MutableLiveData<String>()

    val initialLabelingPassed: LiveData<Boolean> = _initialLabelingPass
    val finalLabelingPassed: LiveData<Boolean> = _finalLabelingPass
    val detectionPassed: LiveData<Boolean> = _detectionPass
    val errors: LiveData<String> = _errors

    fun filterLabelInitial(path: String) {
        FilterImageOnDevice(path, landmarkDetector, AsyncTransformerFactory.create<Boolean>()).execute()
                .subscribe({ passed ->
                               if (passed)
                                   info(TAG_LOG, "Initial filtering passed!")
                               else
                                   info(TAG_LOG, "Initial filtering has failed!")

                               _initialLabelingPass.value = passed
                           }, { error ->
                               debug(TAG_LOG, "Error encountered while labeling on device, message: ${error.message}")
                               _errors.value = error.message
                           })
                .also { addDisposable(it) }
    }

    fun detectLandmark(landmark: Landmark, imagePath: String) {
        DetectLandmark(landmark, imagePath, landmarkDetector, AsyncTransformerFactory.create<String>()).execute()
                .subscribe({ detection ->
                               if (detection != FirebaseLandmarkDetector.NONE_DETECTED)
                                   info(TAG_LOG, "Recognized the following landmark while analyzing the image: $detection")
                               else
                                   info(TAG_LOG, "Failed to recognize any landmark while analyzing the image")

                               _detectionPass.value = detection != FirebaseLandmarkDetector.NONE_DETECTED
                           }, {
                               debug(TAG_LOG, "Error encountered while detecting, message: ${it.message}")
                               _errors.value = it.message
                           })
                .also { addDisposable(it) }
    }

    fun filterImageFinal(path: String) {
        FilterImageCloud(path, landmarkDetector, AsyncTransformerFactory.create<Boolean>()).execute()
                .subscribe({ passed ->
                               if (passed)
                                   info(TAG_LOG, "Final filtering passed!")
                               else
                                   info(TAG_LOG, "Final filter has failed!")

                               _finalLabelingPass.value = passed
                           }, { error ->
                               debug(TAG_LOG, "Error encountered while labeling on cloud, message: ${error.message}")
                               _errors.value = error.message
                           })
                .also { addDisposable(it) }
    }

    companion object {
        private const val TAG_LOG = "SnapshotLogger"
    }

}