package ubb.license.david.foursquareapi.model

data class Venue(
    val id: String,
    val name: String,
    val location: Location,
    val categories: List<Category>? = null,
    val contact: Contact? = null,
    val url: String? = null,
    val tips: Tips? = null
)