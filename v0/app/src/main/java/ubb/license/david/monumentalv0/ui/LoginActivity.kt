package ubb.license.david.monumentalv0.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.utils.*

class LoginActivity : ProgressOverlayActivity(), View.OnClickListener {

    private val logTag = "MonumentalAuth"
    private val googleAuthRc = 1234

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mCallbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupUi()
        setupGoogleAuth()
        setupFacebookAuth()

        if (intent.hasExtra(EXTRA_SIGNED_OUT))
            signOut()
    }

    private fun setupUi() {
        button_sign_in.setOnClickListener(this)
        button_sign_in_google.setOnClickListener(this)
        button_facebook_custom.setOnClickListener(this)
        content_root.setOnClickListener(this)

        field_email.apply {
            addTextChangedListener {
                if (field_email.error != null) validateEmail()
            }
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) validateEmail()
            }
        }

        field_password.apply {
            addTextChangedListener {
                if (field_password.error != null) validatePassword()
            }
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) validatePassword()
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    emailSignIn()
                    true
                } else false
            }
        }
    }

    // TODO try to find better solution to eliminate back navigation to SplashScreenActivity
    override fun onBackPressed() = finish()

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.content_root -> {
                hideSoftKeyboard()
                clearFocus()
            }
            R.id.button_sign_in_google -> googleSignIn()
            R.id.button_sign_in -> emailSignIn()
            R.id.button_facebook_custom -> button_sign_in_facebook.performClick()
        }
    }

    private fun setupGoogleAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupFacebookAuth() {
        mCallbackManager = CallbackManager.Factory.create()

        button_sign_in_facebook.setReadPermissions("email", "public_profile")
        button_sign_in_facebook.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                info(logTag, "Facebook access token granted: ${result.accessToken}")

                val credentials = FacebookAuthProvider.getCredential(result.accessToken.token)
                firebaseAuth(credentials, "Facebook")
            }

            override fun onCancel() {
                warn(logTag, "Facebook sign-in has been cancelled.")
                shortToast(getString(R.string.message_sign_in_cancelled))
            }

            override fun onError(error: FacebookException?) {
                debug(logTag, "Failed to receive Facebook access token, cause: $error")
                this@LoginActivity.window.decorView.longSnack(getString(R.string.warning_sign_in_provider))
            }
        })
    }

    private fun googleSignIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, googleAuthRc)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == googleAuthRc) {    // Returned from Google authentication activity
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credentials = GoogleAuthProvider.getCredential(account?.idToken, null)
                info(logTag, "Google authentication has been successful, retrieved account: $account")

                firebaseAuth(credentials, "Google")
            } catch (e: ApiException) {
                debug(logTag, "Google authentication has failed, cause: ${e.message}")
                window.decorView.longSnack(getString(R.string.warning_sign_in_provider))
            }
        } else {    // Returned from Facebook authentication, propagate result up to the CallbackManager's listener
            mCallbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun emailSignIn() {
        if (validateFields()) {
            hideSoftKeyboard()
            showLoading()
            mAuth.signInWithEmailAndPassword(field_email.text.toString(), field_password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        info(logTag, "Firebase authentication with email/password has been successful.")
                        finishSignIn()
                    } else {
                        debug(logTag,
                            "Firebase authentication with email/password has failed, issue: ${task.exception?.message}")
                        window.decorView.longSnack(getString(R.string.warning_sign_in_email))
                        hideLoading()
                    }
                }
        }
    }

    private fun firebaseAuth(credentials: AuthCredential, provider: String) {
        showLoading()
        mAuth.signInWithCredential(credentials).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                info(logTag, "Firebase authentication via provider $provider was successful...closing activity.")
                finishSignIn()
            } else {
                debug(logTag,
                    "Firebase authentication with via provider $provider has failed, issue: ${task.exception?.message}")
                window.decorView.longSnack(getString(R.string.warning_sign_in_email))
                hideLoading()
            }
        }
    }

    private fun validateFields(): Boolean {
        var valid = true
        if (!validateEmail()) valid = false
        if (!validatePassword()) valid = false
        return valid
    }

    private fun validateEmail(): Boolean {
        val email = field_email.text
        val pattern = "^[\\w!#\$%&]+(.[\\w!#\$%&]+)*@\\w+\\.\\w+$"

        if (email != null && pattern.toRegex() matches email) {
            field_email.error = null
            return true
        }
        field_email.error = getString(R.string.error_email)
        return false
    }

    private fun validatePassword(): Boolean {
        val password = field_password.text

        if (password != null && password.length >= 6) {
            field_password.error = null
            return true
        }
        field_password.error = getString(R.string.error_password)
        return false
    }

    private fun finishSignIn() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun signOut() {
        mAuth.signOut()
        mGoogleSignInClient.signOut()
        LoginManager.getInstance().logOut()
    }

    companion object {
        const val EXTRA_SIGNED_OUT = "SignOutOnLaunch"
    }
}
