package ubb.thesis.david.domain.entities

import java.util.*

data class Session(
    val userId: String,
    var sessionId: String? = null,
    val landmarkCount: Int,
    val timeStarted: Date,
    var timeFinished: Date? = null)