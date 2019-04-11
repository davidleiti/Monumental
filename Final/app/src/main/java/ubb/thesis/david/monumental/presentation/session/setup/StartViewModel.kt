package ubb.thesis.david.monumental.presentation.session.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ubb.thesis.david.monumental.Injection
import ubb.thesis.david.monumental.domain.entities.Session
import ubb.thesis.david.monumental.presentation.common.BaseViewModel

class StartViewModel : BaseViewModel() {

    private val dataSource = Injection.provideSessionManager()
    private val runningSessionObservable: MutableLiveData<Session?> = MutableLiveData()

    fun getRunningSessionObservable(): LiveData<Session?> = runningSessionObservable

    fun queryRunningSession(userId: String) {
        dataSource.getSession(userId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ session ->
                runningSessionObservable.value = session
            }, {
                runningSessionObservable.value = null
            }, {
                runningSessionObservable.value = null
            }).also { addDisposable(it) }
    }

    fun wipeSessionData(userId: String) {
        dataSource.wipeSession(userId)
            .subscribeOn(Schedulers.io())
            .subscribe().also { addDisposable(it) }
    }
}