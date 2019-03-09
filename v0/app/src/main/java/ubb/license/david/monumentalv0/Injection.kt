package ubb.license.david.monumentalv0

import android.content.Context
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.monumentalv0.persistence.SessionManager
import ubb.license.david.monumentalv0.persistence.cache.SessionDatabase

object Injection {

    fun provideFourSquareApi() = FoursquareApi

    fun provideDatabase(context: Context) = SessionDatabase.getInstance(context)

    fun provideSessionManager(): SessionManager =
        SessionManager.getInstance(
            provideDatabase(BaseApplication.getAppContext()),
            provideFourSquareApi())
}