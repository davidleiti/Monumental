package ubb.license.david.monumentalv0.ui.session.tracking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ubb.license.david.monumentalv0.Injection
import ubb.license.david.monumentalv0.persistence.SessionManager
import ubb.license.david.monumentalv0.persistence.model.Landmark
import ubb.license.david.monumentalv0.ui.BaseViewModel

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