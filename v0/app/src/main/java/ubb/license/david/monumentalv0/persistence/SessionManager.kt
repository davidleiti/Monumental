package ubb.license.david.monumentalv0.persistence

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.monumentalv0.persistence.cache.SessionDatabase
import ubb.license.david.monumentalv0.persistence.model.Landmark
import ubb.license.david.monumentalv0.persistence.model.Session
import ubb.license.david.monumentalv0.utils.debug
import ubb.license.david.monumentalv0.utils.info
import java.util.*

class SessionManager private constructor(private val database: SessionDatabase, private val api: FoursquareApi) {

    fun setupSession(userId: String, hostCity: String, landmarks: Array<Landmark>): Completable =
        Completable.fromCallable {
            landmarks.forEach { landmark -> landmark.userId = userId }
            database.runInTransaction {
                val session = Session(
                    userId = userId,
                    timeStarted = Date(),
                    city = hostCity
                )
                database.sessionDao().createSession(session)
                database.landmarkDao().addLandmarks(landmarks)
            }
        }.doOnComplete {
            info(TAG_LOG, "Session for user $userId setup successfully.")
        }.doOnError {
            debug(TAG_LOG, "Failed to set up session for user $userId, cause: ${it.message}")
        }

    fun loadLandmarks(location: String, radius: Int, categories: String): Single<Array<Landmark>> =
        api.searchVenues(location, radius, categories)
            .map { venues -> venues.map { venue -> Landmark.fromVenue(venue) }.toTypedArray() }

    fun loadLandmarks(location: String, radius: Int, limit: Int, categories: String): Single<Array<Landmark>> =
        api.searchVenues(location, radius, limit, categories)
            .map { venues -> venues.map { venue -> Landmark.fromVenue(venue) }.toTypedArray() }

    fun getSession(userId: String): Maybe<Session> =
        database.sessionDao().getUserSession(userId)
            .doOnSuccess {
                info(TAG_LOG, "Session $it retrieved successfully.")
            }
            .doOnError {
                debug(TAG_LOG, "Failed to retrieve session data of user $userId, cause: ${it.message}")
            }

    fun getSessionLandmarks(userId: String): Maybe<List<Landmark>> =
        database.landmarkDao().getSessionLandmarks(userId)
            .doOnSuccess {
                info(TAG_LOG, "Landmarks of user $userId retrieved successfully.")
            }
            .doOnError {
                debug(TAG_LOG, "Failed to retrieve landmarks from user $userId's session")
            }

    fun updateLandmark(landmark: Landmark): Completable =
        database.landmarkDao().updateLandmark(landmark)

    fun wipeDatabase(): Completable =
        Completable.fromCallable {
            database.runInTransaction {
                database.sessionDao().clearSessions()
                database.landmarkDao().clearLandmarks()
            }
        }.doOnComplete {
            info(TAG_LOG, "Wiped the session cache database entirely.")
        }.doOnError {
            debug(TAG_LOG, "Failed to wipe session cache, cause: ${it.message}")
        }

    fun wipeSession(userId: String): Completable =
        Completable.fromCallable {
            database.runInTransaction {
                database.sessionDao().clearUserSession(userId)
                database.landmarkDao().clearUserLandmarks(userId)
            }
        }.doOnComplete {
            info(TAG_LOG, "Wiped session data of user $userId")
        }.doOnError {
            debug(TAG_LOG, "Failed to wipe session data of user $userId, cause: ${it.message}")
        }

    companion object {
        @Volatile
        private var sInstance: SessionManager? = null

        private const val TAG_LOG = "SessionManagerLogger"

        fun getInstance(database: SessionDatabase, api: FoursquareApi) =
            sInstance ?: synchronized(this) {
                sInstance
                    ?: SessionManager(database, api)
            }
    }
}