package ubb.thesis.david.monumental.view

import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.navigation.Navigation
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_login.*
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.BaseFragment
import ubb.thesis.david.monumental.utils.clearFocus
import ubb.thesis.david.monumental.utils.hideSoftKeyboard
import ubb.thesis.david.monumental.utils.longToast
import ubb.thesis.david.monumental.utils.shortToast

class LoginFragment : BaseFragment(), View.OnClickListener {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var signInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sharedElementEnterTransition = ChangeBounds().apply { duration = 300 }
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        firebaseAuth = getAuth()
        firebaseAuth.currentUser?.let {
            finishSignIn()
            return
        }
        signInClient = getGoogleSignInClient()
        setupFacebookAuth()
    }

    override fun usesNavigationDrawer(): Boolean = false

    override fun title(): String? = null

    private fun initUi() {
        button_sign_in.setOnClickListener(this)
        button_sign_in_google.setOnClickListener(this)
        button_facebook_custom.setOnClickListener(this)

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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.content_root -> {
                activity!!.hideSoftKeyboard()
                activity!!.clearFocus()
            }
            R.id.button_sign_in_google -> googleSignIn()
            R.id.button_sign_in -> emailSignIn()
            R.id.button_facebook_custom -> button_sign_in_facebook.performClick()
        }
    }

    private fun setupFacebookAuth() {
        callbackManager = CallbackManager.Factory.create()

        button_sign_in_facebook.setReadPermissions("email", "public_profile")
        button_sign_in_facebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                info(TAG_LOG, "Facebook access token granted: ${result.accessToken}")

                val credentials = FacebookAuthProvider.getCredential(result.accessToken.token)
                firebaseAuth(credentials, "Facebook")
            }

            override fun onCancel() {
                debug(TAG_LOG, "Facebook sign-in has been cancelled.")
                context!!.shortToast(getString(R.string.message_sign_in_cancelled))
            }

            override fun onError(error: FacebookException?) {
                debug(TAG_LOG, "Failed to receive Facebook access token, cause: $error")
                context!!.longToast(getString(R.string.warning_sign_in_provider))
            }
        })
    }

    private fun googleSignIn() {
        val signInIntent: Intent = signInClient.signInIntent
        activity!!.startActivityForResult(signInIntent,
                                          RC_GOOGLE_AUTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_GOOGLE_AUTH) {    // Returned from Google authentication activity
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credentials = GoogleAuthProvider.getCredential(account?.idToken, null)
                info(TAG_LOG,
                     "Google authentication has been successful, retrieved account: $account")

                firebaseAuth(credentials, "Google")
            } catch (e: ApiException) {
                debug(TAG_LOG, "Google authentication has failed, cause: $e")
                context!!.longToast(getString(R.string.warning_sign_in_provider))
            }
        } else {    // Returned from Facebook authentication, propagate result up to the CallbackManager's listener
            super.onActivityResult(requestCode, resultCode, data)
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun emailSignIn() {
        if (validateFields()) {
            activity!!.hideSoftKeyboard()
            displayProgress()
            getAuth().signInWithEmailAndPassword(field_email.text.toString(), field_password.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            info(TAG_LOG, "Firebase authentication with email/password has been successful.")
                            finishSignIn()
                        } else {
                            debug(TAG_LOG, "Firebase authentication with email/password has failed," +
                                    "issue: ${task.exception?.message}")
                            context!!.longToast(getString(R.string.warning_sign_in_email))
                            hideProgress()
                        }
                    }
        }
    }

    private fun firebaseAuth(credentials: AuthCredential, provider: String) {
        displayProgress()
        getAuth().signInWithCredential(credentials).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                info(TAG_LOG,
                     "Firebase authentication via provider $provider was successful...closing activity.")
                finishSignIn()
            } else {
                debug(TAG_LOG,
                      "Firebase authentication with via provider $provider has failed, issue: ${task.exception?.message}")
                context!!.longToast(getString(R.string.warning_sign_in_email))
                hideProgress()
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
        hideProgress()
        Navigation.findNavController(view!!).navigate(
                LoginFragmentDirections.actionAdvance())
    }

    companion object {
        private const val TAG_LOG = "AuthorizationLogger"
        private const val RC_GOOGLE_AUTH = 1234
    }
}