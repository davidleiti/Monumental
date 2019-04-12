package ubb.thesis.david.monumental.domain.usecases.base

import io.reactivex.Observable
import ubb.thesis.david.monumental.domain.common.Transformer

abstract class ObservableUseCase<T>(private val transformer: Transformer<T>) {

    abstract fun createSource(): Observable<T>

    fun execute(): Observable<T> =
        createSource().compose(transformer)
}