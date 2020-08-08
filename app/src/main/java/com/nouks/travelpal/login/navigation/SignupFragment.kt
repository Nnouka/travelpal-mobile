package com.nouks.travelpal.login.navigation


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.nouks.travelpal.R

/**
 * A simple [Fragment] subclass.
 */
class SignupFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.i("SignupFragment", "Signup Created")
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }


}
