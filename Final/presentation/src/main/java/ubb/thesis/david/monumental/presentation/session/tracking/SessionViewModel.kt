package ubb.thesis.david.monumental.presentation.session.tracking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.monumental.Injection
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.GetSessionLandmarks
import ubb.thesis.david.monumental.presentation.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.presentation.common.BaseViewModel

class SessionViewModel : BaseViewModel() {

    private val sessionManager: ubb.thesis.david.domain.SessionManager = Injection.provideSessionManager()
    private val errorsObservable: MutableLiveData<String> = MutableLiveData()
    private val landmarksObservable: MutableLiveData<List<ubb.thesis.david.domain.entities.Landmark>> = MutableLiveData()

    fun getLandmarksObservable(): LiveData<List<ubb.thesis.david.domain.entities.Landmark>> = landmarksObservable

    fun getErrorsObservable(): LiveData<String> = errorsObservable

    fun loadSessionLandmarks(sessionId: String) {
        ubb.thesis.david.domain.usecases.GetSessionLandmarks(sessionId, sessionManager,
                                                             AsyncTransformerFactory.create<List<ubb.thesis.david.domain.entities.Landmark>>())
                .execute()
                .subscribe({ landmarksObservable.value = it },
                           { errorsObservable.postValue(it.message) })
                .also { addDisposable(it) }
    }
}