package com.trava.user.ui.home.services

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.transition.Explode
import androidx.transition.Transition
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hbb20.CountryCodePicker
import com.trava.user.AppVersionConstants
import com.trava.user.R
import com.trava.user.databinding.FragmentServicesBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.complete_ride.CompleteRideFragment
import com.trava.user.ui.home.deliverystarts.DeliveryStartsFragment
import com.trava.user.ui.home.deliverystarts.paymentview.PaymentFragment
import com.trava.user.ui.home.dropofflocation.SetupDropOffLocationFragment
import com.trava.user.ui.home.processingrequest.ProcessingRequestFragment
import com.trava.user.ui.home.road_pickup.RoadPickupActivity
import com.trava.user.ui.home.stories.WatchStories
import com.trava.user.ui.home.vehicles.SelectVehicleTypeFragment
import com.trava.user.ui.menu.bookings.BookingsActivity
import com.trava.user.ui.menu.emergencyContacts.contacts.ContactsActivity
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.PermissionUtils
import com.trava.user.utils.StaticFunction
import com.trava.user.utils.ValidationUtils
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.models.HomeMarkers
import com.trava.user.webservices.models.contacts.ContactModel
import com.trava.user.webservices.models.contacts.NewContactModel
import com.trava.user.webservices.models.homeapi.HomeDriver
import com.trava.user.webservices.models.homeapi.ServiceDetails
import com.trava.user.webservices.models.homeapi.TerminologyData
import com.trava.user.webservices.models.order.Order
import com.trava.user.webservices.models.roadPickup.RoadPickupResponse
import com.trava.utilities.*
import com.trava.utilities.BuildConfig
import com.trava.utilities.Constants.SECRET_DB_KEY
import com.trava.utilities.constants.*
import com.trava.utilities.location.LocationProvider
import com.trava.utilities.webservices.models.AppDetail
import com.trava.utilities.webservices.models.Service
import com.yarolegovich.discretescrollview.DiscreteScrollView
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_contacts.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_services.*
import kotlinx.android.synthetic.main.fragment_services.ivProfile
import kotlinx.android.synthetic.main.fragment_services.rlAddHome
import kotlinx.android.synthetic.main.fragment_services.rlAddWork
import kotlinx.android.synthetic.main.fragment_services.tvAddHomeTitle
import kotlinx.android.synthetic.main.fragment_services.tvAddWorkTitle
import kotlinx.android.synthetic.main.fragment_services.tvHomeAddress
import kotlinx.android.synthetic.main.fragment_services.tvWorkAddress
import kotlinx.android.synthetic.main.fragment_services.viewHome
import org.json.JSONObject
import permissions.dispatcher.*
import permissions.dispatcher.PermissionRequest
import java.util.*
import kotlin.collections.ArrayList

@RuntimePermissions
class ServicesFragment : Fragment(), View.OnClickListener, ServiceContract.View {
    private var selectedContactsList = ArrayList<ContactModel>()
    private lateinit var serviceList: ArrayList<Service>
    private var selectedCategoryId = 0
    private var map: GoogleMap? = null
    private var lat = 0.0
    private var lng = 0.0
    private var address = ""
    private var TAG = "ServiceFragmentScreen"
    private var category_id_select = "0"
    private var selectedServicePosition = 0
    private var dialogIndeterminate: DialogIndeterminate? = null
    private val presenter = ServicePresenter()
    private var apiInProgress = false
    private var homeActivity: HomeActivity? = null
    private var markersList = ArrayList<HomeMarkers?>()
    private var timerDrivers = Timer()
    private var currentLocation: Location? = null
    lateinit var mImages: IntArray
    lateinit var mImagesSelected: IntArray
    private var isCurrentLocation = false
    private var currentZoomLevel = 14f
    private val ICON_WIDTH: Int = 60
    private val ICON_HEIGHT: Int = 90
    private var listener: ServicesDetailInterface? = null
    var qrCodeData: RoadPickupResponse? = null
    private var isFindingAddress: Boolean = false

    private var dialog: BottomSheetDialog? = null
    private var isPhoneNoValid = false

    private var etGiftPhoneNumber: EditText? = null
    private var etGiftFriendName: EditText? = null
    private var newContactModel = ArrayList<NewContactModel>()
    private var nnewContactModel: NewContactModel? = null

    fun setListener(listener: ServicesDetailInterface) {
        this.listener = listener
    }

