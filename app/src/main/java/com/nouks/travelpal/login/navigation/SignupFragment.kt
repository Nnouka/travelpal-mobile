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
import com.nouks.travelpal.api.travelpal.dto.RegisterUserRequest
import com.nouks.travelpal.database.TravelDatabase
import com.nouks.travelpal.databinding.FragmentSignupBinding
import com.nouks.travelpal.login.LoginActivity
import com.nouks.travelpal.maps.EXTRA_MESSAGE
import com.nouks.travelpal.maps.MapsActivity

/**
 * A simple [Fragment] subclass.
 */
class SignupFragment : Fragment() {

    lateinit var signUpViewModel: SignUpViewModel
    lateinit var loginViewModel: LoginViewModel
    lateinit var authService: AuthService
    var anonymously = false;
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentSignupBinding>(
            inflater, R.layout.fragment_signup,
            container,
            false
        )
        authService = AuthService()
        binding.setLifecycleOwner(this)
        val application = requireNotNull(activity).application
        val dataSource = TravelDatabase.getInstance(application).travelDatabaseDao
        val viewModelFactory = SignUpViewModelFactory(dataSource,application)
        signUpViewModel = ViewModelProviders.of(
            this, viewModelFactory
        ).get(SignUpViewModel::class.java)
        val loginViewModelFactory = LoginViewModelFactory(dataSource, application)
        loginViewModel = ViewModelProviders.of(
            this, loginViewModelFactory
        ).get(LoginViewModel::class.java)
        val signUpButton = binding.buttonSignUp
        val anonymousButton = binding.buttonSkipSignup

        val clientHeader = authService.generateClientAuthHeader(
            getString(R.string.client_id), getString(R.string.client_secret)
        )
        val fullName = binding.fullName
        val email = binding.emailSignUp
        val phone = binding.phoneSignUp
        val password = binding.passwordSignUp
        val driver = binding.driverCheck
        val progressLayout = binding.loading
        signUpButton.isEnabled = true
        anonymousButton.isEnabled = true
        signUpButton.setOnClickListener {
            anonymously = false
            signUpViewModel.signUpFormState.observe(viewLifecycleOwner, Observer {
                    state ->
                if (state.isDataValid) {
                    signUpViewModel.register(
                        RegisterUserRequest(
                            fullName.text.toString(),
                            email.text.toString(),
                            phone.text.toString(),
                            password.text.toString(),
                            driver.isChecked
                        ),
                        clientHeader,
                        progressLayout,
                        context
                    )
                }
            })
        }

        anonymousButton.setOnClickListener {
            anonymously = true
            signUpViewModel.signUpFormState.observe(viewLifecycleOwner, Observer {
                    state ->
                if (state.isDataValid) {
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
                if (anonymously) {
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
                    anonymously = false
                } else {
                    Toast.makeText(context, "Registration successful, Please sign in", Toast.LENGTH_LONG).show()
                    anonymously = true
                    promptSignin()
                }
            }
            })

            loginViewModel.loginResult.observe(viewLifecycleOwner, Observer {
                    result ->
                if (result.success != null) {
                    startMaps()
                }
            })
        }
        return binding.root
    }

    fun promptSignin() {

        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, 1)
        }
        startActivity(intent)
    }

    fun startMaps() {

        val intent = Intent(context, MapsActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, true)
        }
        startActivity(intent)
    }


}
