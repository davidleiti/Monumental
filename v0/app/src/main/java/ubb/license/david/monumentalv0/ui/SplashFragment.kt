package ubb.license.david.monumentalv0.ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import kotlinx.android.synthetic.main.fragment_splash.*
import ubb.license.david.monumentalv0.R

class SplashFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_splash, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        disableUserNavigation()
    }

    override fun onStart() {
        super.onStart()
        Handler().postDelayed({
            val signedIn = getAuth().currentUser != null
            val navController = Navigation.findNavController(logo_splash)

            if (signedIn) {
                navController.navigate(SplashFragmentDirections.toStartDestination())
            } else {
                val extras =
                    FragmentNavigator.Extras.Builder().addSharedElement(logo_splash, "transition_logo_splash").build()
                navController.navigate(SplashFragmentDirections.toLoginDestination(), extras)
            }
        }, 1500)
    }
}