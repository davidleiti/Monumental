package ubb.thesis.david.monumental.view.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.subjects.BehaviorSubject
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.CreateSession
import ubb.thesis.david.domain.usecases.SearchLandmarks
import ubb.thesis.david.monumental.Configuration
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseViewModel

class ResultViewModel(private val beaconManager: BeaconManager) : BaseViewModel() {

    private val landmarkApi = Configuration.provideLandmarkApi()
    private val sessionManager = Configuration.provideSessionManager()

    private val sessionCreatedObservable = BehaviorSubject.create<Unit>()
    private val landmarksObservable = MutableLiveData<List<Landmark>>()
    private val errorsObservable = MutableLiveData<String>()

    fun getVenuesObservable(): LiveData<List<Landmark>> = landmarksObservable

    fun getSessionCreatedObservable(): BehaviorSubject<Unit> = sessionCreatedObservable

    fun getErrorsObservable(): LiveData<String> = errorsObservable

    fun searchLandmarks(lat: Double, long: Double, radius: Int, limit: Int, categories: String) {
        val params = SearchLandmarks.RequestValues(lat, long, radius, categories, limit)
        SearchLandmarks(params, landmarkApi, AsyncTransformerFactory.create<List<Landmark>>())
                .execute()
                .subscribe({ landmarksObservable.value = it },
                           { errorsObservable.value = it.message })
                .also { addDisposable(it) }
    }

    fun setupSession(userId: String, landmarks: List<Landmark>) {
        val params = CreateSession.RequestValues(userId, landmarks)
        CreateSession(params, sessionManager, beaconManager, AsyncTransformerFactory.create())
                .execute()
                .subscribe({ sessionCreatedObservable.onNext(Unit) },
                           { errorsObservable.value = it.message })
                .also { addDisposable(it) }
    }
}