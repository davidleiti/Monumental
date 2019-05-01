package ubb.thesis.david.monumental.domain.common

import io.reactivex.Observable

abstract class Mapper<E, T> {

    abstract fun mapFrom(obj: E): T

    abstract fun mapTo(obj: T): E

    fun mapObservable(from: E): Observable<T> {
        return Observable.fromCallable { mapFrom(from) }
    }

    fun mapObservable(from: List<E>): Observable<List<T>> {
        return Observable.fromCallable { from.map { mapFrom(it) } }
    }

}