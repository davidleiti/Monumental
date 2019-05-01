package ubb.thesis.david.data

import io.reactivex.Single
import io.reactivex.functions.BiFunction
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.foursquareapi.model.Photo
import ubb.license.david.foursquareapi.model.Venue
import ubb.thesis.david.domain.LandmarkApi
import ubb.thesis.david.domain.entities.Landmark
import java.util.*

class FoursquareApiAdapter(private val api: FoursquareApi) : LandmarkApi {

    override fun searchVenues(location: String,
                              radius: Int,
                              categories: String): Single<List<Landmark>> {
        val searchRes = api.searchVenues(location, radius, FoursquareApi.ID_MONUMENT)
                .transformToLandmarkList()

        val exploreRes = api.exploreVenues(location, radius, FoursquareApi.SECTION_ARTS)
                .filterExploreResults(categories)
                .transformToLandmarkList()

        return Single.zip(searchRes, exploreRes, BiFunction { searchResults, exploreResults ->
            combineResults(searchResults, exploreResults)
        })
    }

    // TODO map result to actual entity object
    override fun getVenueDetails(id: String): Single<Venue> =
        api.venueDetails(id)

    // TODO map result to actual entity object
    override fun getVenuePhotos(id: String): Single<List<Photo>> =
        api.venuePhotos(id)

    private fun combineResults(searchResults: List<Landmark>,
                               exploreResults: List<Landmark>): List<Landmark> {
        val allVenues = ArrayList<Landmark>().apply {
            addAll(searchResults)
            addAll(exploreResults)
        }
        return allVenues.distinctBy { venue -> venue.id }
    }
}

private fun Single<List<Venue>>.filterExploreResults(categoriesString: String): Single<List<Venue>> =
    map { venues -> venues.filter { venue -> categoriesString.contains(venue.categories!![0].id) } }

private fun Single<List<Venue>>.transformToLandmarkList(): Single<List<Landmark>> =
    map { venues -> venues.map { venue -> Landmark.fromVenue(venue) } }