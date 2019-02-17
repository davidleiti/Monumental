package model

data class Photo(
    val id: String,
    val prefix: String,
    val suffix: String,
    val width: String,
    val height: String
) {
    fun generateUrl(): String {
        return "$prefix${width}x$height$suffix"
    }
}