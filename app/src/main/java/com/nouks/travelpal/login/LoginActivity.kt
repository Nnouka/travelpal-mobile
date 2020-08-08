package com.nouks.travelpal.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.nouks.travelpal.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val sigInTab: TabItem? = findViewById(R.id.sign_in_tab)
        val sigUpTab: TabItem? = findViewById(R.id.sign_up_tab)
        val loginPager: ViewPager? = findViewById(R.id.login_pager)
        val tabLayout: TabLayout? = findViewById(R.id.tabLayout)
        val pagerAdapter: LoginPagerAdapter = LoginPagerAdapter(supportFragmentManager)
        loginPager!!.adapter = pagerAdapter

        Log.i("LoginActivity", "View Created")

        loginPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                loginPager.currentItem = p0!!.position
            }
        })
    }
}
