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
import ubb.thesis.david.monumental.utils.default

class SessionViewModel : BaseViewModel() {

    private val sessionManager: SessionManager = Configuration.provideSessionManager()
    private val _errorMessage: MutableLiveData<String> = MutableLiveData()
    private val _sessionLandmarks: MutableLiveData<List<Landmark>> = MutableLiveData()
    private val _nearestLandmark: MutableLiveData<Landmark> = MutableLiveData()
    private val _distanceToTarget = MutableLiveData<Float>().default(0.0F)

    val sessionLandmarks: LiveData<List<Landmark>> = _sessionLandmarks
    val errorMessages: LiveData<String> = _errorMessage
    val nearestLandmark: LiveData<Landmark> = _nearestLandmark
    val distanceToTarget: LiveData<Float> = _distanceToTarget

    fun loadSessionLandmarks(sessionId: String) {
        GetSessionLandmarks(sessionId, sessionManager, AsyncTransformerFactory.create<List<Landmark>>())
                .execute()
                .subscribe({ _sessionLandmarks.value = it },
                           { _errorMessage.postValue(it.message) })
                .also { addDisposable(it) }
    }

    fun queryNearestLandmark(location: Location) {
        val landmarks = _sessionLandmarks.value
        landmarks?.let {
            val sortedByDistance = landmarks.sortedWith(Comparator { l1, l2 ->
                val dist1 = l1.transformToLocation().distanceTo(location).toInt()
                val dist2 = l2.transformToLocation().distanceTo(location).toInt()
                dist1 - dist2
            })
            _nearestLandmark.value = sortedByDistance[0]
            _distanceToTarget.value = sortedByDistance[0].transformToLocation().distanceTo(location)
        }
    }
}

fun Landmark.transformToLocation(): Location =
    Location("").apply {
        latitude = lat
        longitude = lng
    }
