package com.example.android.productiville

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.android.productiville.databinding.ActivityMainBinding
import com.google.api.client.util.DateTime
import java.time.ZonedDateTime

class MainActivity : AppCompatActivity() {

    private val CLIENT_ID = "2"
    private val REDIRECT_URI = "https://productiville-9b19e.firebaseapp.com/__/auth/handler"
    private val CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar"

    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        navController = this.findNavController(R.id.nav_host_fragment)

        sharedPreferences = getPreferences(Context.MODE_PRIVATE)
    }

    override fun onStart() {
        super.onStart()

        val uri = intent.data

        if(checkRefresh() && sharedPreferences.getString("prevToken", "") != "") {
            val token = sharedPreferences.getString("prevToken", "")

            setToken(token!!)
        } else if (uri != null && uri.toString().startsWith(REDIRECT_URI)) {
            val token = uri.toString().substringAfter("access_token=").substringBefore("&token_type")

            val sharedPrefEditor = sharedPreferences.edit()
            sharedPrefEditor.putString("prevToken", token)
            sharedPrefEditor.putString("lastRefreshed", DateTime(System.currentTimeMillis()).toString())
            sharedPrefEditor.commit()

            setToken(token)
        } else {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://accounts.google.com/o/oauth2/v2/auth?client_id=${CLIENT_ID}&response_type=token&redirect_uri=${REDIRECT_URI}&scope=${CALENDAR_SCOPE}"))

            startActivity(intent)
        }
    }

    private fun checkRefresh(): Boolean {
        val currentTime = ZonedDateTime.parse(DateTime(System.currentTimeMillis()).toString())
        val nextRefresh =
            ZonedDateTime
            .parse(sharedPreferences.getString("lastRefreshed", DateTime(System.currentTimeMillis()).toString()))
                .plusHours(1)

        return currentTime < nextRefresh
    }

    private fun setToken(token: String) {
        val bundle = Bundle()
        bundle.putString("token", token)
        navController.setGraph(R.navigation.nav_graph, bundle)
    }
}