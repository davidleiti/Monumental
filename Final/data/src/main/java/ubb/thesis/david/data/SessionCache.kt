package ubb.thesis.david.data

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

    private fun saveSessionData(session: Session, beacons: List<BeaconData>) =
        Completable.fromCallable {
            database.runInTransaction {
                val sessionData = SessionData.fromEntity(session)
                database.sessionDao().createSession(sessionData)
                database.beaconDao().addBeacons(beacons)
            }
        }.doOnComplete {
            info(TAG_LOG, "Session for user ${session.userId} setup successfully.")
        }.doOnError {
            debug(TAG_LOG, "Failed to set up session for user ${session.userId}, cause: ${it.message}")
        }

    override fun getSession(userId: String): Maybe<Session> =
        database.sessionDao().getUserSession(userId)
                .doOnSuccess {
                    info(TAG_LOG, "Session $it retrieved successfully.")
                }.doOnError {
                    debug(TAG_LOG, "Failed to retrieve session data of user $userId, cause: ${it.message}")
                }.map { SessionData.toEntity(it) }

    override fun getSessionLandmarks(userId: String): Maybe<Map<Landmark, Discovery?>> =
        database.beaconDao().getSessionBeacons(userId)
                .doOnSuccess {
                    info(TAG_LOG, "Landmarks of user $userId retrieved successfully.")
                }.doOnError {
                    debug(TAG_LOG, "Failed to retrieve landmarks from user $userId's session")
                }.map { beacons ->
                    val entityMap = HashMap<Landmark, Discovery?>()
                    beacons.forEach { data ->
                        entityMap[data.extractEntity()] = data.extractDiscovery()
                    }
                    entityMap
                }

    override fun updateLandmark(landmark: Landmark, userId: String, photoId: String?, foundAt: Date?): Completable =
        database.beaconDao().updateBeacon(BeaconData.fromEntity(landmark, userId, photoId, foundAt))

    override fun wipeSession(userId: String): Completable =
        Completable.fromCallable {
            database.runInTransaction {
                database.sessionDao().clearUserSession(userId)
                database.beaconDao().clearSessionBeacons(userId)
            }
        }.doOnComplete {
            info(TAG_LOG, "Wiped session data of user $userId")
        }.doOnError {
            debug(TAG_LOG, "Failed to wipe session data of user $userId, cause: ${it.message}")
        }

    private fun wipeDatabase(): Completable =
        Completable.fromCallable {
            database.runInTransaction {
                database.sessionDao().clearSessions()
                database.beaconDao().clearBeacons()
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

        fun getInstance(database: SessionDatabase) =
            sInstance ?: synchronized(this) {
                sInstance
                    ?: SessionCache(database)
            }
    }
}