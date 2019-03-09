package ubb.license.david.monumentalv0.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.monumentalv0.persistence.SessionManager
import ubb.license.david.monumentalv0.ui.session.setup.ResultViewModel
import ubb.license.david.monumentalv0.ui.session.tracking.SessionViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory<T>(private val creator: () -> T) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return creator() as T
    }
}