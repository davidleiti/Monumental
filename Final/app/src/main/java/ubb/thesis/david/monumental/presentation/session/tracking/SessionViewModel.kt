package ubb.thesis.david.monumental.presentation.session.tracking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ubb.thesis.david.monumental.Injection
import ubb.thesis.david.monumental.data.SessionManager
import ubb.thesis.david.monumental.domain.entities.Landmark
import ubb.thesis.david.monumental.presentation.BaseViewModel

class SessionViewModel : BaseViewModel() {

    private val dataSource: SessionManager = Injection.provideSessionManager()
    private val errorsObservable: MutableLiveData<String> = MutableLiveData()
    private val landmarksObservable: MutableLiveData<List<Landmark>> = MutableLiveData()

    fun getLandmarksObservable(): LiveData<List<Landmark>> = landmarksObservable

    fun getErrorsObservable(): LiveData<String> = errorsObservable

    fun loadSessionLandmarks(sessionId: String) {
        dataSource.getSessionLandmarks(sessionId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                landmarksObservable.value = it
            }, {
                errorsObservable.postValue(it.message)
            }).also { addDisposable(it) }
    }
}