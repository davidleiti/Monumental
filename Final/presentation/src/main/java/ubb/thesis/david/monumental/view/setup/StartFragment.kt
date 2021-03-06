package ubb.thesis.david.monumental.view.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_start.*
import ubb.thesis.david.monumental.MainApplication
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.BaseFragment
import ubb.thesis.david.monumental.common.TextDialog
import ubb.thesis.david.monumental.databinding.FragmentStartBinding
import ubb.thesis.david.monumental.utils.getViewModel
import ubb.thesis.david.monumental.view.HostActivity


class StartFragment : BaseFragment() {

    private lateinit var viewModel: StartViewModel

    override fun usesNavigationDrawer(): Boolean = true

    override fun title(): String? = getString(R.string.title_home)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentStartBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_start, container, false)
        binding.lifecycleOwner = this

        viewModel = getViewModel {
            StartViewModel(getBeaconManager(), getDataSource(), MainApplication.getAppContext())
        }
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if ((activity!! as HostActivity).shouldDisplaySplash) {
            Navigation.findNavController(view!!).navigate(StartFragmentDirections.actionDisplaySplash())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_resume.setOnClickListener { navigateToSession() }
        button_start_new.setOnClickListener {
            promptStartNew()
        }
        button_load.setOnClickListener {
            displayProgress()
            viewModel.loadSessionBackup(getUserId()!!)
        }
        observeData()
    }

    private fun promptStartNew() {
        TextDialog(context!!, getString(R.string.label_warning), getString(R.string.message_prompt_start))
                .also { dialog ->
                    dialog.updatePositiveButton(getString(R.string.label_yes)) {
                        displayProgress()
                        viewModel.wipeSessionData(getUserId()!!)
                    }
                    dialog.setupNegativeButton(getString(R.string.label_cancel))
                    dialog.show()
                }
    }

    override fun onStart() {
        super.onStart()
        checkCachedSession()
    }

    private fun observeData() {
        viewModel.backupLoaded.observe(viewLifecycleOwner, Observer { result ->
            hideProgress()
            onBackupQueryCompleted(result)
        })
        viewModel.sessionWiped.observe(viewLifecycleOwner, Observer {
            hideProgress()
            navigateToSetup()
        })
        viewModel.errors.observe(viewLifecycleOwner, Observer {
            hideProgress()
            TextDialog(context!!, getString(R.string.label_error), getString(R.string.message_error_operation)).show()
        })
    }

    private fun onBackupQueryCompleted(success: Boolean) {
        if (!success) {
            TextDialog(context!!, getString(R.string.label_error), getString(R.string.message_no_session_found)).show()
            checkCachedSession()
        }
    }

    private fun checkCachedSession() = viewModel.loadSessionCache(getUserId()!!)

    private fun navigateToSetup() =
        Navigation.findNavController(view!!).navigate(StartFragmentDirections.actionSetupSession())

    private fun navigateToSession() =
        Navigation.findNavController(view!!).navigate(StartFragmentDirections.actionResumeSession())
}

