package ubb.thesis.david.monumental.view.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.entities.Session
import ubb.thesis.david.domain.usecases.cloud.GetUserSessions
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseViewModel
import ubb.thesis.david.monumental.utils.default

class SessionHistoryViewModel(private val cloudDataSource: CloudDataSource) : BaseViewModel() {

    // Observable sources
    private val _sessionsRetrieved = MutableLiveData<List<Session>>()
    private val _displayList = MutableLiveData<Boolean>().default(false)
    private val _errors = MutableLiveData<Throwable>()

    // Exposed observable properties
    val sessionsRetrieved: LiveData<List<Session>> = _sessionsRetrieved
    val errors: LiveData<Throwable> = _errors

    // Data binding properties
    val displayList: LiveData<Boolean> = _displayList

    fun fetchSessions(userId: String) {
        GetUserSessions(userId, cloudDataSource, AsyncTransformerFactory.create<List<Session>>())
                .execute()
                .subscribe({ sessions ->
                               _sessionsRetrieved.value = sessions
                               _displayList.value = sessions.isNotEmpty()
                           },
                           { error -> _errors.value = error })
                .also { addDisposable(it) }
    }

}
