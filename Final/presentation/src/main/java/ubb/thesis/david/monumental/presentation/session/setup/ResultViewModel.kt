package ubb.thesis.david.monumental.presentation.session.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.subjects.BehaviorSubject
import ubb.thesis.david.monumental.Injection
import ubb.thesis.david.monumental.GeofencingClientAdapter
import ubb.thesis.david.monumental.presentation.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.presentation.common.BaseViewModel

class ResultViewModel(private val geofencingClient: GeofencingClientAdapter) : BaseViewModel() {

    private val landmarkApi = Injection.provideLandmarkApi()
    private val sessionManager = Injection.provideSessionManager()

    private val sessionCreatedObservable = BehaviorSubject.create<Unit>()
    private val landmarksObservable = MutableLiveData<List<ubb.thesis.david.domain.entities.Landmark>>()
    private val errorsObservable = MutableLiveData<String>()

    fun getVenuesObservable(): LiveData<List<ubb.thesis.david.domain.entities.Landmark>> = landmarksObservable

    fun getSessionCreatedObservable(): BehaviorSubject<Unit> = sessionCreatedObservable

    fun getErrorsObservable(): LiveData<String> = errorsObservable

    fun searchLandmarks(location: String, radius: Int, limit: Int, categories: String) {
        val params = ubb.thesis.david.domain.usecases.SearchLandmarks.RequestValues(location, radius, categories, limit)
        ubb.thesis.david.domain.usecases.SearchLandmarks(params, landmarkApi,
                                                         AsyncTransformerFactory.create<List<ubb.thesis.david.domain.entities.Landmark>>())
                .execute()
                .subscribe({ landmarksObservable.value = it },
                           { errorsObservable.value = it.message })
                .also { addDisposable(it) }
    }

    fun setupSession(userId: String, landmarks: List<ubb.thesis.david.domain.entities.Landmark>) {
        val params = ubb.thesis.david.domain.usecases.CreateSession.RequestValues(userId, landmarks)
        ubb.thesis.david.domain.usecases.CreateSession(params, sessionManager, geofencingClient,
                                                       AsyncTransformerFactory.create())
                .execute()
                .subscribe({ sessionCreatedObservable.onNext(Unit) },
                           { errorsObservable.value = it.message })
                .also { addDisposable(it) }
    }
}