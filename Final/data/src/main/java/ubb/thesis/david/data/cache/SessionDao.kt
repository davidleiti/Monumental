package ubb.thesis.david.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Maybe
import ubb.thesis.david.data.entities.SessionData

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE userId = :userId")
    fun getUserSession(userId: String): Maybe<SessionData>

    @Insert
    fun createSession(session: SessionData)

    @Insert
    fun createSessionAsync(session: SessionData): Completable

    @Update
    fun updateSession(session: SessionData): Completable

    @Query("DELETE FROM sessions WHERE userId = :userId")
    fun clearUserSession(userId: String)
}