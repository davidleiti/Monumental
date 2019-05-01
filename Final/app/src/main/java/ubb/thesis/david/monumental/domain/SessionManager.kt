package ubb.thesis.david.monumental.domain

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ubb.thesis.david.monumental.domain.entities.Landmark
import ubb.thesis.david.monumental.domain.entities.Session

interface SessionManager {

    fun getSession(userId: String): Maybe<Session>
    fun setupSession(userId: String, landmarks: List<Landmark>): Completable
    fun wipeSession(userId: String): Completable

    fun getSessionLandmarks(userId: String): Maybe<List<Landmark>>
    fun updateLandmark(userId: String, landmark: Landmark): Completable

}