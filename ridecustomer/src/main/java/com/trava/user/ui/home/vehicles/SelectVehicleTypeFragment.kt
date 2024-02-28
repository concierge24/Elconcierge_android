package com.trava.user.ui.home.vehicles


import android.animation.ValueAnimator
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Explode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trava.user.AppVersionConstants
import com.trava.user.R
import com.trava.user.databinding.FragmentSelectVehicleTypeBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.HomeActivity.Companion.polygonData
import com.trava.user.ui.home.comfirmbooking.ConfirmBookingFragment
import com.trava.user.ui.home.deliverystarts.DeliveryStartsFragment
import com.trava.user.ui.home.orderdetails.heavyloads.EnterOrderDetailsFragment
import com.trava.user.ui.home.processingrequest.ProcessingRequestFragment
import com.trava.user.ui.home.promocodes.PromoCodesActivity
import com.trava.user.ui.home.services.ServiceContract
import com.trava.user.ui.home.services.ServicePresenter
import com.trava.user.ui.home.submodels.SelectProductsFragment
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.GooglePoints
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.models.HomeMarkers
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.homeapi.*
import com.trava.user.webservices.models.order.Order
import com.trava.user.webservices.models.promocodes.CouponsItem
import com.trava.utilities.*
import com.trava.utilities.Constants.PROMO_AMMOUNT
import com.trava.utilities.Constants.PROMO_ID
import com.trava.utilities.Constants.SECRET_DB_KEY
import com.trava.utilities.constants.*
import com.trava.utilities.location.LocationProvider
import io.socket.client.Ack
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_confirm_booking.*
import kotlinx.android.synthetic.main.fragment_select_vehicle_type.*
import kotlinx.android.synthetic.main.fragment_select_vehicle_type.cvToolbar
import kotlinx.android.synthetic.main.fragment_select_vehicle_type.ivBack
import kotlinx.android.synthetic.main.fragment_select_vehicle_type.ivBack_snd
import kotlinx.android.synthetic.main.fragment_select_vehicle_type.ivProfile
import kotlinx.android.synthetic.main.fragment_select_vehicle_type.rootView
import kotlinx.android.synthetic.main.fragment_select_vehicle_type.rvCompanies
import kotlinx.android.synthetic.main.fragment_services.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class SelectVehicleTypeFragment : Fragment(), VehicleContract.View, ServiceContract.View, SelectVehicleTypeAdapter.OnInfocallBack, SelectVehicleTypeAdapter.OnCategorySelected {

    private var categoriesList: ArrayList<Category>? = ArrayList()

    private lateinit var serviceRequest: ServiceRequestModel

    private var adapter: SelectVehicleTypeAdapter? = null

    private var map: GoogleMap? = null

    private var presenter = VehiclePresenter()
    lateinit var selectVehicleTypeBinding: FragmentSelectVehicleTypeBinding

    private var homeActivity: HomeActivity? = null

    private var isCurrentLocation = false
    private var timerDrivers = Timer()

    private val servicePresenter = ServicePresenter()
    var serviceRequestModel = ServiceRequestModel()

    private var apiInProgress = false
    var isMover: Boolean = false
    val REQUEST_CODE = 11
    val RESULT_CODE = 12
    private var lat = 0.0

    private var lng = 0.0
    private var address = ""
    var isCorsa: Boolean = false

    private var markersList = ArrayList<HomeMarkers?>()

    private var currentZoomLevel = 14f

    private val ICON_WIDTH: Int = 60

    private val ICON_HEIGHT: Int = 90
    private var currentLocation: Location? = null

    private var listener: com.trava.user.ui.home.services.ServicesDetailInterface? = null

    private var dialog: BottomSheetDialog? = null

    fun setListener(listener: com.trava.user.ui.home.services.ServicesDetailInterface) {
        this.listener = listener
    }

    private var afterPromoAmount = 0.0

    private var promoData: CouponsItem? = null
    private var cateEstSearched:Boolean =  false
    private var estimation:String = ""
    private var categoryData: Category ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceRequest = (activity as HomeActivity).serviceRequestModel
        serviceRequest.category_brand_id = -1
        serviceRequest.category_brand_product_id = -1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enterTransition = Explode()
            exitTransition = Explode()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        selectVehicleTypeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_vehicle_type, container, false);
        selectVehicleTypeBinding.color = ConfigPOJO.Companion
        val view = selectVehicleTypeBinding.root

        if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
            isMover = true
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            selectVehicleTypeBinding.tvNextPromo.visibility = View.VISIBLE
            serviceRequest?.promoData=null
        }
        selectVehicleTypeBinding?.tvRemove.setOnClickListener {
            clearPromoCodesPref()
            serviceRequest?.promoData=null
            selectVehicleTypeBinding?.promoRl.visibility = View.GONE
            tvNextPromo.visibility=View.GONE
            tvNextPromo.setText(activity?.getString(R.string.applyPromoCode))
        }
        selectVehicleTypeBinding?.tvNextPromo.setOnClickListener {
            clearPromoCodesPref()
            startActivityForResult(Intent(activity, PromoCodesActivity::class.java)
                    .putExtra("amount",""+getInitialCharge(categoriesList?.get(adapter?.getSelectedPosition() ?: 0)?.products?.get(0)!!,activity as HomeActivity))
                    .putExtra("actual_value",""+categoriesList?.get(adapter?.getSelectedPosition() ?: 0)?.products?.get(0)?.actual_value), REQUEST_CODE)
        }

        return view
    }

    /*Ride initial charge*/
    fun getInitialCharge(product: Product, context: Context): Double {
        var distance_r = 0.0
        var time_r = 0.0
        if (ConfigPOJO.settingsResponse?.key_value?.is_distance == "true")//distance required
        {
            distance_r = (context as HomeActivity).serviceRequestModel.order_distance?.toDouble()?.times(product.price_per_distance
                    ?: 0.0f) ?: 0.0
        }
        if (ConfigPOJO.settingsResponse?.key_value?.is_time == "true")//Time required
        {
            time_r = (context as HomeActivity).serviceRequestModel.eta?.toDouble()?.times(product.price_per_hr
                    ?: 0.0f) ?: 0.0
        }

        var airport_charge = 0.0
        if (serviceRequest?.airportChargesPickup != "") {
            airport_charge = serviceRequest?.airportChargesPickup?.toDouble() ?: 0.0
        }
        if (serviceRequest?.airportChargesDrop != "") {
            airport_charge += serviceRequest?.airportChargesDrop?.toDouble() ?: 0.0
        }

        var scheduleCharge = 0.0
        if (ConfigPOJO.settingsResponse?.key_value?.schedule_charge == "true") {
            scheduleCharge =product.schedule_charge.toDouble()
        }

        //normal
        val totalPrice = distance_r.plus(time_r).plus(product.alpha_price ?: 0.0f).plus(airport_charge).plus(scheduleCharge)

        return totalPrice
    }

    private fun setCurrentPosition() {
        LocationProvider.CurrentLocationBuilder(activity).build().getLastKnownLocation(OnSuccessListener {
            currentLocation = it
            if (it != null) {
                focusOnCurrentLocation(it.latitude, it.longitude)
            } else {
                startLocationUpdates()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //  homeActivity?.getMapAsync()

        presenter.attachView(this)
        servicePresenter.attachView(this)

        homeActivity = activity as HomeActivity

        if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
            homeActivity?.getMapAsync()

            activity?.registerReceiver(networkBroadcastReceiver, IntentFilter("update"))
            if (homeActivity?.serviceRequestModel?.bookingType?.isNotEmpty() == true) {
                homeActivity?.serviceRequestModel?.bookingType = ""
                homeActivity?.serviceRequestModel?.bookingFriendName = ""
                homeActivity?.serviceRequestModel?.bookingFriendCountryCode = ""
                homeActivity?.serviceRequestModel?.bookingFriendPhoneNumber = ""
            }
            homeActivity?.serviceRequestModel?.stops?.clear()

        }

        if (serviceRequest.bookingFlow == "1") {
            if (SECRET_DB_KEY != "f068ea614d6e726084d6660cdfc4dc14242c4f98380b537e89446cd49b0022f7"&&
                    SECRET_DB_KEY != "f068ea614d6e726084d6660cdfc4dc14bb88ffcd9d48b45b6cd0b34d5a3554e5") {
                tvTitle.text = getString(R.string.parcel_type)
            }
        }

        if (SECRET_DB_KEY == "5025ff88429c4cb7b796cb3ee7128009"&&
            SECRET_DB_KEY == "5025ff88429c4cb7b796cb3ee7128009") {
            tvTitle.text = getString(R.string.select_vehicle_type)
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE)
        {
            tvNext.text=getString(R.string.next)
            tvTitle.text = getString(R.string.select_team)
        }
        if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
            ivBack.visibility = GONE
            ivProfile.visibility = VISIBLE
            ivProfile.setImageResource(R.drawable.ic_nav_drawer)
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
            cvToolbar.visibility = View.VISIBLE

            ivBack.visibility = View.GONE
        }
        selectVehicleTypeBinding.ivProfile.setOnClickListener {
            if (homeActivity?.serviceRequestModel?.pkgData != null) {
                activity?.onBackPressed()
            } else {
                activity?.drawer_layout?.openDrawer(GravityCompat.START)
            }
        }
        map = (activity as HomeActivity).googleMapHome

        if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
            setCurrentPosition()
        }

        categoriesList?.clear()
        hitOrderDistanceApi()

        tvNext.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        tvNextPromo.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

        val list = (activity as HomeActivity).serviceDetails?.categories as? ArrayList
        var driversList = (activity as HomeActivity).serviceRequestModel.driverList
        if (driversList.isEmpty()) {
            driversList = ((activity as HomeActivity).serviceDetails?.drivers as? ArrayList)
                    ?: ArrayList()
        }

        if (serviceRequest.pkgData != null) {
            val catBrands = serviceRequest.pkgData?.packageData?.pricingData?.categoryBrands
            for (item in list ?: ArrayList()) {
                for (brandItem in catBrands ?: ArrayList()) {
                    if (item.category_brand_id == brandItem.categoryBrandId) {
                        categoriesList?.add(item)
                    }
                }
            }
        } else {
            if (list?.isEmpty() == true) {
                val data = SharedPrefs.get().getObject("catData", ServiceDetails::class.java)
                data?.categories?.let { list.addAll(it) }
                categoriesList?.addAll(list)
            } else {
                list?.let { categoriesList?.addAll(it) }
            }
        }

        if (categoriesList?.isNotEmpty() == true) {

            /*Set zone based pricing*/
            if (polygonData.size > 0) {
                for (i in polygonData.indices) {
                    val array = JSONArray("["+polygonData[i].coordinates+"]")
                    if (array.length() > 0) {
                        val rectOptions = PolygonOptions()
                        for (j in 0 until array.length()) {
                            val obj = array.getJSONArray(j)
                            rectOptions.add(LatLng(obj[0].toString().toDouble(), obj[1].toString().toDouble()))
                        }
                        if (GooglePoints.pointInPolygon(LatLng(homeActivity?.serviceRequestModel?.pickup_latitude
                                        ?: 0.0,
                                        homeActivity?.serviceRequestModel?.pickup_longitude
                                                ?: 0.0), rectOptions.points)) {
                            if (polygonData[i].airport_charges == "yes") {
                                homeActivity?.serviceRequestModel?.airportChargesPickup = polygonData[i].airport_pickup_charge_fee
                            }
                        }
                        if (GooglePoints.pointInPolygon(LatLng(homeActivity?.serviceRequestModel?.dropoff_latitude
                                        ?: 0.0,
                                        homeActivity?.serviceRequestModel?.dropoff_longitude
                                                ?: 0.0), rectOptions.points)) {
                            if (polygonData[i].airport_charges == "yes") {
                                homeActivity?.serviceRequestModel?.airportChargesDrop = polygonData[i].airport_drop_charge_fee
                            }
                        }
                    }
                }
            }

            /*Set categories brands on adapter that's are fetching from home api*/
            for (subItem in categoriesList ?: ArrayList()) {
                for (item in driversList) {
                    if (item.category_brand_id == subItem.category_brand_id) {
                        subItem.driverList.add(item)
                    }
                }
            }

            /*Check Sur Charge feature is enabled from admin if yes
            * then we handled sur charge value for request*/
            if (ConfigPOJO.settingsResponse?.key_value?.surCharge == "true") {
                for (k in categoriesList?.indices!!) {
                    if (categoriesList?.get(k)?.products!!.isNotEmpty()) {
                        for (i in categoriesList?.get(k)?.products?.indices!!) {
                            if (categoriesList?.get(k)?.products?.get(i)!!.surChargeAdmin.size > 0) {
                                for (j in categoriesList?.get(k)?.products?.get(i)?.surChargeAdmin?.indices!!) {
                                    if (checkingTimeSlotAfter(categoriesList?.get(k)?.products?.get(i)!!.surChargeAdmin[j].start_time) &&
                                            checkingTimeSlotBefore(categoriesList?.get(k)?.products?.get(i)!!.surChargeAdmin[j].end_time)) {
                                        categoriesList?.get(k)?.products?.get(i)!!.surchargeValue = categoriesList?.get(k)?.products?.get(i)!!.surChargeAdmin[j].value.toInt()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val selectedPosition = getSelectedPosition(serviceRequest.category_brand_id ?: 0)
            rvCompanies?.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
            adapter = SelectVehicleTypeAdapter(categoriesList, selectedPosition, isMover, this@SelectVehicleTypeFragment, this@SelectVehicleTypeFragment)
            rvCompanies?.adapter = adapter
        }

        this.map?.uiSettings.let {
            it?.isZoomGesturesEnabled = true
            it?.setAllGesturesEnabled(true)
            it?.isScrollGesturesEnabled = true
        }

        setListeners()
    }

    fun onMapReady(map: GoogleMap?) {
        if (isVisible) {
            homeApiCall()
            isCurrentLocation = true
            fabMyLocation?.setImageResource(R.drawable.ic_my_location_blue)
            this.map = map
            this.map?.setOnCameraMoveStartedListener(onCameraMoveStarted)
            this.map?.setOnCameraMoveListener(onCameraMoveListener)
            this.map?.setOnCameraIdleListener(onCameraMoveIdle)
            val mapType = SharedPrefs.with(activity).getInt(PREF_MAP_VIEW, GoogleMap.MAP_TYPE_NORMAL)
            if (fabSatellite != null) {
                fabSatellite.setImageResource(if (mapType == GoogleMap.MAP_TYPE_HYBRID)
                    R.drawable.ic_satellite_blue else R.drawable.ic_satellite)

                tvMapNormal.isSelected = mapType == GoogleMap.MAP_TYPE_NORMAL
                tvSatellite.isSelected = mapType == GoogleMap.MAP_TYPE_HYBRID

            }

            updateDropOffAddress()
        }

        Log.e("", "")
    }

    private fun focusOnCurrentLocation(latitude: Double?, longitude: Double?) {
        val target = LatLng(latitude ?: 0.0, longitude ?: 0.0)
        val builder = CameraPosition.Builder()
        builder.zoom(14f)
        builder.target(target)
        map?.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()))
    }

    private fun startLocationUpdates() {
        val locationProvider = LocationProvider.LocationUpdatesBuilder(activity).apply {
            interval = 1000
            fastestInterval = 1000
        }.build()
        locationProvider.startLocationUpdates(object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                focusOnCurrentLocation(p0?.lastLocation?.latitude, p0?.lastLocation?.longitude)
                locationProvider.stopLocationUpdates(this)
                homeActivity?.getMapAsync()
            }

        })
    }

    private fun startDriverTimer(delay: Long) {
        timerDrivers.cancel()
        timerDrivers = Timer()
        timerDrivers.schedule(object : TimerTask() {
            override fun run() {
                getDrivers()
            }
        }, delay)
    }

    private fun getDrivers() {
        val jsonObject = JSONObject()
        jsonObject.put("type", CommonEventTypes.HOME_MAP_DRIVERS)
        jsonObject.put("access_token", SharedPrefs.get().getString(ACCESS_TOKEN_KEY, ""))
        jsonObject.put("latitude", lat)
        jsonObject.put("longitude", lng)
        jsonObject.put("category_id", homeActivity?.serviceRequestModel?.category_id)
        jsonObject.put("distance", 500000)
        jsonObject.put("app_language_id", SharedPrefs.get().getString(LANGUAGE_CODE, ""))
        Log.e("asasasaS",SharedPrefs.get().getString(LANGUAGE_CODE, ""))
        AppSocket.get().emit(Events.COMMON_EVENT, jsonObject, Ack {

            Logger.e("Drivers", it[0].toString())
            Handler(Looper.getMainLooper()).post {
                if (isVisible) {
                    val typeToken = object : TypeToken<ApiResponse<ServiceDetails>>() {}.type
                    val response = Gson().fromJson(it[0].toString(), typeToken) as ApiResponse<ServiceDetails>
                    if (response.statusCode == SUCCESS_CODE) {
                        val drivers = response.result?.drivers
                        if (drivers?.isEmpty() == false) {
                            homeActivity?.serviceRequestModel?.driverList?.addAll(drivers)
                            servicePresenter.getNearestRoadPoints(drivers)
                        } else {
                            setDriversOnMap(drivers)
                        }
                    }
                }
            }
        })
        startDriverTimer(10000)
    }

    override fun onPause() {
        super.onPause()
        if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
            timerDrivers.cancel()
            timerDrivers.purge()
            removeAllDriverMarkers()
        }
    }

    fun homeApiCall() {
        removeAllDriverMarkers()
        timerDrivers.cancel()
        val map = HashMap<String, String>()

        serviceRequestModel.category_id = 4
        //homeActivity?.serviceRequestModel?.category_id=categoryId
        map["category_id"] = serviceRequestModel.category_id.toString()

        map["latitude"] = lat.toString()
        map["longitude"] = lng.toString()
        map["distance"] = "50000"
        if (CheckNetworkConnection.isOnline(activity)) {
            apiInProgress = true
            servicePresenter.homeApi(map)
            timerDrivers.cancel()
        }
    }

    private val onCameraMoveListener = GoogleMap.OnCameraMoveListener {
        markersList.forEach {
            if (currentZoomLevel != map?.cameraPosition?.zoom) {
                currentZoomLevel = map?.cameraPosition?.zoom ?: 14f
                scaleDownMarker(it?.marker, (currentZoomLevel / 14f), it?.iconImageUrl ?: "")
            }
        }
    }

    private val onCameraMoveStarted = GoogleMap.OnCameraMoveStartedListener {
        tvDropOffLocation?.text = ""
        fabMyLocation?.setImageResource(R.drawable.ic_my_location)
        isCurrentLocation = false
    }

    private val onCameraMoveIdle = GoogleMap.OnCameraIdleListener {
        updateDropOffAddress()
        if (MapUtils.getDistanceBetweenTwoPoints(LatLng(map?.cameraPosition?.target?.latitude
                        ?: 0.0,
                        map?.cameraPosition?.target?.longitude
                                ?: 0.0), LatLng(currentLocation?.latitude
                        ?: 0.0, currentLocation?.longitude ?: 0.0)) < 1) {
            fabMyLocation?.setImageResource(R.drawable.ic_my_location_blue)
            isCurrentLocation = true
        } else {
            fabMyLocation?.setImageResource(R.drawable.ic_my_location)
            isCurrentLocation = false
        }

    }

    private fun hitOrderDistanceApi() {
        if (CheckNetworkConnection.isOnline(context)) {
            cateEstSearched = false
            presenter.getOrderDistance(LatLng(serviceRequest.pickup_latitude
                    ?: 0.0, serviceRequest.pickup_longitude ?: 0.0),
                    LatLng(serviceRequest.dropoff_latitude ?: 0.0, serviceRequest.dropoff_longitude
                            ?: 0.0), "en")
        }
    }

    private fun updateDropOffAddress() {
        lat = map?.cameraPosition?.target?.latitude ?: 0.0
        lng = map?.cameraPosition?.target?.longitude ?: 0.0
        address = ""
//        LocationProvider.CurrentLocationBuilder(activity).build().getAddressFromLatLng(lat, lng,addressListener)
        activity?.runOnUiThread {
            getAddressFromLatLng(lat, lng)
        }
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double) {
        val geocoder: Geocoder = Geocoder(activity, LocaleManager.getLocale(activity?.resources))
        Thread(Runnable {

            try {
                val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)

                // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                val addressString = StringBuilder()
                if (addresses.isNotEmpty()) {
                    SharedPrefs.get().save(Constants.COUNTRY_ISO, addresses[0].countryCode)
                    for (i in 0 until addresses[0].maxAddressLineIndex + 1) {
                        addressString.append(addresses[0].getAddressLine(i))
                        if (i != addresses[0].maxAddressLineIndex) {
                            addressString.append(",")
                        }
                    }
                }

                Handler(Looper.getMainLooper()).post {
                    this.address = addressString.toString()
                    tvDropOffLocation?.text = address
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }).start()

    }

    fun removeAllDriverMarkers() {
        try {
            if (map != null) {
                map?.clear()
                markersList.forEach {
                    it?.moveValueAnimate?.cancel()
                    it?.rotateValueAnimator?.cancel()
                }
                markersList.clear()
            }
        } catch (ex: Exception) {
            Log.e("remove  exception : ", ex.message ?: "")
        }

    }


    fun setDriversOnMap(drivers: List<HomeDriver>?) {
        val updatedMarkers = ArrayList<HomeMarkers?>()
        val tempMarkers = ArrayList<HomeMarkers?>()
        tempMarkers.addAll(markersList)
        drivers?.forEach { driver ->
            var isDriverFound = false
            tempMarkers.forEach { marker ->
                if (driver.user_detail_id == marker?.marker?.tag as? Int?) {
                    isDriverFound = true
                    markersList.remove(marker)
                    marker?.iconImageUrl = driver.icon_image_url
                    marker?.catrgory_brand_id = driver.category_brand_id
                    updatedMarkers.add(marker)
                    val distance = FloatArray(3)
                    Location.distanceBetween(marker?.marker?.position?.latitude
                            ?: 0.0, marker?.marker?.position?.longitude ?: 0.0,
                            driver.latitude ?: 0.0, driver.longitude
                            ?: 0.0, distance)
                    val bearing = MapUtils.bearingBetweenLocations(LatLng(marker?.marker?.position?.latitude
                            ?: 0.0, marker?.marker?.position?.longitude ?: 0.0),
                            LatLng(driver.latitude ?: 0.0, driver.longitude
                                    ?: 0.0))
                    if (distance[0] > 5.0) {
                        marker?.marker.let {
                            //                            animateMarker(it, LatLng(driver.latitude ?: 0.0, driver.longitude
//                                    ?: 0.0), false)
                            marker?.moveValueAnimate?.cancel()
                            marker?.moveValueAnimate = animateMarker(it, LatLng(driver.latitude
                                    ?: 0.0, driver.longitude
                                    ?: 0.0))
                            marker?.rotateValueAnimator?.cancel()
                            marker?.rotateValueAnimator = rotateMarker(it, LatLng(driver.latitude
                                    ?: 0.0, driver.longitude
                                    ?: 0.0), bearing)
                        }
//                        marker?.let { rotateMarker(it, driver.bearing ?: 0f) }
                    }
//                    marker.position = LatLng(driver.latitude?:0.0, driver.longitude?:0.0)
                }
            }
            if (!isDriverFound) {
                val driverMarker = createDriverMarker(driver, getVehicleResId(driver.category_id))
                driverMarker?.isFlat = true
                val homeMarkers = HomeMarkers()
                homeMarkers.marker = driverMarker
                updatedMarkers.add(homeMarkers)
            }
        }

        markersList.forEach {
            it?.marker?.remove()
        }
        markersList.clear()
        markersList.addAll(updatedMarkers)
    }

    private fun createDriverMarker(driver: HomeDriver?, iconResID: Int): Marker? {
        val marker = map?.addMarker(MarkerOptions()
                .position(LatLng(driver?.latitude ?: 0.0, driver?.longitude ?: 0.0))
                .anchor(0.5f, 0.5f)
                .rotation(driver?.bearing ?: 0f))
        scaleDownMarker(marker, (currentZoomLevel / 14f), driver?.icon_image_url ?: "")
        marker?.tag = driver?.user_detail_id
        return marker
    }

    private fun rotateMarker(marker: Marker?, latLng: LatLng?, bearing: Float?): ValueAnimator? {
        var valueAnimatorRotate: ValueAnimator? = null
        if (marker != null) {
            val startPosition = marker.position
            val endPosition = LatLng(latLng?.latitude ?: 0.0, latLng?.longitude ?: 0.0)

            val latLngInterpolator = LatLngInterpolator.LinearFixed()
//            valueAnimatorRotate?.cancel()
            val startRotation = marker.rotation
            valueAnimatorRotate = ValueAnimator.ofFloat(0f, 1f)
//            valueAnimatorRotate?.startDelay = 200
            valueAnimatorRotate?.duration = 5000 // duration 1 second
            valueAnimatorRotate?.interpolator = LinearInterpolator()
            valueAnimatorRotate?.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    try {
                        val v = animation.animatedFraction
//                        val newPosition = latLngInterpolator.interpolate(v, startPosition
//                                ?: LatLng(0.0, 0.0), endPosition)
//                        carMarker?.setPosition(newPosition)
                        marker.rotation = computeRotation(v, startRotation, bearing
                                ?: 0f)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            })
            valueAnimatorRotate?.start()

        }
        return valueAnimatorRotate
    }

    private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
        val normalizeEnd = end - start // rotate start to 0
        val normalizedEndAbs = (normalizeEnd + 360) % 360

        val direction = (if (normalizedEndAbs > 180) -1 else 1).toFloat() // -1 = anticlockwise, 1 = clockwise
        val rotation: Float
        if (direction > 0) {
            rotation = normalizedEndAbs
        } else {
            rotation = normalizedEndAbs - 360
        }

        val result = fraction * rotation + start
        return (result + 360) % 360
    }


    fun animateMarker(marker: Marker?, latLng: LatLng?): ValueAnimator? {
        var valueAnimatorMove: ValueAnimator? = null
        if (marker != null) {
            val startPosition = marker.position
            val endPosition = LatLng(latLng?.latitude ?: 0.0, latLng?.longitude ?: 0.0)
//            val startRotation = marker.rotation
            val latLngInterpolator = LatLngInterpolator.LinearFixed()
//            valueAnimatorMove?.cancel()
            valueAnimatorMove = ValueAnimator.ofFloat(0f, 1f)
            valueAnimatorMove?.duration = 10000 // duration 1 second
            valueAnimatorMove?.interpolator = LinearInterpolator()
            valueAnimatorMove?.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    try {
                        val v = animation.animatedFraction
                        val newPosition = latLngInterpolator.interpolate(v, startPosition
                                ?: LatLng(0.0, 0.0), endPosition)
                        marker.position = newPosition
//                        carMarker?.setRotation(computeRotation(v, startRotation?:0f, currentBearing))
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            })
            valueAnimatorMove?.start()
        }
        return valueAnimatorMove
    }

    private interface LatLngInterpolator {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng
        class LinearFixed : LatLngInterpolator {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (categoriesList?.isNotEmpty() == true) {
            categoriesList?.get(adapter?.getSelectedPosition() ?: 0)?.isSelected = false
        }
        presenter.detachView()
        servicePresenter.detachView()
        if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
            activity?.unregisterReceiver(networkBroadcastReceiver)
        }
    }

    private fun setListeners() {
        ivBack.setOnClickListener { activity?.onBackPressed() }
        ivBack_snd.setOnClickListener { activity?.onBackPressed() }
        tvNext.setOnClickListener {
            if (categoriesList?.isEmpty() == true) {
                rootView.showSnack(R.string.no_vehicles_available)
                return@setOnClickListener
            }

            serviceRequest.helper_percentage = adapter?.getSelectedCategoryHelperPercentageId()
            serviceRequest.category_brand_id = adapter?.getSelectedCategoryBrandId()
            serviceRequest.brandName = adapter?.getSelectedCategoryBrandName()
            serviceRequest.brandImage = adapter?.getSelectedCategoryBrandImage()

            if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
                adapter?.getSelectedPosition().let {
                    if (categoriesList?.get(it!!)?.products?.isNotEmpty() == true) {
                        serviceRequest.category_brand_product_id = categoriesList?.get(it!!)?.products!![0].category_brand_product_id
                        serviceRequest.productName = categoriesList?.get(it!!)?.products!![0].name
                        serviceRequest.final_charge = getFinalCharge(categoriesList?.get(it!!)?.products!![0])
                        serviceRequest.surChargeValue = categoriesList?.get(it!!)?.products!![0].surchargeValue.toDouble() ?: 0.0
                    }
                }

                val fragment = EnterOrderDetailsFragment()
                val bundle = Bundle()
                fragment.arguments = bundle
                fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
            } else {
                adapter?.getSelectedPosition().let {
                    if (categoriesList?.get(it!!)?.products?.isNotEmpty() == true) {
                        serviceRequest.category_brand_product_id = categoriesList?.get(it!!)?.products!![0].category_brand_product_id
                        serviceRequest.productName = categoriesList?.get(it!!)?.products!![0].name
                        serviceRequest.final_charge = getFinalCharge(categoriesList?.get(it!!)?.products!![0])
                        serviceRequest.surChargeValue = categoriesList?.get(it!!)?.products!![0].surchargeValue.toDouble() ?: 0.0
                    }
                }
                serviceRequest.images = ArrayList()
                serviceRequest.product_weight = 0F
                serviceRequest.material_details = ""
                serviceRequest.details = ""
                serviceRequest.future = ServiceRequestModel.BOOK_NOW

                openSelectProductFragment()
            }
        }
    }

    fun clearPromoCodesPref() {
        SharedPrefs.with(context).save(PROMO_ID, "p")
        SharedPrefs.with(context).save(PROMO_AMMOUNT, "a")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE || resultCode == Activity.RESULT_OK) {
            if (data != null) {
                this.promoData = data?.getParcelableExtra<CouponsItem>(Constants.PROMO_DATA)
                serviceRequest?.promoData=data.getParcelableExtra<CouponsItem>(Constants.PROMO_DATA)
                applyPromocode(promoData)
            }
            tvNextPromo.visibility=View.VISIBLE
            tvNextPromo.text = activity?.getString(R.string.applied)

        } else if (resultCode == Activity.RESULT_CANCELED) {
            openSelectProductFragment()
        }
    }

    private val products: ArrayList<Product?>? = ArrayList()

    private fun openSelectProductFragment() {
        val bundle = Bundle()
        if (serviceRequest.pkgData != null) {
            val list = serviceRequest.pkgData?.packageData?.pricingData?.categoryBrands
            for (item in list ?: ArrayList()) {
                if (serviceRequest.category_brand_id == item.categoryBrandId) {
                    for (adapterItem in adapter?.getSelectedCategoryProducts()
                            ?: ArrayList()) {
                        for (productItem in item.products) {
                            if (productItem.category_brand_product_id==adapterItem.category_brand_product_id)
                            {
                                productItem.name = adapterItem.name
                                productItem.image_url = adapterItem.image_url
                                productItem.price_per_hr=productItem.pricePerMin?.toFloat()?:0F
                                productItem.price_per_distance=productItem.pricePerKm?.toFloat()?:0F
                            }
                        }
                    }
                    products?.addAll(item.products)
                    bundle.putString("submodels", Gson().toJson(item.products))
                    break
                }
            }
        } else {
            products?.addAll(adapter?.getSelectedCategoryProducts()!!)
            var drivers = adapter?.getSelectedCategoryDrivers()

            bundle.putString("submodels", Gson().toJson(adapter?.getSelectedCategoryProducts()))
        }

        if (SECRET_DB_KEY=="322ba1dca88f587737f810d70789f0a4"||
                SECRET_DB_KEY=="23d6f777b154b9172eda1c76c8f5536f")
        {

            serviceRequest.bookingTypeTemp = serviceRequest.bookingType
            serviceRequest.category_brand_product_id = products?.get(0)?.category_brand_product_id
            serviceRequest.load_unload_charges = products?.get(0)?.load_unload_charges
            serviceRequest.productName = products?.get(0)?.name
            serviceRequest.seating_capacity = products?.get(0)?.seating_capacity
            serviceRequest.selectedProduct = products?.get(0)
            if (serviceRequest.pkgData != null) {
                val totalPrice = products?.get(0)?.timeFixedPrice
                serviceRequest.final_charge = totalPrice?.toDouble()
                serviceRequest.price_per_min = products?.get(0)?.pricePerMin ?: 0.0
                serviceRequest.distance_price_fixed = products?.get(0)?.distancePriceFixed
                        ?: 0.0
                serviceRequest.time_fixed_price = products?.get(0)?.timeFixedPrice ?: 0.0
                serviceRequest.price_per_km = products?.get(0)?.pricePerKm ?: 0.0

            } else {

                serviceRequest.final_charge = getFinalCharge(products?.get(0))
            }
            serviceRequest.images = ArrayList()
            serviceRequest.product_weight = 0F
            serviceRequest.material_details = ""
            serviceRequest.details = ""

            /*Check Flow
            * if Booking flow 1 = User move on request detail screen. where user fill all required fields.
            * if Booking flow 0 = No nned to fill extra details for request and direct move on confirm booking screen*/
            if (serviceRequest.bookingFlow == "1") {
                val fragment = EnterOrderDetailsFragment()
                val bundle = Bundle()
                bundle.putParcelable("productItem", products?.get(0))
                fragment.arguments = bundle
                fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
            } else {
                serviceRequest.future = ServiceRequestModel.BOOK_NOW
                fragmentManager?.beginTransaction()?.replace(R.id.container, ConfirmBookingFragment())?.addToBackStack("backstack")?.commit()
            }
        }
        else
        {
            val fragment = SelectProductsFragment()
            fragment.arguments = bundle
            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
        }
    }

    private fun getFinalCharge(product: Product?): Double? {
        val service = (context as HomeActivity).serviceDetails?.categories

        val price = product?.price_per_distance?.toDouble() ?: 0.0
                // .plus(product?.price_per_quantity?.times(serviceRequest.product_quantity ?: 1)?:0f)
                //.plus(product?.alpha_price?.toDouble() ?: 0.0)
                .times(serviceRequest.order_distance ?: 0f)
        var buraqPercentage = 0f
        service?.forEach {
            if (it.category_id == serviceRequest.category_id) {
                buraqPercentage = it.buraq_percentage ?: 0f
            }
        }
        return price.div(100).times(buraqPercentage).plus(price)

    }


    fun applyPromocode(promoData: CouponsItem?) {
        when (promoData?.couponType) {
            COUPEN_TYPE.VALUE -> {
                afterPromoAmount = serviceRequest?.final_charge?.minus(promoData?.amountValue?.toDouble()
                        ?: 0.0) ?: 0.0

                selectVehicleTypeBinding.promoRl.visibility = View.VISIBLE
                selectVehicleTypeBinding.tvPromoName.setText(promoData.code?.toUpperCase())
            }

            COUPEN_TYPE.PERCENTAGE -> {
                afterPromoAmount = serviceRequest?.final_charge?.minus(serviceRequest?.final_charge?.times(promoData?.amountValue?.toDouble()
                        ?: 0.0)?.div(100) ?: 0.0) ?: 0.0

                selectVehicleTypeBinding.promoRl.visibility = View.VISIBLE
                selectVehicleTypeBinding.tvPromoName.setText(promoData.code?.toUpperCase())
            }
        }
        serviceRequest?.isPromoApplied = true
        serviceRequest?.amountValue = promoData?.amountValue
        serviceRequest?.couponType = promoData?.couponType
        serviceRequest?.couponcode = promoData?.code
        serviceRequest?.coupenId = promoData?.couponId
        serviceRequest?.afterPromoFinalCharge = afterPromoAmount

        SharedPrefs.with(context).save(PROMO_ID, promoData?.couponId)
        SharedPrefs.with(context).save(PROMO_AMMOUNT, afterPromoAmount.toString())


    }


    private fun getSelectedPosition(categoryBrandId: Int): Int {
        for (i in categoriesList.orEmpty().indices) {
            if (categoriesList?.get(i)?.category_brand_id == categoryBrandId) {
                return i
            }
        }
        return 0
    }

    override fun orderDistanceSuccess(jsonRootObject: JSONObject) {
        val routeArray = jsonRootObject.getJSONArray("routes")
        if (routeArray.length() == 0) {
            /* No route found */
            return
        }
        var routes: JSONObject? = null
        for (i in routeArray.length() - 1 downTo 0) {
            routes = routeArray.getJSONObject(i)
        }

        val estimatedDistance = (((routes?.get("legs") as JSONArray).get(0) as JSONObject).get("distance") as JSONObject).get("value") as Int
        try {
            serviceRequest.order_distance = estimatedDistance / 1000f
        } catch (e: Exception) {
            serviceRequest.order_distance = 0.0f
        }

        val eta = (((routes.get("legs") as JSONArray).get(0) as JSONObject).get("duration") as JSONObject).get("text") as String
        if (eta.isNotEmpty()) {
            val etaSplit = eta.split(" ")
            if (etaSplit.size == 2) {
                serviceRequest.eta = etaSplit[0].toInt()
            }

            if(cateEstSearched){
                for(i in 0 until categoriesList!!.size){
                    if(categoriesList!![i].category_brand_id == categoryData!!.category_brand_id){
                        categoryData!!.estimatedTime = eta
                        categoriesList!!.removeAt(i)
                        categoriesList!!.add(i,categoryData!!)
//                        adapter?.notifyDataSetChanged()
                    }
                }
            }
        }

        adapter?.notifyDataSetChanged()
    }

    override fun onApiSuccess(response: ServiceDetails?) {
        SharedPrefs.get().save("catData", response)
        var isForceUpdate = activity?.let { showForcedUpdateDialog(it, response?.Versioning?.ANDROID?.user?.force.toString(), response?.Versioning?.ANDROID?.user?.normal.toString(), AppVersionConstants.VERSION_NAME) }
        homeActivity?.serviceDetails = response
       // listener?.getDriverData(response?.drivers as ArrayList<HomeDriver>)

        if (isForceUpdate == false) {
            if (response?.currentOrders?.isNotEmpty() == true) {
                handleCurrentOrderStatus(response.currentOrders[0])
            } else if (response?.lastCompletedOrders?.isNotEmpty() == true) {
                if (arguments != null) {
                    val via = arguments?.getString("via")
                    if (via.equals("skipNow")) {
                        homeActivity?.serviceDetails = response
                    } else {
                        //openInvoiceFragment(Gson().toJson(response.lastCompletedOrders[0]))
                    }
                } else {
                    // openInvoiceFragment(Gson().toJson(response.lastCompletedOrders[0]))
                }
            } else {
                homeActivity?.serviceDetails = response
                if (response?.categories?.isNotEmpty() == true) {
                    homeActivity?.serviceRequestModel?.category_id = response.categories[0].category_id
                    homeActivity?.serviceRequestModel?.category_brand_id = response.categories[0].category_brand_id
                    startDriverTimer(0)
                    /* for (driver in response.drivers as ArrayList) {
                         createMarker(driver.latitude, driver.longitude, R.drawable.ic)
                     }*/
                }

//                if (qrCodeData != null) {
//                    apiInProgress = false
//                    QRScanResultAction()
//                }
            }
            apiInProgress = false
        }
    }

    override fun onApiSuccess(response: TerminologyData?) {
        TODO("Not yet implemented")
    }

    private fun handleCurrentOrderStatus(order: Order?) {
        when (order?.order_status) {
            OrderStatus.SEARCHING -> {
                openProcessingFragment(order)
            }

            OrderStatus.CONFIRMED, OrderStatus.REACHED, OrderStatus.ONGOING -> {
                openDeliveryStartsFragment(order)
            }
        }
    }

    private fun openProcessingFragment(order: Order?) {
            val fragment = ProcessingRequestFragment()
            val bundle = Bundle()
            bundle.putString(ORDER, Gson().toJson(order))
            bundle.putBoolean("changeTimeOut", true)
            fragment.arguments = bundle
            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
    }

    private fun openDeliveryStartsFragment(order: Order?) {
        try {
            val fragment = DeliveryStartsFragment()
            val bundle = Bundle()
            bundle.putString("order", Gson().toJson(order))
            fragment.arguments = bundle
            fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            activity?.supportFragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
        } catch (e: java.lang.Exception) {
            Log.e("Services Frag Excep: ", e.message ?: "")
        }
    }


    override fun snappedDrivers(snappedDrivers: List<HomeDriver>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showLoader(isLoading: Boolean) {
    }

    override fun apiFailure() {
        rootView?.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(activity as Activity)
        } else {
            showErrorDialog(false)
        }
    }

    private fun showErrorDialog(networkError: Boolean) {
        val msgId: Int = if (networkError) {
            R.string.network_error
        } else {
            R.string.api_error_msg
        }
        AlertDialogUtil.getInstance().createOkCancelDialog(activity, R.string.error, msgId,
                R.string.retry, 0, false, object : AlertDialogUtil.OnOkCancelDialogListener {
            override fun onOkButtonClicked() {
                hitOrderDistanceApi()
            }

            override fun onCancelButtonClicked() {

            }
        }).show()

    }

    private fun scaleDownMarker(marker: Marker?, scale: Float, iconImageUrl: String) {
        if ((ICON_WIDTH * scale).toInt() > 0 && (ICON_HEIGHT * scale).toInt() > 0 && isAdded) {
            Thread {
                val mapIcon: Bitmap? = getIconBitmap()
                val newBitMap: Bitmap? = Bitmap.createScaledBitmap(
                        mapIcon!!,
                        (ICON_WIDTH * scale).toInt(),
                        (ICON_HEIGHT * scale).toInt(),
                        false)
                Handler(Looper.getMainLooper()).post {
                    try {
                        marker?.setIcon(BitmapDescriptorFactory.fromBitmap(newBitMap))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.start()
        }
    }

    private fun getIconBitmap(): Bitmap {
        requireContext().resources
        return homeActivity?.resources.let { BitmapFactory.decodeResource(it, getVehicleResId(4)) }
    }

    interface ServicesDetailInterface {
        fun getDriverData(driversList: ArrayList<HomeDriver>)
    }

    private val networkBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
                if (intent.action == "update") {
                    homeApiCall()
                }
            }
        }
    }

    override fun onInfoClick(title: String) {
        setUpBottomSheetDialog(title)
    }

    private fun setUpBottomSheetDialog(name: String?) {
        Log.e("asasasasas", name!!)
        val layoutDialog = View.inflate(context, R.layout.info_layout_bottom_view_layout, null)

        dialog = BottomSheetDialog(context as Activity)
        dialog?.setContentView(layoutDialog)
        val bottomSheetInternal = dialog?.findViewById(R.id.design_bottom_sheet) as View?

        bottomSheetInternal?.let {
            BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
        }
        val cat_tv = (layoutDialog.findViewById<TextView>(R.id.cat_tv))
        val tvDescription = (layoutDialog.findViewById<TextView>(R.id.tvDescription))
        val drop_iv = (layoutDialog.findViewById<ImageView>(R.id.drop_iv))

        cat_tv.setText(name)
        tvDescription.setText(getString(R.string.cat_description))
//        fullName.background=StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
//        phoneNumber.background=StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
//        cvCountry.background=StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
//        tvCancel.background=StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
//        btnContinue.background=StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)


        drop_iv.setOnClickListener {
            dialog?.dismiss()
        }


        dialog?.show()
    }

    override fun onCategorySelected(position: Int) {
        if(ConfigPOJO.is_asap == "true") {
            serviceRequest.category_brand_id = adapter?.getSelectedCategoryBrandId()
            serviceRequest.brandName = adapter?.getSelectedCategoryBrandName()
            serviceRequest.brandImage = adapter?.getSelectedCategoryBrandImage()
            homeActivity?.showMarker(LatLng(serviceRequest.pickup_latitude ?: 0.0,serviceRequest.pickup_longitude ?: 0.0),LatLng(serviceRequest.dropoff_latitude ?: 0.0,serviceRequest.dropoff_longitude ?:0.0))
        }
    }


}
