package ubb.thesis.david.domain.usecases

import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import ubb.thesis.david.domain.SessionManager
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.base.CompletableUseCase
import java.util.*

class UpdateLandmarkData(private val params: Params,
                         private val sessionManager: SessionManager,
                         transformer: CompletableTransformer) : CompletableUseCase(transformer) {

    data class Params(val landmark: Landmark,
                      val userId: String,
                      val photoPath: String? = null,
                      val foundAt: Date? = null)

    override fun createSource(): Completable =
        sessionManager.updateLandmark(
                landmark = params.landmark,
                userId = params.userId,
                photoPath = params.photoPath,
                foundAt = params.foundAt
        )
}