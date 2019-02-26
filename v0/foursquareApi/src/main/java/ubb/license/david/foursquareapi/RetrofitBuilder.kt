package ubb.license.david.foursquareapi

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

abstract class RetrofitBuilder {
    companion object {
        fun build(): Retrofit {
            val gson: Gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .create()

            return Retrofit.Builder()
                .baseUrl(FoursquareApi.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
    }
}