package ubb.thesis.david.monumental.presentation.common

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ubb.thesis.david.monumental.domain.common.Transformer

class AsyncTransformer<T> : Transformer<T>() {
    override fun apply(upstream: Observable<T>): ObservableSource<T> =
        upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
}