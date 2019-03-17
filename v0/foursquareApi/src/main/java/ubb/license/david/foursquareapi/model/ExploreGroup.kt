package ubb.license.david.foursquareapi.model

internal data class ExploreGroup(
    val type: String,
    val items: List<ExploreGroupItem>) {
    fun getVenues(): List<Venue> = items.map { item -> item.venue }
}