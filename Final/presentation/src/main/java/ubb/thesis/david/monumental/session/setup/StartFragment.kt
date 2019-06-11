package ubb.thesis.david.monumental.session.setup

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavDeepLinkBuilder
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkRunningSession()

        button_resume.setOnClickListener{ navigateToSession() }
        button_start_new.setOnClickListener {
            navigateToSetup()
            viewModel.wipeExistingSession()
        }
        notification_button.setOnClickListener {
            sendNotification("some id")
        }
    }

    private fun sendNotification(fenceId: String) {
        val notificationManager = context!!.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        val actionIntent = Intent(context, HostActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(HostActivity.KEY_LAUNCH_FOUND_ID, fenceId)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pendingIntent2 = NavDeepLinkBuilder(context!!)
                .setGraph(R.navigation.nav_recognition)
                .setDestination(R.id.snapshotDestination)
                .setArguments(bundleOf("targetId" to fenceId))
                .createPendingIntent()

        NotificationCompat.Builder(context!!, BaseApplication.GEOFENCE_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_logo_128)
                .setContentTitle(getString(R.string.title_notification))
                .setContentText(getString(R.string.content_notification))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent2)
                .setAutoCancel(true)
                .build()
                .also { notification ->
                    notificationManager.notify(fenceId.hashCode(), notification)
                }
    }

    override fun usesNavigationDrawer(): Boolean = true

    private fun checkRunningSession() = viewModel.queryRunningSession(getUserId())

    private fun navigateToSetup() =
        Navigation.findNavController(view!!).navigate(StartFragmentDirections.actionSetupSession())

    private fun navigateToSession() =
        Navigation.findNavController(view!!).navigate(StartFragmentDirections.actionResumeSession())
}

