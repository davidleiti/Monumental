package ubb.license.david.monumentalv0.ui

import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import ubb.license.david.monumentalv0.GeofencingClientWrapper

abstract class BaseFragment : Fragment() {
    protected fun enableUserNavigation() = (activity as UiActions).enableUserNavigation()
    protected fun disableUserNavigation() = (activity as UiActions).disableUserNavigation()
    protected fun showLoading() = (activity as UiActions).showLoading()
    protected fun hideLoading() = (activity as UiActions).hideLoading()

    protected fun getUserId(): String = (activity as ClientProvider).getUserId()
    protected fun getAuth(): FirebaseAuth = (activity as ClientProvider).getAuth()
    protected fun getGoogleApiClient(): GoogleApiClient = (activity as ClientProvider).getApiClient()
    protected fun getGoogleSignInClient(): GoogleSignInClient = (activity as ClientProvider).getSignInClient()
    protected fun getGeofencingClient(): GeofencingClientWrapper = (activity as ClientProvider).getGeofencingClient()
}