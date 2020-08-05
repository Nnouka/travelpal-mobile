package com.nouks.travelpal.model.google.directions

import com.google.gson.annotations.SerializedName

data class Polyline(@SerializedName("points")
                    val points: String = "")