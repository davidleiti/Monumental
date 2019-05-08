package ubb.thesis.david.monumental.presentation.session.setup


import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_start.*
import ubb.thesis.david.domain.entities.Session
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.presentation.common.BaseFragment
import ubb.thesis.david.monumental.utils.getViewModel
import java.util.*


class StartFragment : BaseFragment(), View.OnClickListener {

    private var runningSession: Session? = null
    private lateinit var viewModel: StartViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_start, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_start_new.setOnClickListener(this)
        button_resume.setOnClickListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = getViewModel { StartViewModel(getBeaconManager()) }
        observeData()
        checkRunningSession()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_start_new -> {
                setupSession()
                runningSession?.let {
                    viewModel.wipeSessionData(getUserId())
                }
            }
            R.id.button_resume -> {
                resumeSession()
            }
        }
    }

    override fun usesNavigationDrawer(): Boolean = true

    private fun observeData() {
        viewModel.getRunningSessionObservable().observe(viewLifecycleOwner, Observer {
            runningSession = it
            it?.let { updateUi() } ?: run { resetUi() }
            hideLoading()
        })
    }

    private fun checkRunningSession() {
        showLoading()
        viewModel.queryRunningSession(getUserId())
    }

    private fun updateUi() {
        TransitionManager.beginDelayedTransition(content_root)
        val timeElapsed = getTimeElapsedString(runningSession!!.timeStarted)
        label_start.text = getString(R.string.message_journey_found, timeElapsed)
        label_continue.visibility = View.VISIBLE
        button_resume.visibility = View.VISIBLE
        button_start_new.setText(R.string.start_new)
    }

    private fun resetUi() {
        TransitionManager.beginDelayedTransition(content_root)
        label_start.text = getString(R.string.message_journey_start)
        label_continue.visibility = View.GONE
        button_resume.visibility = View.GONE
        button_start_new.setText(R.string.start)
    }

    private fun setupSession() =
        Navigation.findNavController(view!!)
                .navigate(StartFragmentDirections.actionSetupSession())

    private fun resumeSession() =
        Navigation.findNavController(view!!)
                .navigate(StartFragmentDirections.actionResumeSession())

    private fun getTimeElapsedString(start: Date): String {
        val secondsFactor = 1000
        val minutesFactor = secondsFactor * 60
        val hoursFactor = minutesFactor * 60
        val daysFactor = hoursFactor * 24

        var deltaTime = Date().time - start.time
        val deltaDays = deltaTime / daysFactor
        if (deltaDays > 0)
            return if (deltaDays > 1) "$deltaDays ${getString(R.string.days_ago)}"
            else "1 ${getString(R.string.day_ago)}"

        deltaTime %= daysFactor
        val deltaHours = deltaTime / hoursFactor
        if (deltaHours > 0)
            return if (deltaHours > 1) "$deltaHours ${getString(R.string.hours_ago)}"
            else "1 ${getString(R.string.hour_ago)}"

        deltaTime %= hoursFactor
        val deltaMinutes = deltaTime / minutesFactor
        if (deltaMinutes > 0)
            return if (deltaMinutes > 1) "$deltaMinutes ${getString(R.string.minutes_ago)}"
            else "1 ${getString(R.string.minute_ago)}"

        return getString(R.string.just_now)
    }

    companion object {
        private const val TAG_LOG = "SetupLogger"
    }
}
