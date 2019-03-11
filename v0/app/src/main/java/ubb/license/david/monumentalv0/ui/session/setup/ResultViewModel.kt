package ubb.license.david.monumentalv0.ui.session.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import ubb.license.david.monumentalv0.Injection
import ubb.license.david.monumentalv0.persistence.model.Landmark
import ubb.license.david.monumentalv0.ui.BaseViewModel

class ResultViewModel : BaseViewModel() {

    private val dataSource = Injection.provideSessionManager()
    private val sessionCreatedObservable = BehaviorSubject.create<Unit>()
    private val landmarksObservable = MutableLiveData<Array<Landmark>>()
    private val errorsObservable = MutableLiveData<String>()

    fun getVenuesObservable(): LiveData<Array<Landmark>> = landmarksObservable

    fun getSessionCreatedObservable(): BehaviorSubject<Unit> = sessionCreatedObservable

    fun getErrorsObservable(): LiveData<String> = errorsObservable

    fun searchLandmarks(location: String, radius: Int, categories: String) =
        loadLandmarks(dataSource.loadLandmarks(location, radius, categories))

    fun searchLandmarks(location: String, radius: Int, limit: Int, categories: String) =
        loadLandmarks(dataSource.loadLandmarks(location, radius, limit, categories))

    fun setupSession(userId: String, city: String, landmarks: Array<Landmark>) {
        dataSource.setupSession(userId, city, landmarks)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                sessionCreatedObservable.onNext(Unit)
            }, {
                errorsObservable.value = it.message
            }).also { addDisposable(it) }
    }

    private fun loadLandmarks(loadObservable: Single<Array<Landmark>>) {
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