package ubb.thesis.david.monumental.session.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.domain.entities.Session
import ubb.thesis.david.domain.usecases.GetSession
import ubb.thesis.david.domain.usecases.WipeSession
import ubb.thesis.david.monumental.Injection
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseViewModel

class StartViewModel(private val beaconManager: BeaconManager) : BaseViewModel() {

    private val sessionManager = Injection.provideSessionManager()
    private val runningSessionObservable: MutableLiveData<Session?> = MutableLiveData()

    fun getRunningSessionObservable(): LiveData<Session?> = runningSessionObservable

    fun queryRunningSession(userId: String) {
        GetSession(userId, sessionManager, AsyncTransformerFactory.create<Session>())
                .execute()
                .subscribe({ session -> runningSessionObservable.value = session },
                           { runningSessionObservable.value = null },
                           { runningSessionObservable.value = runningSessionObservable.value })
                .also { addDisposable(it) }
    }

    fun wipeSessionData(userId: String) {
        WipeSession(userId, sessionManager, beaconManager, AsyncTransformerFactory.create())
                .execute()
                .subscribe()
                .also { addDisposable(it) }
    }
}