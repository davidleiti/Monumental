package ubb.license.david.monumentalv0.ui

import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth

abstract class BaseFragment : Fragment() {
    protected fun enableUserNavigation() = (activity as UiActions).enableUserNavigation()
    protected fun disableUserNavigation() = (activity as UiActions).disableUserNavigation()
    protected fun showLoading() = (activity as UiActions).showLoading()
    protected fun hideLoading() = (activity as UiActions).hideLoading()

    protected fun getAuth(): FirebaseAuth = (activity as ServiceProvider).getAuth()
    protected fun getGoogleSignInClient(): GoogleSignInClient = (activity as ServiceProvider).getGoogleSignInClient()
    protected fun getGoogleApiClient(): GoogleApiClient = (activity as ServiceProvider).getGoogleApiClient()
}