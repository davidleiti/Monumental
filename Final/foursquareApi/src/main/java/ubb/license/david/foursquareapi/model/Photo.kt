package ubb.license.david.foursquareapi.model


data class Photo(
    val id: String,
    val width: String,
    val height: String,
    private val prefix: String? = null,
    private val suffix: String? = null
) {
    fun url(): String {
        return "$prefix${width}x$height$suffix"
    }
}