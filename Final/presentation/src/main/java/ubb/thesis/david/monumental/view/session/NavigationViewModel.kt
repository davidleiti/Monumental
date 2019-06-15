package ubb.thesis.david.monumental.view.session

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.data.utils.toLocation
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.device.GetUndiscoveredLandmarks
import ubb.thesis.david.monumental.Configuration
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseViewModel
import ubb.thesis.david.monumental.common.SingleLiveEvent

class SessionViewModel : BaseViewModel() {

    private val sessionManager: SessionManager = Configuration.provideSessionManager()

    private val _sessionLandmarks: SingleLiveEvent<List<Landmark>> = SingleLiveEvent()
    private val _nearestLandmark: SingleLiveEvent<Landmark> = SingleLiveEvent()
    private val _distanceToTarget: SingleLiveEvent<Float> = SingleLiveEvent()
    private val _errorMessage: MutableLiveData<String> = MutableLiveData()

    val sessionLandmarks: LiveData<List<Landmark>> = _sessionLandmarks
    val nearestLandmark: LiveData<Landmark> = _nearestLandmark
    val distanceToTarget: LiveData<Float> = _distanceToTarget
    val errorMessages: LiveData<String> = _errorMessage

    fun loadSessionLandmarks(sessionId: String) {
        GetUndiscoveredLandmarks(sessionId, sessionManager,
                                                                         AsyncTransformerFactory.create<List<Landmark>>())
                .execute()
                .subscribe({ _sessionLandmarks.value = it },
                           { _errorMessage.postValue(it.message) })
                .also { addDisposable(it) }
    }

    fun queryNearestLandmark(location: Location) {
        val landmarks = _sessionLandmarks.value
        if (landmarks != null && landmarks.isNotEmpty()) {
            val sortedByDistance = landmarks.sortedWith(Comparator { l1, l2 ->
                val dist1 = l1.toLocation().distanceTo(location).toInt()
                val dist2 = l2.toLocation().distanceTo(location).toInt()
                dist1 - dist2
            })
            _nearestLandmark.value = sortedByDistance[0]
            _distanceToTarget.value = sortedByDistance[0].toLocation().distanceTo(location)
        }
    }
}