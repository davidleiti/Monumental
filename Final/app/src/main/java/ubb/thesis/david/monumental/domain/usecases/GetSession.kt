package ubb.thesis.david.monumental.domain.usecases

import io.reactivex.Observable
import ubb.thesis.david.monumental.domain.SessionManager
import ubb.thesis.david.monumental.domain.common.Transformer
import ubb.thesis.david.monumental.domain.entities.Session
import ubb.thesis.david.monumental.domain.usecases.base.ObservableUseCase

class GetSession(private val userId: String,
                 private val sessionManager: SessionManager,
                 transformer: Transformer<Session>): ObservableUseCase<Session>(transformer) {

    override fun createSource(): Observable<Session> =
            sessionManager.getSession(userId).toObservable()

}