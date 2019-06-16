package ubb.thesis.david.domain.usecases.base

import io.reactivex.Completable
import io.reactivex.CompletableTransformer

abstract class CompletableUseCase(protected val transformer: CompletableTransformer) {

    abstract fun createSource(): Completable

    fun execute(): Completable {
        return createSource().compose(transformer)
    }

}