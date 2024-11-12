package com.gigzz.android.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gigzz.android.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.util.Locale

class LocationHelper(
    val context: Context,
    private val pActivity: Activity,
    listener: LocationUpdateListener?,
) : MultiplePermissionsListener {

    private var RC_LOCATION_REQUEST = 1000
    private var RC_SETTINGS = 99

    private var locationUpdates: LocationUpdateListener? = null
    private var locationRequest: LocationRequest
    private lateinit var task: Task<LocationSettingsResponse>
    private var mFusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    init {
        locationUpdates = listener

        locationRequest = LocationRequest.create().apply {
            isWaitForAccurateLocation = true
            interval = 2000
            fastestInterval = 500
            priority = Priority.PRIORITY_HIGH_ACCURACY
            numUpdates = 2

        }
    }

    @SuppressLint("MissingPermission")
    fun setupGoogleMapScreenSettings(mMap: GoogleMap, canScrollMap: Boolean) {
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.isMyLocationEnabled = false
        mMap.isBuildingsEnabled = false
        mMap.isIndoorEnabled = false
        mMap.isTrafficEnabled = false

        mMap.uiSettings.apply {
            setAllGesturesEnabled(false)
            isMyLocationButtonEnabled = false
            isScrollGesturesEnabled = canScrollMap
            isZoomGesturesEnabled = canScrollMap
            isIndoorLevelPickerEnabled = false
            isZoomControlsEnabled = false
            isCompassEnabled = false
            isTiltGesturesEnabled = false
            isRotateGesturesEnabled = false
        }
    }

    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }


    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        if (checkLocationPermission()) {
            if (isLocationEnabled()) {
                try {
                    val cancellationToken = CancellationTokenSource()
                    mFusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationToken.token
                    )
                        .addOnSuccessListener { location: Location? ->
                            if (location != null) {
                                locationUpdates?.onNewLocation(location)
                            } else {
                                mFusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                                    loc?.let {
                                        locationUpdates?.onNewLocation(loc)
                                    }
                                }
                            }
                        }
                } catch (unlikely: SecurityException) {
                }
            } else {
                checkLocationSettings()
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun checkLocationSettings() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(context)
        task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { response ->
            getCurrentLocation()
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(pActivity, RC_LOCATION_REQUEST)
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }

    private fun requestPermissions() {
        Dexter.withContext(context)
            .withPermissions(permissions)
            .withListener(this)
            .withErrorListener {
                showToast(context, it.name)
            }
            .check()
    }

    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        report?.let {
            if (report.areAllPermissionsGranted() || report.grantedPermissionResponses?.size!! >= 1) {
                getCurrentLocation()
            } else {
                pActivity.displayNeverAskAgainDialog()
            }
        }
    }


    override fun onPermissionRationaleShouldBeShown(
        requestList: MutableList<PermissionRequest>?,
        token: PermissionToken?,
    ) {
        token?.continuePermissionRequest()
    }


    fun getAddress(context: Context, lat: Double, lng: Double): Address? {
        var address: Address? = null
        val geoCoder = Geocoder(context, Locale.getDefault())
        try {
            val addressList = geoCoder.getFromLocation(lat, lng, 1)
            if (addressList != null && addressList.size > 0) {
                address = addressList[0]
            }
            if (!isNetworkAvailable(context)) {
                showToast(
                    context,
                    context.resources.getString(R.string.no_internet)
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            showToast(context, context.resources.getString(R.string.no_internet))
        }
        return address
    }

    private fun Activity.displayNeverAskAgainDialog() {
        val alertDialog = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
            .setCancelable(false)
            .setMessage(getString(R.string.manual_permission_txt))
            .setPositiveButton(getString(R.string.permit_txt)) { dialog, _ ->
                dialog.dismiss()
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, RC_SETTINGS)
            }
            .create()
        alertDialog.show()
        val btn1 = alertDialog.findViewById<Button>(android.R.id.button1)
        val textView = alertDialog.findViewById<TextView>(android.R.id.message)
        if (textView != null) {
            textView.typeface = Typeface.createFromAsset(
                assets,
                "fonts/"
            )
            textView.setTextColor(ContextCompat.getColor(this, R.color.black))
            btn1?.typeface = Typeface.createFromAsset(
                assets,
                "fonts/"
            )
            btn1?.setTextColor(ContextCompat.getColor(this, R.color.theme_blue))
        }
    }

    interface LocationUpdateListener {
        fun onNewLocation(location: Location)
    }
}