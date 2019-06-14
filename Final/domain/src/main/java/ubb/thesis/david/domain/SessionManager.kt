package ubb.thesis.david.domain

import io.reactivex.Completable
import io.reactivex.Maybe
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.entities.Session
import java.util.*

interface SessionManager {

    fun getSession(userId: String): Maybe<Session>
    fun setupSession(userId: String, landmarks: List<Landmark>): Completable
    fun wipeSession(userId: String): Completable

    fun getSessionLandmarks(userId: String): Maybe<List<Landmark>>
    fun updateLandmark(landmark: Landmark, userId: String, photoPath: String?, foundAt: Date?): Completable

}