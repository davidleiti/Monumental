package ubb.license.david.foursquareapi

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ubb.license.david.foursquareapi.FoursquareApi.Companion.PARAM_CATEGORIES
import ubb.license.david.foursquareapi.FoursquareApi.Companion.PARAM_CLIENT_ID
import ubb.license.david.foursquareapi.FoursquareApi.Companion.PARAM_CLIENT_SECRET
import ubb.license.david.foursquareapi.FoursquareApi.Companion.PARAM_LAT_LONG
import ubb.license.david.foursquareapi.FoursquareApi.Companion.PARAM_LIMIT
import ubb.license.david.foursquareapi.FoursquareApi.Companion.PARAM_RADIUS
import ubb.license.david.foursquareapi.FoursquareApi.Companion.PARAM_VERSION
import ubb.license.david.foursquareapi.FoursquareApi.Companion.PATH_VENUE_ID
import ubb.license.david.foursquareapi.FoursquareApi.Companion.URL_DETAILS
import ubb.license.david.foursquareapi.FoursquareApi.Companion.URL_PHOTOS
import ubb.license.david.foursquareapi.FoursquareApi.Companion.URL_SEARCH
import ubb.license.david.foursquareapi.responses.PhotosResponse
import ubb.license.david.foursquareapi.responses.VenueDetailsResponse
import ubb.license.david.foursquareapi.responses.VenuesResponse

internal interface FoursquareService {

    @GET(URL_SEARCH)
    fun fetchAll(
        @Query(PARAM_LAT_LONG) location: String, @Query(PARAM_RADIUS) radius: Int,
        @Query(PARAM_CATEGORIES) categories: String, @Query(PARAM_CLIENT_ID) clientId: String,
        @Query(PARAM_CLIENT_SECRET) clientSecret: String, @Query(PARAM_VERSION) version: String
    ): Single<VenuesResponse>

    @GET(URL_SEARCH)
    fun fetchLimited(
        @Query(PARAM_LAT_LONG) location: String, @Query(PARAM_RADIUS) radius: Int,
        @Query(PARAM_LIMIT) limit: Int, @Query(PARAM_CATEGORIES) categories: String,
        @Query(PARAM_CLIENT_ID) clientId: String, @Query(PARAM_CLIENT_SECRET) clientSecret: String,
        @Query(PARAM_VERSION) version: String
    ): Single<VenuesResponse>

    @GET(URL_PHOTOS)
    fun fetchPhotos(
        @Path(PATH_VENUE_ID) venueId: String, @Query(PARAM_CLIENT_ID) clientId: String,
        @Query(PARAM_CLIENT_SECRET) clientSecret: String, @Query(PARAM_VERSION) version: String
    ): Single<PhotosResponse>

    @GET(URL_DETAILS)
    fun fetchDetails(
        @Path(PATH_VENUE_ID) venueId: String, @Query(PARAM_CLIENT_ID) clientId: String,
        @Query(PARAM_CLIENT_SECRET) clientSecret: String, @Query(PARAM_VERSION) version: String
    ): Single<VenueDetailsResponse>

}