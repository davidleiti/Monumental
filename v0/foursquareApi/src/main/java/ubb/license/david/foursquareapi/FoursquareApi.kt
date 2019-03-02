package ubb.license.david.foursquareapi

import io.reactivex.Single
import ubb.license.david.foursquareapi.model.Photo
import ubb.license.david.foursquareapi.model.Venue
import java.util.*

class FoursquareApi {

    private object Holder {
        val INSTANCE = FoursquareApi()
    }

    private val clientId: String
    private val clientSecret: String
    private val version: String
    private val service: FoursquareService =
        RetrofitBuilder.build().create(FoursquareService::class.java)

    init {
        val resources = ResourceBundle.getBundle("config")
        clientId = resources.getString("FoursquareClientID")
        clientSecret = resources.getString("FoursquareClientSecret")
        version = resources.getString("FoursquareVersionNumber")
    }

    fun searchVenues(location: String, radius: Int, categories: String): Single<Array<Venue>> =
        service.fetchAll(location, radius, categories, clientId, clientSecret, version)
            .map { response -> response.body.venues }

    fun searchVenuesLimited(location: String, radius: Int, limit: Int, categories: String): Single<Array<Venue>> =
        service.fetchLimited(location, radius, limit, categories, clientId, clientSecret, version)
            .map { response -> response.body.venues }

    fun venueDetails(venueId: String): Single<Venue> =
        service.fetchDetails(venueId, clientId, clientSecret, version)
            .map { response -> response.body.venue }

    fun venuePhotos(venueId: String): Single<Array<Photo>> =
        service.fetchPhotos(venueId, clientId, clientSecret, version)
            .map { response -> response.body.photos.items }

    companion object {
        val Instance: FoursquareApi by lazy { Holder.INSTANCE }

        const val ID_MONUMENT = "4bf58dd8d48988d12d941735"
        const val ID_PUBLIC_ART = "52e81612bcbc57f1066b79ed"
        const val ID_STADIUM = "4bf58dd8d48988d184941735"
        const val ID_BRIDGE = "4bf58dd8d48988d1df941735"
        const val ID_CASTLE = "50aaa49e4b90af0d42d5de11"
        const val ID_HISTORIC_SITE = "4deefb944765f83613cdba6e"
        const val ID_MUSEUMS = "4bf58dd8d48988d181941735"
        const val ID_OPERA_HOUSE = "4bf58dd8d48988d136941735"
        const val ID_THEATRE = "4bf58dd8d48988d137941735"

        internal const val PATH_VENUE_ID = "venueId"
        internal const val PARAM_RADIUS = "radius"
        internal const val PARAM_LIMIT = "limit"
        internal const val PARAM_LAT_LONG = "ll"
        internal const val PARAM_CATEGORIES = "categoryId"
        internal const val PARAM_CLIENT_ID = "client_id"
        internal const val PARAM_CLIENT_SECRET = "client_secret"
        internal const val PARAM_VERSION = "v"

        internal const val URL_BASE = "https://api.foursquare.com/v2/"
        internal const val URL_SEARCH = "venues/search"
        internal const val URL_DETAILS = "venues/{$PATH_VENUE_ID}"
        internal const val URL_PHOTOS = "venues/{$PATH_VENUE_ID}/photos"
    }
}