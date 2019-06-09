package ubb.thesis.david.monumental.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseAndroidViewModel(application: Application) : AndroidViewModel(application) {

    private val compositeDisposable = CompositeDisposable()

    protected fun addDisposable(d: Disposable) = compositeDisposable.add(d)

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

}