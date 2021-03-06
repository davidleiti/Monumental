package ubb.thesis.david.monumental.common

import io.reactivex.CompletableTransformer
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ubb.thesis.david.domain.common.Transformer

sealed class AsyncTransformerFactory {
    companion object {
        fun create(): CompletableTransformer =
            CompletableTransformer { completable ->
                completable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError { error -> error.printStackTrace() }
            }

        inline fun <reified T : Any> create(): Transformer<T> =
            object : Transformer<T>() {
                override fun apply(upstream: Observable<T>): ObservableSource<T> =
                    upstream.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError { error -> error.printStackTrace() }
            }
    }
}