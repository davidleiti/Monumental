package ubb.thesis.david.domain

import io.reactivex.Completable
import io.reactivex.Maybe
import ubb.thesis.david.domain.entities.Backup
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.entities.Session
import java.util.*

interface SessionManager {

    fun createSession(session: Session, landmarks: List<Landmark>): Completable
    fun saveSessionBackup(backup: Backup): Completable

    fun getSession(userId: String): Maybe<Session>
    fun getSessionLandmarks(userId: String): Maybe<Map<Landmark, Discovery?>>

    fun updateLandmark(landmark: Landmark, userId: String, photoPath: String?, foundAt: Date?): Completable
    fun wipeSession(userId: String): Completable

}