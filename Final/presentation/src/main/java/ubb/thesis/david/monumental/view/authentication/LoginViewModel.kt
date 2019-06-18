package ubb.thesis.david.monumental.view.authentication

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.AuthCredential
import ubb.thesis.david.domain.UserAuthenticator
import ubb.thesis.david.domain.entities.Credentials
import ubb.thesis.david.domain.usecases.cloud.EmailLogin
import ubb.thesis.david.domain.usecases.cloud.ThirdPartyLogin
import ubb.thesis.david.monumental.MainApplication
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.AsyncTransformerFactory
import ubb.thesis.david.monumental.common.BaseAndroidViewModel

class LoginViewModel(private val userAuthenticator: UserAuthenticator,
                     appContext: MainApplication) : BaseAndroidViewModel(appContext) {

    // Resource
    private val resources = getApplication<Application>().resources

    // Observable sources
    private val _emailError = MutableLiveData<String?>()
    private val _passwordError = MutableLiveData<String?>()
    private val _authenticationFinished = MutableLiveData<Unit>()
    private val _errors = MutableLiveData<Throwable>()

    // Exposed observable properties
    val emailError: LiveData<String?> = _emailError
    val passwordError: LiveData<String?> = _passwordError
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
        val credentials = object : Credentials {
            override fun getCredentials() = authCredentials
        }

        ThirdPartyLogin(credentials, userAuthenticator, AsyncTransformerFactory.create())
                .execute()
                .subscribe({ _authenticationFinished.value = Unit },
                           { error -> _errors.value = error })
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