package com.nouks.travelpal.maps

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.nouks.travelpal.model.google.directions.Distance
import com.nouks.travelpal.model.google.directions.Duration
import com.nouks.travelpal.model.others.LocationAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

enum class GoogleApiStatus { LOADING, ERROR, DONE }
class MapViewModel: ViewModel() {

    private val _lastLocation = MutableLiveData<Location>()
    val lastLocation: LiveData<Location>
        get() = _lastLocation
    private val _lastOrigin = MutableLiveData<LatLng>()
    val lastOrigin: LiveData<LatLng>
        get() = _lastOrigin
    private val _lastDestination = MutableLiveData<LatLng>()
    val lastDestination: LiveData<LatLng>
        get() = _lastDestination
    private val _locationUpdateState = MutableLiveData<Boolean>()
    val locationUpdateState: LiveData<Boolean>
        get() = _locationUpdateState
    private val _mapReadyState = MutableLiveData<Boolean>()
    val mapReadyState: LiveData<Boolean>
        get() = _mapReadyState
    private val _distance = MutableLiveData<Distance>()
    val distance: LiveData<Distance>
        get() = _distance
    private val _duration = MutableLiveData<Duration>()
    val duration: LiveData<Duration>
        get() = _duration

    private val _lastAddress = MutableLiveData<LocationAddress>()
    val lastAddress: LiveData<LocationAddress>
        get() = _lastAddress

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<GoogleApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<GoogleApiStatus>
        get() = _status
    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        _locationUpdateState.value = false
        _mapReadyState.value = false
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
