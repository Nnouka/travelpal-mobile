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
import com.nouks.travelpal.api.travelpal.dto.RegisterUserRequest
import com.nouks.travelpal.database.TravelDatabase
import com.nouks.travelpal.database.TravelDatabaseDao
import com.nouks.travelpal.databinding.FragmentSignupBinding
import com.nouks.travelpal.login.LoginActivity
import com.nouks.travelpal.maps.EXTRA_MESSAGE

/**
 * A simple [Fragment] subclass.
 */
class SignupFragment : Fragment() {

    lateinit var signUpViewModel: SignUpViewModel
    lateinit var authService: AuthService
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
        Log.i("SignupFragment", "Signup Created")
        val signUpButton = binding.buttonSignUp

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
        signUpButton.setOnClickListener {
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
        signUpViewModel.signUpResult.observe(viewLifecycleOwner, Observer {
            result -> if (result.success != null) {
                    Toast.makeText(context, "Registration successful, please login", Toast.LENGTH_LONG).show()
                    promptSignin()
                }
        })
        return binding.root
    }

    fun promptSignin() {

        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, 1)
        }
        startActivity(intent)
    }


}
