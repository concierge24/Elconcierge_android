package com.trava.utilities.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.trava.utilities.LocaleManager
import com.trava.utilities.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE


/**
created by Rohit Sharma on 1/08/18
 */

class LocationProvider private constructor(private val activity: Activity?, private val fragment: Any?, private val locationRequest: LocationRequest?) {

    private val TAG = LocationProvider::class.java.simpleName

    private val REQUEST_PERMISSIONS_CURRENT_LOCATION = 1818

    private val REQUEST_PERMISSIONS_LOCATION_UPDATES = 2818

    private val REQUEST_CHECK_SETTINGS_CURRENT_LOCATION = 3818

    private val REQUEST_CHECK_SETTINGS_LOCATION_UPDATES = 4818

    private var locationUpdatesCallback: LocationCallback? = null

    private var currentLocationListener: OnSuccessListener<Location>? = null

    private var fusedLocationClient = activity?.let { LocationServices.getFusedLocationProviderClient(it) }

    private var isSettingsSatisfied = false

    private var mRequestingLocationUpdates: Boolean = false

    /** Builder to get the current location.
     *  Use get lastKnownLocation(OnSuccessListener<Location>) to get current location. */
    class CurrentLocationBuilder(val activity: Activity?, var fragment: Any? = null) {

        fun build(): LocationProvider {
            return LocationProvider(activity, fragment, LocationRequest())
        }
    }

    /** Builder to get continuous location updates.
     * Use startLocationUpdates(LocationCallback) to get continuous location updates*/
    class LocationUpdatesBuilder(val activity: Activity?, var fragment: Any? = null) {
        var interval = 10000L
        var fastestInterval = 5000L
        var priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        fun build(): LocationProvider {
            val locationRequest = LocationRequest().apply {
                interval = this@LocationUpdatesBuilder.interval
                fastestInterval = this@LocationUpdatesBuilder.fastestInterval
                priority = this@LocationUpdatesBuilder.priority

            }
            return LocationProvider(activity, fragment, locationRequest)
        }
    }

    /** Get current location*/
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(listener: OnSuccessListener<Location>?) {
        currentLocationListener = listener
        if (!checkPermissions()) {
            requestPermission(REQUEST_PERMISSIONS_CURRENT_LOCATION)
            return
        }
        val builder = locationRequest?.let { LocationSettingsRequest.Builder().addLocationRequest(it) }
        val client: SettingsClient? = activity?.let { LocationServices.getSettingsClient(it) }
        val task: Task<LocationSettingsResponse>? = client?.checkLocationSettings(builder?.build())
        task?.addOnSuccessListener {
            isSettingsSatisfied = true
            listener?.let { fusedLocationClient?.lastLocation?.addOnSuccessListener(it) }
        }

        task?.addOnFailureListener { exception ->
            isSettingsSatisfied = false
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS_CURRENT_LOCATION)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

    }

    /** Get continuous location updates */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(locationCallback: LocationCallback?) {
        if (locationCallback != null) {
            this.locationUpdatesCallback = locationCallback
            if (!checkPermissions()) {
                requestPermission(REQUEST_PERMISSIONS_LOCATION_UPDATES)
                return
            }
            val builder = locationRequest?.let { LocationSettingsRequest.Builder().addLocationRequest(it) }
            val client: SettingsClient? = activity?.let { LocationServices.getSettingsClient(it) }
            val task: Task<LocationSettingsResponse>? = client?.checkLocationSettings(builder?.build())
            task?.addOnSuccessListener {
                isSettingsSatisfied = true
                fusedLocationClient?.requestLocationUpdates(locationRequest, locationUpdatesCallback,
                        null /* Looper */)
            }

            task?.addOnFailureListener { exception ->
                isSettingsSatisfied = false
                if (exception is ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS_LOCATION_UPDATES)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
        }
    }

    /** Stop continuous location updates.
     *  Must call this method after you are done with the location updates*/
    fun stopLocationUpdates(locationCallback: LocationCallback?) {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    private fun checkPermissions() = activity?.let { ActivityCompat.checkSelfPermission(it, ACCESS_FINE_LOCATION) } == PERMISSION_GRANTED

    private fun requestPermission(requestCode: Int) {
        if (activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, ACCESS_FINE_LOCATION) } == true) {
            // Provide an additional rationale to the user. This would happen if the user denied the
            // request previously, but didn't check the "Don't ask again" checkbox.
            showSnackBar(R.string.location_permisssion, android.R.string.ok, View.OnClickListener {
                // Request permission
                askPermissionAccordingToUiComponent(requestCode)
            })
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
//            activity?.let { ActivityCompat.requestPermissions(it, arrayOf(ACCESS_FINE_LOCATION), requestCode) }
            askPermissionAccordingToUiComponent(requestCode)
        }
    }

