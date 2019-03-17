package ubb.license.david.monumentalv0.persistence.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Maybe
import ubb.license.david.monumentalv0.persistence.model.Landmark

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