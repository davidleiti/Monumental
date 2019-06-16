package ubb.thesis.david.monumental.view.session

import android.location.Location
import androidx.lifecycle.LiveData
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.data.utils.toLocation
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.common.SaveSessionProgress
import ubb.thesis.david.domain.usecases.local.GetUndiscoveredLandmarks
import ubb.thesis.david.domain.usecases.local.WipeCachedSession
import ubb.thesis.david.monumental.Configuration
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseViewModel
import ubb.thesis.david.monumental.common.SingleLiveEvent

class NavigationViewModel(private val cloudDataSource: CloudDataSource,
                          private val beaconManager: BeaconManager) : BaseViewModel() {

    // Resources
    private val sessionManager: SessionManager = Configuration.provideSessionManager()

    // Observable sources
    private val _sessionLandmarks: SingleLiveEvent<List<Landmark>> = SingleLiveEvent()
    private val _nearestLandmark: SingleLiveEvent<Landmark> = SingleLiveEvent()
    private val _distanceToTarget: SingleLiveEvent<Float> = SingleLiveEvent()
    private val _progressSaved: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _errorsOccurred: SingleLiveEvent<Throwable> = SingleLiveEvent()

    // Exposed observable properties
    val sessionLandmarks: LiveData<List<Landmark>> = _sessionLandmarks
    val nearestLandmark: LiveData<Landmark> = _nearestLandmark
    val distanceToTarget: LiveData<Float> = _distanceToTarget
    val progressSaved: LiveData<Unit> = _progressSaved
    val errorsOccurred: LiveData<Throwable> = _errorsOccurred

    fun loadSessionLandmarks(sessionId: String) {
        GetUndiscoveredLandmarks(sessionId, sessionManager, AsyncTransformerFactory.create<List<Landmark>>())
                .execute()
                .subscribe({ _sessionLandmarks.value = it },
                           { _errorsOccurred.value = it })
                .also { addDisposable(it) }
    }

    fun saveSessionProgress(userId: String) {
        SaveSessionProgress(userId, sessionManager, cloudDataSource, AsyncTransformerFactory.create())
                .execute()
                .subscribe({ _progressSaved.call() },
                           { error -> _errorsOccurred.value = error })
                .also { addDisposable(it) }
    }

    fun wipeSessionCache(userId: String) {
        WipeCachedSession(userId, sessionManager, beaconManager, AsyncTransformerFactory.create())
                .execute()
                .subscribe({ info(TAG_LOG, "Wiped session cache of user $userId") },
                           { debug(TAG_LOG, "Failed to wipe cache of user $userId, cause: ${it.message}") })
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

    companion object {
        private const val TAG_LOG = "NavigationViewModelLogger"
    }
}