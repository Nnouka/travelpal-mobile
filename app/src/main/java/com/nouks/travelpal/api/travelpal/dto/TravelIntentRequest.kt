package com.nouks.travelpal.api.travelpal.dto

data class TravelIntentRequest(
    val originFormattedAddress: String,
    val originLongitude: Double,
    val originLatitude: Double,
    val destinationFormattedAddress: String,
    val destinationLongitude: Double,
    val destinationLatitude: Double,
    val distance: Double,
    val duration: Long,
    val durationText: String
)