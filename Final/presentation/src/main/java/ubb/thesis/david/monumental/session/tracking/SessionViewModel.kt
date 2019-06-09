package ubb.thesis.david.monumental.session.tracking

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.GetSessionLandmarks
import ubb.thesis.david.monumental.Configuration
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseViewModel

class SessionViewModel : BaseViewModel() {

    private val sessionManager: SessionManager = Configuration.provideSessionManager()
    private val errorsObservable: MutableLiveData<String> = MutableLiveData()
    private val landmarksObservable: MutableLiveData<List<Landmark>> = MutableLiveData()
    private val nearestLandmark: MutableLiveData<Landmark> = MutableLiveData()

    fun getLandmarksObservable(): LiveData<List<Landmark>> = landmarksObservable

    fun getErrorsObservable(): LiveData<String> = errorsObservable

    fun getNearestLandmarkObservable(): LiveData<Landmark> = nearestLandmark

    fun loadSessionLandmarks(sessionId: String) {
        GetSessionLandmarks(sessionId, sessionManager, AsyncTransformerFactory.create<List<Landmark>>())
                .execute()
                .subscribe({ landmarksObservable.value = it },
                           { errorsObservable.postValue(it.message) })
                .also { addDisposable(it) }
    }

    fun queryNearestLandmark(location: Location) {
        val landmarks = landmarksObservable.value
        landmarks?.let {
            val sortedByDistance = landmarks.sortedWith(Comparator { l1, l2 ->
                val dist1 = l1.transformToLocation().distanceTo(location).toInt()
                val dist2 = l2.transformToLocation().distanceTo(location).toInt()
                dist1 - dist2
            })
            nearestLandmark.value = sortedByDistance[0]
        }
    }
}

fun Landmark.transformToLocation(): Location =
    Location("").apply {
        latitude = lat
        longitude = lng
    }
