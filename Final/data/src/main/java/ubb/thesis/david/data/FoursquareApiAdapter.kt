package ubb.thesis.david.data

import io.reactivex.Single
import io.reactivex.functions.BiFunction
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.foursquareapi.model.Photo
import ubb.license.david.foursquareapi.model.Venue
import ubb.thesis.david.domain.LandmarkProvider
import ubb.thesis.david.domain.common.Mapper
import ubb.thesis.david.domain.entities.ImageEntity
import ubb.thesis.david.domain.entities.Landmark
import java.util.*

class FoursquareApiAdapter(private val api: FoursquareApi,
                           private val landmarkMapper: Mapper<Venue, Landmark>,
                           private val imageMapper: Mapper<Photo, ImageEntity>) : LandmarkProvider {

    override fun searchVenues(lat: Double,
                              long: Double,
                              radius: Int,
                              categories: String): Single<List<Landmark>> {
        val searchRes = api.searchVenues("$lat,$long", radius, FoursquareApi.ID_MONUMENT)
                .transformToLandmarkList()

        val exploreRes = api.exploreVenues("$lat,$long", radius, FoursquareApi.SECTION_ARTS)
                .filterExploreResults(categories)
                .transformToLandmarkList()

        return Single.zip(searchRes, exploreRes, BiFunction { searchResults, exploreResults ->
            combineResults(searchResults, exploreResults)
        })
    }

    override fun getLandmarkDetails(id: String): Single<Landmark> =
        api.venueDetails(id).map { landmarkMapper.mapFrom(it) }

    override fun getLandmarkImages(id: String): Single<List<ImageEntity>> =
        api.venuePhotos(id).map { it.map { photo -> imageMapper.mapFrom(photo) } }

    private fun combineResults(searchResults: List<Landmark>,
                               exploreResults: List<Landmark>): List<Landmark> {
        val allVenues = ArrayList<Landmark>().apply {
            addAll(searchResults)
            addAll(exploreResults)
        }
        return allVenues.distinctBy { venue -> venue.id }
    }

    private fun Single<List<Venue>>.filterExploreResults(categoriesString: String): Single<List<Venue>> =
        map { venues -> venues.filter { venue -> categoriesString.contains(venue.categories!![0].id) } }

    private fun Single<List<Venue>>.transformToLandmarkList(): Single<List<Landmark>> =
        map { venues -> venues.map { venue -> landmarkMapper.mapFrom(venue) } }
}