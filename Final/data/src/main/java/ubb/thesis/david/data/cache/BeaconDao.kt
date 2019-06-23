package ubb.thesis.david.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Maybe
import ubb.thesis.david.data.entities.BeaconData

@Dao
interface BeaconDao {

    @Query("SELECT * FROM beacons WHERE userId = :userId")
    fun getSessionBeacons(userId: String): Maybe<List<BeaconData>>

    @Insert
    fun addBeacons(beacons: List<BeaconData>)

    @Insert
    fun addBeaconsAsync(beacons: List<BeaconData>): Completable

    @Update
    fun updateBeacon(landmark: BeaconData): Completable

    @Query("DELETE FROM beacons WHERE userId = :userId")
    fun clearSessionBeacons(userId: String)
}