    private fun askPermissionAccordingToUiComponent(requestCode: Int) {
        if (fragment != null) {
            if (fragment is Fragment) {
                /* You have to override onRequestPermission in activity in this case and call onRequestPermission of this class*/
                activity?.let { ActivityCompat.requestPermissions(it, arrayOf(ACCESS_FINE_LOCATION), requestCode) }
            } else if (fragment is Fragment) {
                /*you hve to override onRequestPermission in fragment and call onRequestPermission of this class*/
                fragment.requestPermissions(arrayOf(ACCESS_FINE_LOCATION), requestCode)
            }
        } else {
            /*you have to override onRequestPermission in activity and call onRequestPermission of this class*/
            activity?.let { ActivityCompat.requestPermissions(it, arrayOf(ACCESS_FINE_LOCATION), requestCode) }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_CURRENT_LOCATION || requestCode == REQUEST_PERMISSIONS_LOCATION_UPDATES) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
            // Permission granted.
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> {
                    if (requestCode == REQUEST_PERMISSIONS_CURRENT_LOCATION) {
                        getLastKnownLocation(currentLocationListener)
                    } else if (requestCode == REQUEST_PERMISSIONS_LOCATION_UPDATES) {
                        startLocationUpdates(locationUpdatesCallback)
                    }
                }

            // Permission denied.

            // Notify the user via a SnackBar that they have rejected a core permission for the
            // app, which makes the Activity useless. In a real app, core permissions would
            // typically be best requested during a welcome-screen flow.

            // Additionally, it is important to remember that a permission might have been
            // rejected without asking the user for permission (device policy or "Never ask
            // again" prompts). Therefore, a user interface affordance is typically implemented
            // when permissions are denied. Otherwise, your app could appear unresponsive to
            // touches or interactions which have required permissions.
                else -> {
                    showSnackBar(R.string.permission_denied_explanation, R.string.settings,
                            View.OnClickListener {
                                // Build intent that displays the App settings screen.
                                val intent = Intent().apply {
                                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    data = Uri.fromParts("package", activity?.packageName, null)
//                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                activity?.startActivity(intent)
                            })
                }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
        // Check for the integer request code originally supplied to startResolutionForResult().
            REQUEST_CHECK_SETTINGS_CURRENT_LOCATION -> when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.i(TAG, "User agreed to make required location settings changes.")
                    getLastKnownLocation(currentLocationListener)
                }
                Activity.RESULT_CANCELED -> {
                    Log.i(TAG, "User choose not to make required location settings changes.")
                    //getLastKnownLocation(currentLocationListener)
                }
            }

            REQUEST_CHECK_SETTINGS_LOCATION_UPDATES ->
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Log.i(TAG, "User agreed to make required location settings changes.")
                        startLocationUpdates(locationUpdatesCallback)
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.i(TAG, "User choose not to make required location settings changes.")
                        //startLocationUpdates(locationUpdatesCallback)
                    }
                }
        }
    }

    private fun showSnackBar(snackStrId: Int, actionStrId: Int = 0, listener: View.OnClickListener? = null) {
        if (activity != null) {
            val snackBar = Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(snackStrId),
                    LENGTH_INDEFINITE)
            if (actionStrId != 0 && listener != null) {
                snackBar.setAction(activity.getString(actionStrId), listener)
            }
            val snackBarView = snackBar.view
            val textView = snackBarView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
            textView.maxLines = 3
            snackBarView.setBackgroundColor(Color.parseColor("#FF394956"))
            snackBar.setActionTextColor(Color.parseColor("#4597d6"))
            snackBar.show()
        }
    }

    fun getAddressFromLatLng(latitude: Double, longitude: Double, listener: OnAddressListener?) {
        val geocoder: Geocoder = Geocoder(activity, LocaleManager.getLocale(activity?.resources))
        Thread(Runnable {

            try {
                val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)

                // Here 1 represent max location result to returned, by documents it recommended 1 to 5

//            val address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//            val city = addresses[0].locality
//            val state = addresses[0].adminArea
//            val country = addresses[0].countryName
//            val postalCode = addresses[0].postalCode
//            val knownName = addresses[0].featureName
                val addressString = StringBuilder()
                if (addresses.isNotEmpty()) {
                    for (i in 0 until addresses[0].maxAddressLineIndex + 1) {
                        addressString.append(addresses[0].getAddressLine(i))
                        if (i != addresses[0].maxAddressLineIndex) {
                            addressString.append(",")
                        }
                    }
                }
                Handler(Looper.getMainLooper()).post {
                    listener?.getAddress(addressString.toString(), addresses)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }).start()

    }

    interface OnAddressListener {
        fun getAddress(address: String, result: List<Address>)
    }


}