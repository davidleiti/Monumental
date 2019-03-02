package ubb.license.david.foursquareapi.model


internal data class Photos(private val count: Int, val items: Array<Photo>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Photos

        if (count != other.count) return false
        if (!items.contentEquals(other.items)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = count
        result = 31 * result + items.contentHashCode()
        return result
    }
}

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