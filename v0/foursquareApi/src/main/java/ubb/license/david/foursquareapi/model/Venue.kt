package ubb.license.david.foursquareapi.model

data class Venue(
    val id: String,
    val name: String,
    val location: Location,
    val categories: List<Category>?,
    val contact: Contact?,
    val url: String?,
    val tips: Tips?
)