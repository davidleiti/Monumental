package ubb.thesis.david.domain

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ubb.thesis.david.domain.entities.Backup
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.entities.Session

interface CloudDataSource {

    fun createSession(session: Session, landmarks: List<Landmark>): Single<String>

    fun getUserSessions(userId: String): Maybe<List<Session>>
    fun getSessionDetails(userId: String, sessionId: String): Maybe<Map<Landmark, Discovery?>>

    fun updateSessionBackup(backup: Backup): Completable
    fun wipeSessionBackup(userId: String): Completable

}