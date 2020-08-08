package com.nouks.travelpal.login.navigation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nouks.travelpal.database.TravelDatabaseDao

class SignUpViewModel (
    val database: TravelDatabaseDao,
    application: Application
): AndroidViewModel(application) {

}