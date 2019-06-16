package ubb.thesis.david.domain.usecases.cloud

import io.reactivex.Maybe
import io.reactivex.Observable
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.common.Transformer
import ubb.thesis.david.domain.entities.Backup
import ubb.thesis.david.domain.usecases.base.ObservableUseCase

class GetBackupSession(private val userId: String,
                       private val cloudDataSource: CloudDataSource,
                       transformer: Transformer<Backup>) : ObservableUseCase<Backup>(transformer) {

    override fun createSource(): Observable<Backup> =
        cloudDataSource.getUserSessions(userId)
                .map { sessions ->
                    sessions.filter { it.timeFinished == null }
                }.concatMap { sessions ->
                    if (sessions.isNotEmpty()) {
                        val backup = sessions[0]
                        cloudDataSource.getSessionDetails(backup.userId, backup.sessionId!!)
                                .map { landmarkData -> Backup(backup, landmarkData) }
                    } else {
                        Maybe.empty()
                    }
                }.toObservable()

}