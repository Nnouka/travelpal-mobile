package com.nouks.travelpal.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val autoCompleteUsed = "AUTOCOMPLETE"
@Entity(tableName = "app_states")
data class AppState(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "value")
    val value: String
)