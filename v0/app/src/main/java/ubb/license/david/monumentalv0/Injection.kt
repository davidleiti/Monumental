package ubb.license.david.monumentalv0

import android.content.Context
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.monumentalv0.persistence.cache.SessionDatabase
import ubb.license.david.monumentalv0.persistence.SessionRepository
import ubb.license.david.monumentalv0.ui.ViewModelFactory

object Injection {

    fun provideDatabase(context: Context): SessionDatabase = SessionDatabase.getInstance(context)

    fun provideDataSource(context: Context): SessionRepository =
        SessionRepository.getInstance(provideDatabase(context))

    fun provideFourSquareApi() = FoursquareApi

    fun provideViewModelFactory(context: Context): ViewModelFactory {
        val dataSource = provideDataSource(context)
        val api = provideFourSquareApi()
        return ViewModelFactory(dataSource, api)
    }
}