package ubb.thesis.david.monumental

import android.content.Context
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.thesis.david.monumental.data.FoursquareApiAdapter
import ubb.thesis.david.monumental.data.SessionRepository
import ubb.thesis.david.monumental.data.cache.SessionDatabase
import ubb.thesis.david.monumental.domain.LandmarkApi
import ubb.thesis.david.monumental.domain.SessionManager

object Injection {

    fun provideLandmarkApi(): LandmarkApi = FoursquareApiAdapter(FoursquareApi)

    fun provideSessionManager(): SessionManager =
        SessionRepository.getInstance(provideDatabase(BaseApplication.getAppContext()))

    private fun provideDatabase(context: Context) = SessionDatabase.getInstance(context)
}