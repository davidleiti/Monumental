package ubb.thesis.david.domain

import io.reactivex.Completable
import io.reactivex.Maybe
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.entities.Session

interface CloudDataSource {

    fun getUserSessions(userId: String): Maybe<Session>
    fun getSessionDetails(userId: String, sessionId: String): Maybe<Map<Landmark, Discovery?>>
    fun createSession(session: Session, landmarks: List<Landmark>): Completable
    fun updateSessionBackup(userId: String, landmarks: Map<Landmark, Discovery?>): Completable
    fun wipeSessionBackup(userId: String): Completable

}