package ubb.thesis.david.monumental.view.setup

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.entities.Backup
import ubb.thesis.david.domain.entities.Session
import ubb.thesis.david.domain.usecases.cloud.GetBackupSession
import ubb.thesis.david.domain.usecases.common.WipeActiveSession
import ubb.thesis.david.domain.usecases.local.CacheSession
import ubb.thesis.david.domain.usecases.local.GetCachedSession
import ubb.thesis.david.domain.usecases.local.WipeCachedSession
import ubb.thesis.david.monumental.Configuration
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseAndroidViewModel
import ubb.thesis.david.monumental.common.SingleLiveEvent
import ubb.thesis.david.monumental.utils.default
import java.util.*

class StartViewModel(private val beaconManager: BeaconManager,
                     private val cloudDataSource: CloudDataSource,
                     application: Application) :
    BaseAndroidViewModel(application) {

    // Resources
    private val sessionManager = Configuration.provideSessionManager()

    // Observable sources
    private val _backupLoaded = SingleLiveEvent<Boolean>()
    private val _sessionWiped = SingleLiveEvent<Unit>()
    private val _sessionAvailable = MutableLiveData<Boolean>().default(false)
    private val _sessionMessage = MutableLiveData<String>().default(resources.getString(R.string.message_journey_start))
    private val _errors = MutableLiveData<Throwable>()

    // Binding properties
    val sessionAvailable: LiveData<Boolean> = _sessionAvailable
    val sessionMessage: LiveData<String> = _sessionMessage

    // Observable properties
    val backupLoaded: LiveData<Boolean> = _backupLoaded
    val sessionWiped: LiveData<Unit> = _sessionWiped
    val errors: LiveData<Throwable> = _errors

    // Synchronization flag
    private var backupFound: Boolean = false

    fun loadSessionBackup(userId: String) {
        backupFound = false
        GetBackupSession(userId, cloudDataSource, AsyncTransformerFactory.create<Backup>())
                .execute()
                .subscribe({ backup ->
                               backupFound = true
                               cacheSession(backup)
                           },
                           { error -> _errors.value = error },
                           {
                               if (!backupFound) {
                                   _backupLoaded.value = false
                                   wipeLocalCache(userId)
                               }
                           })
                .also { addDisposable(it) }
    }

    fun loadSessionCache(userId: String) {
        _sessionAvailable.value = false
        GetCachedSession(userId, sessionManager, AsyncTransformerFactory.create<Session>())
                .execute()
                .subscribe({ session -> updateState(session) },
                           { error ->
                               debug(TAG_LOG, "Failed to retrieve session data for user $userId with error $error")
                           },
                           { if (_sessionAvailable.value == false) updateState(null) })
                .also { addDisposable(it) }
    }

    private fun cacheSession(backup: Backup) {
        CacheSession(backup, sessionManager, beaconManager, AsyncTransformerFactory.create())
                .execute()
                .subscribe({
                               _backupLoaded.value = true
                               updateState(backup.session)
                           }, { error ->
                               _errors.value = error
                           })
                .also { addDisposable(it) }
    }

    fun wipeSessionData(userId: String) {
        WipeActiveSession(userId, cloudDataSource, sessionManager, beaconManager, AsyncTransformerFactory.create())
                .execute()
                .subscribe({ _sessionWiped.value = null },
                           { error -> _errors.value = error })
                .also { addDisposable(it) }
    }

    private fun wipeLocalCache(userId: String) {
        WipeCachedSession(userId, sessionManager, beaconManager, AsyncTransformerFactory.create())
                .execute()
                .subscribe { updateState(null) }
                .also { addDisposable(it) }
    }

    private fun updateState(session: Session?) {
        session?.let {
            _sessionAvailable.value = true
            _sessionMessage.value =
                resources.getString(R.string.message_journey_found, getTimeElapsedString(it.timeStarted))
        } ?: run {
            _sessionAvailable.value = false
            _sessionMessage.value = resources.getString(R.string.message_journey_start)
        }
    }

    private fun getTimeElapsedString(start: Date): String {
        val resources = getApplication<Application>().resources
        val secondsFactor = 1000
        val minutesFactor = secondsFactor * 60
        val hoursFactor = minutesFactor * 60
        val daysFactor = hoursFactor * 24

        var deltaTime = Date().time - start.time
        val deltaDays = deltaTime / daysFactor
        if (deltaDays > 0)
            return if (deltaDays > 1) "$deltaDays ${resources.getString(R.string.days_ago)}"
            else "1 ${resources.getString(R.string.day_ago)}"

        deltaTime %= daysFactor
        val deltaHours = deltaTime / hoursFactor
        if (deltaHours > 0)
            return if (deltaHours > 1) "$deltaHours ${resources.getString(R.string.hours_ago)}"
            else "1 ${resources.getString(R.string.hour_ago)}"

        deltaTime %= hoursFactor
        val deltaMinutes = deltaTime / minutesFactor
        if (deltaMinutes > 0)
            return if (deltaMinutes > 1) "$deltaMinutes ${resources.getString(R.string.minutes_ago)}"
            else "1 ${resources.getString(R.string.minute_ago)}"

        return resources.getString(R.string.just_now)
    }

    companion object {
        private const val TAG_LOG = "StartViewModel"
    }

}