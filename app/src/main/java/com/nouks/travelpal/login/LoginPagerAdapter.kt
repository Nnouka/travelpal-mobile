package com.nouks.travelpal.login

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.nouks.travelpal.login.navigation.LoginFragment
import com.nouks.travelpal.login.navigation.SignupFragment

class LoginPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> LoginFragment()
            else -> SignupFragment()
        }
    }


    override fun getCount(): Int = 2

}