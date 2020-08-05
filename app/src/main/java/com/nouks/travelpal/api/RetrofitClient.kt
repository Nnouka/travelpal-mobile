package liuuu.laurence.maputility.api

import com.nouks.travelpal.api.google.GoogleMethods
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient

object RetrofitClient {
    private const val GOOGLE_BASE_URL = "https://maps.googleapis.com/maps/api/"

    private const val TRAVE_PAL_WEB_BASE_URL = "https://"

    fun googleMethods(): GoogleMethods {
        val retrofit = Retrofit.Builder()
                .baseUrl(GOOGLE_BASE_URL)
                .client(OkHttpClient().newBuilder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        return retrofit.create(GoogleMethods::class.java)
    }
}