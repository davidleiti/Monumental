package ubb.thesis.david.monumental.domain

import io.reactivex.Single
import ubb.license.david.foursquareapi.model.Photo
import ubb.license.david.foursquareapi.model.Venue
import ubb.thesis.david.monumental.domain.entities.Landmark

interface LandmarkApi {

    fun searchVenues(location: String, radius: Int, categories: String): Single<List<Landmark>>

    // TODO replace with entities mapped from the api result objects
    fun getVenueDetails(id: String): Single<Venue>
    fun getVenuePhotos(id: String): Single<List<Photo>>

}