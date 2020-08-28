package com.nouks.travelpal.login.navigation


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.nouks.travelpal.R
import com.nouks.travelpal.api.travelpal.AuthService
import com.nouks.travelpal.api.travelpal.dto.LoginDTO
import com.nouks.travelpal.database.TravelDatabase
import com.nouks.travelpal.databinding.FragmentLoginBinding
import com.nouks.travelpal.details.DetailsActivity
import com.nouks.travelpal.login.LoginActivity
import com.nouks.travelpal.maps.EXTRA_MESSAGE
import com.nouks.travelpal.maps.MapsActivity

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    lateinit var loginViewModel: LoginViewModel
    lateinit var signUpViewModel: SignUpViewModel
    lateinit var authService: AuthService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.i("LoginFragment", "Login Created")
        val binding = DataBindingUtil.inflate<FragmentLoginBinding>(
            inflater, R.layout.fragment_login, container, false
        )

        authService = AuthService()
        binding.setLifecycleOwner(this)
        val application = requireNotNull(activity).application
        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao
        val viewModelFactory = LoginViewModelFactory(dataSource, application)
        loginViewModel = ViewModelProviders.of(
            this, viewModelFactory
        ).get(LoginViewModel::class.java)

        val signUpViewModelFactory = SignUpViewModelFactory(dataSource,application)
        signUpViewModel = ViewModelProviders.of(
            this, signUpViewModelFactory
        ).get(SignUpViewModel::class.java)
        val loginButton = binding.login
        val skipLoginButton = binding.buttonSkipLogin
        val clientHeader = authService.generateClientAuthHeader(
            getString(R.string.client_id), getString(R.string.client_secret)
        )
        val email = binding.username
        val password = binding.password
        val progressLayout = binding.loading
        loginButton.isEnabled = true
        skipLoginButton.isEnabled = true
        loginButton.setOnClickListener {
            loginViewModel.loginFormState.observe(viewLifecycleOwner, Observer {
                state ->
                if (state.isDataValid) {
                    loginViewModel.login(
                        LoginDTO(
                            email.text.toString(),
                            password.text.toString()
                        ),
                        clientHeader,
                        progressLayout,
                        context
                    )
                }
            })
        }

        skipLoginButton.setOnClickListener{
           loginViewModel.currentUser.observe(viewLifecycleOwner, Observer {
               user ->
                if (user != null) {
                    loginViewModel.login(
                        LoginDTO(
                            email.text.toString(),
                            password.text.toString()
                        ),
                        clientHeader,
                        progressLayout,
                        context
                    )
                } else {
                    signUpViewModel.registerAppInstance(
                        clientHeader,
                        progressLayout,
                        context
                    )
                }
           })

            signUpViewModel.signUpResult.observe(viewLifecycleOwner, Observer {
                    result ->
                if (result.success != null) {
                    Toast.makeText(context, "Registration successful, Authenticating... Please wait...", Toast.LENGTH_LONG).show()
                    signUpViewModel.userUpdated.observe(viewLifecycleOwner, Observer {
                            updated ->
                        if (updated) {
                            signUpViewModel.refreshCurrentUser()
                            signUpViewModel.currentUser.observe(viewLifecycleOwner, Observer {
                                    user -> if (user != null) {
                                loginViewModel.login(
                                    LoginDTO(
                                        user.email,
                                        user.email
                                    ),
                                    clientHeader,
                                    progressLayout,
                                    context
                                )
                            }
                            })
                        }
                    })
                }
            })

            loginViewModel.loginResult.observe(viewLifecycleOwner, Observer {
                    result ->
                if (result.success != null) {
                    startMaps()
                }
            })
        }

        loginViewModel.loginResult.observe(viewLifecycleOwner, Observer {
            result ->
            if (result.success != null) {
                     startMaps()
                }
        })
        return binding.root
    }

    fun startMaps() {

        val intent = Intent(context, MapsActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, true)
        }
        startActivity(intent)
    }

    fun startDetails() {

        val intent = Intent(context, DetailsActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, 1)
        }
        startActivity(intent)
    }
}
