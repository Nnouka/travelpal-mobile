package liuuu.laurence.maputility.api

import com.nouks.travelpal.api.google.GoogleMethods
import com.nouks.travelpal.api.travelpal.TravelPalMethods
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient

object RetrofitClient {
    private const val GOOGLE_BASE_URL = "https://maps.googleapis.com/maps/api/"

    private const val TRAVEL_PAL_BASE_URL = "https://travelpal-web.herokuapp.com/api/"

    fun googleMethods(): GoogleMethods {
        val retrofit = Retrofit.Builder()
                .baseUrl(GOOGLE_BASE_URL)
                .client(OkHttpClient().newBuilder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        return retrofit.create(GoogleMethods::class.java)
    }

    fun travelpalMethods(): TravelPalMethods {
        val retrofit = Retrofit.Builder()
            .baseUrl(TRAVEL_PAL_BASE_URL)
            .client(OkHttpClient().newBuilder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(TravelPalMethods::class.java)
    }
}