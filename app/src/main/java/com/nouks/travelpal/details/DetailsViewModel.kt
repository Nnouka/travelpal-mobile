package com.nouks.travelpal.details

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nouks.travelpal.api.travelpal.dto.LoginDTO
import com.nouks.travelpal.api.travelpal.dto.TokenResponseDTO
import com.nouks.travelpal.api.travelpal.dto.TravelIntentRequest
import com.nouks.travelpal.database.TravelDatabaseDao
import com.nouks.travelpal.database.entities.LocationEntity
import com.nouks.travelpal.database.entities.User
import com.nouks.travelpal.login.navigation.LoginResult
import com.nouks.travelpal.model.google.nearbySearch.Location
import com.nouks.travelpal.model.others.FormattedTravel
import com.nouks.travelpal.model.others.LocationAddress
import kotlinx.coroutines.*
import liuuu.laurence.maputility.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    private var _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?>
        get() = _currentUser

    private var _intentRegistered = MutableLiveData<Boolean>()
    val intentRegistered: LiveData<Boolean>
        get() = _intentRegistered
    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var currentLocation = MutableLiveData<LocationEntity?>()

    init {
        initialize()
    }

    private fun initialize() {
        uiScope.launch{
            currentLocation.value = getCurrentLocationFromDatabase()
            _currentUser.value = getCurrentUser()
        }
    }

    fun onPreviewReady(id: Long) {
        uiScope.launch {
            val travel = getTravelById(id)
            if (travel != null) _travel.value = travel
        }
    }

    fun registerTravelIntent(travelIntentRequest: TravelIntentRequest,
              clientAuth: String, userToken: String, progressLayout: View, context: Context?) {
        // call api for registration of travel intent
        progressLayout.visibility = View.VISIBLE
        val autheader = HashMap<String, String>()
        autheader.put("X-Api-Auth", clientAuth.trim())
        autheader.put("Authorization", userToken.trim())
        val resultCall = RetrofitClient.travelpalMethods().registerTravelIntent(
            autheader, travelIntentRequest
        )

        resultCall.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Booking Successful", Toast.LENGTH_LONG).show()
                    _intentRegistered.value = true
                } else {
                    Log.i("SignUpViewModel", response.message())
                    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
                }
                progressLayout.visibility = View.GONE
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show()
                Log.i("SignUpViewModel", t.toString())
                progressLayout.visibility = View.GONE
            }
        })
    }
    private suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            val user = database.getCurrentUser()
            if(user != null) {
                Log.i("MapViewModel", user.token.toString())
            }
            user
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
            val destination = database.getLocationById(travel.destinationLocationId)
            FormattedTravel(
                travel.id,
                Location(origin!!.longitude, origin.latitude),
                LocationAddress(origin.countryCode, origin.formattedAddress, origin.name),
                Location(destination!!.longitude, destination.longitude),
                LocationAddress(destination.countryCode, destination.formattedAddress, destination.name),
                travel.distance,
                travel.duration,
                travel.durationText
            )
        }
    }

    fun getPriceFromDistance(distance: Float): Float {
        val wieght = distance / 1000F
        if (wieght < 1) return 1000F
        else return 1000F + (wieght - 1) * 100
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
