package ubb.license.david.monumentalv0

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.database.FirebaseDatabase

class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        FacebookSdk.setApplicationId(BuildConfig.FACEBOOK_APP_ID)
        FacebookSdk.sdkInitialize(applicationContext)

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}