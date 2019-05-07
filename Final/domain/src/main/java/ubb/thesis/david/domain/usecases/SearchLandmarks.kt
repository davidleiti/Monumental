package ubb.thesis.david.domain.usecases

import io.reactivex.Observable
import ubb.thesis.david.domain.LandmarkApi
import ubb.thesis.david.domain.common.Transformer
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.domain.usecases.base.ObservableUseCase

class SearchLandmarks(private val params: RequestValues,
                      private val api: LandmarkApi,
                      transformer: Transformer<List<Landmark>>) :
    ObservableUseCase<List<Landmark>>(transformer) {

    data class RequestValues(val location: String, val radius: Int, val categories: String, val limit: Int = 0)

    override fun createSource(): Observable<List<Landmark>> {
        val source = api.searchVenues(params.location, params.radius, params.location)
        if (params.limit > 0)
            source.map { it.take(params.limit) }
        return source.toObservable()
    }
}