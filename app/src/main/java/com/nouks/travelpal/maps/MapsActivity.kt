package com.nouks.travelpal.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.nouks.travelpal.details.DetailsActivity
import com.nouks.travelpal.R
import com.nouks.travelpal.database.TravelDatabase
import com.nouks.travelpal.model.google.directions.Directions
import com.nouks.travelpal.model.google.nearbySearch.Location
import com.nouks.travelpal.model.others.LocationAddress
import com.nouks.travelpal.model.others.Route
import kotlinx.android.synthetic.main.activity_maps.*
import liuuu.laurence.maputility.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*

const val EXTRA_MESSAGE = "com.nouks.travelpal.maps.MESSAGE"
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private val TAG = "MapsActivity"
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location // vm
    private lateinit var lastOrigin: LatLng // vm
    private lateinit var lastDestination: LatLng // vm
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false // vm
    private var mapReadyState = false // vm
    private var distance = 0 // vm
    private var duration = "" // vm
    private var durationLong = 0L
    private lateinit var apiKey: String
    private lateinit var lastAddress: LocationAddress // vm
    private lateinit var lastDAddress: LocationAddress
    private lateinit var mMapsController: MapsController
    private lateinit var mapViewModel: MapViewModel
    companion object {
        val ZOOM_WORLD = 1f
        val ZOOM_CONTINENT = 5f
        val ZOOM_CITY = 10f
        val ZOOM_STREETS = 13.5f
        val ZOOM_BUILDINGS = 20f
        private const val PLACE_PICKER_REQUEST = 3
        private const val AUTO_COMPLETE_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment
        mapFragment.getMapAsync(this)
        val application = getApplication()

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao

        val viewModelFactory = MapViewModelFactory(dataSource, application)
        mapViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(MapViewModel::class.java)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                if (p0 !== null) {
                    lastLocation = Location(p0.lastLocation.longitude, p0.lastLocation.latitude)
//                    placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
                }
            }
        }

        createLocationRequest()

        apiKey = getString(R.string.google_maps_key)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        val placesClient = Places.createClient(this)

        val autoCompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment1)
                                        as AutocompleteSupportFragment

        autoCompleteFragment.setHint("Origin(Current Location)")
        val searchIcon = (autoCompleteFragment.getView() as LinearLayout).getChildAt(0) as ImageView

        // Set the desired icon
        searchIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_location_black_24dp))
        addSearchOnClickListener(autoCompleteFragment, true)
        val autoCompleteFragment2 = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment2)
                as AutocompleteSupportFragment
        autoCompleteFragment2.setHint("Search Destination")
        val searchIcon2 = (autoCompleteFragment2.getView() as LinearLayout).getChildAt(0) as ImageView
        // Set the desired icon
        searchIcon2.setImageDrawable(getResources().getDrawable(R.drawable.ic_location_on_black_24dp))
        addSearchOnClickListener(autoCompleteFragment2)

        // set reset button onclick handler
        button_reset.setOnClickListener { view: View? ->
            autoCompleteFragment.setText(null)
            autoCompleteFragment2.setText(null)
            button_panel_layout.visibility = View.GONE
            preview_panel_layout.visibility = View.GONE
        }


        val myLocationButton =
            mapFragment.view!!.findViewById<View>(0x2)

        if (myLocationButton != null && myLocationButton.layoutParams is RelativeLayout.LayoutParams) {
            // location button is inside of RelativeLayout
            val params =
                myLocationButton.layoutParams as RelativeLayout.LayoutParams

            // Align it to - parent BOTTOM|LEFT
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)

            // Update margins, set to 10dp
            val margin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10F,
                resources.displayMetrics
            )
            params.setMargins(margin.toInt(), margin.toInt(), margin.toInt(), margin.toInt())
            myLocationButton.layoutParams = params
        }

    }

    private fun addSearchOnClickListener(autoCompleteFragment: AutocompleteSupportFragment, current: Boolean = false) {
        autoCompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        setUpCurrentLocation(autoCompleteFragment)
        autoCompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                progressLayout.visibility = View.VISIBLE
                button_panel_layout.visibility = View.GONE
                preview_panel_layout.visibility = View.GONE
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: ${place.name}, ${place.id}, ${place.latLng}")
                var latLng = place.latLng as LatLng
                if (!current) {
                    lastOrigin = latLng
                    lastDAddress = lastAddress
                    lastAddress = LocationAddress(lastAddress.countryCode, place.name.toString(), place.id.toString())
                    lastDestination = LatLng(lastLocation.lat, lastLocation.lng)

                } else {
                    lastDestination = latLng
                    lastDAddress = LocationAddress(lastAddress.countryCode, place.name.toString(), place.id.toString())
                    lastOrigin = LatLng(lastLocation.lat, lastLocation.lng)
                }
//                Log.i(TAG, getString(R.string.location_query_id, lastAddress.countryCode))
                if (mapReadyState) {
                    if (current) autoCompleteFragment.setText(lastAddress.formattedAddress)
                    val directionsCall = when(current) {
                        true -> RetrofitClient.googleMethods().getDirections(getString(
                            R.string.location_query_text, latLng.latitude, latLng.longitude),
                            getString(R.string.location_query_text, lastOrigin.latitude, lastOrigin.longitude), apiKey)
                        else -> RetrofitClient.googleMethods().getDirections(getString(
                            R.string.location_query_text, lastLocation.lat, lastLocation.lng),
                            getString(R.string.location_query_text, latLng.latitude, latLng.longitude), apiKey)
                    }

                    directionsCall.enqueue(object : Callback<Directions> {
                        override fun onResponse(call: Call<Directions>, response: Response<Directions>) {
                            val directions = response.body()!!

                            Log.i(TAG, directions.toString())

                            if (directions.status.equals("OK")) {
                                mMapsController.clearMarkersAndRoute()
                                val legs = directions.routes[0].legs[0]
                                Log.i(TAG, legs.distance.value.toString())
                                val route = Route(lastAddress.formattedAddress, place.name as String, legs.startLocation.lat, legs.startLocation.lng, legs.endLocation.lat, legs.endLocation.lng, directions.routes[0].overviewPolyline.points)
                                mMapsController.setMarkersAndRoute(route)
                                distance = legs.distance.value
                                duration = legs.duration.text
                                durationLong = legs.duration.value.toLong()
                                duration_distance_text.text = getString(R.string.duration_distance, duration, "${(distance / 1000)} Km")
                                price_text.text = getString(R.string.price_string, getPriceFromDistance(distance.toFloat()).toString())
                                button_panel_layout.visibility = View.VISIBLE
                                preview_panel_layout.visibility = View.VISIBLE
                            } else {
                                Toast.makeText(this@MapsActivity, directions.status, Toast.LENGTH_SHORT).show()
                            }

                            progressLayout.visibility = View.GONE

                        }

                        override fun onFailure(call: Call<Directions>, t: Throwable) {
                            Toast.makeText(this@MapsActivity, t.toString(), Toast.LENGTH_SHORT).show()
                            progressLayout.visibility = View.GONE
                        }
                    })
                }
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

//        val latitude = 4.15744
//        val longitude = 9.29105
        setMapLongClick(map)
        setPoiClick(map)
//        setMapStyle(map)
        enableMyLocation()
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        mapReadyState = true
        mMapsController = MapsController(this, map)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMarkerClick(p0: Marker?) = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()

                placeMarkerOnMap(place.latLng)
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    // 3
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }
    private fun setMapLongClick(map:GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener {poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failled")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setUpCurrentLocation(fragment: AutocompleteSupportFragment) {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener{
                location ->
            if (location != null) {
                lastLocation = Location(location.longitude, location.latitude)
                val currentLatLng = LatLng(location.latitude, location.longitude)
                lastOrigin = currentLatLng
                lastAddress = getAddress(currentLatLng)
                fragment.setCountry(lastAddress.countryCode)
                if (mapReadyState) {
                    setUpMap()
                }
            }
        }
    }

    private fun setUpMap() {
        val currentLatLng = LatLng(lastLocation.lat, lastLocation.lng)
        placeMarkerOnMap(currentLatLng,
            R.mipmap.ic_user_location, lastAddress.formattedAddress)
    }

    private fun getAddress(latLng: LatLng): LocationAddress {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""
        var countryCode = ""
        var addressId = ""

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (null != addresses && addresses.isNotEmpty()) {
                address = addresses[0]
                Log.i(TAG, address.toString())
                addressText = address.getAddressLine(0)
                countryCode = address.countryCode
            }
        } catch (e: IOException) {
            Log.e(TAG, e.localizedMessage)
        }

        return LocationAddress(countryCode, addressText, addressId)
    }

    private fun placeMarkerOnMap(location: LatLng, icon: Int = -1, titleStr: String = "") {
        val  markerOptions = MarkerOptions().position(location)

        var titleString: String = titleStr
        if (titleStr.equals("")) {
           titleString = getAddress(location).formattedAddress
        }
        markerOptions.title(titleString)
        Log.i(TAG, titleString)
        Log.i(TAG, "message")
        if (icon != -1) {
            markerOptions.icon(
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(resources, icon)
                )
            )
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location,
            ZOOM_STREETS
        ))
        map.addMarker(markerOptions)
    }

    private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()

        try {
            startActivityForResult(builder.build(this@MapsActivity),
                PLACE_PICKER_REQUEST
            )
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }


    private fun  startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val  task = client.checkLocationSettings(builder.build())

        task.addOnFailureListener {
            e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e(TAG, sendEx.message)
                }
            }
        }
    }

    private fun getPriceFromDistance(distance: Float): Float {
        val wieght = distance / 1000F
        if (wieght < 1) return 1000F
        else return 1000F + (wieght - 1) * 100
    }

    fun sendMessage(veiw: View) {
        mapViewModel.onStartTravel(
            Location(lastDestination.longitude, lastDestination.latitude), lastDAddress,
            lastLocation, lastAddress,
            distance.toDouble(), durationLong, duration
        )

        mapViewModel.travelInsertComplete.observe(this,  androidx.lifecycle.Observer {
            complete ->
                if (complete) {
                    val intent = Intent(this, DetailsActivity::class.java).apply {
                        putExtra(EXTRA_MESSAGE, mapViewModel.travelId)
                    }
                    startActivity(intent)
                    mapViewModel.onNavigationComplete()
                }
        })

    }
}
