package com.nouks.travelpal.api.travelpal

import com.nouks.travelpal.api.travelpal.dto.*
import com.nouks.travelpal.model.google.directions.Directions
import com.nouks.travelpal.model.google.nearbySearch.NearbySearch
import retrofit2.Call
import retrofit2.http.*

interface TravelPalMethods {
    // Google Place API -- Nearby search
    @POST("protected/token")
    fun getUserToken(@HeaderMap headerMap: Map<String, String>, @Body loginDTO: LoginDTO): Call<TokenResponseDTO>

    @POST("public/user/register")
    fun registerUser(@HeaderMap headerMap: Map<String, String>, @Body user: RegisterUserRequest): Call<RegisterUserResponse>

    @POST("public/app/register")
    fun registerAppInstance(@HeaderMap headerMap: Map<String, String>): Call<RegisterUserResponse>

    @POST("protected/user/intent/travel")
    fun registerTravelIntent(@HeaderMap headerMap: Map<String, String>, @Body travelIntent: TravelIntentRequest): Call<String>

}