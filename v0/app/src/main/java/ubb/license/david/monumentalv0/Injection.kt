package ubb.license.david.monumentalv0

import android.content.Context
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.monumentalv0.persistence.SessionManager
import ubb.license.david.monumentalv0.persistence.cache.SessionDatabase
import ubb.license.david.monumentalv0.ui.ViewModelFactory

object Injection {

    fun provideFourSquareApi() = FoursquareApi

    fun provideDatabase(context: Context): SessionDatabase = SessionDatabase.getInstance(context)

    fun provideDataSource(context: Context): SessionManager =
        SessionManager.getInstance(provideDatabase(context), provideFourSquareApi())

    fun provideViewModelFactory(context: Context): ViewModelFactory {
        val dataSource = provideDataSource(context.applicationContext)
        return ViewModelFactory(dataSource)
    }
}