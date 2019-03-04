package ubb.license.david.monumentalv0.persistence.cache

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable
import ubb.license.david.monumentalv0.persistence.model.Landmark

@Dao
interface LandmarkDao {

    @Query("SELECT * FROM landmarks WHERE sessionId = :sessionId")
    fun getSessionLandmarks(sessionId: Int): LiveData<List<Landmark>>

    @Insert
    fun addLandmarks(landmarks: Array<Landmark>)

    @Insert
    fun addLandmarksAsync(landmarks: Array<Landmark>): Completable

    @Update
    fun updateLandmark(landmark: Landmark): Completable
}