package ubb.thesis.david.domain.entities

import java.util.*

data class Session(
    val userId: String,
    val city: String,
    val timeStarted: Date,
    var timeFinished: Date? = null)