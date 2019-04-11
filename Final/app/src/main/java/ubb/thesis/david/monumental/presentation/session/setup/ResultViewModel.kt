package ubb.thesis.david.monumental.presentation.session.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import ubb.thesis.david.monumental.Injection
import ubb.thesis.david.monumental.domain.LandmarkApi
import ubb.thesis.david.monumental.domain.SessionManager
import ubb.thesis.david.monumental.domain.entities.Landmark
import ubb.thesis.david.monumental.domain.usecases.SearchLandmarks
import ubb.thesis.david.monumental.presentation.common.AsyncTransformer
import ubb.thesis.david.monumental.presentation.common.BaseViewModel

class ResultViewModel(private val landmarkApi: LandmarkApi, private val sessionManager: SessionManager) :
    BaseViewModel() {

    private val sessionCreatedObservable = BehaviorSubject.create<Unit>()
    private val landmarksObservable = MutableLiveData<List<Landmark>>()
    private val errorsObservable = MutableLiveData<String>()

    fun getVenuesObservable(): LiveData<List<Landmark>> = landmarksObservable

    fun getSessionCreatedObservable(): BehaviorSubject<Unit> = sessionCreatedObservable

    fun getErrorsObservable(): LiveData<String> = errorsObservable

    fun searchLandmarks(location: String, radius: Int, limit: Int, categories: String) {
        val params = SearchLandmarks.SearchParams(location, radius, categories, limit)
        val useCase = SearchLandmarks(params, landmarkApi, AsyncTransformer())
        useCase.execute()
                .subscribe({ landmarksObservable.value = it },
                           { errorsObservable.value = it.message })
                .also { addDisposable(it) }
    }

    fun setupSession(userId: String, landmarks: List<Landmark>) {
        sessionManager.setupSession(userId, landmarks)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ sessionCreatedObservable.onNext(Unit) },
                           { errorsObservable.value = it.message })
                .also { addDisposable(it) }
    }
}