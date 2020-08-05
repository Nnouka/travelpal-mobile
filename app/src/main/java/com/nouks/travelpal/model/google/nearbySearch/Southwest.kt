package com.nouks.travelpal.model.google.nearbySearch

import com.google.gson.annotations.SerializedName

data class Southwest(@SerializedName("lng")
                     val lng: Double = 0.0,
                     @SerializedName("lat")
                     val lat: Double = 0.0)