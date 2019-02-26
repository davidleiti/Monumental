package ubb.license.david.monumentalv0.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.ui.home.HomeActivity
import ubb.license.david.monumentalv0.utils.attachProgressOverlay
import ubb.license.david.monumentalv0.utils.fadeIn
import ubb.license.david.monumentalv0.utils.fadeOut
import ubb.license.david.monumentalv0.utils.hideSoftKeyboard

class LoginActivity : Activity(), View.OnClickListener {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mCallbackManager: CallbackManager
    private lateinit var mProgress: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupUi()

        setupGoogleAuth()
        setupFacebookAuth()

        if (intent.hasExtra(EXTRA_SIGNED_OUT)) signOut()
    }

    private fun setupUi() {
        mProgress = attachProgressOverlay(content_root)

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

    override fun onBackPressed() = finish()

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.content_root -> {
                hideSoftKeyboard()
                content_root.clearFocus()
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
                Log.d(TAG_LOGGER, "Facebook access token granted: ${result.accessToken}")

                val credentials = FacebookAuthProvider.getCredential(result.accessToken.token)
                firebaseAuth(credentials, PROVIDER_FACEBOOK)
            }

            override fun onCancel() {
                Log.d(TAG_LOGGER, "Facebook sign-in has been cancelled.")
                Toast.makeText(this@LoginActivity, "Sign in attempt cancelled.", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException?) {
                Log.d(TAG_LOGGER, "Failed to receive Facebook access token, cause: $error")
                Snackbar.make(this@LoginActivity.window.decorView,
                    ERROR_PROVIDER, Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun googleSignIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun emailSignIn() {
        if (validateFields()) {
            showLoading()
            mAuth.signInWithEmailAndPassword(field_email.text.toString(), field_password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(
                            TAG_LOGGER, "Firebase authentication with email/password has succeeded...closing activity.")
                        finishSignIn()
                    } else {
                        Log.d(
                            TAG_LOGGER,
                            "Firebase authentication with email/password has failed, issue: ${task.exception?.message}")
                        Snackbar.make(window.decorView,
                            ERROR_SIGN_IN_EMAIL, Snackbar.LENGTH_LONG).show()
                        hideLoading()
                    }
                }
        }
    }

    private fun finishSignIn() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {    // Returned from Google authentication activity
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG_LOGGER, "Google authentication has succeeded, retrieved account: $account")

                val credentials = GoogleAuthProvider.getCredential(account?.idToken, null)
                firebaseAuth(credentials, PROVIDER_GOOGLE)
            } catch (e: ApiException) {
                Snackbar.make(window.decorView,
                    ERROR_PROVIDER, Snackbar.LENGTH_LONG).show()
                Log.w(TAG_LOGGER, "Google sign in failed", e)
            }
        } else {    // Returned from Facebook authentication
            mCallbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun firebaseAuth(credentials: AuthCredential, provider: String) {
        showLoading()
        mAuth.signInWithCredential(credentials).addOnCompleteListener(this) { task ->
            run {
                if (task.isSuccessful) {
                    Log.d(
                        TAG_LOGGER, "Firebase authentication via provider $provider was successful...closing activity.")
                    finishSignIn()
                } else {
                    Log.d(
                        TAG_LOGGER,
                        "Firebase authentication with via provider $provider has failed, issue: ${task.exception?.message}")
                    Snackbar.make(window.decorView,
                        ERROR_PROVIDER, Snackbar.LENGTH_LONG).show()
                    hideLoading()
                }
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
        field_email.error = "Not a valid address."
        return false
    }

    private fun validatePassword(): Boolean {
        val password = field_password.text

        if (password != null && password.length >= 6) {
            field_password.error = null
            return true
        }
        field_password.error = "Password must be at least 6 characters long."
        return false
    }

    private fun signOut() {
        mAuth.signOut()
        mGoogleSignInClient.signOut()
        LoginManager.getInstance().logOut()
    }

    private fun showLoading() = mProgress.fadeIn()

    private fun hideLoading() = mProgress.fadeOut()

    companion object {
        private const val TAG_LOGGER = "LoginActivity"
        private const val RC_SIGN_IN = 1234

        private const val ERROR_PROVIDER = "Failed to sign in with third party provider"
        private const val ERROR_SIGN_IN_EMAIL =
            "There was a problem signing in, please check your credentials and your internet connection!"

        private const val PROVIDER_GOOGLE = "Google"
        private const val PROVIDER_FACEBOOK = "Facebook"

        const val EXTRA_SIGNED_OUT = "SignOutOnLaunch"
    }
}
