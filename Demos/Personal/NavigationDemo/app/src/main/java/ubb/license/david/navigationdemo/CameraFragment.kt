package ubb.license.david.navigationdemo


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_camera.*

class CameraFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_home.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.next_destination)
        }

        button_open.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { snapPictureIntent ->
                snapPictureIntent.resolveActivity(activity!!.packageManager)?.also {
                    startActivityForResult(snapPictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            thumbnail_result.setImageBitmap(imageBitmap)
            thumbnail_result.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}
