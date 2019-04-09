package ubb.thesis.david.monumental.data

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.foursquareapi.model.Venue
import ubb.thesis.david.monumental.data.cache.SessionDatabase
import ubb.thesis.david.monumental.domain.SessionRepository
import ubb.thesis.david.monumental.domain.entities.Landmark
import ubb.thesis.david.monumental.domain.entities.Session
import ubb.thesis.david.monumental.utils.debug
import ubb.thesis.david.monumental.utils.info
import java.util.*

class SessionManager private constructor(private val database: SessionDatabase, private val api: FoursquareApi) :
    SessionRepository {

    override fun setupSession(userId: String, landmarks: List<Landmark>): Completable =
        Completable.fromCallable {
            landmarks.forEach { landmark -> landmark.userId = userId }
            database.runInTransaction {
                val session = Session(
                    userId = userId,
                    city = "dummyCity",
                    timeStarted = Date()
                )
                database.sessionDao().createSession(session)
                database.landmarkDao().addLandmarks(landmarks)
            }
        }.doOnComplete {
            info(TAG_LOG, "Session for user $userId setup successfully.")
        }.doOnError {
            debug(TAG_LOG, "Failed to set up session for user $userId, cause: ${it.message}")
        }

    override fun searchLandmarks(
        location: String,
        radius: Int,
        categories: String,
        limit: Int
    ): Single<List<Landmark>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    override fun getSession(userId: String): Maybe<Session> =
        database.sessionDao().getUserSession(userId)
            .doOnSuccess {
                info(TAG_LOG, "Session $it retrieved successfully.")
            }
            .doOnError {
                debug(TAG_LOG, "Failed to retrieve session data of user $userId, cause: ${it.message}")
            }

    override fun getSessionLandmarks(userId: String): Maybe<List<Landmark>> =
        database.landmarkDao().getSessionLandmarks(userId)
            .doOnSuccess {
                info(TAG_LOG, "Landmarks of user $userId retrieved successfully.")
            }
            .doOnError {
                debug(TAG_LOG, "Failed to retrieve landmarks from user $userId's session")
            }

    override fun updateLandmark(landmark: Landmark): Completable =
        database.landmarkDao().updateLandmark(landmark)
    override fun wipeSession(userId: String): Completable =
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

private fun Single<List<Venue>>.filterExploreResults(categoriesString: String): Single<List<Venue>> =
    map { venues -> venues.filter { venue -> categoriesString.contains(venue.categories!![0].id) } }

private fun Single<List<Venue>>.transformToLandmarkList(): Single<List<Landmark>> =
    map { venues -> venues.map { venue -> Landmark.fromVenue(venue) } }