package ubb.thesis.david.monumental

import android.content.Context
import ubb.license.david.foursquareapi.FoursquareApi

object Injection {

    fun provideLandmarkApi(): ubb.thesis.david.domain.LandmarkApi =
        ubb.thesis.david.data.FoursquareApiAdapter(FoursquareApi)

    fun provideSessionManager(): ubb.thesis.david.domain.SessionManager =
        ubb.thesis.david.data.SessionRepository.getInstance(provideDatabase(BaseApplication.getAppContext()))

    private fun provideDatabase(context: Context) = ubb.thesis.david.data.cache.SessionDatabase.getInstance(context)
}