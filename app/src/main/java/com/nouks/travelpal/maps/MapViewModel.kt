package com.nouks.travelpal.maps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nouks.travelpal.database.TravelDatabaseDao
import com.nouks.travelpal.database.entities.AppState
import com.nouks.travelpal.database.entities.LocationEntity
import com.nouks.travelpal.database.entities.Travel
import com.nouks.travelpal.database.entities.User
import com.nouks.travelpal.model.google.nearbySearch.Location
import com.nouks.travelpal.model.others.LocationAddress
import kotlinx.coroutines.*

enum class GoogleApiStatus { LOADING, ERROR, DONE }
enum class LocationName {CURRENT, ORIGIN, DESTINATION}
enum class AppStates {AUTOCOMPLETE}
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
    private var _currentLocationSet = MutableLiveData<Boolean>()
    val currentLocationSet: LiveData<Boolean>
        get() = _currentLocationSet
    private var _autoPromptLogin = MutableLiveData<Boolean>()
    val autoPrompLogin: LiveData<Boolean>
        get() = _autoPromptLogin
    private var _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?>
        get() = _currentUser
    private var _autoCompleteUsed = MutableLiveData<Boolean>()
    val autoCompleteUsed: LiveData<Boolean>
        get() = _autoCompleteUsed

    private var _isUserAuthed = MutableLiveData<Boolean>()
    val isUserAuthed: LiveData<Boolean>
        get() = _isUserAuthed
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
        initializeAppStates()
    }

    private fun initializeAppStates() {
        uiScope.launch{
            _autoCompleteUsed.value = isAutoCompleteUsed()
            _currentUser.value = getCurrentUser()
            _isUserAuthed.value = (_currentUser.value != null && _currentUser.value?.token != null)
        }
    }

    fun onPlacesSelected() {
        uiScope.launch {
            setAutoCompleteUsed()
        }
    }

    fun onNavigationComplete() {
        _travelInsertComplete.value = false
    }

    fun onStartTravel(oLocation: Location, oAddress: LocationAddress,
                      dLocation: Location, dAddress: LocationAddress,
                      distance: Double, duration: Long, durationText: String, price: Double) {
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
                distance, duration, durationText,
                price
            )

            _travelId = insertTravel(travel)!!

            if (_autoCompleteUsed.value == null || _autoCompleteUsed.value != true) {
               _autoCompleteUsed.value = setAutoCompleteUsed()
            }
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
    private suspend fun insertTravel(travel: Travel): Long? {
        return withContext(Dispatchers.IO) {
            database.insertTravel(travel)
        }
    }

    private suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            database.getCurrentUser()
        }
    }

    private suspend fun isAutoCompleteUsed(): Boolean {
        return withContext(Dispatchers.IO) {
            database.getAutoCompleteUsageState(AppStates.AUTOCOMPLETE.toString()) != null
        }
    }

    private suspend fun setAutoCompleteUsed(): Boolean {
        return withContext(Dispatchers.IO) {
            database.setAutoCompleteUsageState(
                AppState(
                0L,
                AppStates.AUTOCOMPLETE.toString(),
                    true.toString()
            ))
            true
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
