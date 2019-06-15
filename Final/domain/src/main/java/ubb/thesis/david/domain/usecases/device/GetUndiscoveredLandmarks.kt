package ubb.thesis.david.domain.usecases.device

import io.reactivex.Observable
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.common.Transformer
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.base.ObservableUseCase

class GetUndiscoveredLandmarks(private val userId: String,
                               private val sessionManager: SessionManager,
                               transformer: Transformer<List<Landmark>>) :
    ObservableUseCase<List<Landmark>>(transformer) {

    override fun createSource(): Observable<List<Landmark>> =
        sessionManager.getSessionLandmarks(userId)
                .map { entityMap ->
                    entityMap.filterValues { it == null }.keys.toList()
                }.toObservable()
}