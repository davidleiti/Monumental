package ubb.thesis.david.monumental

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import kotlinx.android.synthetic.main.fragment_splash.*
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.monumental.common.BaseFragment
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class SplashFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_splash, container, false)

    override fun usesNavigationDrawer(): Boolean = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        getShaSignature()
        Handler().postDelayed({
//            (activity as? HostActivity)?.navigateFromSplashWithId?.let {
//                val navController = Navigation.findNavController(view!!)
//                navController.navigate(R.id.snapshotDestiantion)
//            } ?: run {
                                  //            }
                val signedIn = getAuth().currentUser != null
                val navController = Navigation.findNavController(view!!)

                if (signedIn) {
                    navController.navigate(SplashFragmentDirections.toStartDestination())
                } else {
                    val extras =
                        FragmentNavigator.Extras.Builder()
                                .addSharedElement(logo_splash, "transition_logo_splash")
                                .build()
                    navController.navigate(
                            SplashFragmentDirections.toLoginDestination(), extras)
                }
        }, 1000)
    }

    private fun getShaSignature() {
        try {
            val info = context!!.packageManager.getPackageInfo(BaseApplication.getAppContext().packageName,
                                                               PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }
    }
}