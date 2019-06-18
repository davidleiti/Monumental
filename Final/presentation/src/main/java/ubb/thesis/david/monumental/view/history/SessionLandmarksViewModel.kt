package ubb.thesis.david.monumental.view.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.ImageStorage
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.cloud.DownloadImage
import ubb.thesis.david.domain.usecases.cloud.GetSessionDetails
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseViewModel
import ubb.thesis.david.monumental.utils.default

class SessionLandmarksViewModel(private val cloudDataSource: CloudDataSource,
                                private val imageStorage: ImageStorage) : BaseViewModel() {

    // Observable sources
    private val _displayList = MutableLiveData<Boolean>().default(false)
    private val _landmarkDataRetrieved = MutableLiveData<Map<Landmark, Discovery?>>()
    private val _errors = MutableLiveData<Throwable>()

    // Exposed observable properties
    val landmarkDataRetrieved: LiveData<Map<Landmark, Discovery?>> = _landmarkDataRetrieved
    val errors: LiveData<Throwable> = _errors

    // Data binding properties
    val displayList = _displayList

    fun fetchLandmarks(userId: String, sessionId: String) {
        GetSessionDetails(userId, sessionId, cloudDataSource,
                          AsyncTransformerFactory.create<Map<Landmark, Discovery?>>())
                .execute()
                .subscribe({ landmarkData ->
                               val discovered = landmarkData.filter { it.value != null }
                               _landmarkDataRetrieved.value = discovered
                               _displayList.value = discovered.isNotEmpty()
                           },
                           { error -> _errors.value = error },
                           {
                               _landmarkDataRetrieved.value ?: run {
                                   _errors.value = RuntimeException("No landmarks to display")
                               }
                           })
                .also { addDisposable(it) }
    }

    fun downloadImage(userId: String, photoId: String, targetPath: String) {
        val params = DownloadImage.Params(userId, photoId, targetPath)
        DownloadImage(params, imageStorage, AsyncTransformerFactory.create())
                .execute()
                .subscribe()
                .also { addDisposable(it) }
    }

}
