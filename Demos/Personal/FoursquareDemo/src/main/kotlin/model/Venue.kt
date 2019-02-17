package model

data class Venue(
    val id: String,
    val name: String,
    val location: Location,
    val contact: Contact?,
    val url: String?,
    val tips: Tips?
)
