package ubb.thesis.david.monumental.domain

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ubb.thesis.david.monumental.domain.entities.Landmark
import ubb.thesis.david.monumental.domain.entities.Session

interface SessionRepository {
    fun getSession(userId: String): Maybe<Session>
    fun updateLandmark(landmark: Landmark): Completable
    fun getSessionLandmarks(userId: String): Maybe<List<Landmark>>
    fun setupSession(userId: String, landmarks: List<Landmark>): Completable
    fun searchLandmarks(location: String, radius: Int, categories: String, limit: Int = 0): Single<List<Landmark>>
    fun wipeSession(userId: String): Completable
}