package ubb.license.david.monumentalv0.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.monumentalv0.persistence.SessionRepository
import ubb.license.david.monumentalv0.ui.session.setup.ResultViewModel
import ubb.license.david.monumentalv0.ui.session.tracking.SessionViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val dataSource: SessionRepository,
    private val api: FoursquareApi) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResultViewModel::class.java)) {
            return ResultViewModel(dataSource, api) as T
        }
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            return SessionViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}