package ubb.license.david.foursquareapitesting

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class NetworkAdapter private constructor() {

    private object Holder {
        val INSTANCE = NetworkAdapter()
    }

    private val service: FourquareService

    init {
        val gson: Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(NetworkAdapter.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()

        service = retrofit.create(FourquareService::class.java)
    }

    companion object {
        val instance: NetworkAdapter by lazy { Holder.INSTANCE }

        const val BASE_URL = "https://api.foursquare.com/v2/venues/"
        const val CLIENT_ID = "5FRXDTBNR4CQFCE0LD0ZYSOMRKAQWDVNSWJMVQ4RRPUQWP40"
        const val CLIENT_SECRET = "MR0QFBIFX4QNDE1ZASBF2MJXUWMVQMLZVCRBYOVLHJMVOIOQ"
        const val VERSION = "20190101"
    }

    fun fetchAll(location: String, radius: Int, categories: String): Observable<ResponseBody> {
        return service.fetchAll(location, radius, categories, CLIENT_ID, CLIENT_SECRET, VERSION)
    }

    interface FourquareService {
        @GET("search")
        fun fetchAll(
            @Query("ll") location: String, @Query("radius") radius: Int, @Query("categoryId") categoryString: String,
            @Query("client_id") clientId: String, @Query("client_secret") clientSecret: String, @Query("v") version: String
        ): Observable<ResponseBody>
    }

}