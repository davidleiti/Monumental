package ubb.thesis.david.monumental.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Maybe
import ubb.thesis.david.monumental.domain.entities.Session

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE userId = :userId")
    fun getUserSession(userId: String): Maybe<Session>

    @Insert
    fun createSession(session: Session)

    @Insert
    fun createSessionAsync(session: Session): Completable

    @Update
    fun updateSession(session: Session): Completable

    @Query("DELETE FROM sessions")
    fun clearSessions()

    @Query("DELETE FROM sessions WHERE userId = :userId")
    fun clearUserSession(userId: String)
}