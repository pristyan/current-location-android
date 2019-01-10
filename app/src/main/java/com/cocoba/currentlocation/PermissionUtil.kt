package com.cocoba.currentlocation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import org.jetbrains.anko.alert

/**
 * Created by Chandra on 10/01/19.
 * Need some help?
 * Contact me at y.pristyan.chandra@gmail.com
 */
fun Activity.isLocationGranted(requestCode: Int): Boolean = if (ContextCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    && ContextCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
    true
} else {
    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
        alert("Aplikasi ini membutuhkan akses lokasi anda. Izinkan sekarang?") {
            positiveButton("Ya") { requestLocationPermission(requestCode) }
            negativeButton("Tidak") {}
        }.show()
    } else requestLocationPermission(requestCode)
    false
}

fun Activity.requestLocationPermission(requestCode: Int) {
    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
}

fun Activity.grantFromSetting(requestCode: Int, message: String) {
    alert(message) {
        positiveButton("OK") {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivityForResult(intent, requestCode)
        }
    }.show()
}

fun Activity.handlePermissionResult(permissions: Array<out String>, grantResults: IntArray, permissionListener: PermissionListener) {
    if (grantResults.isNotEmpty()) {
        var isGrant = true
        for (grantResult in grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) isGrant = false
        }

        if (isGrant) permissionListener.onGranted()
        else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]))
            permissionListener.onNeverAsk()
    } else permissionListener.onDenied()
}

interface PermissionListener {
    fun onGranted()
    fun onDenied()
    fun onNeverAsk()
}