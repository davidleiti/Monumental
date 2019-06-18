package ubb.thesis.david.domain.usecases.cloud

import io.reactivex.Observable
import ubb.thesis.david.domain.CloudDataSource
import ubb.thesis.david.domain.common.Transformer
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.base.ObservableUseCase


class GetSessionDetails(private val userId: String,
                        private val sessionId: String,
                        private val cloudDataSource: CloudDataSource,
                        transformer: Transformer<Map<Landmark, Discovery?>>) :
    ObservableUseCase<Map<Landmark, Discovery?>>(transformer) {

    override fun createSource(): Observable<Map<Landmark, Discovery?>> =
        cloudDataSource.getSessionDetails(userId, sessionId).toObservable()

}