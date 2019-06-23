package ubb.thesis.david.monumental.common

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseAndroidViewModel(application: Application) : AndroidViewModel(application) {

    private val compositeDisposable = CompositeDisposable()
    protected val resources: Resources = getApplication<Application>().resources

    protected fun addDisposable(d: Disposable) = compositeDisposable.add(d)

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

}