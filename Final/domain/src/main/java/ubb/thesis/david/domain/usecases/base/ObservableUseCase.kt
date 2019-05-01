package ubb.thesis.david.domain.usecases.base

import io.reactivex.Observable
import ubb.thesis.david.domain.common.Transformer

abstract class ObservableUseCase<T>(private val transformer: Transformer<T>) {

    abstract fun createSource(): Observable<T>

    fun execute(): Observable<T> =
        createSource().compose(transformer)
}