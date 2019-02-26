package ubb.license.david.monumentalv0.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_splash_screen.*
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.ui.home.HomeActivity


class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            val signedIn = FirebaseAuth.getInstance().currentUser != null
            val launchIntent: Intent
            var optionsBundle: Bundle? = null

            if (signedIn) {
                launchIntent = Intent(this, HomeActivity::class.java)
            } else {
                launchIntent = Intent(this, LoginActivity::class.java)
                optionsBundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this@SplashScreenActivity,
                    logo_splash,
                    "transition_logo").toBundle()
            }

            startActivity(launchIntent, optionsBundle)
        }, 2000)
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}
