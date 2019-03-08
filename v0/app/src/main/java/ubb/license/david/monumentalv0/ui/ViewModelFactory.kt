package ubb.license.david.monumentalv0.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.monumentalv0.persistence.SessionManager
import ubb.license.david.monumentalv0.ui.session.setup.ResultViewModel
import ubb.license.david.monumentalv0.ui.session.tracking.SessionViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val dataSource: SessionManager) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResultViewModel::class.java)) {
            return ResultViewModel(dataSource) as T
        }
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            return SessionViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}