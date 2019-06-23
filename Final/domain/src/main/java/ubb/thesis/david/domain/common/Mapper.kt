package ubb.thesis.david.domain.common

abstract class Mapper<E, T> {

    abstract fun mapFrom(obj: E): T

    abstract fun mapTo(obj: T): E

}