package ubb.thesis.david.domain

import io.reactivex.Single
import ubb.thesis.david.domain.entities.ImageEntity
import ubb.thesis.david.domain.entities.Landmark

interface LandmarkApi {

    fun searchVenues(lat: Double, long: Double, radius: Int, categories: String): Single<List<Landmark>>
    fun getLandmarkDetails(id: String): Single<Landmark>
    fun getLandmarkImages(id: String): Single<List<ImageEntity>>

}