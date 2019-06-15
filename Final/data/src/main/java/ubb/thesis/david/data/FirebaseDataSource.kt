package ubb.thesis.david.data

import io.reactivex.Completable
import io.reactivex.Maybe
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.entities.Session

class FirebaseDataSource : CloudDataSource {

    override fun getUserSessions(userId: String): Maybe<Session> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSessionDetails(userId: String, sessionId: String): Maybe<Map<Landmark, Discovery?>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createSession(session: Session, landmarks: List<Landmark>): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateSessionBackup(userId: String, landmarks: Map<Landmark, Discovery?>): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun wipeSessionBackup(userId: String): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}