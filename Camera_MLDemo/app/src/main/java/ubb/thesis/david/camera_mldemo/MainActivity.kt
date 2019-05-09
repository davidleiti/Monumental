package ubb.thesis.david.camera_mldemo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var savedPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissionsIfNeeded()

        button_open_cam.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    imageView.setImageBitmap(BitmapFactory.decodeFile(savedPhotoPath))
                    addImageToGallery()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RC_REQUEST_PERMISSIONS -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionsIfNeeded()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun addImageToGallery() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val file = File(savedPhotoPath)
            mediaScanIntent.data = Uri.fromFile(file)
            sendBroadcast(mediaScanIntent)
        }
    }

    @Throws(IOException::class)
    private fun createTempFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDirectory: File = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDirectory).also {
            savedPhotoPath = it.absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val tempFile: File? = try {
                    createTempFile()
                } catch (ex: IOException) {
                    Toast.makeText(this, "Failed to create temporary file...", Toast.LENGTH_SHORT).show()
                    ex.printStackTrace()
                    null
                }
                tempFile?.let {
                    val uri = FileProvider.getUriForFile(this, "ubb.thesis.david.camera_mldemo", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    startActivityForResult(takePictureIntent, RC_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun requestPermissionsIfNeeded() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                       Manifest.permission.READ_EXTERNAL_STORAGE), RC_REQUEST_PERMISSIONS)
        }
    }

    companion object {
        private const val RC_IMAGE_CAPTURE = 10
        private const val RC_REQUEST_PERMISSIONS = 20
    }
}
