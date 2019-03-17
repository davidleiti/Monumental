package ubb.license.david.monumentalv0.persistence

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.foursquareapi.model.Venue
import ubb.license.david.monumentalv0.persistence.cache.SessionDatabase
import ubb.license.david.monumentalv0.persistence.model.Landmark
import ubb.license.david.monumentalv0.persistence.model.Session
import ubb.license.david.monumentalv0.utils.debug
import ubb.license.david.monumentalv0.utils.info
import java.util.*

class SessionManager private constructor(private val database: SessionDatabase, private val api: FoursquareApi) {

    fun setupSession(userId: String, hostCity: String, landmarks: List<Landmark>): Completable =
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

    fun loadLandmarks(location: String, radius: Int, categories: String, limit: Int = 0): Single<List<Landmark>> {
        val searchRes = api.searchVenues(location, radius, FoursquareApi.ID_MONUMENT)
            .transformToLandmarkList()

        val exploreRes = api.exploreVenues(location, radius, FoursquareApi.SECTION_ARTS)
            .filterExploreResults(categories)
            .transformToLandmarkList()

        val combinedResult: Single<List<Landmark>> =
            Single.zip(searchRes, exploreRes, BiFunction { searchResults, exploreResults ->
                combineResults(searchResults, exploreResults)
            })

        return if (limit > 0)
            combinedResult.map { landmarks -> landmarks.take(limit) }
        else
            combinedResult
    }

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

    private fun combineResults(searchResults: List<Landmark>, exploreResults: List<Landmark>): List<Landmark> {
        val allVenues = ArrayList<Landmark>().apply {
            addAll(searchResults)
            addAll(exploreResults)
        }
        return allVenues.distinctBy { venue -> venue.id }
    }

    private fun Single<List<Venue>>.filterExploreResults(categoriesString: String): Single<List<Venue>> =
        map { venues -> venues.filter { venue -> categoriesString.contains(venue.categories!![0].id) } }

    private fun Single<List<Venue>>.transformToLandmarkList(): Single<List<Landmark>> =
        map { venues -> venues.map { venue -> Landmark.fromVenue(venue) } }

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