package ubb.thesis.david.data.entities

import androidx.room.Entity
import ubb.thesis.david.domain.entities.Landmark
import java.util.*

@Entity(
        tableName = "beacons",
        primaryKeys = ["id", "userId"])
data class BeaconData(val id: String,
                      val lat: Double,
                      val lng: Double,
                      var label: String? = "null",
                      var userId: String = "",
                      var foundAt: Date? = null) {
    companion object {
        fun fromEntity(entity: Landmark, userId: String): BeaconData =
            BeaconData(id = entity.id,
                       lat = entity.lat,
                       lng = entity.lng,
                       label = entity.label,
                       userId = userId,
                       foundAt = null
            )

        fun toEntity(beaconData: BeaconData): Landmark =
            Landmark(id = beaconData.id,
                     lat = beaconData.lat,
                     lng = beaconData.lng,
                     label = beaconData.label,
                     photoPath = null
            )
    }
}