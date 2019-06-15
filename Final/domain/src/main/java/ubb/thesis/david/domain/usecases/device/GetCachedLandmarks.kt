package ubb.thesis.david.domain.usecases.device

import io.reactivex.Observable
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.common.Transformer
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.base.ObservableUseCase

class GetCachedLandmarks(private val userId: String,
                         private val sessionManager: SessionManager,
                         transformer: Transformer<Map<Landmark, Discovery?>>)
    : ObservableUseCase<Map<Landmark, Discovery?>>(transformer) {

    override fun createSource(): Observable<Map<Landmark, Discovery?>> =
        sessionManager.getSessionLandmarks(userId).toObservable()

}