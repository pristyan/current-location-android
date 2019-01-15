package com.cocoba.currentlocation

import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.location.*

/**
 * Created by Chandra on 10/01/19.
 * Need some help?
 * Contact me at y.pristyan.chandra@gmail.com
 */

/**
 * 1. Get live location first
 * 2. Result -> fail or null ? get last known location
 * */

abstract class BaseLocationActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var handler: Handler
    private lateinit var callback: MyLocationCallback

    companion object {
        private const val TAG = "BaseLocationActivity"
    }

    private val locationRequest: LocationRequest
        get() = LocationRequest.create().apply {
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = NUM_UPDATES
            maxWaitTime = MAX_WAIT_TIME
            setExpirationDuration(MAX_WAIT_TIME)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        handler = Handler()
    }

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
    }

    private var runnable = Runnable {
        /** this function will be called if location updates get timeout (> MAX_WAIT_TIME). */
        getLastLocation(callback)
    }

    private fun initLocationCallback(myCallback: MyLocationCallback) {
        Log.i(TAG, "initLocationCallback")
        callback = myCallback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                Log.i(TAG, "initLocationCallback -> onLocationResult")

                /** if 1st try = fail (result = null), try the 2nd way, get from last known location */
                if (result == null) {
                    getLastLocation(callback)
                    return
                }

                try {
                    callback.onLocationReceive(result.locations[0])
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.onLocationError(e.message)
                } finally {
                    stopLocationUpdates()
                }
            }
        }
    }

    /** 1st try -> get from location updates */
    fun getCurrentLocation(myCallback: MyLocationCallback) {
        try {
            Log.i(TAG, "startLocationUpdates")
            initLocationCallback(myCallback)
            val task = fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            task.addOnSuccessListener {
                Log.i(TAG, "startLocationUpdates -> addOnSuccessListener")
            }.addOnFailureListener {
                Log.i(TAG, "startLocationUpdates -> addOnFailureListener")
                it.printStackTrace()
            }.addOnCompleteListener {
                Log.i(TAG, "startLocationUpdates -> addOnCompleteListener")
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } finally {
            handler.postDelayed(runnable, MAX_WAIT_TIME)
        }
    }

    private fun stopLocationUpdates() {
        Log.i(TAG, "stopLocationUpdates")
        handler.removeCallbacks(runnable)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun getLastLocation(callback: MyLocationCallback) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener {
                    callback.onLocationReceive(it)
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    callback.onLocationError(it.message)
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
            callback.onLocationError(e.message)
        } finally {
            stopLocationUpdates()
        }
    }
}

/* Location request parameter */
const val UPDATE_INTERVAL: Long = 3000
const val FASTEST_INTERVAL: Long = 1500
const val MAX_WAIT_TIME: Long = 5000
const val NUM_UPDATES: Int = 1

interface MyLocationCallback {
    fun onLocationReceive(location: Location)
    fun onLocationError(message: String?)
}