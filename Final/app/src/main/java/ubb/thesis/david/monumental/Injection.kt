package ubb.thesis.david.monumental

import android.content.Context
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.thesis.david.monumental.data.SessionManager
import ubb.thesis.david.monumental.data.cache.SessionDatabase

object Injection {

    fun provideFourSquareApi() = FoursquareApi

    fun provideDatabase(context: Context) = SessionDatabase.getInstance(context)

    fun provideSessionManager(): SessionManager =
        SessionManager.getInstance(
                provideDatabase(BaseApplication.getAppContext()),
                provideFourSquareApi())
}