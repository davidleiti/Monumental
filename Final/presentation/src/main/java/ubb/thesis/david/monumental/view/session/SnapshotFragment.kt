package ubb.thesis.david.monumental.view.session

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_snapshot.*
import ubb.thesis.david.data.FirebaseLandmarkDetector
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.monumental.Configuration
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.BaseFragment
import ubb.thesis.david.monumental.common.SimpleDialog
import ubb.thesis.david.monumental.geofencing.GeofencingClientAdapter
import ubb.thesis.david.monumental.utils.checkPermission
import ubb.thesis.david.monumental.utils.getViewModel
import ubb.thesis.david.monumental.utils.shortToast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SnapshotFragment : BaseFragment() {

    private var tempPhotoPath: String? = null
    private var invalidatedPhotoPath: String? = null
    private lateinit var viewModel: SnapshotViewModel
    private lateinit var targetLandmark: Landmark

    override fun usesNavigationDrawer(): Boolean = true

    override fun title(): String? = "Image capture"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_snapshot, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        targetLandmark = SnapshotFragmentArgs.fromBundle(arguments!!).targetLandmark

        viewModel = getViewModel {
            SnapshotViewModel(sessionManager = Configuration.provideSessionManager(),
                              beaconManager = GeofencingClientAdapter(context!!),
                              landmarkDetector = FirebaseLandmarkDetector(context!!))
        }

        observeData()

        button_take_photo.setOnClickListener {
            requestStoragePermission()
        }
        button_accept_photo.setOnClickListener {
            displayProgress()
            viewModel.filterLabelInitial(tempPhotoPath!!)
        }
    }

    private fun observeData() {
        viewModel.initialLabelingPassed.observe(viewLifecycleOwner, Observer { passed ->
            onInitialFilteringFinished(passed)
        })
        viewModel.detectionPassed.observe(viewLifecycleOwner, Observer { passed ->
            onDetectionFinished(passed)
        })
        viewModel.finalLabelingPassed.observe(viewLifecycleOwner, Observer { passed ->
            onFinalFilteringFinished(passed)
        })
        viewModel.onLandmarkSaved.observe(viewLifecycleOwner, Observer {
            onLandmarkSaved()
        })
        viewModel.errors.observe(viewLifecycleOwner, Observer { error ->
            onErrorOccurred(error)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    setImagePreview(tempPhotoPath!!)
                    invalidatedPhotoPath?.let {
                        deletePhoto(it)
                    } ?: run {
                        updateUi()
                    }
                } else {
                    invalidatedPhotoPath?.let {
                        deletePhoto(tempPhotoPath!!)
                        tempPhotoPath = invalidatedPhotoPath
                    }
                }
                invalidatedPhotoPath = null
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RC_REQUEST_PERMISSIONS -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    launchCaptureIntent()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun onInitialFilteringFinished(passed: Boolean) {
        if (passed) {
            viewModel.detectLandmark(targetLandmark, tempPhotoPath!!)
        } else {
            SimpleDialog(context!!, getString(R.string.message_oops), getString(R.string.desc_photo_invalid))
                    .show()

            hideProgress()
        }
    }

    private fun onDetectionFinished(passed: Boolean) {
        if (passed)
            onLandmarkRecognized()
        else
            viewModel.filterImageFinal(tempPhotoPath!!)
    }

    private fun onFinalFilteringFinished(passed: Boolean) {
        hideProgress()
        if (passed)
            onLandmarkRecognized()
        else
            SimpleDialog(context!!, getString(R.string.title_unrecognized_photo),
                         getString(R.string.desc_unrecognized_photo)).show()
    }

    private fun onLandmarkRecognized() {
        hideProgress()
        viewModel.saveLandmark(targetLandmark, getUserId(), tempPhotoPath!!, Date())
    }

    private fun onLandmarkSaved() {
        SimpleDialog(context!!, getString(R.string.title_landmark_saved), getString(R.string.message_landmark_Saved))
                .also { dialog ->
                    dialog.updatePositiveButton(getString(R.string.label_ok)) {
                        Navigation.findNavController(view!!).navigateUp()
                    }
                    dialog.show()
                }
    }

    private fun onErrorOccurred(error: Throwable) {
        SimpleDialog(context!!,
                     getString(R.string.message_oops),
                     getString(R.string.message_error_detection, error.message))
                .show()
        hideProgress()
    }

    private fun updateUi() {
        TransitionManager.beginDelayedTransition(container)
        button_accept_photo.visibility = View.VISIBLE
        button_take_photo.invertColors()
        photo_preview.background = null
    }

    private fun requestStoragePermission() {
        if (context!!.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            launchCaptureIntent()
        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), RC_REQUEST_PERMISSIONS)
        }
    }

    private fun launchCaptureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(context!!.packageManager)?.also {
                createTempFile()?.let {
                    val uri = FileProvider.getUriForFile(context!!, "ubb.thesis.david.monumental", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    activity!!.startActivityForResult(takePictureIntent, RC_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun createTempFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDirectory: File = context!!.getExternalFilesDir(DIRECTORY_PICTURES)!!
        return try {
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDirectory).also {
                invalidatedPhotoPath = tempPhotoPath
                tempPhotoPath = it.absolutePath
            }
        } catch (ex: IOException) {
            context!!.shortToast("Failed to create temporary file...")
            ex.printStackTrace()
            null
        }
    }

    private fun setImagePreview(path: String) {
        val photo = File(path)
        if (photo.exists()) {
            val bitmap = BitmapFactory.decodeFile(photo.absolutePath)
            RoundedBitmapDrawableFactory.create(resources, bitmap).also { drawable ->
                drawable.cornerRadius = 100 * Resources.getSystem().displayMetrics.density // 25 dp
                photo_preview.setImageDrawable(drawable)
            }
        }
    }

    private fun deletePhoto(photoPath: String) {
        val photo = File(photoPath)
        if (photo.exists()) {
            if (photo.delete()) {
                info(TAG_LOGGER, "Deleted photo at path $photoPath")
                MediaScannerConnection.scanFile(context!!, arrayOf(photoPath), null) { path, uri ->
                    info(TAG_LOGGER, "Scanned at path $path; uri $uri")
                }
            } else {
                debug(TAG_LOGGER, "Failed to delete photo at path $photoPath")
            }
        }
    }

    companion object {
        private const val TAG_LOGGER = "SnapshotLogger"
        private const val RC_REQUEST_PERMISSIONS = 10
        private const val RC_IMAGE_CAPTURE = 15
    }

}
