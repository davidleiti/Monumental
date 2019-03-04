package ubb.license.david.monumentalv0.persistence.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Single
import ubb.license.david.monumentalv0.persistence.model.Session

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY timeStarted DESC")
    fun getUserSessions(userId: String): Single<List<Session>>

    @Insert
    fun createSession(session: Session): Long

    @Insert
    fun createSessionAsync(session: Session): Completable

    @Update
    fun updateSession(session: Session): Completable

    @Query("DELETE FROM sessions")
    fun deleteSessions()

    @Query("DELETE FROM sessions WHERE userId = :userId AND timeFinished = null")
    fun deleteUnfinishedSessions(userId: String)

}