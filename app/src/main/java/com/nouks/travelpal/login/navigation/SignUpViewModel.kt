package com.nouks.travelpal.login.navigation

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nouks.travelpal.R
import com.nouks.travelpal.api.travelpal.dto.RegisterUserRequest
import com.nouks.travelpal.api.travelpal.dto.RegisterUserResponse
import com.nouks.travelpal.api.travelpal.dto.TokenResponseDTO
import com.nouks.travelpal.database.TravelDatabaseDao
import com.nouks.travelpal.database.entities.User
import com.nouks.travelpal.maps.AppStates
import kotlinx.coroutines.*
import liuuu.laurence.maputility.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpViewModel (
    val database: TravelDatabaseDao,
    application: Application,
    val intent: Intent?
): AndroidViewModel(application) {

    private val _signUpForm = MutableLiveData<SignUpFormState>()
    val signUpFormState: LiveData<SignUpFormState> = _signUpForm

    private val _signUpResult = MutableLiveData<LoginResult>()
    val signUpResult: LiveData<LoginResult> = _signUpResult

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _userUpdated = MutableLiveData<Boolean>()
    val userUpdated: LiveData<Boolean> = _userUpdated

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        _signUpForm.value = SignUpFormState(null, null, true)
        _userUpdated.value = false
        _currentUser.value = null
    }

    fun register(registerUserRequest: RegisterUserRequest,
                 clientAuth: String, progressLayout: View, context: Context?) {
        // call api for registration
        progressLayout.visibility = View.VISIBLE
        val autheader = HashMap<String, String>()
        autheader.put("X-Api-Auth", clientAuth.trim())
        val resultCall = RetrofitClient.travelpalMethods().registerUser(
            autheader, registerUserRequest
        )

        resultCall.enqueue(object : Callback<RegisterUserResponse> {
            override fun onResponse(
                call: Call<RegisterUserResponse>,
                response: Response<RegisterUserResponse>
            ) {
                if (response.isSuccessful) {
                    val userResponse = response.body()!!
                    Log.i("SignUpViewModel", userResponse.toString())
                    uiScope.launch {
                        storeNewUser(userResponse)
                    }
                    _signUpResult.value = LoginResult(1)
                } else {
                    Log.i("SignUpViewModel", response.message())
                    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
                }


                progressLayout.visibility = View.GONE
            }

            override fun onFailure(call: Call<RegisterUserResponse>, t: Throwable) {
                Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show()
                Log.i("SignUpViewModel", t.toString())
                progressLayout.visibility = View.GONE
            }
        })

        /*if (result is ) {
            _signUpResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _signUpResult.value = LoginResult(error = R.string.login_failed)
        }*/
    }

    fun registerAppInstance(clientAuth: String, progressLayout: View, context: Context?) {
        // call api for registration
        progressLayout.visibility = View.VISIBLE
        val autheader = HashMap<String, String>()
        autheader.put("X-Api-Auth", clientAuth.trim())
        val resultCall = RetrofitClient.travelpalMethods().registerAppInstance(
            autheader
        )

        resultCall.enqueue(object : Callback<RegisterUserResponse> {
            override fun onResponse(
                call: Call<RegisterUserResponse>,
                response: Response<RegisterUserResponse>
            ) {
                if (response.isSuccessful) {
                    val userResponse = response.body()!!
                    Log.i("SignUpViewModel", userResponse.toString())
                    uiScope.launch {
                        _userUpdated.value = storeNewUser(userResponse)
                        _signUpResult.value = LoginResult(1)
                    }
                } else {
                    Log.i("SignUpViewModel", response.message())
                    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
                }

                progressLayout.visibility = View.GONE
            }

            override fun onFailure(call: Call<RegisterUserResponse>, t: Throwable) {
                Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show()
                Log.i("SignUpViewModel", t.toString())
                progressLayout.visibility = View.GONE
            }
        })

        /*if (result is ) {
            _signUpResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _signUpResult.value = LoginResult(error = R.string.login_failed)
        }*/
    }

    /*fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _signUpForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _signUpForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _signUpForm.value = LoginFormState(isDataValid = true)
        }
    }*/

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
    private suspend fun storeNewUser(userResponse: RegisterUserResponse): Boolean {
        var roles = ""
        userResponse.roles.forEach {
                role -> roles += "$role," }

        return withContext(Dispatchers.IO) {
            val user: User? = database.getCurrentUser()
            if (user == null) {
                database.insertUser(User(
                    userResponse.userId,
                    userResponse.phone ?: "",
                    userResponse.email ?: "",
                    roles,
                    null,
                    0L,
                    null
                ))
            } else {
                user.email = userResponse.email ?: ""
                user.phoneNumber = userResponse.email ?: ""
                database.updateUser(user)
            }
            true
        }
    }

    private suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            database.getCurrentUser()
        }
    }

    fun refreshCurrentUser() {
        uiScope.launch {
            _currentUser.value = getCurrentUser()
        }
    }


}