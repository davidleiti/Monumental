package ubb.thesis.david.monumental

import android.content.Context
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.thesis.david.data.FoursquareApiAdapter
import ubb.thesis.david.data.SessionRepository
import ubb.thesis.david.data.cache.SessionDatabase
import ubb.thesis.david.data.common.ImageMapper
import ubb.thesis.david.data.common.LandmarkMapper
import ubb.thesis.david.domain.LandmarkApi
import ubb.thesis.david.domain.SessionManager

object Configuration {

    fun provideLandmarkApi(): LandmarkApi = FoursquareApiAdapter(FoursquareApi, LandmarkMapper(), ImageMapper())

    fun provideSessionManager(): SessionManager =
        SessionRepository.getInstance(provideDatabase(MainApplication.getAppContext()))

    private fun provideDatabase(context: Context) = SessionDatabase.getInstance(context)
}