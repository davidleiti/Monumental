package ubb.license.david.monumentalv0.ui.session.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ubb.license.david.foursquareapi.model.Venue
import ubb.license.david.monumentalv0.persistence.SessionManager
import ubb.license.david.monumentalv0.persistence.model.Landmark

class ResultViewModel(private val mDataSource: SessionManager) : ViewModel() {

    private val sessionIdObservable = MutableLiveData<Long>()
    private val landmarksObservable = MutableLiveData<Array<Landmark>>()
    private val errorsObservable = MutableLiveData<String>()
    private var mDisposables = CompositeDisposable()

    fun getVenuesObservable(): LiveData<Array<Landmark>> = landmarksObservable

    fun getSessionIdObservable(): LiveData<Long> = sessionIdObservable

    fun getErrorsObservable(): LiveData<String> = errorsObservable

    fun cancelRequests() = mDisposables.clear()

    fun searchLandmarks(location: String, radius: Int, categories: String) =
        loadLandmarks(mDataSource.loadLandmarks(location, radius, categories))

    fun searchLandmarks(location: String, radius: Int, limit: Int, categories: String) =
            loadLandmarks(mDataSource.loadLandmarks(location, radius, limit, categories))

    fun setupSession(userId: String, city: String, landmarks: Array<Landmark>) {
        val disposable = mDataSource.setupSession(userId, city, landmarks)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ id ->
                sessionIdObservable.value = id
            }, {
                errorsObservable.value = it.message
            })
        mDisposables.add(disposable)
    }

    private fun loadLandmarks(observable: Single<Array<Landmark>>) {
        val disposable = observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                landmarksObservable.value = it
            }, {
                errorsObservable.value = it.message
            })
        mDisposables.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        mDisposables.dispose()
    }
}