package ubb.license.david.monumentalv0.ui.session.tracking


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.lifecycle.Observer
import ubb.license.david.monumentalv0.GeofencingClientWrapper
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.persistence.model.Landmark
import ubb.license.david.monumentalv0.ui.BaseFragment
import ubb.license.david.monumentalv0.utils.debug
import ubb.license.david.monumentalv0.utils.getViewModel
import ubb.license.david.monumentalv0.utils.info
import ubb.license.david.monumentalv0.utils.shortSnack

class NavigationFragment : BaseFragment() {

    private lateinit var viewModel: SessionViewModel
    private lateinit var fencingClient: GeofencingClientWrapper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_navigation, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fencingClient = getGeofencingClient()
        viewModel = getViewModel()
        observeData()
        showLoading()

        viewModel.loadSessionLandmarks(getUserId())
    }

    private fun observeData() {
        viewModel.getLandmarksObservable().observe(viewLifecycleOwner, Observer { landmarks ->
            initializeFencesIfNeeded(landmarks)
            hideLoading()
        })
        viewModel.getErrorsObservable().observe(viewLifecycleOwner, Observer { error ->
            debug(TAG_LOG, "The following error has occurred: $error")
            view?.shortSnack("An error has occurred!")
            hideLoading()
        })
    }

    private fun initializeFencesIfNeeded(landmarks: List<Landmark>) {
        val sharedPrefs = context!!.getSharedPreferences(getUserId(), Context.MODE_PRIVATE)
        sharedPrefs?.let {
            for (landmark in landmarks) {
                if (!sharedPrefs.contains(landmark.id))
                    createGeofence(landmark, sharedPrefs)
            }
        }
    }

    private fun createGeofence(landmark: Landmark, sharedPrefs: SharedPreferences) {
        fencingClient.createGeofence(landmark.id, landmark.lat, landmark.lng,
            onSuccess = {
                info(TAG_LOG, "Created and registered geofence for $landmark")
                sharedPrefs.edit {
                    putBoolean(landmark.id, true)
                }
            },
            onFailure = {
                debug(TAG_LOG, "Failed to create geofence for $landmark, cause: $it")
            })
    }

    companion object {
        private const val TAG_LOG = "SessionLogger"
    }
}
