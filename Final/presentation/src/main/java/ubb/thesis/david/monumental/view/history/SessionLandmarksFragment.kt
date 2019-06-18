package ubb.thesis.david.monumental.view.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_session_landmarks.*
import ubb.thesis.david.data.utils.FileUtils
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.BaseFragment
import ubb.thesis.david.monumental.common.TextDialog
import ubb.thesis.david.monumental.databinding.FragmentSessionLandmarksBinding
import ubb.thesis.david.monumental.utils.getViewModel
import java.text.SimpleDateFormat
import java.util.*

class SessionLandmarksFragment : BaseFragment() {

    private lateinit var viewModel: SessionLandmarksViewModel
    private lateinit var listAdapter: LandmarkDataListAdapter

    override fun usesNavigationDrawer() = true

    override fun title(): String? = getString(R.string.title_session_landmarks)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FragmentSessionLandmarksBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_session_landmarks, container, false)
        binding.lifecycleOwner = this

        viewModel = getViewModel { SessionLandmarksViewModel(getDataSource(), getImageStorage()) }
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionId = SessionLandmarksFragmentArgs.fromBundle(arguments!!).sessionId

        observeData()

        viewModel.landmarkDataRetrieved.value ?: run {
            displayProgress()
            viewModel.fetchLandmarks(getUserId()!!, sessionId)
        }
    }

    private fun observeData() {
        viewModel.landmarkDataRetrieved.observe(viewLifecycleOwner, Observer { landmarkData ->
            onLandmarkDataRetrieved(landmarkData)
        })
        viewModel.errors.observe(viewLifecycleOwner, Observer { error ->
            onError(error)
        })
    }

    private fun onLandmarkDataRetrieved(landmarkData: Map<Landmark, Discovery?>) {
        info(TAG_LOG, "Retrieved ${landmarkData.size} discovered landmarks for this session.")
        hideProgress()

        if (landmarkData.isNotEmpty() && ::listAdapter.isInitialized.not()) {
            val sortedByDiscovery = landmarkData.toList().sortedBy { (_, discovery) ->
                discovery?.time
            }.toMap()

            listAdapter = LandmarkDataListAdapter(sortedByDiscovery) { photoId ->
                downloadImage(photoId)
            }
            list_landmarks.adapter = listAdapter
            list_landmarks.layoutManager = LinearLayoutManager(context!!)
        }
    }

    private fun onError(error: Throwable) {
        debug(TAG_LOG, "Failed to retrieve session landmarks with error ${error.message}")
        TextDialog(context!!, getString(R.string.label_error), getString(R.string.message_error_operation))
                .also { dialog ->
                    dialog.updatePositiveButton(getString(R.string.label_ok)) {
                        Navigation.findNavController(view!!).navigateUp()
                    }
                }
    }

    private fun downloadImage(photoId: String) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val targetFile = FileUtils.createSharedFile("${photoId}_$timeStamp")

        targetFile?.let { file ->
            viewModel.downloadImage(getUserId()!!, photoId, file.absolutePath)
        } ?: run {
            onFileCreationFailed()
        }
    }

    private fun onFileCreationFailed() =
        TextDialog(context!!, getString(R.string.label_error), getString(R.string.message_error_download_file)).show()

    companion object {
        private const val TAG_LOG = "SessionLandmarksViewLogger"
    }

}
