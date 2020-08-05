package com.nouks.travelpal.api.travelpal

import com.nouks.travelpal.model.google.directions.Directions
import com.nouks.travelpal.model.google.nearbySearch.NearbySearch
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TravelPalMethods {
    // Google Place API -- Nearby search
    @GET("place/nearbysearch/json")
    fun getNearbySearch(
            @Query("location") location: String,
            @Query("radius") radius: String,
            @Query("type") types: String,
            @Query("key") key: String
    ): Call<NearbySearch>

    // Google Directions API -- directions
    @GET("directions/json")
    fun getDirections(
            @Query("origin") origin: String,
            @Query("destination") destination: String,
            @Query("key") key: String
    ): Call<Directions>
}