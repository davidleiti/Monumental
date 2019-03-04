package ubb.license.david.monumentalv0.persistence.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ubb.license.david.monumentalv0.persistence.model.Landmark
import ubb.license.david.monumentalv0.persistence.model.Session

@Database(entities = [Landmark::class, Session::class], version = 1, exportSchema = false)
@TypeConverters(RoomConverter::class)
abstract class SessionDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun landmarkDao(): LandmarkDao

    companion object {

        const val DATABASE_NAME = "db-session-cache"

        @Volatile
        private var sInstance: SessionDatabase? = null

        fun getInstance(context: Context): SessionDatabase =
            sInstance ?: synchronized(this) {
                sInstance ?: buildDatabase(context).also { sInstance = it }
            }

        private fun buildDatabase(context: Context): SessionDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                SessionDatabase::class.java, DATABASE_NAME
            ).build()
    }
}