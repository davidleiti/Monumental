package ubb.thesis.david.data.entities

import androidx.room.Entity
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

        fun toEntity(beaconData: BeaconData): Landmark =
            Landmark(id = beaconData.id,
                     lat = beaconData.lat,
                     lng = beaconData.lng,
                     label = beaconData.label
            )
    }
}