package ubb.thesis.david.domain.usecases.local

import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.base.CompletableUseCase
import java.util.*

class UpdateCachedLandmark(private val params: Params,
                           private val sessionManager: SessionManager,
                           transformer: CompletableTransformer) : CompletableUseCase(transformer) {

    data class Params(val landmark: Landmark,
                      val userId: String,
                      val photoId: String? = null,
                      val foundAt: Date? = null)

    override fun createSource(): Completable =
        sessionManager.updateLandmark(
                landmark = params.landmark,
                userId = params.userId,
                photoId = params.photoId,
                foundAt = params.foundAt
        )
}