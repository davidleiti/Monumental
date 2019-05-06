package ubb.thesis.david.monumental

import android.content.Context
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.thesis.david.data.FoursquareApiAdapter
import ubb.thesis.david.data.SessionRepository
import ubb.thesis.david.data.cache.SessionDatabase
import ubb.thesis.david.domain.LandmarkApi
import ubb.thesis.david.domain.SessionManager

object Injection {

    fun provideLandmarkApi(): LandmarkApi = FoursquareApiAdapter(FoursquareApi)

    fun provideSessionManager(): SessionManager =
        SessionRepository.getInstance(provideDatabase(BaseApplication.getAppContext()))

    private fun provideDatabase(context: Context) = SessionDatabase.getInstance(context)
}