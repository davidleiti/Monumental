package ubb.license.david.monumentalv0.persistence

import androidx.lifecycle.LiveData
import io.reactivex.Completable
import io.reactivex.Single
import ubb.license.david.monumentalv0.persistence.cache.SessionDatabase
import ubb.license.david.monumentalv0.persistence.model.Landmark
import ubb.license.david.monumentalv0.persistence.model.Session
import java.util.*

class SessionRepository private constructor(private val mDatabase: SessionDatabase) {

    fun getSessionLandmarks(sessionId: Int): LiveData<List<Landmark>> {
        return mDatabase.landmarkDao().getSessionLandmarks(sessionId)
    }

    fun setupSession(user: String, hostCity: String, landmarks: Array<Landmark>): Single<Long> {
        return Single.fromCallable {
            var sessionId = 0L
            mDatabase.runInTransaction {
                val session = Session(
                    id = null,
                    userId = user,
                    timeStarted = Date(),
                    city = hostCity
                )
                sessionId = mDatabase.sessionDao().createSession(session)
                landmarks.forEach { landmark -> landmark.sessionId = sessionId }
                mDatabase.landmarkDao().addLandmarks(landmarks)
            }
            sessionId
        }
    }

    fun updateLandmark(landmark: Landmark): Completable =
        mDatabase.landmarkDao().updateLandmark(landmark)

    fun wipeSessionsCache(): Completable =
        Completable.fromCallable { mDatabase.sessionDao().deleteSessions() }

    fun wipeUserCache(userId: String): Completable =
        Completable.fromCallable { mDatabase.sessionDao().deleteUnfinishedSessions(userId) }

    fun finalizeSession(session: Session): Completable {
        session.timeFinished = Date()
        return mDatabase.sessionDao().updateSession(session)
    }

    companion object {
        @Volatile
        private var sInstance: SessionRepository? = null

        fun getInstance(database: SessionDatabase) =
            sInstance ?: synchronized(this) {
                sInstance
                    ?: SessionRepository(database)
            }
    }
}