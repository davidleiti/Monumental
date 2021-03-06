package ubb.thesis.david.monumental.view.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.AuthCredential
import ubb.thesis.david.domain.UserAuthenticator
import ubb.thesis.david.domain.entities.Credential
import ubb.thesis.david.domain.usecases.cloud.authentication.EmailLogin
import ubb.thesis.david.domain.usecases.cloud.authentication.ThirdPartyLogin
import ubb.thesis.david.monumental.MainApplication
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseAndroidViewModel

class LoginViewModel(private val userAuthenticator: UserAuthenticator,
                     appContext: MainApplication) : BaseAndroidViewModel(appContext) {

    // Observable sources
    private val _emailError = MutableLiveData<String?>()
    private val _passwordError = MutableLiveData<String?>()
    private val _authenticationFinished = MutableLiveData<Unit>()
    private val _errors = MutableLiveData<Throwable>()

    // Data binding properties
    val emailError: LiveData<String?> = _emailError
    val passwordError: LiveData<String?> = _passwordError

    // Exposed observable properties
    val authenticationFinished: LiveData<Unit> = _authenticationFinished
    val errors: LiveData<Throwable> = _errors

    fun emailAuth(email: String, password: String) {
        EmailLogin(email, password, userAuthenticator, AsyncTransformerFactory.create())
                .execute()
                .subscribe({ _authenticationFinished.value = Unit },
                           { error -> _errors.value = error })
                .also { addDisposable(it) }
    }

    fun thirdPartyAuth(authCredentials: AuthCredential) {
        val credentials = object : Credential {
            override fun getCredentials() = authCredentials
        }

        ThirdPartyLogin(credentials, userAuthenticator, AsyncTransformerFactory.create())
                .execute()
                .subscribe({ _authenticationFinished.value = Unit },
                           { error -> _errors.value = error })
                .also { addDisposable(it) }
    }

    fun validateEmail(email: String?): Boolean {
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