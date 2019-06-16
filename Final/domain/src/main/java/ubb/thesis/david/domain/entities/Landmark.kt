package ubb.thesis.david.domain.entities

import java.io.Serializable

data class Landmark(val id: String,
                    val lat: Double,
                    val lng: Double,
                    var label: String? = null) : Serializable