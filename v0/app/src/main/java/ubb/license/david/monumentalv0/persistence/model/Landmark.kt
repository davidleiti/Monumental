package ubb.license.david.monumentalv0.persistence.model


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import ubb.license.david.foursquareapi.model.Venue
import java.util.*

@Entity(
    tableName = "landmarks",
    primaryKeys = ["id", "sessionId"],
    foreignKeys = [ForeignKey(
        entity = Session::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["sessionId"])])
data class Landmark(val id: String,
                    val lat: Double,
                    val lng: Double,
                    var sessionId: Long = -1L,
                    var foundAt: Date? = null,
                    var photoPath: String? = null) {
    companion object {
        fun fromVenue(venue: Venue): Landmark {
            return Landmark(venue.id, venue.location.lat.toDouble(), venue.location.lng.toDouble())
        }
    }
}