package ubb.thesis.david.domain.usecases

import io.reactivex.Observable
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.common.Transformer
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.base.ObservableUseCase

class GetSessionLandmarks(private val userId: String,
                          private val sessionManager: SessionManager,
                          transformer: Transformer<List<Landmark>>): ObservableUseCase<List<Landmark>>(transformer) {

    override fun createSource(): Observable<List<Landmark>> =
            sessionManager.getSessionLandmarks(userId).toObservable()

}