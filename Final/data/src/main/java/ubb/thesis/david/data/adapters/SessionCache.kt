package ubb.thesis.david.data.adapters

import io.reactivex.Completable
import io.reactivex.Maybe
import ubb.thesis.david.data.cache.SessionDatabase
import ubb.thesis.david.data.entities.BeaconData
import ubb.thesis.david.data.entities.SessionData
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.entities.Backup
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.entities.Session
import java.util.*
import kotlin.collections.HashMap

class SessionCache private constructor(private val database: SessionDatabase) :
    SessionManager {

    override fun createSession(session: Session, landmarks: List<Landmark>): Completable =
        saveSessionData(session, landmarks.map { BeaconData.fromEntity(it, session.userId) })

    override fun saveSessionBackup(backup: Backup): Completable =
        saveSessionData(backup.session, backup.landmarks.map { BeaconData.fromMapEntry(backup.session.userId, it) })

    override fun getSession(userId: String): Maybe<Session> =
        database.sessionDao().getUserSession(userId)
                .doOnSuccess {
                    info(TAG_LOG, "Cached session $it retrieved successfully.")
                }.doOnError { error ->
                    debug(TAG_LOG, "Failed to retrieve session data of user $userId, cause: ${error.message}")
                }.map { SessionData.toEntity(it) }

    override fun getSessionLandmarks(userId: String): Maybe<Map<Landmark, Discovery?>> =
        database.beaconDao().getSessionBeacons(userId)
                .doOnSuccess {
                    info(TAG_LOG, "Cached landmarks of user $userId retrieved successfully.")
                }.doOnError { error ->
                    debug(TAG_LOG, "Failed to retrieve cached landmarks with error ${error.message}")
                }.map { beacons ->
                    val entityMap = HashMap<Landmark, Discovery?>()
                    beacons.forEach { data ->
                        entityMap[data.extractEntity()] = data.extractDiscovery()
                    }
                    entityMap
                }

    override fun updateLandmark(landmark: Landmark, userId: String, photoId: String?, foundAt: Date?): Completable =
        database.beaconDao().updateBeacon(BeaconData.fromEntity(landmark, userId, photoId, foundAt))
                .doOnComplete {
                    info(TAG_LOG, "Cached data of landmark ${landmark.id} has been updated successfully!")
                }.doOnError { error ->
                    debug(TAG_LOG, "Failed to update cached landmark ${landmark.id} with error ${error.message}")
                }

    override fun wipeSession(userId: String): Completable =
        Completable.fromCallable {
            database.runInTransaction {
                database.sessionDao().clearUserSession(userId)
                database.beaconDao().clearSessionBeacons(userId)
            }
        }.doOnComplete {
            info(TAG_LOG, "Wiped cached session data of user $userId")
        }.doOnError { error ->
            debug(TAG_LOG, "Failed to wipe cached session data of user $userId with error: ${error.message}")
        }

    private fun saveSessionData(session: Session, beacons: List<BeaconData>) =
        Completable.fromCallable {
            database.runInTransaction {
                val sessionData = SessionData.fromEntity(session)
                database.sessionDao().createSession(sessionData)
                database.beaconDao().addBeacons(beacons)
            }
        }.doOnComplete {
            info(TAG_LOG, "Session for cache for user ${session.userId} has been set up successfully.")
        }.doOnError { error ->
            debug(TAG_LOG, "Failed to set up session cache for user ${session.userId} with error: ${error.message}")
        }

    companion object {
        @Volatile
        private var sInstance: SessionManager? = null

        private const val TAG_LOG = "SessionManagerLogger"

        fun getInstance(database: SessionDatabase) =
            sInstance ?: synchronized(this) {
                sInstance
                    ?: SessionCache(database)
            }
    }
}