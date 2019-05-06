package ubb.thesis.david.monumental.presentation.session.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.subjects.BehaviorSubject
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.CreateSession
import ubb.thesis.david.domain.usecases.SearchLandmarks
import ubb.thesis.david.monumental.GeofencingClientAdapter
import ubb.thesis.david.monumental.Injection
import ubb.thesis.david.monumental.presentation.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.presentation.common.BaseViewModel

class ResultViewModel(private val geofencingClient: GeofencingClientAdapter) : BaseViewModel() {

    private val landmarkApi = Injection.provideLandmarkApi()
    private val sessionManager = Injection.provideSessionManager()

    private val sessionCreatedObservable = BehaviorSubject.create<Unit>()
    private val landmarksObservable = MutableLiveData<List<Landmark>>()
    private val errorsObservable = MutableLiveData<String>()

    fun getVenuesObservable(): LiveData<List<Landmark>> = landmarksObservable

    fun getSessionCreatedObservable(): BehaviorSubject<Unit> = sessionCreatedObservable

    fun getErrorsObservable(): LiveData<String> = errorsObservable

    fun searchLandmarks(location: String, radius: Int, limit: Int, categories: String) {
        val params = SearchLandmarks.RequestValues(location, radius, categories, limit)
        SearchLandmarks(params, landmarkApi,
                        AsyncTransformerFactory.create<List<Landmark>>())
                .execute()
                .subscribe({ landmarksObservable.value = it },
                           { errorsObservable.value = it.message })
                .also { addDisposable(it) }
    }

    fun setupSession(userId: String, landmarks: List<Landmark>) {
        val params = CreateSession.RequestValues(userId, landmarks)
        CreateSession(params, sessionManager, geofencingClient,
                      AsyncTransformerFactory.create())
                .execute()
                .subscribe({ sessionCreatedObservable.onNext(Unit) },
                           { errorsObservable.value = it.message })
                .also { addDisposable(it) }
    }
}