package com.nouks.travelpal.maps

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nouks.travelpal.database.TravelDatabaseDao

class MapViewModelFactory(
    private val dataSource: TravelDatabaseDao,
    private val application: Application,
    val authed: Boolean = false
): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(dataSource, application, authed) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}