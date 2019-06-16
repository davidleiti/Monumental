package ubb.thesis.david.domain.usecases.cloud

import io.reactivex.Observable
import ubb.thesis.david.domain.LandmarkProvider
import ubb.thesis.david.domain.common.Transformer
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.base.ObservableUseCase

class SearchLandmarks(private val params: RequestValues,
                      private val provider: LandmarkProvider,
                      transformer: Transformer<List<Landmark>>) : ObservableUseCase<List<Landmark>>(transformer) {

    data class RequestValues(val lat: Double,
                             val long: Double,
                             val radius: Int,
                             val categories: String,
                             val limit: Int = 0)

    override fun createSource(): Observable<List<Landmark>> {
        val source = provider.searchVenues(params.lat, params.long, params.radius, params.categories)
        if (params.limit > 0)
            source.map { it.take(params.limit) }
        return source.toObservable()
    }
}