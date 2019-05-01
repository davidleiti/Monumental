package ubb.thesis.david.monumental.domain.entities


import ubb.license.david.foursquareapi.model.Venue
import java.util.*

data class Landmark(val id: String,
                    val lat: Double,
                    val lng: Double,
                    var label: String? = "null",
                    var photoPath: String? = null) {
    companion object {
        fun fromVenue(venue: Venue): Landmark {
            return Landmark(venue.id, venue.location.lat.toDouble(), venue.location.lng.toDouble(), venue.name)
        }
    }
}