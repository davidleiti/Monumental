package ubb.thesis.david.data.cache

import androidx.room.TypeConverter
import java.util.*

class DataConverter {

    @TypeConverter
    fun dateToTimeStamp(date: Date?) = date?.time

    @TypeConverter
    fun timeStampToDate(timeStamp: Long?) = timeStamp?.let { Date(timeStamp) }

}