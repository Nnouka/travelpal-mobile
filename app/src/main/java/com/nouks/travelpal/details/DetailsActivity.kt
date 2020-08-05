package com.nouks.travelpal.details

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.nouks.travelpal.R
import com.nouks.travelpal.database.TravelDatabase
import com.nouks.travelpal.maps.EXTRA_MESSAGE
import com.nouks.travelpal.maps.MapViewModel
import com.nouks.travelpal.maps.MapViewModelFactory
import kotlinx.android.synthetic.main.activity_details.*

class DetailsActivity : AppCompatActivity() {
    private lateinit var detailsViewModel: DetailsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = getApplication()

        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao

        val viewModelFactory = DetailsViewModelFactory(dataSource, application)
        detailsViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(DetailsViewModel::class.java)
        setContentView(R.layout.activity_details)
        val message = intent.getLongExtra(EXTRA_MESSAGE, 0)
        details_text.text = message.toString()
        detailsViewModel.onPreviewReady(message)
        detailsViewModel.travel.observe(this, Observer {  travel ->
            travel?.let {
                details_text.text = travel.toString()
            }
        })
    }
}
