package ubb.thesis.david.monumental.view.authentication

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ubb.thesis.david.domain.UserAuthenticator
import ubb.thesis.david.domain.usecases.cloud.authentication.EmailRegister
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
    private val _errors = MutableLiveData<Throwable>()

    // Data binding properties
    val emailError: LiveData<String?> = _emailError
    val passwordError: LiveData<String?> = _passwordError

    // Exposed observable properties
    val registrationFinished: LiveData<Unit> = _registrationFinished
    val errors: LiveData<Throwable> = _errors

    fun signUp(email: String, password: String) {
        val params = EmailRegister.Params(email, password)

        EmailRegister(params, userAuthenticator, AsyncTransformerFactory.create())
                .execute()
                .subscribe({ _registrationFinished.value = Unit },
                           { error -> _errors.value = error })
                .also { addDisposable(it) }
    }

    fun validateEmail(email: String?) : Boolean {
        val pattern = "^[\\w!#\$%&]+(.[\\w!#\$%&]+)*@\\w+\\.\\w+$"

        return if (email != null && pattern.toRegex() matches email) {
            _emailError.value = null
            true
        } else {
            _emailError.value = resources.getString(R.string.error_email)
            false
        }
    }

    fun validatePassword(password: String?): Boolean {
        if (password != null && password.length >= 6) {
            val specials = "^.*[._?!%$&*#~<>].*$".toRegex()
            val numbers = "^.*[0123456789].*$".toRegex()
            if (specials matches password && numbers matches password) {
                _passwordError.value = null
                return true
            } else {
                _passwordError.value = resources.getString(R.string.password_no_spec_char)
            }
        } else {
            _passwordError.value = resources.getString(R.string.error_password)
        }

        return false
    }
}