package ubb.thesis.david.monumental.presentation.session.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import ubb.thesis.david.monumental.Injection
import ubb.thesis.david.monumental.domain.entities.Landmark
import ubb.thesis.david.monumental.presentation.BaseViewModel

class ResultViewModel : BaseViewModel() {

    private val dataSource = Injection.provideSessionManager()
    private val sessionCreatedObservable = BehaviorSubject.create<Unit>()
    private val landmarksObservable = MutableLiveData<List<Landmark>>()
    private val errorsObservable = MutableLiveData<String>()

    fun getVenuesObservable(): LiveData<List<Landmark>> = landmarksObservable

    fun getSessionCreatedObservable(): BehaviorSubject<Unit> = sessionCreatedObservable

    fun getErrorsObservable(): LiveData<String> = errorsObservable

    fun searchLandmarks(location: String, radius: Int, categories: String) =
        loadLandmarks(dataSource.loadLandmarks(location, radius, categories))

    fun searchLandmarks(location: String, radius: Int, limit: Int, categories: String) =
        loadLandmarks(dataSource.loadLandmarks(location, radius, categories, limit))

    fun setupSession(userId: String, landmarks: List<Landmark>) {
        dataSource.setupSession(userId, landmarks)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                sessionCreatedObservable.onNext(Unit)
            }, {
                errorsObservable.value = it.message
            }).also { addDisposable(it) }
    }

    private fun loadLandmarks(loadObservable: Single<List<Landmark>>) {
        loadObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                landmarksObservable.value = it
            }, {
                errorsObservable.value = it.message
            }).also { addDisposable(it) }
    }
}