package ubb.thesis.david.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import ubb.thesis.david.domain.entities.Session
import java.util.*

@Entity(tableName = "sessions")
data class SessionData(
    @PrimaryKey
    val userId: String,
    val city: String,
    val timeStarted: Date,
    var timeFinished: Date? = null
) {
    companion object {

        fun fromEntity(entity: Session): SessionData =
            SessionData(userId = entity.userId,
                        city = entity.city,
                        timeStarted = entity.timeStarted,
                        timeFinished = entity.timeFinished)

        fun toEntity(data: SessionData): Session =
            Session(userId = data.userId,
                    city = data.city,
                    timeStarted = data.timeStarted,
                    timeFinished = data.timeFinished)

    }
}