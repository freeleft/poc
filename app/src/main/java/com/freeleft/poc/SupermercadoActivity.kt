package com.freeleft.poc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class SupermercadoActivity : AppCompatActivity() {

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    protected var mLastLocation: Location? = null
    private var mLatitudeLabel: String? = null
    private var mLongitudeLabel: String? = null
    private var mLatitudeText: TextView? = null
    private var mLongitudeText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supermercado)

        mLatitudeLabel = resources.getString(R.string.latitude_label)
        mLongitudeLabel = resources.getString(R.string.longitude_label)
        mLatitudeText = findViewById<View>(R.id.latitude_text) as TextView
        mLongitudeText = findViewById<View>(R.id.longitude_text) as TextView

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    public override fun onStart() {
        super.onStart()

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationClient!!.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    mLastLocation = task.result

                    mLatitudeText!!.setText(
                        mLatitudeLabel+":   "+
                                (mLastLocation )!!.latitude)
                    mLongitudeText!!.setText(mLongitudeLabel+":   "+
                            (mLastLocation )!!.longitude)
                } else {
                    Log.w(TAG, "getLastLocation:exception", task.exception)
                    showMessage(getString(R.string.no_location_detected))
                }
            }
    }

    private fun showMessage(text: String) {
        val container = findViewById<View>(R.id.main_activity_container)
        if (container != null) {
            Toast.makeText(this@SupermercadoActivity, text, Toast.LENGTH_LONG).show()
        }
    }

    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int,
                             listener: View.OnClickListener) {
        Toast.makeText(this@SupermercadoActivity, getString(mainTextStringId), Toast.LENGTH_LONG).show()
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this@SupermercadoActivity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE)
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                View.OnClickListener {
                    // Request permission
                    startLocationPermissionRequest()
                })

        } else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                    View.OnClickListener {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    })
            }
        }
    }

    companion object {
        private val TAG = "LocationProvider"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
}
