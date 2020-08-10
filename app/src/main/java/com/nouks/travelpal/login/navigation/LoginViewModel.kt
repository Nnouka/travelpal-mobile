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
import com.nouks.travelpal.api.travelpal.dto.LoginDTO
import com.nouks.travelpal.api.travelpal.dto.RegisterUserRequest
import com.nouks.travelpal.api.travelpal.dto.RegisterUserResponse
import com.nouks.travelpal.api.travelpal.dto.TokenResponseDTO
import com.nouks.travelpal.database.TravelDatabaseDao
import com.nouks.travelpal.database.entities.User
import kotlinx.coroutines.*
import liuuu.laurence.maputility.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel (
    val database: TravelDatabaseDao,
    application: Application,
    val intent: Intent?
): AndroidViewModel(application) {

    private val _loginForm = MutableLiveData<SignUpFormState>()
    val loginFormState: LiveData<SignUpFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        _loginForm.value = SignUpFormState(null, null, true)
    }

    fun login(loginDTO: LoginDTO,
                 clientAuth: String, progressLayout: View, context: Context?) {
        // call api for registration
        progressLayout.visibility = View.VISIBLE
        val autheader = HashMap<String, String>()
        autheader.put("X-Api-Auth", clientAuth.trim())
        val resultCall = RetrofitClient.travelpalMethods().getUserToken(
            autheader, loginDTO
        )

        resultCall.enqueue(object : Callback<TokenResponseDTO> {
            override fun onResponse(
                call: Call<TokenResponseDTO>,
                response: Response<TokenResponseDTO>
            ) {
                if (response.isSuccessful) {
                    val tokenResponse = response.body()!!
                    uiScope.launch {
                        storeToken(tokenResponse)
                    }
                    Log.i("SignUpViewModel", tokenResponse.toString())
                    _loginResult.value = LoginResult(1)
                } else {
                    Log.i("SignUpViewModel", response.message())
                    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
                }
                progressLayout.visibility = View.GONE
            }

            override fun onFailure(call: Call<TokenResponseDTO>, t: Throwable) {
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

    private suspend fun storeToken(tokenResponse: TokenResponseDTO): Boolean {
        return withContext(Dispatchers.IO) {
            val user: User? = database.getCurrentUser()
            if (user == null) {
                database.insertUser(
                    User(
                        0L,
                        "",
                        "",
                        "USER",
                        tokenResponse.accessToken,
                        tokenResponse.expiresAt,
                        tokenResponse.refreshToken
                    )
                )
            } else {
                user.refreshToken = tokenResponse.refreshToken
                user.token = tokenResponse.accessToken
                user.tokenExpAt = tokenResponse.expiresAt
                database.updateUser(user)
            }

            true
        }
    }


}