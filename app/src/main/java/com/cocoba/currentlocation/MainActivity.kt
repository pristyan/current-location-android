package com.cocoba.currentlocation

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import com.cocoba.currentlocation.R.string.msg_permission_rationale
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseLocationActivity(), MyLocationCallback {

    private val currentTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_location_update?.setOnClickListener {
            if (isLocationGranted(1)) getCurrentLocation(this)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onLocationReceive(location: Location) {
        txt_result?.text = "$currentTime\nLat : ${location.latitude} | Long : ${location.longitude}"
    }

    override fun onLocationError(message: String?) {
        message?.let { alert(it) { positiveButton("OK") {} }.show() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handlePermissionResult(permissions, grantResults, object : PermissionListener {
            override fun onGranted() = getCurrentLocation(this@MainActivity)
            override fun onDenied() = Unit
            override fun onNeverAsk() = grantFromSetting(0, getString(msg_permission_rationale))

        })
    }
}
