package ubb.license.david.monumentalv0

import android.app.Application
import com.facebook.FacebookSdk

class BaseApplication : Application() {

    init {
        appContext = this
    }

    companion object {
        private lateinit var appContext: BaseApplication
        fun getAppContext(): BaseApplication = appContext
    }
}