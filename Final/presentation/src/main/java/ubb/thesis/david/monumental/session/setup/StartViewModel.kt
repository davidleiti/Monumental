package ubb.thesis.david.monumental.session.setup

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.domain.BeaconManager
import ubb.thesis.david.domain.entities.Session
import ubb.thesis.david.domain.usecases.GetSession
import ubb.thesis.david.domain.usecases.WipeSession
import ubb.thesis.david.monumental.Configuration
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseAndroidViewModel
import ubb.thesis.david.monumental.utils.default
import java.util.*

class StartViewModel(private val beaconManager: BeaconManager, application: Application) :
    BaseAndroidViewModel(application) {

    private var runningSession: Session? = null
    private val resources = getApplication<Application>().resources
    private val sessionManager = Configuration.provideSessionManager()

    private val _sessionAvailable = MutableLiveData<Boolean>().default(false)
    private val _sessionMessage = MutableLiveData<String>().default(resources.getString(R.string.message_journey_start))

    val sessionAvailable: LiveData<Boolean> = _sessionAvailable
    val sessionMessage: LiveData<String> = _sessionMessage

    fun queryRunningSession(userId: String) {
        GetSession(userId, sessionManager, AsyncTransformerFactory.create<Session>())
                .execute()
                .subscribe({ session -> updateState(session) },
                           { debug(TAG_LOG, "Failed to retrieve session data for user $userId") })
                .also { addDisposable(it) }
    }

    fun wipeExistingSession() {
        runningSession?.let { session ->
            WipeSession(session.userId, sessionManager, beaconManager, AsyncTransformerFactory.create())
                    .execute()
                    .subscribe({ updateState(null) },
                               { debug(TAG_LOG, "Failed to wipe session data for user ${session.userId}") })
                    .also { addDisposable(it) }
        }
    }

    private fun updateState(session: Session?) {
        runningSession = session
        runningSession?.let {
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