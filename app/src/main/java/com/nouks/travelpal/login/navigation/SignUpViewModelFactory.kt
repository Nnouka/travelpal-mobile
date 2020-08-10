package com.nouks.travelpal.login.navigation

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nouks.travelpal.database.TravelDatabaseDao

class SignUpViewModelFactory (
    private val dataSource: TravelDatabaseDao,
    private val application: Application,
    private val intent: Intent? = null
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            return SignUpViewModel(dataSource, application, intent) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}