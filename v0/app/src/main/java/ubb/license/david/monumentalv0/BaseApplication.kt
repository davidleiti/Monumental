package ubb.license.david.monumentalv0

import android.app.Application

class BaseApplication : Application() {

    init {
        Instance = this
    }

    companion object {
        private lateinit var Instance: BaseApplication
        fun getAppContext(): BaseApplication = Instance
    }
}