package com.nouks.travelpal.model.others

import com.nouks.travelpal.model.google.nearbySearch.Location

data class FormattedTravel(
    val id: Long,
    val originLocation: Location,
    val originAddress: LocationAddress,
    val destinationLocation: Location,
    val destinationAddress: LocationAddress,
    val distance: Double,
    val duration: Long,
    val durationText: String
)