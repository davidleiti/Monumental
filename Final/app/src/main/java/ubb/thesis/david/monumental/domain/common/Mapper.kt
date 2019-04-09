package ubb.thesis.david.monumental.domain.common

import io.reactivex.Observable

abstract class Mapper<in E, T> {

    abstract fun mapFrom(from: E): T

    fun mapObservable(from: E): Observable<T> {
        return Observable.fromCallable { mapFrom(from) }
    }

    fun mapObservable(from: List<E>): Observable<List<T>> {
        return Observable.fromCallable { from.map { mapFrom(it) } }
    }

}