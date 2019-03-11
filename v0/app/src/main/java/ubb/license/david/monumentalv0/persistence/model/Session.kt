package ubb.license.david.monumentalv0.persistence.model

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