package ubb.license.david.monumentalv0.persistence.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import org.jetbrains.anko.doAsync
import ubb.license.david.monumentalv0.persistence.model.Landmark
import ubb.license.david.monumentalv0.persistence.model.Session

@Database(entities = [Landmark::class, Session::class], version = 3, exportSchema = false)
@TypeConverters(RoomConverter::class)
abstract class SessionDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun landmarkDao(): LandmarkDao

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
                SessionDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
//                .addCallback(wipeCallback(context))
                .build()

        private fun wipeCallback(context: Context) = object: RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                doAsync {
                    getInstance(context).sessionDao().clearSessions()
                    getInstance(context).landmarkDao().clearLandmarks()
                }
            }
        }
    }
}