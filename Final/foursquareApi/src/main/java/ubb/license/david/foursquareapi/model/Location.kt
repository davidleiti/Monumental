package ubb.license.david.foursquareapi.model

data class Location(
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val crossStreet: String? = null,
    val lat: String,
    val lng: String,
    val distance: Int? = null
)