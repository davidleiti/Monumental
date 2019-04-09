package ubb.thesis.david.monumental.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Maybe
import ubb.thesis.david.monumental.domain.entities.Landmark

@Dao
interface LandmarkDao {

    @Query("SELECT * FROM landmarks WHERE userId = :userId")
    fun getSessionLandmarks(userId: String): Maybe<List<Landmark>>

    @Insert
    fun addLandmarks(landmarks: List<Landmark>)

    @Insert
    fun addLandmarksAsync(landmarks: List<Landmark>): Completable

    @Update
    fun updateLandmark(landmark: Landmark): Completable

    @Query("DELETE FROM landmarks")
    fun clearLandmarks()

    @Query("DELETE FROM landmarks WHERE userId = :userId")
    fun clearUserLandmarks(userId: String)
}