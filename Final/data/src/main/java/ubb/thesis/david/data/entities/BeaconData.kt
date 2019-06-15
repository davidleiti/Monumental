package ubb.thesis.david.data.entities

import androidx.room.Entity
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import java.util.*

@Entity(tableName = "beacons",
        primaryKeys = ["id", "userId"])
data class BeaconData(val id: String,
                      val lat: Double,
                      val lng: Double,
                      var label: String?,
                      var userId: String,
                      var photoPath: String?,
                      var foundAt: Date?) {

    fun extractEntity(): Landmark =
        Landmark(id = id,
                 lat = lat,
                 lng = lng,
                 label = label)

    fun extractDiscovery(): Discovery? {
        if (photoPath != null && foundAt != null)
            return Discovery(foundAt!!, photoPath!!)
        return null
    }

    companion object {
        fun fromEntity(entity: Landmark, userId: String, photoPath: String? = null, foundAt: Date? = null): BeaconData =
            BeaconData(id = entity.id,
                       lat = entity.lat,
                       lng = entity.lng,
                       label = entity.label,
                       userId = userId,
                       photoPath = photoPath,
                       foundAt = foundAt
            )
    }
}