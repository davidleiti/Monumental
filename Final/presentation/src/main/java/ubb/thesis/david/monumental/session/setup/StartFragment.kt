package ubb.thesis.david.monumental.session.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_start.*
import ubb.thesis.david.monumental.BaseApplication
import ubb.thesis.david.monumental.HostActivity
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.BaseFragment
import ubb.thesis.david.monumental.databinding.FragmentStartBinding
import ubb.thesis.david.monumental.utils.getViewModel


class StartFragment : BaseFragment() {

    private lateinit var viewModel: StartViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentStartBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_start, container, false)
        binding.lifecycleOwner = this

        viewModel = getViewModel { StartViewModel(getBeaconManager(), BaseApplication.getAppContext()) }
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if ((activity!! as HostActivity).shouldDisplaySplash) {
            Navigation.findNavController(view!!).navigate(StartFragmentDirections.actionDisplaySplash())
        }

        button_resume.setOnClickListener { navigateToSession() }
        button_start_new.setOnClickListener {
            navigateToSetup()
            viewModel.wipeExistingSession()
        }
    }

    override fun onStart() {
        super.onStart()
        checkRunningSession()
    }

    override fun usesNavigationDrawer(): Boolean = true

    override fun title(): String? = "Home"

    private fun checkRunningSession() = viewModel.queryRunningSession(getUserId())

    private fun navigateToSetup() =
        Navigation.findNavController(view!!).navigate(StartFragmentDirections.actionSetupSession())

    private fun navigateToSession() =
        Navigation.findNavController(view!!).navigate(StartFragmentDirections.actionResumeSession())
}

