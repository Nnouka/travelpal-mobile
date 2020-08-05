package com.nouks.travelpal.details

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.nouks.travelpal.database.TravelDatabaseDao
import com.nouks.travelpal.database.entities.LocationEntity
import com.nouks.travelpal.database.entities.Travel
import com.nouks.travelpal.model.google.directions.Distance
import com.nouks.travelpal.model.google.directions.Duration
import com.nouks.travelpal.model.google.nearbySearch.Location
import com.nouks.travelpal.model.others.FormattedTravel
import com.nouks.travelpal.model.others.LocationAddress
import kotlinx.coroutines.*

enum class GoogleApiStatus { LOADING, ERROR, DONE }
enum class LocationName {CURRENT, ORIGIN, DESTINATION}
class DetailsViewModel(
    val database: TravelDatabaseDao,
    application: Application
): AndroidViewModel(application) {

    val TAG = "MapViewModel"

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<GoogleApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<GoogleApiStatus>
        get() = _status

    private val _travel = MutableLiveData<FormattedTravel> ()

    val travel: LiveData<FormattedTravel>
        get() = _travel
    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var currentLocation = MutableLiveData<LocationEntity?>()

    init {
        initializeCurrentLocation()
    }

    private fun initializeCurrentLocation() {
        uiScope.launch{
            currentLocation.value = getCurrentLocationFromDatabase()
        }
    }

    fun onPreviewReady(id: Long) {
        uiScope.launch {
            val travel = getTravelById(id)
            if (travel != null) _travel.value = travel
        }
    }
    private suspend fun getCurrentLocationFromDatabase(): LocationEntity? {
        return withContext(Dispatchers.IO) {
            var cLocation = database.getLocationByName(LocationName.CURRENT.toString())
            cLocation
        }
    }

    private suspend fun insertLocation(locationEntity: LocationEntity?) {
        withContext(Dispatchers.IO) {
            if (locationEntity != null)
                database.insertLocation(locationEntity)
        }
    }

    private suspend fun getLocationByName(name: String): LocationEntity? {
        return withContext(Dispatchers.IO) {
            database.getLocationByName(name)
        }
    }

    private suspend fun getTravelById(id: Long): FormattedTravel? {
        return withContext(Dispatchers.IO) {
           val travel = database.getTravel(id)
            val origin = database.getLocationById(travel!!.originLocationId)
            val destination = database.getLocationById(travel!!.destinationLocationId)
            FormattedTravel(
                travel.id,
                Location(origin!!.longitude, origin.latitude),
                LocationAddress(origin.countryCode, origin.formattedAddress, origin.name),
                Location(destination!!.longitude, destination.longitude),
                LocationAddress(destination.countryCode, destination.formattedAddress, destination.name),
                travel.distance,
                travel.duration
            )
        }
    }


    /**
     * When the [ViewModel] is finished, we cancel our coroutine [viewModelJob], which tells the
     * Retrofit service to stop.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
