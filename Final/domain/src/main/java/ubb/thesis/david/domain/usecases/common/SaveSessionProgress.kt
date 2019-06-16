package ubb.thesis.david.domain.usecases.common

import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.entities.Backup
import ubb.thesis.david.domain.usecases.base.CompletableUseCase
import java.util.*

class SaveSessionProgress(private val userId: String,
                          private val sessionManager: SessionManager,
                          private val cloudDataSource: CloudDataSource,
                          transformer: CompletableTransformer) : CompletableUseCase(transformer) {

    override fun createSource(): Completable =
        sessionManager.getSession(userId)
                .concatMap { session ->
                    sessionManager.getSessionLandmarks(userId)
                            .map { sessionData ->
                                Backup(session, sessionData).also { backup ->
                                    if (backup.landmarks.filter { it.value == null }.isEmpty())
                                        backup.session.timeFinished = Date()
                                }
                            }
                }.flatMapCompletable { backup ->
                    cloudDataSource.updateSessionBackup(backup)
                }
}