package ubb.license.david.monumentalv0.persistence.model


import androidx.room.Entity
import ubb.license.david.foursquareapi.model.Venue
import java.util.*

@Entity(
    tableName = "landmarks",
    primaryKeys = ["id", "userId"])
data class Landmark(val id: String,
                    val lat: Double,
                    val lng: Double,
                    var label: String? = "null",
                    var userId: String = "",
                    var foundAt: Date? = null,
                    var photoPath: String? = null) {
    companion object {
        fun fromVenue(venue: Venue): Landmark {
            return Landmark(venue.id, venue.location.lat.toDouble(), venue.location.lng.toDouble(), venue.name)
        }
    }
}