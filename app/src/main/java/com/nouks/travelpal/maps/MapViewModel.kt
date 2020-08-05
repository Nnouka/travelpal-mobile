package com.nouks.travelpal.maps

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
import com.nouks.travelpal.model.others.LocationAddress
import kotlinx.coroutines.*

enum class GoogleApiStatus { LOADING, ERROR, DONE }
enum class LocationName {CURRENT, ORIGIN, DESTINATION}
class MapViewModel(
    val database: TravelDatabaseDao,
    application: Application
): AndroidViewModel(application) {

    val TAG = "MapViewModel"
    private val _travelInsertComplete = MutableLiveData<Boolean>()
    val travelInsertComplete: LiveData<Boolean>
        get() = _travelInsertComplete
    private var _travelId = 0L
    val travelId: Long
        get() = _travelId

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<GoogleApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<GoogleApiStatus>
        get() = _status
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

    fun onNavigationComplete() {
        _travelInsertComplete.value = false
    }

    fun onStartTravel(oLocation: Location, oAddress: LocationAddress,
                      dLocation: Location, dAddress: LocationAddress, distance: Double, duration: Long, durationText: String) {
        uiScope.launch {
            _travelInsertComplete.value = false
            var oLocEntity : LocationEntity? = LocationEntity(
                0L,
                oAddress.formattedAddress,
                oAddress.countryCode,
                LocationName.ORIGIN.toString(),
                oLocation.lat,
                oLocation.lng
            )
            insertLocation(oLocEntity)
            oLocEntity = getLatestLocation()
            var dLocEntity : LocationEntity? = LocationEntity(
                0L,
                dAddress.formattedAddress,
                dAddress.countryCode,
                LocationName.DESTINATION.toString(),
                dLocation.lat,
                dLocation.lng
            )
            insertLocation(dLocEntity)
            dLocEntity = getLatestLocation()

            // insert Journey
            val travel = Travel(
                0L,
                oLocEntity!!.id,
                dLocEntity!!.id,
                distance, duration, durationText
            )

            _travelId = insertTravel(travel)!!.id
            _travelInsertComplete.value = true
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

    private suspend fun getLatestLocation(): LocationEntity? {
        return withContext(Dispatchers.IO) {
            database.getLatestLocation()
        }
    }
    private suspend fun insertTravel(travel: Travel): Travel? {
        return withContext(Dispatchers.IO) {
            database.insertTravel(travel)
            database.getLatestTravel()
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
