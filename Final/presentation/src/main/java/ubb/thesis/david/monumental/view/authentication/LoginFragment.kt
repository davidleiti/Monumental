package ubb.thesis.david.monumental.view.authentication

import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
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
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_login.*
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.monumental.MainApplication
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.BaseFragment
import ubb.thesis.david.monumental.common.TextDialog
import ubb.thesis.david.monumental.databinding.FragmentLoginBinding
import ubb.thesis.david.monumental.utils.*

class LoginFragment : BaseFragment(), View.OnClickListener {

    private lateinit var viewModel: LoginViewModel
    private lateinit var signInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private val facebookAuthCallback: FacebookCallback<LoginResult> by lazy { createFacebookAuthCallback() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sharedElementEnterTransition = ChangeBounds().apply { duration = 300 }

        val binding: FragmentLoginBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.lifecycleOwner = this

        viewModel = getViewModel {
            LoginViewModel(getUserAuthenticator(), MainApplication.getAppContext())
        }
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        observeData()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        signInClient = getGoogleSignInClient()
        setupFacebookAuth()
    }

    override fun usesNavigationDrawer(): Boolean = false

    override fun title(): String? = null

    private fun observeData() {
        viewModel.authenticationFinished.observe(viewLifecycleOwner, Observer {
            onAuthenticationFinished()
        })
        viewModel.errors.observe(viewLifecycleOwner, Observer {
            onError()
        })
    }

    private fun initUi() {
        button_sign_in.setOnClickListener(this)
        button_sign_up.setOnClickListener(this)
        button_sign_in_google.setOnClickListener(this)
        button_facebook_custom.setOnClickListener(this)

        field_email.apply {
            addTextChangedListener {
                validateEmail()
            }
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) validateEmail()
            }
        }

        field_password.apply {
            addTextChangedListener {
                validatePassword()
            }
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) validatePassword()
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    emailAuth()
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
            R.id.button_sign_up -> navigateToSignUp()
            R.id.button_sign_in_google -> googleSignIn()
            R.id.button_sign_in -> emailAuth()
            R.id.button_facebook_custom -> button_sign_in_facebook.performClick()
        }
    }

    private fun setupFacebookAuth() {
        callbackManager = CallbackManager.Factory.create()
        button_sign_in_facebook.setReadPermissions("email", "public_profile")
        button_sign_in_facebook.registerCallback(callbackManager, facebookAuthCallback)
    }

    private fun createFacebookAuthCallback(): FacebookCallback<LoginResult> = object : FacebookCallback<LoginResult> {
        override fun onSuccess(result: LoginResult) {
            info(TAG_LOG, "Facebook authentication has been successful!! Attempting firebase authentication...")
            val credentials = FacebookAuthProvider.getCredential(result.accessToken.token)
            firebaseAuth(credentials)
        }

        override fun onCancel() {
            debug(TAG_LOG, "Facebook sign-in has been cancelled.")
            context!!.shortToast(getString(R.string.message_sign_in_cancelled))
        }

        override fun onError(error: FacebookException?) {
            debug(TAG_LOG, "Failed to receive Facebook access token, cause: $error")
            context!!.longToast(getString(R.string.warning_sign_in_provider))
        }
    }

    private fun googleSignIn() {
        val signInIntent: Intent = signInClient.signInIntent
        activity!!.startActivityForResult(signInIntent, RC_GOOGLE_AUTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_GOOGLE_AUTH) {    // Returned from Google authentication activity
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                info(TAG_LOG, "Google authentication has been successful! Attempting firebase authentication...")
                val account = task.getResult(ApiException::class.java)
                val credentials = GoogleAuthProvider.getCredential(account?.idToken, null)

                firebaseAuth(credentials)
            } catch (e: ApiException) {
                debug(TAG_LOG, "Google authentication has failed, cause: $e")
                context!!.longToast(getString(R.string.warning_sign_in_provider))
            }
        } else {    // Returned from Facebook authentication, propagate result up to the CallbackManager's listener
            super.onActivityResult(requestCode, resultCode, data)
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun emailAuth() {
        if (validateFields()) {
            activity!!.hideSoftKeyboard()
            displayProgress()
            viewModel.emailAuth(field_email.text.toString(), field_password.text.toString())
        }
    }

    private fun firebaseAuth(authCredential: AuthCredential) {
        displayProgress()
        viewModel.thirdPartyAuth(authCredential)
    }

    private fun onAuthenticationFinished() {
        hideProgress()
        Navigation.findNavController(view!!).navigate(
                LoginFragmentDirections.actionAdvance())
    }

    private fun onError() {
        hideProgress()
        TextDialog(context!!, getString(R.string.label_error), getString(R.string.message_error_signin)).show()
    }

    private fun validateEmail() = viewModel.validateEmail(field_email.text.toString())

    private fun validatePassword() = viewModel.validatePassword(field_password.text.toString())

    private fun validateFields(): Boolean =
        validateEmail() && validatePassword()

    private fun navigateToSignUp() =
        Navigation.findNavController(view!!).navigate(LoginFragmentDirections.actionRegister())

    companion object {
        private const val TAG_LOG = "AuthorizationLogger"
        private const val RC_GOOGLE_AUTH = 1234
    }
}