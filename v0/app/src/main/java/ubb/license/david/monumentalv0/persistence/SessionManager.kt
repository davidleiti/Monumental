package ubb.license.david.monumentalv0.persistence

import androidx.lifecycle.LiveData
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.monumentalv0.persistence.cache.SessionDatabase
import ubb.license.david.monumentalv0.persistence.model.Landmark
import ubb.license.david.monumentalv0.persistence.model.Session
import java.util.*

class SessionManager private constructor(private val mDatabase: SessionDatabase, private val api: FoursquareApi) {

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

    fun loadLandmarks(location: String, radius: Int, categories: String): Single<Array<Landmark>> =
        api.searchVenues(location, radius, categories)
            .map { venues -> venues.map { venue -> Landmark.fromVenue(venue) }.toTypedArray() }

    fun loadLandmarks(location: String, radius: Int, limit: Int, categories: String): Single<Array<Landmark>> =
        api.searchVenues(location, radius, limit, categories)
            .map { venues -> venues.map { venue -> Landmark.fromVenue(venue) }.toTypedArray() }

    fun clearSessions(): Completable =
        Completable.fromCallable { mDatabase.sessionDao().clearSessions() }

    fun cancelSession(sessionId: Long): Completable =
        Completable.fromCallable { mDatabase.sessionDao().clearUnfinishedSessions(sessionId) }

    fun finalizeSession(session: Session): Completable {
        session.timeFinished = Date()
        return mDatabase.sessionDao().updateSession(session)
    }

    fun getSessionLandmarks(sessionId: Int): LiveData<List<Landmark>> =
        mDatabase.landmarkDao().getSessionLandmarks(sessionId)

    fun getSession(sessionId: Long): Maybe<Session> =
        mDatabase.sessionDao().getSessionById(sessionId)

    fun updateLandmark(landmark: Landmark): Completable =
        mDatabase.landmarkDao().updateLandmark(landmark)

    companion object {
        @Volatile
        private var sInstance: SessionManager? = null

        fun getInstance(database: SessionDatabase, api: FoursquareApi) =
            sInstance ?: synchronized(this) {
                sInstance
                    ?: SessionManager(database, api)
            }
    }
}