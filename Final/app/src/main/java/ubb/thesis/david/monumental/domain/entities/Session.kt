package ubb.thesis.david.monumental.domain.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey
    val userId: String,
    val city: String,
    val timeStarted: Date,
    var timeFinished: Date? = null)