    lateinit var servicesBinding: FragmentServicesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        servicesBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_services, container, false)
        servicesBinding.color = ConfigPOJO.Companion
        val view = servicesBinding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        if (ConfigPOJO.dynamicBar?.road_pickup==true) {
            tvRoadPickup.visibility = View.VISIBLE
        } else {
            tvRoadPickup.visibility = View.GONE
        }

        context?.let {
            LocalBroadcastManager
                    .getInstance(it)
                    .registerReceiver(notificationBroadcast, IntentFilter(Broadcast.NOTIFICATION))
        }

        dialogIndeterminate = DialogIndeterminate(requireActivity())
        homeActivity = activity as HomeActivity

        try {
            LocationProvider.CurrentLocationBuilder(activity).build().getLastKnownLocation(OnSuccessListener {
                currentLocation = it
                if (it != null) {
                    lat = it.latitude
                    lng = it.longitude
                    getDrivers()
                } else {

                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (homeActivity?.serviceRequestModel?.pkgData != null) {
            ivProfile.setImageResource(R.drawable.ic_back_arrow_white)
            tvBookBycall.hide()
            cvToolbar.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            tvHeader.text = getString(R.string.packageBooking)
            tvHeader.visibility=View.VISIBLE
        } else {
            ivProfile.setImageResource(R.drawable.ic_menu)
            tvBookBycall.hide()
            cvToolbar.background = null
            tvHeader.text = ""
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
            ivProfile.setImageResource(R.drawable.ic_menu)
            header_icon.visibility = VISIBLE
            bottom_fab_location.visibility = VISIBLE
            fabMyLocation.visibility = GONE
            top_where_to.visibility = VISIBLE
            cvBottom.visibility = GONE
            cvToolbar.background = StaticFunction.changeBorderTextColor(ConfigPOJO.headerColor, ConfigPOJO.headerColor, GradientDrawable.RECTANGLE)
        }

        if (ConfigPOJO.is_gift == "true") {
            top_where_to.visibility = View.VISIBLE
            tvHeader.text = getString(R.string.my_location)
            tvHeader.visibility=View.VISIBLE
            cvToolbar.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        }

        if (getString(R.string.app_name) == "Wasila") {
            tvMapNormal.visibility = View.VISIBLE
            tvSatellite.visibility = View.VISIBLE
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            cvToolbar.background = null
            ivProfile.setImageResource(R.drawable.ic_menu)
            tv_whereTo.setText(getString(R.string.where_u_going))
            fabMyLocation.visibility = View.GONE
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.DELIVERY20) {
            cvToolbar.background = null
            ivProfile.setImageResource(R.drawable.ic_nav_drawer)
            tv_whereTo.setText(getString(R.string.where_to))
            fabMyLocation.visibility = View.GONE
        }
        tv_whereTo.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE) {
            tv_whereTo.setText(getString(R.string.delivery_parcel))
            tv_whereTo.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.white_color, GradientDrawable.RECTANGLE)
        }
        if (SECRET_DB_KEY == "456454ae09ad1d988be2677c9d477385" ||
                SECRET_DB_KEY == "8f12ee7aaf0fa196926a8590472154a9") {
            tv_whereTo.text = getString(R.string.where_do_want_deleivery)
        }

        if (SECRET_DB_KEY == "b25dcb3ed072a432e0b76634d9d7ae4d" || SECRET_DB_KEY == "c0cb897ef919b1c32ee9f672d74421228890d20962b7623535f9ab29d3e9f88a") {
            tv_whereTo.text = getString(R.string.how_we_help_you)
        }

        if (Constants.SECRET_DB_KEY == "d190fc6d825399e5276673ee8467d415af6dc774ffe7005ea59b3d5b6a2f0314"
                || Constants.SECRET_DB_KEY == "d190fc6d825399e5276673ee8467d415cebf52a7d89e2a2f532b4d8d1268fbc7") {
            tv_whereTo.text = getString(R.string.choose_your_location)
        }

        if(ConfigPOJO.is_hood_app=="1")
        {
            great_day_text.text=getString(R.string.just_click_away_you)
        }

        if (ConfigPOJO.is_water_platform == "true") {
            llCategoryData.visibility = View.GONE
            tv_whereTo.text = getString(R.string.where_do_want_deleivery)
            tv_whereTo.setCompoundDrawablesWithIntrinsicBounds(0, 0, (R.drawable.ic_recent_locations), 0)
        } else {
            llCategoryData.visibility = View.VISIBLE
        }

        serviceList = Gson().fromJson<ArrayList<Service>>(SharedPrefs.with(activity).getString(SERVICES, ""), object : TypeToken<ArrayList<Service>>() {}.type)

        if (serviceList.size == 1 || homeActivity?.serviceRequestModel?.pkgData != null) {
            rvCompanies.visibility = View.GONE
            v_line.visibility = View.GONE
            viewSelected.visibility = View.GONE
            tvSelectedService.visibility = View.GONE
        }

        if (SECRET_DB_KEY=="a9f7a7a5157859a3b4b6c4172567c015")
        {
            tvSelectedService.visibility=View.VISIBLE
        }
        if (homeActivity?.serviceRequestModel?.pkgData?.packageData?.packagePricings?.get(0)?.categoryId != null) {
            selectedCategoryId = homeActivity?.serviceRequestModel?.pkgData?.packageData?.packagePricings?.get(0)?.categoryId
                    ?: 0
            for (i in serviceList.indices) {
                if (serviceList[i].category_id == selectedCategoryId) {
                    selectedServicePosition = i
                }
            }
        }

        ivSupport?.isEnabled = false
        rvCompanies.adapter = context?.let { ServiceAdapterDynamic(it, rvCompanies, serviceList) }
        rvCompanies.setItemTransformer(ScaleTransformer.Builder().setMaxScale(1f)
                .setMinScale(0.9f)
                .setPivotX(Pivot.X.CENTER)
                .setPivotY(Pivot.Y.CENTER)
                .build())
        rvCompanies.setSlideOnFling(true)
        rvCompanies.setSlideOnFlingThreshold(1000)

        if (homeActivity?.serviceRequestModel?.pkgData != null) {
            for (i in serviceList.indices) {
                if (homeActivity?.serviceRequestModel?.pkgData?.packageData?.pricingData?.categories?.get(0)?.category_id == serviceList[i].category_id) {
                    rvCompanies.scrollToPosition(i)
                }
            }
        }
        homeActivity?.getMapAsync()
        setListeners()
        val profile = SharedPrefs.with(context).getObject(PROFILE, AppDetail::class.java)
        if (profile != null || profile?.user != null) {
            tvHelloText.text = String.format("%s", "${getString(R.string.hi)} ${profile.user?.name}")
        }

        if (SECRET_DB_KEY == "d190fc6d825399e5276673ee8467d415fd9de35ce5a4fadce32bbaee91cdc036" ||
                SECRET_DB_KEY == "ad567131d8bf9d931405e8b68956dfa6") {
            tv_whereTo.text = getString(R.string.ship_for_you)
            tvHelloText.text = String.format("%s", "${getString(R.string.hi)} ${profile.user?.name!!.split(" ")[0]}")
        }

        activity?.registerReceiver(networkBroadcastReceiver, IntentFilter("update"))
        if (homeActivity?.serviceRequestModel?.bookingType?.isNotEmpty() == true) {
            homeActivity?.serviceRequestModel?.bookingType = ""
            homeActivity?.serviceRequestModel?.bookingFriendName = ""
            homeActivity?.serviceRequestModel?.bookingFriendCountryCode = ""
            homeActivity?.serviceRequestModel?.bookingFriendPhoneNumber = ""
        }
        homeActivity?.serviceRequestModel?.stops?.clear()

        if (ConfigPOJO.TEMPLATE_CODE == Constants.TRAVE_APP_BASE || ConfigPOJO.TEMPLATE_CODE == Constants.EAGLE)//view show according to template selection
        {
            ivProfile.setImageResource(R.drawable.ic_nav_drawer)
            // cvToolbar.background = null
        } else {
            if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                ivProfile.setImageResource(R.drawable.ic_nav_drawer)
                cvToolbar.background = null
                tv_whereTo.setText(getString(R.string.where_is_thr_pivkup))
                fabMyLocation!!.visibility = GONE
                ll_recent_location!!.visibility = VISIBLE
            } else {
                // ivProfile.setImageResource(R.drawable.ic_menu)
                if (ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
                    cvToolbar.background = StaticFunction.changeBorderTextColor(ConfigPOJO.headerColor, ConfigPOJO.headerColor, GradientDrawable.RECTANGLE)
                } else {
                    cvToolbar.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
                }
            }

            if (ConfigPOJO.TEMPLATE_CODE == Constants.DELIVERY20) {
                cvToolbar.background = null
                ivProfile.setImageResource(R.drawable.ic_nav_drawer)
                tv_whereTo.setText(getString(R.string.where_to))
                fabMyLocation.visibility = View.GONE
            }
        }

        if (homeActivity?.serviceRequestModel?.pkgData != null) {
            ivProfile.setImageResource(R.drawable.ic_back_arrow_white)
        }

        if (arguments != null) {
            if (arguments!!.containsKey("for") && arguments!!.getString("for").equals("book_ride", false)) {
                tv_whereTo.performClick()
            }
        }

        if (ConfigPOJO.is_gift == "true" && Constants.SECRET_DB_KEY == "8969983867fe9a873b1b39d290ffa25c") {
            tvContinue.visibility = View.VISIBLE
            tv_whereTo.visibility = View.GONE
            tvContinue.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        }

        if (Constants.SECRET_DB_KEY == "ad567131d8bf9d931405e8b68956dfa6" || Constants.SECRET_DB_KEY == "d190fc6d825399e5276673ee8467d415fd9de35ce5a4fadce32bbaee91cdc036") {
            tv_whereTo.text = getString(R.string.what_do_you_want_to_ship)
            if (profile != null || profile?.user != null) {
                tvHelloText.text = String.format("%s", "${getString(R.string.hi)} ${profile.user?.firstName}")
            }
        }
    }

    var notif = ""
    var booking_flow = ""
    var order_exist = false
    private val notificationBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.getStringExtra("type") == "DApproved") {
                notif = "true"
                homeApiCall()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startDriverTimer(0)
        AppSocket.get().on(Events.ORDER_EVENT, orderEventListener)
        AppSocket.get().on(Socket.EVENT_CONNECT, onSocketConnected)
        AppSocket.get().on(Socket.EVENT_DISCONNECT, onSocketDisconnected)
        if (!order_exist) {
            homeApiCall()
        }
    }

    override fun onPause() {
        super.onPause()
        timerDrivers.cancel()
        timerDrivers.purge()
        removeAllDriverMarkers()
        AppSocket.get().off(Events.ORDER_EVENT, orderEventListener)
        AppSocket.get().off(Socket.EVENT_CONNECT, onSocketConnected)
        AppSocket.get().off(Socket.EVENT_DISCONNECT, onSocketDisconnected)
    }

    private val onSocketConnected = Emitter.Listener {
        activity?.runOnUiThread {

        }
    }


    private val onSocketDisconnected = Emitter.Listener {

    }

    private val orderEventListener = Emitter.Listener { args ->
        Logger.e("orderEventListener", args[0].toString())
        activity?.runOnUiThread {
            when (JSONObject(args[0].toString()).getString("type")) {
                OrderEventType.ONGOING -> {
                    val orderJson = JSONObject(args[0].toString()).getJSONArray("order").get(0).toString()
                    val orderModel = Gson().fromJson(orderJson, Order::class.java)
                    handleCurrentOrderStatus(orderModel)
                }
            }
        }
    }

    private fun setListeners() {
        ivProfile.setOnClickListener(this)
        ivSupport.setOnClickListener(this)
        tvDropOffLocation.setOnClickListener(this)
        tv_whereTo.setOnClickListener(this)
        tvContinue.setOnClickListener(this)
        ivNext.setOnClickListener(this)
        rvCompanies.addOnItemChangedListener(itemChangeListener)
        rvCompanies.addScrollStateChangeListener(scrollListener)
        fabMyLocation.setOnClickListener(this)
        btn_home.setOnClickListener(this)
        fabSatellite.setOnClickListener(this)
        tvMapNormal.setOnClickListener(this)
        tvSatellite.setOnClickListener(this)
        tvRoadPickup.setOnClickListener(this)
        tvBookBycall.setOnClickListener(this)
        top_where_to.setOnClickListener(this)
        bottom_fab_location.setOnClickListener(this)
        tv_watch_earn.setOnClickListener(this)
        fabActiveRides.setOnClickListener(this)

        if (ConfigPOJO.multiple_request.toInt() == 1) {
            fabActiveRides.visibility = View.GONE
        }

        if (ConfigPOJO.is_merchant == "true") {
            tv_watch_earn.visibility = View.VISIBLE
        }
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
            fabSatellite.setImageResource(if (mapType == GoogleMap.MAP_TYPE_SATELLITE)
                R.drawable.ic_satellite_blue else R.drawable.ic_satellite)

            tvMapNormal.isSelected = mapType == GoogleMap.MAP_TYPE_NORMAL
            tvSatellite.isSelected = mapType == GoogleMap.MAP_TYPE_SATELLITE

            updateDropOffAddress()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fabMyLocation -> {
                LocationProvider.CurrentLocationBuilder(activity).build().getLastKnownLocation(OnSuccessListener {
                    currentLocation = it
                    if (it != null) {
                        focusOnCurrentLocation(it.latitude, it.longitude)
                    } else {
                        startLocationUpdates()
                    }
                })
            }
            R.id.btn_home->{
                activity?.finish()
            }
            R.id.bottom_fab_location -> {
                fabMyLocation.performClick()
            }
            R.id.tv_watch_earn -> {
//                val fragment = WatchStories()
//                fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                startActivity(Intent(activity!!, WatchStories::class.java))
            }
            R.id.fabActiveRides -> {
                startActivity(Intent(activity!!, BookingsActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }

            R.id.fabSatellite -> {
                val mapType = SharedPrefs.with(activity).getInt(PREF_MAP_VIEW, GoogleMap.MAP_TYPE_NORMAL)
                if (mapType == GoogleMap.MAP_TYPE_NORMAL) {
                    map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    SharedPrefs.with(activity).save(PREF_MAP_VIEW, GoogleMap.MAP_TYPE_SATELLITE)
                    fabSatellite.setImageResource(R.drawable.ic_satellite_blue)
                } else {
                    map?.mapType = GoogleMap.MAP_TYPE_NORMAL
                    SharedPrefs.with(activity).save(PREF_MAP_VIEW, GoogleMap.MAP_TYPE_NORMAL)
                    fabSatellite.setImageResource(R.drawable.ic_satellite)
                }
            }
            R.id.tvMapNormal -> {
                tvMapNormal.isSelected = true
                tvSatellite.isSelected = false
                map?.mapType = GoogleMap.MAP_TYPE_NORMAL
                SharedPrefs.with(activity).save(PREF_MAP_VIEW, GoogleMap.MAP_TYPE_NORMAL)
            }

            R.id.tvSatellite -> {
                tvMapNormal.isSelected = false
                tvSatellite.isSelected = true
                map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                SharedPrefs.with(activity).save(PREF_MAP_VIEW, GoogleMap.MAP_TYPE_SATELLITE)
            }
            R.id.ivProfile -> {
                if (homeActivity?.serviceRequestModel?.pkgData != null) {
                    activity?.onBackPressed()
                } else {
                    activity?.drawer_layout?.openDrawer(GravityCompat.START)
                }
            }
            R.id.ivSupport -> activity?.drawer_layout?.openDrawer(GravityCompat.END)

            R.id.tv_whereTo, R.id.tvDropOffLocation, R.id.ivNext -> {
                setWhereToClick("normal")
            }

            R.id.tvDropOffLocation, R.id.ivNext -> {
                tv_whereTo.performClick()
            }

            R.id.top_where_to -> {
                if (ConfigPOJO.is_gift == "true") {
                    openPlacePickerIntent(Constants.PLACEAUTOCOMPLETE_REQUESTCODE)
                } else {

                    tv_whereTo.performClick()
                }
            }

            R.id.tvContinue -> {
                if (ConfigPOJO.is_gift == "true" && homeActivity?.serviceRequestModel?.category_id == 12) {
                    setUpBottomSheetDialog()
                } else {
                    tv_whereTo.performClick()
                }
            }

            R.id.tvRoadPickup -> {
                startActivityForResult(Intent(context, RoadPickupActivity::class.java), Constants.QR_CODE_REQUEST_CODE)
            }

            R.id.tvBookBycall -> {
                context?.dialPhone("+94-760111154")
            }
        }
    }

    private fun setWhereToClick(bookingType: String) {
        homeActivity?.serviceRequestModel?.dropoff_address=""
        if (CheckNetworkConnection.isOnline(activity)) {
            if (!apiInProgress) {
                if (ConfigPOJO.is_omco == "true" && homeActivity?.serviceRequestModel!!.category_name == "Receive") {
                    val fragment = SelectVehicleTypeFragment()
                    var bundle = Bundle()
                    fragment.arguments = bundle
                    fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                } else {
                    val explode = Explode()
                    explode.excludeChildren(cvToolbar, true)
                    val viewRect = Rect()
                    viewRect.set(0, 0, activity?.let { it1 -> Utils.getScreenWidth(it1) }
                            ?: 0, Utils.dpToPx(90).toInt())
                    explode.epicenterCallback = object : Transition.EpicenterCallback() {
                        override fun onGetEpicenter(transition: Transition): Rect {
                            return viewRect
                        }
                    }
                    qrCodeData = null
                    exitTransition = explode
                    val fragment = SetupDropOffLocationFragment()
                    val bundle = Bundle()
                    bundle.putDouble(Constants.LATITUDE, lat)
                    bundle.putDouble(Constants.LONGITUDE, lng)
                    bundle.putString(Constants.ADDRESS, address)
                    bundle.putString(Constants.BOOKING_TYPE, bookingType)
                    if (arguments != null) {
                        if (arguments!!.containsKey("destination")) {
                            bundle.putString("destination", arguments!!.getString("destination"))
                        }
                    }
                    fragment.arguments = bundle
                    fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                }
            }
        }
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

    private fun focusOnCurrentLocation(latitude: Double?, longitude: Double?) {
        val target = LatLng(latitude ?: 0.0, longitude ?: 0.0)
        val builder = CameraPosition.Builder()
        builder.zoom(14f)
        builder.target(target)
        ((activity as? HomeActivity)?.googleMapHome)?.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()))
    }

    private val itemChangeListener = DiscreteScrollView.OnItemChangedListener<ServiceAdapterDynamic.ViewHolder>
    { viewHolder, adapterPosition ->
        viewHolder?.setSelected(adapterPosition)
        showSelectedText(adapterPosition)
        homeApiCall()
    }

    /*Home api call to fetch all services categories and their relates brands or products*/
    fun homeApiCall() {
        removeAllDriverMarkers()
        timerDrivers.cancel()
        val map = HashMap<String, String>()

        if (notif == "true") {
            booking_flow = serviceList[selectedServicePosition].booking_flow ?: ""
            map["category_id"] = serviceList[selectedServicePosition].category_id.toString()
            category_id_select = serviceList[selectedServicePosition].category_id.toString()
        } else {
            if (qrCodeData == null) {
                if (serviceList != null && serviceList.size > 0) {
                    (activity as HomeActivity).serviceRequestModel.bookingFlow = serviceList[selectedServicePosition].booking_flow
                    (activity as HomeActivity).serviceRequestModel.is_manual_assignment = serviceList[selectedServicePosition].is_manual_assignment
                    booking_flow = serviceList[selectedServicePosition].booking_flow ?: ""
                    (activity as HomeActivity).serviceRequestModel.category_id = serviceList[selectedServicePosition].category_id
                    (activity as HomeActivity).serviceRequestModel.category_name = serviceList[selectedServicePosition].name
                            ?: ""
                    map["category_id"] = serviceList[selectedServicePosition].category_id.toString()
                    category_id_select = serviceList[selectedServicePosition].category_id.toString()
                } else {
                    if (homeActivity?.serviceRequestModel?.pkgData?.packageData?.packagePricings?.get(0)?.categoryId != null) {
                        selectedCategoryId = homeActivity?.serviceRequestModel?.pkgData?.packageData?.packagePricings?.get(0)?.categoryId
                                ?: 0

                        serviceList = Gson().fromJson<ArrayList<Service>>(SharedPrefs.with(activity).getString(SERVICES, ""), object : TypeToken<ArrayList<Service>>() {}.type)

                        for (i in serviceList.indices) {
                            if (serviceList[i].category_id == selectedCategoryId) {
                                selectedServicePosition = i
                            }
                        }
                    }
                    (activity as HomeActivity).serviceRequestModel.is_manual_assignment = serviceList[selectedServicePosition].is_manual_assignment
                    (activity as HomeActivity).serviceRequestModel.bookingFlow = serviceList[selectedServicePosition].booking_flow
                    booking_flow = serviceList[selectedServicePosition].booking_flow ?: ""
                    (activity as HomeActivity).serviceRequestModel.category_id = serviceList[selectedServicePosition].category_id
                    (activity as HomeActivity).serviceRequestModel.category_name = serviceList[selectedServicePosition].name
                            ?: ""
                    map["category_id"] = serviceList[selectedServicePosition].category_id.toString()
                    category_id_select = serviceList[selectedServicePosition].category_id.toString()
                }

            } else {
                for (i in serviceList.indices) {
                    if (serviceList[i].category_id == qrCodeData?.categoryId) {
                        (activity as HomeActivity).serviceRequestModel.is_manual_assignment = serviceList[i].is_manual_assignment
                        (activity as HomeActivity).serviceRequestModel.bookingFlow = serviceList[i].booking_flow
                        (activity as HomeActivity).serviceRequestModel.category_name = serviceList[i].name
                                ?: ""
                        booking_flow = serviceList[i].booking_flow ?: ""
                    }
                }

                (activity as HomeActivity).serviceRequestModel.category_id = qrCodeData?.categoryId
                map["category_id"] = qrCodeData?.categoryId.toString()
                category_id_select = qrCodeData?.categoryId.toString()
            }
        }

        /*Check package data exists for request*/
        if (homeActivity?.serviceRequestModel?.pkgData?.packageData?.packagePricings?.get(0)?.categoryId != null) {
            for (i in serviceList.indices) {
                if (serviceList[i].category_id == selectedCategoryId) {
                    (activity as HomeActivity).serviceRequestModel.is_manual_assignment = serviceList[i].is_manual_assignment
                    (activity as HomeActivity).serviceRequestModel.bookingFlow = serviceList[i].booking_flow
                    (activity as HomeActivity).serviceRequestModel.category_name = serviceList[i].name
                            ?: ""
                    booking_flow = serviceList[i].booking_flow ?: ""
                }
            }

            (activity as HomeActivity).serviceRequestModel.category_id = selectedCategoryId
            map["category_id"] = selectedCategoryId.toString()
            category_id_select = selectedCategoryId.toString()
        }
        notif = ""

        map["latitude"] = lat.toString()
        map["longitude"] = lng.toString()
        if (ConfigPOJO.is_asap == "true") {
            map["distance"] = (ConfigPOJO.distance_search_start.toInt() + (ConfigPOJO.search_count.toInt() * ConfigPOJO.distance_search_increment.toInt())).toString()
        } else {
            map["distance"] = "500000"
        }
        if (CheckNetworkConnection.isOnline(activity)) {
            apiInProgress = true
            presenter.homeApi(map)
            timerDrivers.cancel()
        }
    }

    var serviceDetailsCurrent: ServiceDetails? = null
    var serviceDetailsexist = false
    private val scrollListener = object : DiscreteScrollView.ScrollStateChangeListener<ServiceAdapterDynamic.ViewHolder> {
        override fun onScroll(scrollPosition: Float, currentPosition: Int, newPosition: Int, currentHolder: ServiceAdapterDynamic.ViewHolder?, newCurrent: ServiceAdapterDynamic.ViewHolder?) {

        }

        override fun onScrollEnd(currentItemHolder: ServiceAdapterDynamic.ViewHolder, adapterPosition: Int) {
            showSelectedText(adapterPosition)
        }

        override fun onScrollStart(currentItemHolder: ServiceAdapterDynamic.ViewHolder, adapterPosition: Int) {
            currentItemHolder.setUnSelected(adapterPosition)
            hideSelectedText()
        }
    }

    private val onCameraMoveListener = GoogleMap.OnCameraMoveListener {
        markersList.forEach {
            if (currentZoomLevel != map?.cameraPosition?.zoom) {
                currentZoomLevel = map?.cameraPosition?.zoom ?: 14f
                if (ConfigPOJO.is_asap == "true") {
                    createDriverIconByCategory(it?.catrgory_brand_id!!, it?.marker!!)
                } else if (SECRET_DB_KEY == "eecf5a33e8575b1a860cc17dd778ea6f" ||
                        SECRET_DB_KEY == "456049b71e28127ccd109b3fa9392fdb") {
                    createDriverIconByCategoryMyokada(it?.category_id!!, it?.marker!!)
                } else {
                    scaleDownMarker(it?.marker, (currentZoomLevel / 14f), it?.iconImageUrl ?: "")
                }
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
        return homeActivity?.resources.let { BitmapFactory.decodeResource(it, getVehicleResId(serviceList[selectedServicePosition].category_id)) }
    }

    private fun updateDropOffAddress() {
        lat = map?.cameraPosition?.target?.latitude ?: 0.0
        lng = map?.cameraPosition?.target?.longitude ?: 0.0
        address = ""
        activity?.runOnUiThread {
            getAddressFromLatLng(lat, lng)
        }
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double) {
        val geocoder: Geocoder = Geocoder(activity, LocaleManager.getLocale(activity?.resources))
        Thread(Runnable {
            try {
                val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
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
                    this@ServicesFragment.address = addressString.toString()
                    tvDropOffLocation?.text = address
                    if (ConfigPOJO.is_gift == "true") {
                        top_where_to.text = address
                        homeActivity?.serviceRequestModel?.pickup_address = address
                        homeActivity?.serviceRequestModel?.pickup_latitude = latitude
                        homeActivity?.serviceRequestModel?.pickup_longitude = longitude
                        homeActivity?.serviceRequestModel?.dropoff_address = address
                        homeActivity?.serviceRequestModel?.dropoff_latitude = latitude
                        homeActivity?.serviceRequestModel?.dropoff_longitude = longitude
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }).start()
    }

    private fun showSelectedText(position: Int) {
        selectedServicePosition = position
        tvSelectedService.text = serviceList[position].name
        tvSelectedService?.animate()?.alpha(1f)?.setDuration(200)?.start()
    }

    private fun hideSelectedText() {
        tvSelectedService?.animate()?.alpha(0f)?.setDuration(600)?.start()
    }

    override fun onDestroyView() {
        presenter.detachView()
        activity?.unregisterReceiver(networkBroadcastReceiver)
        rvCompanies?.removeItemChangedListener(itemChangeListener)
        rvCompanies?.removeScrollStateChangeListener(scrollListener)
        map?.setOnCameraIdleListener(null)
        map?.setOnCameraMoveStartedListener(null)
        super.onDestroyView()
    }

    private fun openInvoiceFragment(orderJson: String?, payment_body: String) {
        if (ConfigPOJO.gateway_unique_id.equals("wipay") || ConfigPOJO.gateway_unique_id.toLowerCase() == "qpaypro") {
            var order = Gson().fromJson(orderJson, Order::class.java)
            if (order?.payment?.payment_type == "Card") {
                val fragment = PaymentFragment()
                val bundle = Bundle()
                bundle.putString("payment_body", payment_body)
                bundle.putString("order", orderJson)
                fragment.arguments = bundle
                fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commitAllowingStateLoss()
            } else {
                val fragment = CompleteRideFragment()
                val bundle = Bundle()
                bundle.putString("order", orderJson)
                fragment.arguments = bundle
                fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commitAllowingStateLoss()
            }
        } else {
            val fragment = CompleteRideFragment()
            val bundle = Bundle()
            bundle.putString("payment_body", payment_body)
            bundle.putString("order", orderJson)
            fragment.arguments = bundle
            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commitAllowingStateLoss()
        }
    }


    /*Home api request response fetch and set data on fragments*/
    override fun onApiSuccess(response: ServiceDetails?) {
        SharedPrefs.get().save("catData", response)
        var isForceUpdate = activity?.let { showForcedUpdateDialog(it, response?.Versioning?.ANDROID?.user?.force.toString(), response?.Versioning?.ANDROID?.user?.normal.toString(), AppVersionConstants.VERSION_NAME) }
        homeActivity?.serviceDetails = response
        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
            if (homeActivity?.serviceDetails?.addresses?.home?.address != null) {
                tvAddHomeTitle?.text = homeActivity?.serviceDetails?.addresses?.home?.addressName
                tvHomeAddress?.text = homeActivity?.serviceDetails?.addresses?.home?.address
                tvHomeAddress.show()
                rlAddHome.show()
                viewHome.show()
            }

            if (homeActivity?.serviceDetails?.addresses?.work?.address != null) {
                tvAddWorkTitle?.text = homeActivity?.serviceDetails?.addresses?.work?.addressName
                tvWorkAddress?.text = homeActivity?.serviceDetails?.addresses?.work?.address
                tvWorkAddress.show()
                rlAddWork.show()
            }
        }

        if (isForceUpdate == false) {
            when {
                response?.currentOrders?.isNotEmpty() == true -> {
                    /*Pending*/
                    if (response?.lastCompletedOrders?.isNotEmpty()==true)
                    {
                        var itemPos = 0
                        for (i in response.lastCompletedOrders.indices) {
                            if (response.lastCompletedOrders[i].payment?.payment_status == "Failed" ||
                                    response.lastCompletedOrders[i].payment?.payment_status == "Pending") {
                                itemPos = i
                            }
                        }

                        if (response.lastCompletedOrders[itemPos].payment?.payment_status == "Failed" ||
                                response.lastCompletedOrders[itemPos].payment?.payment_status == "Pending") {
                            openInvoiceFragment(Gson().toJson(response.lastCompletedOrders[itemPos]), response.payment_body ?: "")
                        } else {

                            order_exist = response.currentOrders[0].order_status != "Pending"
                            serviceDetailsCurrent = response
                            serviceDetailsexist = true
                            presenter.terminologyApi(response.currentOrders[0].category_id.toString())
                        }
                        order_exist = false
                    }
                    else{
                        order_exist = response.currentOrders[0].order_status != "Pending"
                        serviceDetailsCurrent = response
                        serviceDetailsexist = true
                        presenter.terminologyApi(response.currentOrders[0].category_id.toString())
                    }
                }
                response?.lastCompletedOrders?.isNotEmpty() == true -> {
                    /*last order data if payment pending of last request */
                    if (arguments != null) {
                        val via = arguments?.getString("via")
                        if (via.equals("skipNow")) {
                            homeActivity?.serviceDetails = response
                        }
                    }

                    var itemPos = 0
                    for (i in response.lastCompletedOrders.indices) {
                        if (response.lastCompletedOrders[i].payment?.payment_status == "Failed" ||
                                response.lastCompletedOrders[i].payment?.payment_status == "Pending") {
                            itemPos = i
                        }
                    }

                    if (response.lastCompletedOrders[itemPos].payment?.payment_status == "Failed" ||
                            response.lastCompletedOrders[itemPos].payment?.payment_status == "Pending") {
                        openInvoiceFragment(Gson().toJson(response.lastCompletedOrders[itemPos]), response.payment_body ?: "")
                    } else {
                        presenter.terminologyApi(response.lastCompletedOrders[itemPos].category_id.toString())
                    }
                    order_exist = false
                }
                else -> {
                    homeActivity?.serviceDetails = response
                    if (response?.categories?.isNotEmpty() == true) {
                        homeActivity?.serviceRequestModel?.category_id = response.categories[0].category_id
                        homeActivity?.serviceRequestModel?.category_brand_id = response.categories[0].category_brand_id
                        startDriverTimer(0)
                    }
                    presenter.terminologyApi(category_id_select)
                }
            }

            if (qrCodeData != null) {
                apiInProgress = false
                QRScanResultAction()
            }
            apiInProgress = false
        }
        startDriverTimer(0)
    }

    override fun onApiSuccess(response: TerminologyData?) {
        ConfigPOJO.TerminologyData = response
        (activity as HomeActivity).hideMenu()
        if (serviceDetailsexist) {
            if (arguments != null) {
                if (arguments!!.containsKey("from") && arguments!!.getString("from") == "booking_details" ||
                        arguments!!.containsKey("from") && arguments?.getString("from") == "Notification") {
                    var orderID = arguments?.getString("order_id")
                    for (i in 0 until serviceDetailsCurrent?.currentOrders!!.size) {
                        if (serviceDetailsCurrent?.currentOrders?.get(i)?.order_id.toString() == orderID) {
                            handleCurrentOrderStatus(serviceDetailsCurrent?.currentOrders?.get(i))
                        }
                    }
                } else {
                    handleCurrentOrderStatus(serviceDetailsCurrent?.currentOrders?.get(0))
                }
            } else {
                handleCurrentOrderStatus(serviceDetailsCurrent?.currentOrders?.get(0))
            }
        }
    }

    override fun snappedDrivers(snappedDrivers: List<HomeDriver>?) {
        setDriversOnMap(snappedDrivers)
    }

    private fun handleCurrentOrderStatus(order: Order?) {
        when (order?.order_status) {
            OrderStatus.SEARCHING -> {
                if (ConfigPOJO.is_gift == "true" && order?.category_id ?: 0 == 12) {

                } else {
                    openProcessingFragment(order)
                }
            }

            OrderStatus.CONFIRMED, OrderStatus.REACHED, OrderStatus.ONGOING, OrderStatus.CustCancelReq -> {
                if (arguments != null) {
                    if (arguments?.getString("from") == "home_activity") {

                    } else {
                        openDeliveryStartsFragment(order)
                    }
                } else {
                    openDeliveryStartsFragment(order)
                }
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
            if (arguments?.getString("from") == "Notification") {
                bundle.putString("from", arguments?.getString("from"))
            } else if (arguments?.getString("from") == "booking_details") {
                bundle.putString("from", arguments?.getString("from"))
            } else {
                bundle.putString("from", "nothing")
            }
            bundle.putString("order", Gson().toJson(order))
            fragment.arguments = bundle
            fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            activity?.supportFragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
        } catch (e: java.lang.Exception) {
            Log.e("Services Frag Excep: ", e.message ?: "")
        }
    }

    override fun showLoader(isLoading: Boolean) {
        if (isLoading) {
            dialogIndeterminate?.show()
        } else {
            dialogIndeterminate?.dismiss()
        }
    }

    override fun apiFailure() {
        apiInProgress = false
    }

    override fun handleApiError(code: Int?, error: String?) {
        apiInProgress = false
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
                homeApiCall()
            }

            override fun onCancelButtonClicked() {

            }
        }).show()

    }

    private fun startDriverTimer(delay: Long) {
        if (timerDrivers != null) {
            timerDrivers.cancel()
            timerDrivers = Timer()
            timerDrivers.schedule(object : TimerTask() {
                override fun run() {
                    getDrivers()
                }
            }, delay)
        }
    }

    private fun getDrivers() {
        val jsonObject = JSONObject()
        jsonObject.put("type", CommonEventTypes.HOME_MAP_DRIVERS)
        jsonObject.put("access_token", SharedPrefs.get().getString(ACCESS_TOKEN_KEY, ""))
        jsonObject.put("latitude", lat)
        jsonObject.put("longitude", lng)
        jsonObject.put("app_language_id", SharedPrefs.get().getString(LANGUAGE_CODE, ""))
        jsonObject.put("category_id", homeActivity?.serviceRequestModel?.category_id)
        if (ConfigPOJO.is_asap == "true") {
            jsonObject.put("distance", (ConfigPOJO.distance_search_start.toInt() + (ConfigPOJO.search_count.toInt() * ConfigPOJO.distance_search_increment.toInt())).toString())
        } else {
            jsonObject.put("distance", 500000)
        }
        AppSocket.get().emit(Events.COMMON_EVENT, jsonObject, Ack {
            Log.e("Drivers", it[0].toString() + "ddssdf" + jsonObject)
            Handler(Looper.getMainLooper()).post {
                if (isVisible) {
                    val typeToken = object : TypeToken<ApiResponse<ServiceDetails>>() {}.type
                    val response = Gson().fromJson(it[0].toString(), typeToken) as ApiResponse<ServiceDetails>
                    if (response.statusCode == SUCCESS_CODE) {
                        val drivers = response.result?.drivers
                        if (drivers?.isEmpty() == false) {
                            homeActivity?.serviceRequestModel?.driverList?.addAll(drivers)
                            presenter.getNearestRoadPoints(drivers)
                        } else {
                            setDriversOnMap(drivers)
                        }
                    }
                }
            }
        })
        //startDriverTimer(10000)
    }

    private fun setDriversOnMap(drivers: List<HomeDriver>?) {
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
                    marker?.catrgory_brand_id = driver?.category_brand_id ?: 0
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
//                            marker?.moveValueAnimate?.cancel()
                            marker?.moveValueAnimate = animateMarker(it, LatLng(driver.latitude
                                    ?: 0.0, driver.longitude
                                    ?: 0.0))
//                            marker?.rotateValueAnimator?.cancel()
                            marker?.rotateValueAnimator = rotateMarker(it, LatLng(driver.latitude
                                    ?: 0.0, driver.longitude
                                    ?: 0.0), bearing)
                        }
                    }
                }
            }
            if (!isDriverFound) {
                val driverMarker = createDriverMarker(driver, getVehicleResId(driver.category_id))
                driverMarker?.isFlat = true
                val homeMarkers = HomeMarkers()
                homeMarkers.marker = driverMarker
                homeMarkers.catrgory_brand_id = driver?.category_brand_id ?: 0
                homeMarkers.category_id = driver?.category_id ?: 0
                updatedMarkers.add(homeMarkers)
            }
        }

        markersList.forEach {
            it?.marker?.remove()
        }
        markersList.clear()
        markersList.addAll(updatedMarkers)
    }

    private fun createDriverIconByCategory(category_brand_id: Int, driverMarker: Marker) {
        when (category_brand_id) {
            24 -> {
                driverMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_auto_icon)))
            }
            25 -> {
                driverMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_car_icon)))
            }
            23 -> {
                driverMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_bike_icon)))
            }
            27 -> {
                driverMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_cycle_icon)))
            }
            else -> {
                driverMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_car_icon)))
            }
        }
    }

    private fun createDriverIconByCategoryMyokada(category_brand_id: Int, driverMarker: Marker) {
        when (category_brand_id) {
            4 -> {
                driverMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_del)))
            }
            7 -> {
                driverMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_bike)))
            }
            12 -> {
                driverMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_tri)))
            }
            14 -> {
                driverMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_auto)))
            }
            else -> {
                driverMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_car_cab)))
            }
        }
    }

    private fun createDriverMarker(driver: HomeDriver?, iconResID: Int): Marker? {
        val marker = map?.addMarker(MarkerOptions()
                .position(LatLng(driver?.latitude ?: 0.0, driver?.longitude ?: 0.0))
                .anchor(0.5f, 0.5f)
                .rotation(driver?.bearing ?: 0f))
        if (ConfigPOJO.is_asap == "true") {
            createDriverIconByCategory(driver?.category_brand_id!!, marker!!)
        } else if (SECRET_DB_KEY == "eecf5a33e8575b1a860cc17dd778ea6f" ||
                SECRET_DB_KEY == "456049b71e28127ccd109b3fa9392fdb") {
            createDriverIconByCategoryMyokada(driver?.category_id!!, marker!!)
        } else {
            scaleDownMarker(marker, (currentZoomLevel / 14f), driver?.icon_image_url ?: "")
        }
        marker?.tag = driver?.user_detail_id
        return marker
    }

    private fun removeAllDriverMarkers() {
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

    fun animateMarker(marker: Marker?, latLng: LatLng?): ValueAnimator? {
        var valueAnimatorMove: ValueAnimator? = null
        if (marker != null) {
            val startPosition = marker.position
            val endPosition = LatLng(latLng?.latitude ?: 0.0, latLng?.longitude ?: 0.0)
            val latLngInterpolator = LatLngInterpolator.LinearFixed()
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
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            })
            valueAnimatorMove?.start()
        }
        return valueAnimatorMove
    }

    private fun rotateMarker(marker: Marker?, latLng: LatLng?, bearing: Float?): ValueAnimator? {
        var valueAnimatorRotate: ValueAnimator? = null
        if (marker != null) {
            val startRotation = marker.rotation
            valueAnimatorRotate = ValueAnimator.ofFloat(0f, 1f)
            valueAnimatorRotate?.duration = 5000 // duration 1 second
            valueAnimatorRotate?.interpolator = LinearInterpolator()
            valueAnimatorRotate?.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    try {
                        val v = animation.animatedFraction
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

    private interface LatLngInterpolator {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng
        class LinearFixed : LatLngInterpolator {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        newContactModel.clear()

        if (resultCode == Activity.RESULT_OK && requestCode == Constants.QR_CODE_REQUEST_CODE && data != null) {
            qrCodeData = data.getParcelableExtra<RoadPickupResponse>("data")

            rvCompanies.visibility = View.GONE
            v_line.visibility = View.GONE
            viewSelected.visibility = View.GONE
            tvSelectedService.visibility = View.GONE
            homeApiCall()
        } else if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_PICK_CONTACT && data != null) {
            selectedContactsList = data.getSerializableExtra("contacts") as ArrayList<ContactModel>
            var tm = activity!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var countryCode = tm.getSimCountryIso()

            countryCode = ConfigPOJO.defaultCoutryCode

            for (item in selectedContactsList) {
                item.phoneNumber = AppUtils.getCountryCodeFromPhoneNumber(item.phoneNumber
                        ?: "", countryCode.toUpperCase()).replace("//s".toRegex(), "")
            }

            for (i in 0..selectedContactsList.size - 1) {
                if (!TextUtils.isEmpty(selectedContactsList.get(i).phoneCode)) {
                    nnewContactModel = NewContactModel(selectedContactsList.get(i).phoneNumber.toString().trim(), selectedContactsList.get(i).phoneCode.toString(), selectedContactsList.get(i).name.toString().trim())
                } else {
                    var phoneNumb = selectedContactsList.get(i).phoneNumber.toString()
                    nnewContactModel = NewContactModel(phoneNumb.substring(phoneNumb.indexOf(" ") + 1), countryCode.toUpperCase(), selectedContactsList.get(i).name.toString().trim())
                }
                newContactModel.add(nnewContactModel!!)
            }


            etGiftPhoneNumber?.setText(newContactModel[0].phone_number?.replace(" ", "")?.trim().toString())
            etGiftFriendName?.setText(newContactModel[0].name)
        } else if (requestCode == Constants.PLACEAUTOCOMPLETE_REQUESTCODE && resultCode == Activity.RESULT_OK) {
            val place = if (data?.hasExtra("place") ?: false) {
                data?.getParcelableExtra<Place>("place")
            } else {
                Autocomplete.getPlaceFromIntent(data ?: Intent())
            }
            val addressData = place?.address ?: ""
            val placeLatLng = place?.latLng
            Log.e(TAG, "Place Lat : " + placeLatLng?.latitude + " Place Lon : " + placeLatLng?.longitude)

            lat = placeLatLng?.latitude ?: 0.0
            lng = placeLatLng?.longitude ?: 0.0
            address = addressData

            top_where_to.text = address
            homeActivity?.serviceRequestModel?.pickup_longitude = lng
            homeActivity?.serviceRequestModel?.pickup_latitude = lat
            homeActivity?.serviceRequestModel?.pickup_address = address
            homeActivity?.serviceRequestModel?.dropoff_longitude = lng
            homeActivity?.serviceRequestModel?.dropoff_latitude = lat
            homeActivity?.serviceRequestModel?.dropoff_address = address

            focusOnCurrentLocation(lat, lng)
        }
    }

    private fun QRScanResultAction() {
        homeActivity?.serviceRequestModel?.bookingType = BOOKING_TYPE.ROAD_PICKUP
        homeActivity?.serviceRequestModel?.driverId = qrCodeData?.userId.toString()
        homeActivity?.serviceRequestModel?.userId = qrCodeData?.user?.userId.toString()
        homeActivity?.serviceRequestModel?.category_id = qrCodeData?.categoryId
        homeActivity?.serviceRequestModel?.category_brand_id = qrCodeData?.categoryBrandId
        homeActivity?.serviceRequestModel?.category_brand_product_id = qrCodeData?.userDriverDetailProducts?.get(0)?.categoryBrandProductId
        val list = (activity as HomeActivity).serviceDetails?.categories as? ArrayList

        for (item in list ?: ArrayList()) {
            if (item.category_brand_id == qrCodeData?.categoryBrandId) {
                for (j in item.products ?: ArrayList()) {
                    if (j.category_brand_product_id == qrCodeData?.userDriverDetailProducts?.get(0)?.categoryBrandProductId) {
                        homeActivity?.serviceRequestModel?.selectedProduct = j
                    }
                }

                val product = item.products?.get(0)
                homeActivity?.serviceRequestModel?.productName = product?.name
                homeActivity?.serviceRequestModel?.seating_capacity = product?.seating_capacity
                val distance = homeActivity?.serviceRequestModel?.order_distance
                val totalTime = homeActivity?.serviceRequestModel?.eta
                val totalPrice = (distance?.times(product?.price_per_distance ?: 0.0f))
                        ?.plus((totalTime?.times(product?.price_per_hr ?: 0.0f) ?: 0.0f))
                        ?.plus(product?.actual_value ?: 0.0f)
                homeActivity?.serviceRequestModel?.final_charge = totalPrice?.toDouble()
                break
            }
        }
        setWhereToClick(BOOKING_TYPE.ROAD_PICKUP)
    }

    private val networkBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "update") {
                homeApiCall()
            }
        }
    }

    private fun setUpBottomSheetDialog() {
        val layoutDialog = View.inflate(context, R.layout.dialog_where_u_going, null)
        dialog = BottomSheetDialog(context as Activity)
        dialog?.setContentView(layoutDialog)
        val bottomSheetInternal = dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet) as View?

        bottomSheetInternal?.let {
            BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
        }
        val fullName = (layoutDialog.findViewById<EditText>(R.id.etFullName))
        val tvHeader = (layoutDialog.findViewById<TextView>(R.id.tvHeader))
        val relationShip = (layoutDialog.findViewById<EditText>(R.id.etRelationStatus))
        val tvRelationStatus = (layoutDialog.findViewById<TextView>(R.id.tvRelationStatus))
        val phoneNumber = (layoutDialog.findViewById<EditText>(R.id.etPhone))
        val countryCodePicker = (layoutDialog.findViewById<CountryCodePicker>(R.id.countryCodePicker))
        val btnContinue = (layoutDialog.findViewById<TextView>(R.id.tvContinue))
        val tvCancel = (layoutDialog.findViewById<TextView>(R.id.tvCancel))
        val cvCountry = (layoutDialog.findViewById<LinearLayout>(R.id.cvCountry))
        val ivAddContacts = (layoutDialog.findViewById<ImageView>(R.id.ivAddContacts))

        tvCancel.visibility = View.VISIBLE
        tvHeader.setText(getString(R.string.send_a_gift))
        phoneNumber.setHint(getString(R.string.enter_phone_number_login))

        if (ConfigPOJO.is_gift == "true") {
            relationShip.visibility = View.GONE
            tvRelationStatus.visibility = View.GONE
        }

        fullName.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        relationShip.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        phoneNumber.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        cvCountry.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        tvCancel.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        btnContinue.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        tvCancel.setTextColor(Color.parseColor(ConfigPOJO.primary_color))

        if (ConfigPOJO.settingsResponse?.key_value?.iso_code?.length == 2) {
            countryCodePicker.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.toLowerCase())
        } else {
            countryCodePicker.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                    ?: 0)?.toLowerCase())
        }
        countryCodePicker.setNumberAutoFormattingEnabled(false)
        countryCodePicker.registerCarrierNumberEditText(phoneNumber) // registers the EditText with the country code picker


        etGiftFriendName = fullName
        etGiftPhoneNumber = phoneNumber

        tvCancel.setOnClickListener {
            dialog?.dismiss()
        }

        ivAddContacts.setOnClickListener {
            getUserContactsWithPermissionCheck()
        }

        btnContinue.setOnClickListener {
            countryCodePicker.setPhoneNumberValidityChangeListener(phoneNoValidationChangeListener)
            when {
                fullName.text.toString().trim().isEmpty() -> Toast.makeText(btnContinue.context, getString(R.string.fullname_empty_validation_message), Toast.LENGTH_SHORT).show()
                phoneNumber.text.toString().trim().isEmpty() -> Toast.makeText(btnContinue.context, getString(R.string.phone_empty_validation_message), Toast.LENGTH_SHORT).show()
                ValidationUtils.isPhoneNumberAllZeros(phoneNumber.text.toString()) -> Toast.makeText(btnContinue.context, getString(R.string.phone_valid_validation_message), Toast.LENGTH_SHORT).show()
                else -> {
                    homeActivity?.serviceRequestModel?.isGifted = "1"
                    homeActivity?.serviceRequestModel?.bookingType = BOOKING_TYPE.GIFT
                    homeActivity?.serviceRequestModel?.bookingFriendName = fullName.text.toString().trim()
                    homeActivity?.serviceRequestModel?.bookingFriendPhoneNumber = phoneNumber.text.toString().trim()
                    homeActivity?.serviceRequestModel?.bookingFriendCountryCode = countryCodePicker.selectedCountryCodeWithPlus
                    val fragment = SelectVehicleTypeFragment()
                    var bundle = Bundle()
                    fragment.arguments = bundle
                    fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                    dialog?.dismiss()
                }
            }
        }
        dialog?.show()
    }

    private val phoneNoValidationChangeListener = CountryCodePicker.PhoneNumberValidityChangeListener {
        isPhoneNoValid = it
    }

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    fun getUserContacts() {
        launchContactActivity()
    }

    private fun launchContactActivity() {
        startActivityForResult(Intent(activity!!, ContactsActivity::class.java), Constants.REQUEST_CODE_PICK_CONTACT)
//        startActivityForResult(Intent(this,SosActivity :: class.java),SOS)
    }

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
                        .setCountry(country_code).build(context as Activity)
            } else {
                intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(context as Activity)
            }

            startActivityForResult(intent, requestCode)
        } catch (e: GooglePlayServicesRepairableException) {
            Log.e(TAG, "Exception : " + e.message)
        } catch (e: GooglePlayServicesNotAvailableException) {
            Log.e(TAG, "Exception : " + e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Exception : " + e.message)
        }
    }
}

interface ServicesDetailInterface {
    fun getDriverData(driversList: ArrayList<HomeDriver>)
}