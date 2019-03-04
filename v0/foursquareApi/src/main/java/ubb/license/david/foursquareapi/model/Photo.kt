package ubb.license.david.foursquareapi.model


data class Photo(
    val id: String,
    val width: String,
    val height: String,
    private val prefix: String,
    private val suffix: String
) {
    fun url(): String {
        return "$prefix${width}x$height$suffix"
    }
}