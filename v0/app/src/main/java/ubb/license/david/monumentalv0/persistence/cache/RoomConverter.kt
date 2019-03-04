package ubb.license.david.monumentalv0.persistence.cache

import androidx.room.TypeConverter
import java.util.*

class RoomConverter {

    @TypeConverter
    fun dateToTimeStamp(date: Date?) = date?.time

    @TypeConverter
    fun timeStampToDate(timeStamp: Long?) = timeStamp?.let { Date(timeStamp) }

}