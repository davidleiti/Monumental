import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import responses.PhotosResponse
import responses.VenueDetailsResponse
import responses.VenuesResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class FoursquareAPI {

    private object Holder {
        val INSTANCE = FoursquareAPI()
    }

    private val service: FoursquareService

    init {
        val gson: Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .create()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        service = retrofit.create(FoursquareService::class.java)
    }

    companion object {
        val instance: FoursquareAPI by lazy { Holder.INSTANCE }

        const val URL_BASE: String = "https://api.foursquare.com/v2/venues/"
        const val CLIENT_ID = "5FRXDTBNR4CQFCE0LD0ZYSOMRKAQWDVNSWJMVQ4RRPUQWP40"
        const val CLIENT_SECRET = "MR0QFBIFX4QNDE1ZASBF2MJXUWMVQMLZVCRBYOVLHJMVOIOQ"
        const val VERSION = "20190101"
    }

    fun fetchAll(location: String, radius: Int, categories: String): Call<VenuesResponse> {
        return service.fetchAll(location, radius, categories, CLIENT_ID, CLIENT_SECRET, VERSION)
    }

    fun fetchDetails(venueId: String): Call<VenueDetailsResponse> {
        return service.fetchDetails(venueId, CLIENT_ID, CLIENT_SECRET, VERSION)
    }

    fun fetchPhotos(venueId: String): Call<PhotosResponse> {
        return service.fetchPhotos(venueId, CLIENT_ID, CLIENT_SECRET, VERSION)
    }

    private interface FoursquareService {

        @GET("search")
        fun fetchAll(
            @Query("ll") location: String, @Query("radius") radius: Int,
            @Query("categoryId") categories: String, @Query("client_id") clientId: String,
            @Query("client_secret") clientSecret: String, @Query("v") version: String
        ): Call<VenuesResponse>

        @GET("{venueId}/photos")
        fun fetchPhotos(
            @Path("venueId") venueId: String, @Query("client_id") clientId: String,
            @Query("client_secret") clientSecret: String, @Query("v") version: String
        ): Call<PhotosResponse>

        @GET("{venueId}")
        fun fetchDetails(
            @Path("venueId") venueId: String, @Query("client_id") clientId: String,
            @Query("client_secret") clientSecret: String, @Query("v") version: String
        ): Call<VenueDetailsResponse>

    }
}