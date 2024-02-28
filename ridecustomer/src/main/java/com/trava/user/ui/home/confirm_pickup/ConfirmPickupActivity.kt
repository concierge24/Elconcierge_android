package com.trava.user.ui.home.confirm_pickup

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trava.user.R
import com.trava.user.databinding.ActivityConfirmPickupBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.homeapi.HomeDriver
import com.trava.user.webservices.models.homeapi.ServiceDetails
import com.trava.utilities.*
import com.trava.utilities.constants.ACCESS_TOKEN_KEY
import com.trava.utilities.constants.LANGUAGE_CODE
import com.trava.utilities.constants.PREF_MAP_VIEW
import com.trava.utilities.constants.SUCCESS_CODE
import com.trava.utilities.location.LocationProvider
import io.socket.client.Ack
import kotlinx.android.synthetic.main.activity_confirm_pickup.*
import kotlinx.android.synthetic.main.activity_confirm_pickup.cvToolbar
import kotlinx.android.synthetic.main.activity_confirm_pickup.fabMyLocation
import org.json.JSONObject
import java.util.*

class ConfirmPickupActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var lat = 0.0
    private var lng = 0.0
    private var fullAddress = ""
    private var type = 0
    private var cityName = ""
    private var cat_id = 0
    private var sub_cat_id = 0
    private var product_id = 0
    private var country = ""
    private var lastLoc: LatLng? = null
    private var mCenterLatLong: LatLng? = null
    private var mapFragment: SupportMapFragment? = null
    private var isMapLoaded = false
    lateinit var confirmPickupBinding: ActivityConfirmPickupBinding
    private var driver = arrayListOf<HomeDriver>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        confirmPickupBinding = DataBindingUtil.setContentView(this, R.layout.activity_confirm_pickup)
        confirmPickupBinding.color = ConfigPOJO.Companion

        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)
        ivBack.setOnClickListener { finish() }
        tvConfirmPickup.background = StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour, GradientDrawable.RECTANGLE)
        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
            cvToolbar.visibility = View.VISIBLE
            ivBack.visibility = View.GONE
            if (ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
                header_text.visibility = VISIBLE
                ll_pick.visibility = GONE
                tvConfirmPickup.setText(getString(R.string.conf_booking))
            }
        } else {
            cvToolbar.visibility = View.GONE
            ivBack.visibility = View.VISIBLE
        }

        if (intent.hasExtra("REQ_DATA")) {
            try {
                LocationProvider.CurrentLocationBuilder(this).build().getLastKnownLocation(OnSuccessListener {
                    currentLocation = it
                    if (it != null) {
                        lat = it.latitude
                        lng = it.longitude
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
            var serviceRequest = intent.getParcelableExtra<ServiceRequestModel>("REQ_DATA")
            cat_id = serviceRequest?.category_id ?: 0
            sub_cat_id = serviceRequest?.category_brand_id ?: 0
            product_id = serviceRequest?.category_brand_product_id ?: 0
        }

        if (intent.hasExtra("CREATE_RIDE")) {
            type = intent.getIntExtra("CREATE_RIDE", 0)
            ll_pick.visibility = View.GONE
            tvDesc.visibility = View.GONE
            tvSetPickupSpot.text = getString(R.string.set_pick_spto)
            tvConfirmPickup.text = getString(R.string.confirm_loc)
        }

        setupGoogleMap()
        setListener()
    }

    private fun getDrivers(catid: String) {
        val jsonObject = JSONObject()
        jsonObject.put("type", CommonEventTypes.HOME_MAP_DRIVERS)
        jsonObject.put("access_token", SharedPrefs.get().getString(ACCESS_TOKEN_KEY, ""))
        jsonObject.put("latitude", lat)
        jsonObject.put("longitude", lng)
        jsonObject.put("app_language_id", SharedPrefs.get().getString(LANGUAGE_CODE, ""))
        jsonObject.put("category_id", catid.toInt())
        if (ConfigPOJO.is_asap == "true") {
            jsonObject.put("distance", (ConfigPOJO.distance_search_start.toInt() + (ConfigPOJO.search_count.toInt() * ConfigPOJO.distance_search_increment.toInt())).toString())
        } else {
            jsonObject.put("distance", 500000)
        }
        AppSocket.get().emit(Events.COMMON_EVENT, jsonObject, Ack {
            Log.e("Drivers", it[0].toString() + "ddssdf" + jsonObject)
            Handler(Looper.getMainLooper()).post {
                val typeToken = object : TypeToken<ApiResponse<ServiceDetails>>() {}.type
                val response = Gson().fromJson(it[0].toString(), typeToken) as ApiResponse<ServiceDetails>
                if (response.statusCode == SUCCESS_CODE) {
                    val drivers = response.result?.drivers
                    if (drivers?.isEmpty() == false) {
                        driver?.addAll(drivers)
                    }
                }
            }
        })
    }

    private fun alertTochooseDriver() {
        val dialog = let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setContentView(R.layout.choose_driver_alert)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog?.findViewById<TextView>(R.id.tvTryAgain)?.background = StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour, GradientDrawable.RECTANGLE)
        dialog?.findViewById<TextView>(R.id.tvCancel)?.background = StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour, GradientDrawable.RECTANGLE)

        dialog?.findViewById<TextView>(R.id.tvCancel)?.setOnClickListener {
            exitSreen()
            dialog.dismiss()
        }

        var mainList = arrayListOf<HomeDriver>()
        for (i in driver.indices) {
            if (driver[i].category_id == cat_id && driver[i].category_brand_id == sub_cat_id && driver[i].category_brand_product_id == product_id) {
                mainList.add(driver[i])
            }
        }

        dialog?.findViewById<TextView>(R.id.tvTryAgain)?.setOnClickListener {
            startActivityForResult(Intent(this, SelectDriverActivity::class.java)
                    .putExtra("drivers", mainList),100)
            dialog.dismiss()
        }

        dialog?.show()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun startLocationUpdates() {
        val locationProvider = LocationProvider.LocationUpdatesBuilder(this)
                .apply {
                    interval = 1000
                    fastestInterval = 1000
                }.build()
        locationProvider.startLocationUpdates(object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                focusOnCurrentLocation(p0?.lastLocation?.latitude, p0?.lastLocation?.longitude)
                locationProvider.stopLocationUpdates(this)
                mapFragment?.getMapAsync(this@ConfirmPickupActivity)
            }
        })
    }

    private fun focusOnCurrentLocation(latitude: Double?, longitude: Double?) {
        val target = LatLng(latitude ?: 0.0, longitude ?: 0.0)
        val builder = CameraPosition.Builder()
        builder.zoom(14f)
        builder.target(target)
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()))
    }

    private fun setupGoogleMap() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    // handling google placing api callbacks
    private fun openPlacePickerIntent(requestCode: Int) {
        try {
            // Set the fields to specify which types of place data to return.
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)

            // Start the autocomplete intent
            var intent: Intent? = null
            if (ConfigPOJO.settingsResponse?.key_value?.is_countrycheck == "true") {

                var country_code = ""
                if (ConfigPOJO.settingsResponse?.key_value?.iso_code?.length == 2) {
                    country_code = ConfigPOJO.settingsResponse?.key_value?.iso_code?.toLowerCase()
                            ?: ""
                } else {
                    country_code = ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                            ?: 0)?.toLowerCase() ?: ""
                }

                intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .setCountry(country_code).build(this)
            } else {
                intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
            }

            startActivityForResult(intent, requestCode)
        } catch (e: GooglePlayServicesRepairableException) {
            Log.e("TAG", "Exception : " + e.message)
        } catch (e: GooglePlayServicesNotAvailableException) {
            Log.e("TAG", "Exception : " + e.message)
        } catch (e: Exception) {
            Log.e("TAG", "Exception : " + e.message)
        }
    }

    private var currentLocation: Location? = null
    private fun setListener() {

        fabMyLocation.setOnClickListener {
            LocationProvider.CurrentLocationBuilder(this).build().getLastKnownLocation(OnSuccessListener {
                currentLocation = it
                if (it != null) {
                    focusOnCurrentLocation(it.latitude, it.longitude)
                } else {
                    startLocationUpdates()
                }
            })
        }

        acPickupAddress.setOnClickListener {
            openPlacePickerIntent(Constants.PLACEAUTOCOMPLETE_REQUESTCODE)
        }

        ivBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        ivBack_snd.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        tvConfirmPickup.setOnClickListener {
            if (isMapLoaded) {
                if (Constants.SECRET_DB_KEY == "e03beef2da96672a58fddb43e7468881" || Constants.SECRET_DB_KEY == "0035690c91fbcffda0bb1df570e8cb98") {
                    alertTochooseDriver()
                } else {
                    exitSreen()
                }

            } else {
                Toast.makeText(this, getString(R.string.mapLoading), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exitSreen() {
        isMapLoaded = false
        val intent = Intent()
        intent.putExtra(Constants.LAT, lat)
        intent.putExtra(Constants.LNG, lng)
        intent.putExtra(Constants.ADDRESS, fullAddress)
        intent.putExtra("CREATE_RIDE", type)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onMapReady(map: GoogleMap) {
        this.mMap = map

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap.isMyLocationEnabled = true
        setMyLocationIcon()

        lat = intent.getDoubleExtra(Constants.LAT, 0.0)
        lng = intent.getDoubleExtra(Constants.LNG, 0.0)
        lastLoc = LatLng(lat ?: 0.0, lng ?: 0.0)

        mMap.setOnCameraIdleListener {
            if (mMap != null) {
                mMap.clear()
            }
            mCenterLatLong = mMap.cameraPosition.target
            lat = mCenterLatLong?.latitude ?: 0.0
            lng = mCenterLatLong?.longitude ?: 0.0
            fetchLocationAddress(lat, lng)
            getDrivers(cat_id.toString())
        }
        if (ConfigPOJO.is_darkMap == "true") {
            mMap?.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.dark_map_style))
        }
        val mapType = SharedPrefs.with(this).getInt(PREF_MAP_VIEW, GoogleMap.MAP_TYPE_NORMAL)
        if (mapType == GoogleMap.MAP_TYPE_SATELLITE)
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        else
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        mMap.setOnCameraMoveListener {
            if (mMap != null) {
                mMap.clear()
            }
            mCenterLatLong = mMap.cameraPosition.target
            lat = mCenterLatLong?.latitude ?: 0.0
            lng = mCenterLatLong?.longitude ?: 0.0
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLoc, 16f))
        ivMarker.show()
        map.setOnMapLoadedCallback {
            isMapLoaded = true
        }
    }

    private fun setMyLocationIcon() {
        val locationButton =
                (mapFragment?.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(
                        Integer.parseInt("2")
                )
        val rlp = locationButton.layoutParams as RelativeLayout.LayoutParams
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 300)
    }


    private fun fetchLocationAddress(lat: Double, lng: Double): Boolean {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>
            addresses = geocoder.getFromLocation(lat, lng, 1)
            if (addresses.isEmpty()) {
                return false
            }

            if (!addresses[0].getAddressLine(0).isNullOrEmpty()) {
                this.fullAddress = addresses[0].getAddressLine(0)
            }
            if (!addresses[0].countryName.isNullOrEmpty()) {
                this.country = addresses[0].countryName
            }

            if (!addresses[0].locality.isNullOrEmpty())
                cityName = addresses[0].locality
            else {
                if (!addresses[0].adminArea.isNullOrEmpty()) {
                    cityName = addresses[0].adminArea
                }
            }
            Log.e("address : ", "$fullAddress $cityName")
            tvPickupLocation.text = fullAddress
            acPickupAddress.text = fullAddress
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.PLACEAUTOCOMPLETE_REQUESTCODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data ?: Intent())
                    val address = place.address ?: ""
                    val placeLatLng = place.latLng
                    Log.e("TAG", "Place Lat : " + placeLatLng?.latitude + " Place Lon : " + placeLatLng?.longitude)

                    lat = placeLatLng?.latitude ?: 0.0
                    lng = placeLatLng?.longitude ?: 0.0
                    fullAddress = address
                    focusOnCurrentLocation(lat, lng)
                    acPickupAddress.text = address
                    tvPickupLocation.text = address

                    Log.e("TAG", "Place: " + place.address + ", " + place.latLng)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data ?: Intent())
                    Log.e("TAG", status.statusMessage ?: "")
                }
                Activity.RESULT_CANCELED -> // The user canceled the operation.
                    Toast.makeText(this, getString(R.string.requestCancelled), Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == 100 && resultCode == 101) {
            isMapLoaded = false
            val intent = Intent()
            intent.putExtra(Constants.LAT, lat)
            intent.putExtra(Constants.LNG, lng)
            intent.putExtra("driver_id", data?.getStringExtra("driver_id"))
            intent.putExtra("user_detail_id", data?.getStringExtra("user_detail_id"))
            intent.putExtra(Constants.ADDRESS, fullAddress)
            intent.putExtra("CREATE_RIDE", type)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}