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
                      var photoId: String?,
                      var foundAt: Date?) {

    fun extractEntity(): Landmark =
        Landmark(id = id,
                 lat = lat,
                 lng = lng,
                 label = label)

    fun extractDiscovery(): Discovery? {
        if (photoId != null && foundAt != null)
            return Discovery(foundAt!!, photoId!!)
        return null
    }

    companion object {
        fun fromEntity(entity: Landmark, userId: String, photoId: String? = null, foundAt: Date? = null): BeaconData =
            BeaconData(id = entity.id,
                       lat = entity.lat,
                       lng = entity.lng,
                       label = entity.label,
                       userId = userId,
                       photoId = photoId,
                       foundAt = foundAt
            )

        fun fromMapEntry(userId: String, data: Map.Entry<Landmark, Discovery?>): BeaconData =
            fromEntity(data.key, userId, data.value?.photoId, data.value?.time)
    }
}