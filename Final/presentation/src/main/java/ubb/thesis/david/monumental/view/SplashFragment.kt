package ubb.thesis.david.monumental.view

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import kotlinx.android.synthetic.main.fragment_splash.*
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.BaseFragment


class SplashFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_splash, container, false)

    override fun usesNavigationDrawer(): Boolean = false

    override fun title(): String? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val signedIn = getUserId() != null
        val navController = Navigation.findNavController(view!!)

        Handler().postDelayed(
                {
                    if (signedIn) {
                        navController.navigate(
                                SplashFragmentDirections.toStartDestination())
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

}