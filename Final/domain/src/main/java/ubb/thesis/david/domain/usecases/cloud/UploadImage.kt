package ubb.thesis.david.domain.usecases.cloud

import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import ubb.thesis.david.domain.ImageStorage
import ubb.thesis.david.domain.usecases.base.CompletableUseCase

class UploadImage(private val params: Params,
                  private val imageStorage: ImageStorage,
                  transformer: CompletableTransformer) : CompletableUseCase(transformer) {

    data class Params(val userId: String, val photoId: String, val photoPath: String)

    override fun createSource(): Completable =
            imageStorage.storeImage(params.userId, params.photoId, params.photoPath)

}