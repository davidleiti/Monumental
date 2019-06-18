package ubb.thesis.david.monumental.view.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.cloud.SearchLandmarks
import ubb.thesis.david.domain.usecases.common.CreateSession
import ubb.thesis.david.monumental.Configuration
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseViewModel

class ResultViewModel(private val beaconManager: BeaconManager,
                      private val cloudDataSource: CloudDataSource) : BaseViewModel() {

    // Resources
    private val landmarkApi = Configuration.provideLandmarkApi()
    private val sessionManager = Configuration.provideSessionManager()

    // Observable sources
    private val _sessionCreatedObservable = MutableLiveData<Unit>()
    private val _landmarksObservable = MutableLiveData<List<Landmark>>()
    private val _errors = MutableLiveData<Throwable>()

    // Exposed observable properties
    val foundLandmarks: LiveData<List<Landmark>> = _landmarksObservable
    val sessionCreated: LiveData<Unit> = _sessionCreatedObservable
    val errors: LiveData<Throwable> = _errors

    fun searchLandmarks(lat: Double, long: Double, radius: Int, limit: Int, categories: String) {
        val params = SearchLandmarks.RequestValues(lat, long, radius, categories, limit)
        SearchLandmarks(params, landmarkApi, AsyncTransformerFactory.create<List<Landmark>>())
                .execute()
                .subscribe({ _landmarksObservable.value = it },
                           { _errors.value = it })
                .also { addDisposable(it) }
    }

    fun setupSession(userId: String, landmarks: List<Landmark>) {
        val params = CreateSession.RequestValues(userId, landmarks)
        CreateSession(params, cloudDataSource, sessionManager, beaconManager, AsyncTransformerFactory.create())
                .execute()
                .subscribe({ _sessionCreatedObservable.value = Unit },
                           { _errors.value = it })
                .also { addDisposable(it) }
    }
}