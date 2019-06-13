package ubb.thesis.david.domain.entities

data class Prediction(
    val name: String,
    val type: Type,
    val confidence: Float,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    enum class Type {
        LABEL, LANDMARK
    }
}