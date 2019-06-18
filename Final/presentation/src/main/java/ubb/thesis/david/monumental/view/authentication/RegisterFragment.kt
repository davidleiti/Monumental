package ubb.thesis.david.monumental.view.authentication

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.method.TransformationMethod
import android.transition.ChangeBounds
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_register.*
import ubb.thesis.david.monumental.MainApplication
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.BaseFragment
import ubb.thesis.david.monumental.common.TextDialog
import ubb.thesis.david.monumental.databinding.FragmentRegisterBinding
import ubb.thesis.david.monumental.utils.getViewModel
import ubb.thesis.david.monumental.utils.hideSoftKeyboard

class RegisterFragment : BaseFragment() {

    private lateinit var viewModel: RegisterViewModel

    override fun usesNavigationDrawer(): Boolean = false

    override fun title(): String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentRegisterBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        binding.lifecycleOwner = this

        viewModel = getViewModel {
            RegisterViewModel(getUserAuthenticator(), MainApplication.getAppContext())
        }
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        observeData()
    }

    private fun observeData() {
        viewModel.registrationFinished.observe(viewLifecycleOwner, Observer {
            onSignUpFinished()
        })
        viewModel.errors.observe(viewLifecycleOwner, Observer {
            onError()
        })
    }

    private fun initUi() {
        button_sign_up.setOnClickListener { performSignUp() }
        button_show_password.setOnClickListener { toggleShowPassword() }

        field_email.apply {
            addTextChangedListener {
                if (viewModel.emailError.value != null) validateEmail()
            }
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) validateEmail()
            }
        }

        field_password.apply {
            addTextChangedListener {
                if (viewModel.passwordError.value != null) validatePassword()
            }
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) validatePassword()
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    performSignUp()
                    true
                } else false
            }
        }
    }

    private fun toggleShowPassword() {
        val currentTransformationMethod: TransformationMethod = field_password.transformationMethod

        field_password.transformationMethod =
            if (currentTransformationMethod == PasswordTransformationMethod.getInstance())
                HideReturnsTransformationMethod.getInstance()
            else
                PasswordTransformationMethod.getInstance()
    }

    private fun performSignUp() {
        if (validateFields()) {
            activity!!.hideSoftKeyboard()
            displayProgress()
            viewModel.signUp(field_email.text.toString(), field_password.text.toString())
        }
    }

    private fun onError() {
        hideProgress()
        TextDialog(context!!, getString(R.string.label_error), getString(R.string.message_error_signup)).show()
    }

    private fun onSignUpFinished() {
        hideProgress()
        Navigation.findNavController(view!!).navigate(RegisterFragmentDirections.actionNavigateHome())
    }

    private fun validateEmail() = viewModel.validateEmail(field_email.text.toString())

    private fun validatePassword() = viewModel.validatePassword(field_password.text.toString())

    private fun validateFields(): Boolean {
        validateEmail()
        validatePassword()
        return viewModel.emailError.value == null && viewModel.passwordError.value == null
    }

}