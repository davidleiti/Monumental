package ubb.thesis.david.monumental.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import ubb.thesis.david.domain.*
import ubb.thesis.david.monumental.view.HostActivity
import ubb.thesis.david.monumental.view.FragmentHostActions

abstract class BaseFragment : Fragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (usesNavigationDrawer())
            (activity as FragmentHostActions).enableUserNavigation()
        else
            (activity as FragmentHostActions).disableUserNavigation()
        (activity as FragmentHostActions).setTitle(title())
    }

    // Fragment display options
    protected abstract fun usesNavigationDrawer(): Boolean
    protected abstract fun title(): String?

    // Progress overlay actions
    protected fun displayProgress() = (activity as FragmentHostActions).displayProgress()
    protected fun hideProgress() = (activity as FragmentHostActions).hideProgress()

    // Client provider actions
    protected fun getUserId(): String? = (activity as HostActivity).getUserId()
    protected fun getBeaconManager(): BeaconManager = (activity as ClientProvider).getBeaconManager()
    protected fun getGoogleSignInClient(): GoogleSignInClient = (activity as ClientProvider).getSignInClient()
    protected fun getDataSource(): CloudDataSource = (activity as ClientProvider).getDataSource()
    protected fun getImageStorage(): ImageStorage = (activity as ClientProvider).getImageStorage()
    protected fun getLandmarkDetector(): LandmarkDetector = (activity as ClientProvider).getLandmarkDetector()
    protected fun getUserAuthenticator(): UserAuthenticator = (activity as ClientProvider).getUserAuthenticator()

}