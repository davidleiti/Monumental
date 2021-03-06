package ubb.thesis.david.data.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ubb.thesis.david.data.entities.BeaconData
import ubb.thesis.david.data.entities.SessionData

@Database(entities = [BeaconData::class, SessionData::class], version = 8, exportSchema = false)
@TypeConverters(DataConverter::class)
abstract class SessionDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun beaconDao(): BeaconDao

    companion object {
        private const val DATABASE_NAME = "db-session-cache"

        @Volatile
        private var Instance: SessionDatabase? = null

        fun getInstance(context: Context): SessionDatabase =
            Instance ?: synchronized(this) {
                Instance ?: buildDatabase(context).also { Instance = it }
            }

        private fun buildDatabase(context: Context): SessionDatabase =
            Room.databaseBuilder(
                    context.applicationContext,
                    SessionDatabase::class.java,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()

    }
}