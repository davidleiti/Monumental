package ubb.thesis.david.monumental.presentation.session.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.monumental.Injection
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.domain.entities.Session
import ubb.thesis.david.domain.usecases.GetSession
import ubb.thesis.david.domain.usecases.WipeSession
import ubb.thesis.david.monumental.presentation.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.presentation.common.BaseViewModel

class StartViewModel(private val beaconManager: ubb.thesis.david.domain.BeaconManager) : BaseViewModel() {

    private val sessionManager = Injection.provideSessionManager()
    private val runningSessionObservable: MutableLiveData<ubb.thesis.david.domain.entities.Session?> = MutableLiveData()

    fun getRunningSessionObservable(): LiveData<ubb.thesis.david.domain.entities.Session?> = runningSessionObservable

    fun queryRunningSession(userId: String) {
        ubb.thesis.david.domain.usecases.GetSession(userId, sessionManager,
                                                    AsyncTransformerFactory.create<ubb.thesis.david.domain.entities.Session>())
                .execute()
                .subscribe({ session -> runningSessionObservable.value = session },
                           { runningSessionObservable.value = null },
                           { runningSessionObservable.value = runningSessionObservable.value })
                .also { addDisposable(it) }
    }

    fun wipeSessionData(userId: String) {
        ubb.thesis.david.domain.usecases.WipeSession(userId, sessionManager, beaconManager,
                                                     AsyncTransformerFactory.create())
                .execute()
                .subscribe()
                .also { addDisposable(it) }
    }
}