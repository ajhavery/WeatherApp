package com.ajhavery.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class SplashScreen : AppCompatActivity() {

    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var myRequestCode = 1010;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    // 1. location permission -> denied
    // 2. location denied from settings
    // 3. GPS off
    // 4. Take location permission when app open
    // 5. Take location using client when device restarts

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (isLocationPermissionGranted()) { // check if permission is given
            if (isLocationEnabled()) { // check if location is enabled on phone
                mFusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    var location: Location? = task.result
                    if (location == null) { // e.g. if location play services crashed
                        newLocation()
                    } else {
                        // If Location is obtained, pass it to next screen
                        // postDelayed method executes a runnable after a delay time
                        // to pass data with Intent, we use - Intent.putExtra
                        Handler(Looper.getMainLooper()).postDelayed({
                            var intent = Intent(this, MainActivity::class.java)
                            intent.putExtra("lat", location.latitude.toString())
                            intent.putExtra("long", location.longitude.toString())
                            startActivity(intent)
                            finish() // to ensure back button doesn't bring to Splash screen
                        }, 2000)
                    }
                }
            } else { // if location is not enabled, ask to enable
                Toast.makeText(this, "Please turn on your location", Toast.LENGTH_LONG).show()
            }
        } else { // request permission if not given
            requestLocationPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun newLocation() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation: Location = p0.lastLocation
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                )
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            myRequestCode
        )
    }

    private fun isLocationPermissionGranted(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == myRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }
}