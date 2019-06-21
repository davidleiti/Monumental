package ubb.thesis.david.monumental

import android.content.Context
import ubb.license.david.foursquareapi.service.FoursquareApi
import ubb.thesis.david.data.adapters.FoursquareApiAdapter
import ubb.thesis.david.data.adapters.SessionCache
import ubb.thesis.david.data.cache.SessionDatabase
import ubb.thesis.david.data.mappers.ImageMapper
import ubb.thesis.david.data.mappers.LandmarkMapper
import ubb.thesis.david.domain.LandmarkProvider
import ubb.thesis.david.domain.SessionManager

object Configuration {

    fun provideLandmarkApi(): LandmarkProvider =
        FoursquareApiAdapter(FoursquareApi, LandmarkMapper(), ImageMapper())

    fun provideSessionManager(): SessionManager =
        SessionCache.getInstance(provideDatabase(MainApplication.getAppContext()))

    private fun provideDatabase(context: Context) = SessionDatabase.getInstance(context)
}