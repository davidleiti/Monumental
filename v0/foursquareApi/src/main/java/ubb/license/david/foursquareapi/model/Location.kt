package ubb.license.david.foursquareapi.model

data class Location(
    val address: String,
    val crossStreet: String,
    val lat: String,
    val lng: String,
    val distance: Int?,
    val formattedAddress: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Location

        if (address != other.address) return false
        if (lat != other.lat) return false
        if (lng != other.lng) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + lat.hashCode()
        result = 31 * result + lng.hashCode()
        return result
    }
}