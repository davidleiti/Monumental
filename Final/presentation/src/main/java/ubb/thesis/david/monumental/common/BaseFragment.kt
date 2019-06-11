package ubb.thesis.david.monumental.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import ubb.thesis.david.domain.BeaconManager

abstract class BaseFragment : Fragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (usesNavigationDrawer())
            (activity as UiActions).enableUserNavigation()
        else
            (activity as UiActions).disableUserNavigation()
    }

    protected abstract fun usesNavigationDrawer(): Boolean
    protected fun displayProgress() = (activity as UiActions).displayProgress()
    protected fun hideProgress() = (activity as UiActions).hideProgress()

    protected fun getUserId(): String = (activity as ClientProvider).getUserId()
    protected fun getAuth(): FirebaseAuth = (activity as ClientProvider).getAuth()
    protected fun getGoogleApiClient(): GoogleApiClient = (activity as ClientProvider).getApiClient()
    protected fun getGoogleSignInClient(): GoogleSignInClient = (activity as ClientProvider).getSignInClient()
    protected fun getBeaconManager(): BeaconManager = (activity as ClientProvider).getGeofencingClient()
}