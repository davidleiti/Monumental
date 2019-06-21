package ubb.license.david.foursquareapi.service

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ubb.license.david.foursquareapi.service.FoursquareApi.PARAM_CATEGORIES
import ubb.license.david.foursquareapi.service.FoursquareApi.PARAM_CLIENT_ID
import ubb.license.david.foursquareapi.service.FoursquareApi.PARAM_CLIENT_SECRET
import ubb.license.david.foursquareapi.service.FoursquareApi.PARAM_LAT_LONG
import ubb.license.david.foursquareapi.service.FoursquareApi.PARAM_RADIUS
import ubb.license.david.foursquareapi.service.FoursquareApi.PARAM_SECTION
import ubb.license.david.foursquareapi.service.FoursquareApi.PARAM_VERSION
import ubb.license.david.foursquareapi.service.FoursquareApi.PATH_VENUE_ID
import ubb.license.david.foursquareapi.service.FoursquareApi.URL_DETAILS
import ubb.license.david.foursquareapi.service.FoursquareApi.URL_EXPLORE
import ubb.license.david.foursquareapi.service.FoursquareApi.URL_PHOTOS
import ubb.license.david.foursquareapi.service.FoursquareApi.URL_SEARCH
import ubb.license.david.foursquareapi.responses.ApiResponse

internal interface FoursquareService {

    @GET(URL_SEARCH)
    fun fetchSearch(
        @Query(PARAM_LAT_LONG) location: String, @Query(PARAM_RADIUS) radius: Int,
        @Query(PARAM_CATEGORIES) categories: String, @Query(PARAM_CLIENT_ID) clientId: String,
        @Query(PARAM_CLIENT_SECRET) clientSecret: String, @Query(PARAM_VERSION) version: String
    ): Single<ApiResponse.SearchVenuesResponse>

    @GET(URL_EXPLORE)
    fun fetchExplore(
        @Query(PARAM_LAT_LONG) location: String, @Query(PARAM_RADIUS) radius: Int,
        @Query(PARAM_SECTION) section: String, @Query(PARAM_CLIENT_ID) clientId: String,
        @Query(PARAM_CLIENT_SECRET) clientSecret: String, @Query(PARAM_VERSION) version: String
    ): Single<ApiResponse.ExploreVenuesResponse>

    @GET(URL_PHOTOS)
    fun fetchPhotos(
        @Path(PATH_VENUE_ID) venueId: String, @Query(PARAM_CLIENT_ID) clientId: String,
        @Query(PARAM_CLIENT_SECRET) clientSecret: String, @Query(PARAM_VERSION) version: String
    ): Single<ApiResponse.PhotosResponse>

    @GET(URL_DETAILS)
    fun fetchDetails(
        @Path(PATH_VENUE_ID) venueId: String, @Query(PARAM_CLIENT_ID) clientId: String,
        @Query(PARAM_CLIENT_SECRET) clientSecret: String, @Query(PARAM_VERSION) version: String
    ): Single<ApiResponse.VenueDetailsResponse>

}