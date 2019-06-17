package ubb.thesis.david.monumental.view.authentication

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.domain.UserAuthenticator
import ubb.thesis.david.domain.usecases.cloud.EmailRegister
import ubb.thesis.david.monumental.MainApplication
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseAndroidViewModel

class RegisterViewModel(private val userAuthenticator: UserAuthenticator,
                        appContext: MainApplication) : BaseAndroidViewModel(appContext) {

    // Resource
    private val resources = getApplication<Application>().resources

    // Observable sources
    private val _emailError = MutableLiveData<String?>()
    private val _passwordError = MutableLiveData<String?>()
    private val _registrationFinished = MutableLiveData<Unit>()
    private val _errorsOccurred = MutableLiveData<Throwable>()

    // Exposed observable properties
    val emailError: LiveData<String?> = _emailError
    val passwordError: LiveData<String?> = _passwordError
    val registrationFinished: LiveData<Unit> = _registrationFinished
    val errorsOccurred: LiveData<Throwable> = _errorsOccurred

    fun signUp(email: String, password: String) {
        val params = EmailRegister.Params(email, password)

        EmailRegister(params, userAuthenticator, AsyncTransformerFactory.create())
                .execute()
                .subscribe({ _registrationFinished.value = Unit },
                           { error -> _errorsOccurred.value = error })
                .also { addDisposable(it) }
    }

    fun validateEmail(email: String?) {
        val pattern = "^[\\w!#\$%&]+(.[\\w!#\$%&]+)*@\\w+\\.\\w+$"

        if (email != null && pattern.toRegex() matches email) {
            _emailError.value = null
        } else {
            _emailError.value = resources.getString(R.string.error_email)
        }
    }

    fun validatePassword(password: String?) {
        if (password != null && password.length >= 6) {
            _passwordError.value = null
        } else {
            _passwordError.value = resources.getString(R.string.error_password)
        }
    }

}