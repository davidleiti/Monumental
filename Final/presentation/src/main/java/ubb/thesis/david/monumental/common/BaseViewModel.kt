package ubb.thesis.david.monumental.common

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseViewModel: ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    protected fun addDisposable(d: Disposable) = compositeDisposable.add(d)

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}