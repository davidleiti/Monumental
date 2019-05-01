package ubb.thesis.david.domain.entities


import ubb.license.david.foursquareapi.model.Venue

data class Landmark(val id: String,
                    val lat: Double,
                    val lng: Double,
                    var label: String? = "null",
                    var photoPath: String? = null) {
    companion object {
        fun fromVenue(venue: Venue): Landmark {
            return Landmark(venue.id, venue.location.lat.toDouble(),
                                                             venue.location.lng.toDouble(), venue.name)
        }
    }
}