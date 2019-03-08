package ubb.license.david.monumentalv0

import android.app.Application
import com.facebook.FacebookSdk

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id))
        FacebookSdk.sdkInitialize(applicationContext)
    }

}