package com.nouks.travelpal.details

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.nouks.travelpal.R
import com.nouks.travelpal.api.travelpal.AuthService
import com.nouks.travelpal.api.travelpal.dto.TravelIntentRequest
import com.nouks.travelpal.database.TravelDatabase
import com.nouks.travelpal.login.LoginActivity
import com.nouks.travelpal.maps.EXTRA_MESSAGE
import com.nouks.travelpal.maps.MapsActivity
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_maps.*

class DetailsActivity : AppCompatActivity() {
    private lateinit var detailsViewModel: DetailsViewModel
    private lateinit var authService: AuthService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = getApplication()

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao

        val viewModelFactory = DetailsViewModelFactory(dataSource, application)
        detailsViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(DetailsViewModel::class.java)
        setContentView(R.layout.activity_details)
        authService = AuthService()
        val clientHeader = authService.generateClientAuthHeader(
            getString(R.string.client_id), getString(R.string.client_secret)
        )

        val message = intent.getLongExtra(EXTRA_MESSAGE, 0)
        detailsViewModel.onPreviewReady(message)
        detailsViewModel.travel.observe(this, Observer {  travel ->
            travel?.let {
                origin_details.text = travel.originAddress.formattedAddress
                destination_details.text = travel.destinationAddress.formattedAddress
                distance_duration_details.text = getString(R.string.duration_distance, travel.durationText, "${(travel.distance / 1000)} Km")
                price_details.text = getString(R.string.price_string, detailsViewModel.getPriceFromDistance(travel.distance.toFloat()).toString())

                detailsViewModel.currentUser.observe(this, Observer {
                    user -> user?.let {
                    book_button.setOnClickListener { view: View? ->
                        detailsViewModel.registerTravelIntent(
                            TravelIntentRequest(
                                travel.originAddress.formattedAddress,
                                travel.originLocation.lng,
                                travel.originLocation.lat,
                                travel.destinationAddress.formattedAddress,
                                travel.destinationLocation.lng,
                                travel.destinationLocation.lat,
                                travel.distance,
                                travel.duration,
                                travel.durationText
                            ),
                            clientHeader,
                            "Bearer ${user.token}",
                            loading_travel,
                            this
                        )
                    }
                }
                })
            }
        })
        detailsViewModel.intentRegistered.observe(this, Observer {
            registered ->
                if (registered) {
                    startMaps()
                }
        })
        detailsViewModel.unauthorized.observe(this, Observer {
            unauthorized -> if (unauthorized) {
                promptSignin(message)
            }
        })
    }

    fun startMaps() {

        val intent = Intent(this, MapsActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, true)
        }
        startActivity(intent)
    }

    fun promptSignin(travelId: Long) {

        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, travelId)
        }
        startActivity(intent)
    }
}
