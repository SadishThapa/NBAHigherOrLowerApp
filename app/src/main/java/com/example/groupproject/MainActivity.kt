package com.example.groupproject

/*
 Group members: Nicolas Diaz, Sadish Thapa, Alex Cruz, Bryan Yang
 Each member did their fair share of work (~1/4 of the project)
*/

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Looper
import androidx.activity.result.ActivityResultCallback
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class MainActivity : AppCompatActivity() {

    companion object {
        const val SETTINGS_REQUEST_CODE = 1
        var username: String = ""
        var userState: String = ""
    }

    private lateinit var play: Button
    private var selectedSeason = "2024-2025" // default

    private var mediaPlayer: MediaPlayer? = null
    private var playStartupSound = true // Will be connected to settings later

    private lateinit var usernameET: EditText //
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        play = findViewById(R.id.playButton)

        // ADDED: Local persistent storage
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        selectedSeason = prefs.getString("season", "2024-2025") ?: "2024-2025"
        playStartupSound = prefs.getBoolean("startUpSound", true)

        username = prefs.getString("username", "") ?: ""


        play.setOnClickListener { startGame() }

        // ADDED: used normal intent and updated in SettingsActivity instead of MainActivity
        // Thus, got rid of onActivityResult.
        val settingsButton: Button = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        usernameET = findViewById(R.id.username_input)

        // location
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        val permissionStatusFine: Int = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionStatusFine != PackageManager.PERMISSION_GRANTED) {
            // request permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            // permission has been granted
            getLocation()
        }
    }

    fun startGame() {
        MainActivity.username = usernameET.text.toString()


        if (username == "") {
            Toast.makeText(this, "username must be at least one char", Toast.LENGTH_SHORT).show()
        } else {
            // MainActivity.username = usernameET.text.toString()
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)

            prefs.edit().putString("username", MainActivity.username).apply()
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        playStartupSound = prefs.getBoolean("startUpSound", true)


        username = prefs.getString("username", "") ?: ""

        if (playStartupSound) {
            mediaPlayer = MediaPlayer.create(this, R.raw.startup)
            mediaPlayer?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }



    fun getLocation() {
        var listener = object: LocationListener {
            override fun onLocationChanged(location: Location) {
                var latitude: Double = location.latitude
                var longitude: Double = location.longitude
                Log.w("MainActivity", "point is (" + latitude + ", " + longitude + ")")
                latLongToState(latitude, longitude)
                locationManager.removeUpdates(this)
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
        }

        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, Looper.getMainLooper()
        )
    }

    fun latLongToState(latitude: Double, longitude: Double) {
        var geocoder: Geocoder = Geocoder(this, Locale.getDefault())
        try {
            var addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1) as List<Address>
            if (addresses.isNotEmpty()) {
                var state: String = addresses[0].adminArea
                Log.d("MainActivity", "state is " + state)
                MainActivity.userState = state
                Toast.makeText(this, "You are in the state: ${state}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            getLocation()
        } else {
            Log.w("MainActivity", "permission for location denied")
        }
    }
}
