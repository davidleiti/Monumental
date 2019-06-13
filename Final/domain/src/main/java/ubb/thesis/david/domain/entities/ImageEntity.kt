package ubb.thesis.david.domain.entities

data class ImageEntity(val id: String,
                       val landmarkId: String? = null,
                       val width: Int,
                       val height: Int,
                       val url: String)