package ubb.license.david.foursquareapi.model

data class Location(
    val address: String,
    val city: String,
    val country: String,
    val crossStreet: String,
    val lat: String,
    val lng: String,
    val distance: Int?
)