package ubb.license.david.monumentalv0.ui

import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import ubb.license.david.monumentalv0.InitializationException
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.utils.fadeIn
import ubb.license.david.monumentalv0.utils.fadeOut

abstract class ProgressOverlayActivity : AppCompatActivity() {

    private lateinit var mProgressOverlay: View

    override fun onStart() {
        super.onStart()
        attachOverlay()
    }

    fun showLoading() {
        checkInitialized()
        mProgressOverlay.fadeIn()
    }

    fun hideLoading() {
        checkInitialized()
        mProgressOverlay.fadeOut()
    }

    private fun checkInitialized() {
        if (!::mProgressOverlay.isInitialized)
            throw InitializationException("Loading overlay may only be displayed after " +
                    "it has been initialized in ProgressOverlayActivity.onPostCreate()")
    }

    private fun attachOverlay() {
        val contentRoot = findViewById<FrameLayout>(R.id.content_root)
        contentRoot?.let {
            mProgressOverlay = layoutInflater.inflate(R.layout.progress_overlay, content_root, false)
                .apply {
                    content_root.addView(this)
                }
        } ?: run {
            throw InitializationException(
                "Activity must contain a container with id 'content_root' of type FrameLayout " +
                        "which should hold all the content over which the loading overlay should be displayed!")
        }
    }
}