package com.nouks.travelpal.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_data")
data class Travel(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    @ColumnInfo(name = "origin_location_id")
    var originLocationId: Long,
    @ColumnInfo(name = "destination_location_id")
    var destinationLocationId: Long,
    @ColumnInfo(name = "distance")
    var distance: Double = 0.0,
    @ColumnInfo(name = "duration")
    var duration: Long = 0L,
    @ColumnInfo(name = "duration_text")
    var durationText: String
)