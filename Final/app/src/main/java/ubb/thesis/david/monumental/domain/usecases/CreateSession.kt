package ubb.thesis.david.monumental.domain.usecases

import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import ubb.thesis.david.monumental.domain.SessionManager
import ubb.thesis.david.monumental.domain.entities.Landmark

class CreateSession(private val params: RequestValues,
                    private val sessionManager: SessionManager,
                    transformer: CompletableTransformer) : CompletableUseCase(transformer) {

    data class RequestValues(val userId: String, val landmarks: List<Landmark>)

    override fun createSource(): Completable =
        sessionManager.setupSession(params.userId, params.landmarks)
}