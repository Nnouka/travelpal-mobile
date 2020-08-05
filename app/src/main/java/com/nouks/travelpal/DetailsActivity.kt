package com.nouks.travelpal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nouks.travelpal.maps.EXTRA_MESSAGE
import kotlinx.android.synthetic.main.activity_details.*

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        val message = intent.getIntExtra(EXTRA_MESSAGE, 0)
        details_text.text = message.toString()
    }
}
