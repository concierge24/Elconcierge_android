package com.trava.user.ui.home

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Location
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.JointType.ROUND
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.maps.android.ui.IconGenerator
import com.razorpay.PaymentResultListener
import com.trava.user.AppVersionConstants
import com.trava.user.R
import com.trava.user.databinding.ActivityHomeBinding
import com.trava.user.databinding.ActivityHomeSummerBinding
import com.trava.user.ui.home.complete_ride.CompleteRideFragment
import com.trava.user.ui.home.complete_ride.InvoiceRequestPresenter
import com.trava.user.ui.home.deliverystarts.paymentview.ThawaniPaymentInterface
import com.trava.user.ui.home.processingrequest.ProcessingRequestFragment
import com.trava.user.ui.home.rating.RatingFragment
import com.trava.user.ui.home.services.ServiceContract
import com.trava.user.ui.home.services.ServicePresenter
import com.trava.user.ui.home.services.ServicesDetailInterface
import com.trava.user.ui.home.services.ServicesFragment
import com.trava.user.ui.home.stories.WatchStories
import com.trava.user.ui.home.vehicles.SelectVehicleTypeFragment
import com.trava.user.ui.home.wallet.UserWalletActivity
import com.trava.user.ui.menu.ReferralActivity
import com.trava.user.ui.menu.bookings.BookingsActivity
import com.trava.user.ui.menu.contactUs.ContactUsActivity
import com.trava.user.ui.menu.earnings.MyEarningsActivity
import com.trava.user.ui.menu.emergencyContacts.EContactsActivity
import com.trava.user.ui.menu.noticeBoard.NoticeBoardActivity
import com.trava.user.ui.menu.settings.SettingsActivity
import com.trava.user.ui.menu.settings.editprofile.EditProfileActivity
import com.trava.user.ui.menu.travelPackages.TravelPackagesActivity
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction.setStatusBarColor
import com.trava.user.webservices.ApiResponseNew
import com.trava.user.webservices.models.GeoFenceData
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.homeapi.HomeDriver
import com.trava.user.webservices.models.homeapi.ServiceDetails
import com.trava.user.webservices.models.homeapi.TerminologyData
import com.trava.user.webservices.models.order.Order
import com.trava.user.webservices.models.travelPackages.PackagesItem
import com.trava.utilities.*
import com.trava.utilities.Constants.SUMMER_APP_BASE
import com.trava.utilities.constants.*
import com.trava.utilities.location.LocationProvider
import com.trava.utilities.webservices.models.AppDetail
import com.trava.utilities.webservices.models.CurrentLocationModel
import com.trava.utilities.webservices.models.LoginModel
import com.trava.utilities.webservices.models.Service
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.nav_header_main2.*
import kotlinx.android.synthetic.main.nav_header_main2.view.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set
import kotlin.system.exitProcess


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, HomeContract.View, ServicesDetailInterface,
        ServiceContract.View, PaymentResultListener, ThawaniPaymentInterface {

    companion object {
        var isFromPackage: Boolean = false
        var fullTrackArray = ArrayList<LatLng>()
        val polygonData: ArrayList<GeoFenceData> = ArrayList()
    }

    lateinit var mediaProjectionManager: MediaProjectionManager

    private val invoicePresenter = InvoiceRequestPresenter()
    private var location: Location? = null
    var fragment: ServicesFragment? = null

    private val presenter = HomePresenter()
    private val servicePresenter = ServicePresenter()

    var dialogIndeterminate: DialogIndeterminate? = null

    var srcMarker: Marker? = null

    var destMarker: Marker? = null

    var mapFragment: SupportMapFragment? = null

    var servicesFragment: ServicesFragment? = ServicesFragment()
    var selectVehicleTypeFragmentManager: SelectVehicleTypeFragment? = SelectVehicleTypeFragment()

    var serviceDetails: ServiceDetails? = null

    var serviceRequestModel = ServiceRequestModel()

    var doubleBackToExitPressedOnce = false

    var googleMapHome: GoogleMap? = null

    lateinit var locationProvider: LocationProvider

    private var blackPolyLine: Polyline? = null
    private var greyPolyLine: Polyline? = null
    private var nav_view: NavigationView? = null
    private var drawer_layout: DrawerLayout? = null
    private var tvLogout: TextView? = null

    private var list: ArrayList<LatLng>? = null
    lateinit var homeBinding: ActivityHomeBinding
    lateinit var homeSummerBinding: ActivityHomeSummerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == Constants.DELIVERY20) {
            homeSummerBinding = DataBindingUtil.setContentView(this, R.layout.activity_home_summer)
            homeSummerBinding.color = ConfigPOJO.Companion

            val statusColor = Color.parseColor(ConfigPOJO.black_color)
            setStatusBarColor(this, statusColor)
        } else if (ConfigPOJO.TEMPLATE_CODE == Constants.TRAVE_APP_BASE ||
                ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE ||
                ConfigPOJO.TEMPLATE_CODE == Constants.MOVER ||
                ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            homeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
            homeBinding.color = ConfigPOJO.Companion

            val statusColor = Color.parseColor(ConfigPOJO.headerColor)
            setStatusBarColor(this, statusColor)
        } else {

            homeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
            homeBinding.color = ConfigPOJO.Companion

            val statusColor = Color.parseColor(ConfigPOJO.headerColor)
            setStatusBarColor(this, statusColor)

        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        nav_view = findViewById(R.id.nav_view)
        drawer_layout = findViewById(R.id.drawer_layout)
        tvLogout = findViewById(R.id.tvLogout)

        presenter.attachView(this)
        servicePresenter.attachView(this)
        dialogIndeterminate = DialogIndeterminate(this)
        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            presenter.onGetPolygon()
        }
        try {
            val version = packageManager.getPackageInfo(packageName, 0).versionName
            tvVersionName?.text = "${getString(R.string.version)} $version"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        AppSocket.get().init() // Initialize socket
        FirebaseApp.initializeApp(this) // Initialise firebase app
        locationProvider = LocationProvider.CurrentLocationBuilder(this).build()
        locationProvider.getLastKnownLocation(OnSuccessListener {
            try {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStackImmediate("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                finishAffinity()
            }

            if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
                getMapAsync()
                homeApiCall("0.0", "0.0")
            } else {
                servicesFragment?.let { it1 ->
                    it1.setListener(this)
                    val bundle = Bundle()
                    if (intent.hasExtra("from_chat")) {
                        bundle.putString("order_id", intent.getStringExtra("order_id"))
                        bundle.putString("from", intent.getStringExtra("from_chat"))
                    } else if (intent.hasExtra("for") && intent.getStringExtra("for").equals("book_ride", false)) {
                        bundle.putString("from", "book_ride")
                        bundle.putString("destination", intent.getStringExtra("destination"))
                    } else if (intent.hasExtra("from_booking_detail")) {
                        bundle.putString("from", "booking_details")
                        bundle.putString("order_id", intent.getStringExtra("order_id"))
                    } else if (intent.hasExtra("home_activity")) {
                        bundle.putString("from", "home_activity")
                    } else {
                        bundle.putString("from", "nothing")
                    }
                    it1.arguments = bundle
                    supportFragmentManager.beginTransaction().add(R.id.container, it1).addToBackStack("backstack").commitAllowingStateLoss()
                }
                mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map) as SupportMapFragment
            }
        })

        setNavMenuItemThemeColors(Color.parseColor(ConfigPOJO.primary_color))
        nav_view?.setNavigationItemSelectedListener(this)

        drawer_layout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        setSupportServices()
        setListener()

        if (intent.hasExtra(Constants.PACKAGE_DATA)) {
            val pkgData = intent.getParcelableExtra<PackagesItem>(Constants.PACKAGE_DATA)
            serviceRequestModel.pkgData = pkgData
            isFromPackage = true

        } else {
            isFromPackage = false
        }

        if (intent.hasExtra(Constants.CATEGORY_DATA)) {
            serviceRequestModel.category_id = intent?.getParcelableExtra<Service>(Constants.CATEGORY_DATA)?.category_id
        }

        if (Constants.SECRET_DB_KEY == "3d7b9c3c1ee5f4d45e391122d21c20298c2e2f089ba3e9b1fde322041c5023a2"
                || Constants.SECRET_DB_KEY == "3d7b9c3c1ee5f4d45e391122d21c20298c2e2f089ba3e9b1fde322041c5023a2") {
            covidAlert()
        }

        if(Constants.SECRET_DB_KEY=="951ee438b7d94b263e10e0d81f382303")
        {
            tvLogout?.visibility=View.VISIBLE
        }
    }

    fun hideMenu() {
        if (serviceRequestModel.bookingFlow == "1") {
            nav_view?.menu?.clear()
            var i: Int = 0
            nav_view?.itemIconTintList = null;
            nav_view?.menu?.add(0, R.id.nav_home, i, R.string.home)?.setIcon(R.drawable.ic_home)
            nav_view?.menu?.findItem(R.id.nav_home)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }

            if (ConfigPOJO.dynamicBar?.my_bookings==true) {
                i++
                if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE) {
                    nav_view?.menu?.add(0, R.id.nav_bookings, i, R.string.my_deliveries)?.setIcon(R.drawable.ic_bookings)
                } else {
                    nav_view?.menu?.add(0, R.id.nav_bookings, i, R.string.my_bookings)?.setIcon(R.drawable.ic_bookings)
                }
                nav_view?.menu?.findItem(R.id.nav_bookings)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }

            if (ConfigPOJO.is_merchant == "true") {
                i++
                nav_view?.menu?.add(0, R.id.nav_myearnings, i, getString(R.string.my_earnings))?.setIcon(R.drawable.ic_refer_earn)
                nav_view?.menu?.findItem(R.id.nav_myearnings)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }

                i++
                nav_view?.menu?.add(0, R.id.nav_watch_earn, i, getString(R.string.watch_amp_earn))?.setIcon(R.drawable.ic_car_icon_new)
                nav_view?.menu?.findItem(R.id.nav_watch_earn)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }

            if (ConfigPOJO.dynamicBar?.app_notifications==true) {
                i++
                nav_view?.menu?.add(0, R.id.nav_noticeBoard, i, R.string.notification)?.setIcon(R.drawable.ic_notify)
                nav_view?.menu?.findItem(R.id.nav_noticeBoard)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }

            if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                i++
                nav_view?.menu?.add(0, R.id.nav_deleiver, i, getString(R.string.del_with_us))
//                tintMenuIcon(this, nav_view?.menu?.findItem(R.id.nav_deleiver)!!,Color.parseColor(ConfigPOJO.secondary_color))
            }
            if (ConfigPOJO?.settingsResponse?.key_value?.share_with_friend_url != null) {
                i++
                nav_view?.menu?.add(0, R.id.nav_discounts, i, R.string.get_discounts)?.setIcon(R.drawable.ic_menu_share)
                nav_view?.menu?.findItem(R.id.nav_discounts)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
            if (ConfigPOJO?.settingsResponse?.key_value?.become_driver_url != null) {
                i++
                nav_view?.menu?.add(0, R.id.becomedriver, i, getString(R.string.become_driver))
            }
            if (ConfigPOJO.dynamicBar?.emergency_contacts==true) {
                i++
                nav_view?.menu?.add(0, R.id.nav_emergency_contact, i, R.string.emergency_contacts)?.setIcon(R.drawable.ic_emergency)
                nav_view?.menu?.findItem(R.id.nav_emergency_contact)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }

            if (ConfigPOJO.dynamicBar?.is_wallet==true) {
                i++
                nav_view?.menu?.add(0, R.id.nav_wallet, i, R.string.my_wallet)?.setIcon(R.drawable.ic_wallet)
                nav_view?.menu?.findItem(R.id.nav_wallet)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
            if (ConfigPOJO.dynamicBar?.is_Refer_and_Earn==true) {
                i++
                nav_view?.menu?.add(0, R.id.nav_refer, i, R.string.refer_earn)?.setIcon(R.drawable.ic_refer_earn)
                nav_view?.menu?.findItem(R.id.nav_refer)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
            /*   if (ConfigPOJO.is_gift == "true") {
                   i++
                   nav_view?.menu?.add(0, R.id.nav_mygifts, i, R.string.my_gifts)?.setIcon(R.drawable.ic_refer_earn)
                   tintMenuIcon(this, nav_view?.menu?.findItem(R.id.nav_mygifts)!!, Color.parseColor(ConfigPOJO.secondary_color))
               }*/
            if (ConfigPOJO.dynamicBar?.app_settings==true) {
                i++
                nav_view?.menu?.add(0, R.id.nav_settings, i, R.string.settings)?.setIcon(R.drawable.ic_settings)
                nav_view?.menu?.findItem(R.id.nav_settings)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
            if (ConfigPOJO.dynamicBar?.contact_us==true) {
                i++
                nav_view?.menu?.add(0, R.id.nav_help, i, R.string.help)?.setIcon(R.drawable.ic_help)
                nav_view?.menu?.findItem(R.id.nav_help)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
        } else {
            nav_view?.menu?.clear()
            var i: Int = 0
            nav_view?.menu?.add(0, R.id.nav_home, i, R.string.home)?.setIcon(R.drawable.ic_home)
            nav_view?.menu?.findItem(R.id.nav_home)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }

            if (ConfigPOJO.dynamicBar?.my_bookings==true) {
                i++
                if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE) {
                    nav_view?.menu?.add(0, R.id.nav_bookings, i, R.string.my_deliveries)?.setIcon(R.drawable.ic_bookings)
                } else {
                    nav_view?.menu?.add(0, R.id.nav_bookings, i, R.string.my_bookings)?.setIcon(R.drawable.ic_bookings)
                }
                nav_view?.menu?.findItem(R.id.nav_bookings)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
            //for freerydz
            if (ConfigPOJO.is_merchant == "true") {
                i++
                nav_view?.menu?.add(0, R.id.nav_myearnings, i, getString(R.string.my_earnings))?.setIcon(R.drawable.ic_refer_earn)
                nav_view?.menu?.findItem(R.id.nav_myearnings)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }

                i++
                nav_view?.menu?.add(0, R.id.nav_watch_earn, i, getString(R.string.watch_amp_earn))?.setIcon(R.drawable.ic_car_icon_new)
                nav_view?.menu?.findItem(R.id.nav_watch_earn)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
            if (ConfigPOJO.dynamicBar?.emergency_contacts==true) {
                i++
                nav_view?.menu?.add(0, R.id.nav_emergency_contact, i, R.string.emergency_contacts)?.setIcon(R.drawable.ic_emergency)
                nav_view?.menu?.findItem(R.id.nav_emergency_contact)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
            if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                i++
                nav_view?.menu?.add(0, R.id.nav_deleiver, i, getString(R.string.del_with_us))
            }
            if (ConfigPOJO?.settingsResponse?.key_value?.share_with_friend_url != null) {
                i++
                nav_view?.menu?.add(0, R.id.nav_discounts, i, R.string.get_discounts)?.setIcon(R.drawable.ic_menu_share)
                nav_view?.menu?.findItem(R.id.nav_discounts)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
            if (ConfigPOJO?.settingsResponse?.key_value?.become_driver_url != null) {
                i++
                nav_view?.menu?.add(0, R.id.becomedriver, i, getString(R.string.become_driver))
            }
            if (ConfigPOJO.dynamicBar?.app_notifications==true) {
                i++
                nav_view?.menu?.add(0, R.id.nav_noticeBoard, i, R.string.notification)?.setIcon(R.drawable.ic_notify)
                nav_view?.menu?.findItem(R.id.nav_noticeBoard)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
            if (ConfigPOJO.dynamicBar?.is_wallet==true) {
                i++
                nav_view?.menu?.add(0, R.id.nav_wallet, i, R.string.my_wallet)?.setIcon(R.drawable.ic_wallet)
                nav_view?.menu?.findItem(R.id.nav_wallet)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
            if (ConfigPOJO.dynamicBar?.travel_packages==true) {
                i++
                nav_view?.menu?.add(0, R.id.nav_packages, i, R.string.packages)?.setIcon(R.drawable.ic_home)
                nav_view?.menu?.findItem(R.id.nav_packages)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
            if (ConfigPOJO.dynamicBar?.is_Refer_and_Earn==true) {
                i++
                nav_view?.menu?.add(0, R.id.nav_refer, i, R.string.refer_earn)?.setIcon(R.drawable.ic_refer_earn)
                nav_view?.menu?.findItem(R.id.nav_refer)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
            /*if (ConfigPOJO.is_gift == "true") {
                i++
                nav_view?.menu?.add(0, R.id.nav_mygifts, i, R.string.my_gifts)?.setIcon(R.drawable.ic_refer_earn)
                tintMenuIcon(this, nav_view?.menu?.findItem(R.id.nav_mygifts)!!, Color.parseColor(ConfigPOJO.secondary_color))
            }*/
            if (ConfigPOJO.dynamicBar?.app_settings==true) {
                i++
                nav_view?.menu?.add(0, R.id.nav_settings, i, R.string.settings)?.setIcon(R.drawable.ic_settings)
                nav_view?.menu?.findItem(R.id.nav_settings)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
            if (ConfigPOJO.dynamicBar?.contact_us==true) {
                i++
                nav_view?.menu?.add(0, R.id.nav_help, i, R.string.help)?.setIcon(R.drawable.ic_help)
                nav_view?.menu?.findItem(R.id.nav_help)?.let { tintMenuIcon(this, it, Color.parseColor(ConfigPOJO.secondary_color)) }
            }
        }
    }

    fun setNavMenuItemThemeColors(color: Int) {
        //Setting default colors for menu item Text and Icon
        val navDefaultTextColor = Color.parseColor(ConfigPOJO.black_color)
        val navDefaultIconColor = Color.parseColor(ConfigPOJO.secondary_color)
        //Defining ColorStateList for menu item Text
        val navMenuTextList = ColorStateList(
                arrayOf<IntArray>(intArrayOf(android.R.attr.state_checked), intArrayOf(android.R.attr.state_enabled), intArrayOf(android.R.attr.state_pressed), intArrayOf(android.R.attr.state_focused), intArrayOf(android.R.attr.state_pressed)),
                intArrayOf(color, navDefaultTextColor, navDefaultTextColor, navDefaultTextColor, navDefaultTextColor)
        )
        //Defining ColorStateList for menu item Icon
        val navMenuIconList = ColorStateList(
                arrayOf<IntArray>(intArrayOf(android.R.attr.state_checked), intArrayOf(android.R.attr.state_enabled), intArrayOf(android.R.attr.state_pressed), intArrayOf(android.R.attr.state_focused), intArrayOf(android.R.attr.state_pressed)),
                intArrayOf(color, navDefaultIconColor, navDefaultIconColor, navDefaultIconColor, navDefaultIconColor)
        )

        try {
            nav_view?.setItemTextColor(navMenuTextList)
            nav_view?.setItemIconTintList(navMenuIconList)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun setListener() {
        tvLogout?.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.app_name))
            builder.setMessage(getString(R.string.logout_confirmation))
            builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                dialog?.dismiss()
                if (CheckNetworkConnection.isOnline(this)) {
                    presenter.logout()
                } else {
                    Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton(R.string.no) { dialog, _ -> dialog?.dismiss() }
            builder.show()
        }
    }

    private fun covidAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.app_name))
        builder.setMessage("Mask is required for both drivers and passengers.")
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            dialog?.dismiss()
        }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        setHeaderView()
        val nMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nMgr.cancelAll() // Clears all the notifications in the system notification tray if exists
        updateDataApiCall((location?.latitude ?: 0.0).toString(), (location?.longitude
                ?: 0.0).toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
        servicePresenter.detachView()
    }

    override fun onBackPressed() {
        if (drawer_layout?.isDrawerOpen(GravityCompat.START)==true) {
            drawer_layout?.closeDrawer(GravityCompat.START)
        } else {
            when {
                supportFragmentManager.fragments.find { it is ProcessingRequestFragment } != null -> {
                    // Do Nothing
                }

                supportFragmentManager.fragments.find {
                    it is CompleteRideFragment
                } != null -> {
                    /* Do Nothing */
                }
                supportFragmentManager.backStackEntryCount == 1 -> {
                    if (serviceRequestModel.pkgData != null) {
                        if (serviceRequestModel.isDriverAccepted) {
                            if (doubleBackToExitPressedOnce) {
                                finishAffinity()
                                exitProcess(0)
                            } else {
                                Toast.makeText(this, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show()
                                setBackPressedOnce()
                            }
                        } else {
                            finish()
                        }
                    } else {
                        if (doubleBackToExitPressedOnce) {
                            finish()
                        } else {
                            Toast.makeText(this, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show()
                            setBackPressedOnce()
                        }
                    }
                }
                else -> super.onBackPressed()
            }
        }
    }

    /* handles the double back press time to exit the app*/
    private fun setBackPressedOnce() {
        doubleBackToExitPressedOnce = true
        Handler().postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)    // changes the value back to false if user doesn't pressed again within given time
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        locationProvider.onActivityResult(requestCode, resultCode, data)

        val fragment = supportFragmentManager?.findFragmentById(R.id.container)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationProvider.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /* Sets the profile header in the side navigation panel*/

    private fun setHeaderView() {
        val profile = SharedPrefs.with(this).getObject(PROFILE, AppDetail::class.java)
        val viewHeader = nav_view?.getHeaderView(0)
        viewHeader?.tvName?.text = profile?.user?.name
        val rating = profile?.ratings_avg
        viewHeader?.ivProfilePic?.setRoundProfileUrl(profile?.profile_pic_url)
        viewHeader?.tvNumber?.text = String.format("%s", "${profile?.user_detail_id} ${"(" + getString(R.string.customerId) + ")"}")
        if (rating ?: 0 > 0) {
            viewHeader?.user_rating?.visibility = View.VISIBLE
            viewHeader?.tvRatingCount?.visibility = View.INVISIBLE
            viewHeader?.user_rating?.rating = profile?.ratings_avg?.toFloat() ?: 0f
            viewHeader?.tvRatingCount?.text = "· " + profile?.rating_count
        } else {
            viewHeader?.user_rating?.visibility = View.INVISIBLE
            viewHeader?.tvRatingCount?.visibility = View.INVISIBLE
        }


        if (ConfigPOJO.is_asap == "true") {
            viewHeader?.user_rating?.visibility = View.VISIBLE
            viewHeader?.tvRatingCount?.visibility = View.INVISIBLE
            if (profile?.ratings_avg == 0) {
                viewHeader?.user_rating?.rating = 5f
            } else {
                viewHeader?.user_rating?.rating = profile?.ratings_avg?.toFloat() ?: 0f
            }
            if (profile?.rating_count == 0) {
                viewHeader?.tvRatingCount?.text = "1"
            } else {
                viewHeader?.tvRatingCount?.text = profile?.rating_count?.toString()
            }
            viewHeader?.tvNumber?.visibility = View.GONE
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
            viewHeader?.tvNumber?.visibility = View.GONE
        }

        flProfilePic?.setOnClickListener {
            if (ConfigPOJO.TEMPLATE_CODE != Constants.MOBY) {
                startActivity(Intent(this, EditProfileActivity::class.java))
            }
        }

        edit_tv?.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            drawer_layout?.closeDrawer(GravityCompat.START)
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == Constants.DELIVERY20) {
            viewHeader?.ll_header_view?.setBackgroundColor(Color.parseColor(ConfigPOJO.white_color))
        } else if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            viewHeader?.ll_header_view?.setBackgroundResource(R.drawable.ic_bg_corsa)
        } else {
            viewHeader?.ll_header_view?.setBackgroundColor(Color.parseColor(ConfigPOJO.primary_color))
        }
    }

    private fun shareApp() {
        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
            val uri = Uri.parse("market://details?id=com.gomove.rider")
            var intent = Intent(Intent.ACTION_VIEW, uri)

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                intent = Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + AppVersionConstants.APPLICATION_ID))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "No play store or browser app", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            val uri = Uri.parse("market://details?id=" + AppVersionConstants.APPLICATION_ID)
            var intent = Intent(Intent.ACTION_VIEW, uri)

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                intent = Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + AppVersionConstants.APPLICATION_ID))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "No play store or browser app", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /* Callback to handles the selection from the options in the side navigation panel */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_promotions -> {
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
            }
            R.id.nav_bookings -> {
                startActivity(Intent(this, BookingsActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
            /*  R.id.nav_mygifts -> {
                  startActivity(Intent(this, MyGiftsActivity::class.java)
                          .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
              }*/
            R.id.nav_myearnings -> {
                startActivity(Intent(this, MyEarningsActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
            R.id.nav_watch_earn -> {
                startActivity(Intent(this, WatchStories::class.java))
            }
            R.id.nav_emergency_contact -> {
                startActivity(Intent(this, EContactsActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
            R.id.nav_packages -> {
                startActivity(Intent(this, TravelPackagesActivity::class.java)
                        .putExtra("via", "home"))
            }
            R.id.nav_noticeBoard -> {
                startActivity(Intent(this, NoticeBoardActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
            R.id.nav_editProfile -> {
                startActivity(Intent(this, EditProfileActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
            R.id.nav_discounts -> {
                try {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.setType("text/plain")
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                    var shareMessage = "\nLet me recommend you this application\n\n"
                    if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                        shareMessage = shareMessage + "https://user.gomove.co/eZAT/share" + "\n\n"
                    } else {
                        shareMessage = shareMessage + ConfigPOJO?.settingsResponse?.key_value?.share_with_friend_url
//                        shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + com.trava.user.BuildConfig.driverpackage + "\n\n"
                    }
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                    startActivity(Intent.createChooser(shareIntent, "choose one"))
                } catch (e: Exception) {
                    //e.toString();
                }
            }
            R.id.nav_deleiver -> {
                shareApp()
            }
            R.id.becomedriver -> {
                val uri = Uri.parse(ConfigPOJO?.settingsResponse?.key_value?.become_driver_url)
                var intent = Intent(Intent.ACTION_VIEW, uri)

                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    intent = Intent(Intent.ACTION_VIEW, Uri.parse(ConfigPOJO?.settingsResponse?.key_value?.become_driver_url))
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "No play store or browser app", Toast.LENGTH_LONG).show()
                    }
                }
            }
            R.id.nav_refer -> {
                startActivity(Intent(this, ReferralActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
            R.id.nav_wallet -> {
                startActivity(Intent(this, UserWalletActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
            R.id.nav_help -> {
                startActivity(Intent(this, ContactUsActivity::class.java))
            }
            R.id.nav_logout -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.app_name))
                builder.setMessage(getString(R.string.logout_confirmation))
                builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                    dialog?.dismiss()
                    if (CheckNetworkConnection.isOnline(this)) {
                        presenter.logout()
                    } else {
                        Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show()
                    }
                }
                builder.setNegativeButton(R.string.no) { dialog, _ -> dialog?.dismiss() }
                builder.show()
            }
            R.id.nav_home -> {
                finish()
                var inetnt = Intent(this, HomeActivity::class.java)
                intent.putExtra("home_activity", "home_activity")
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)

            }
        }

        drawer_layout?.closeDrawer(GravityCompat.START)
        return true
    }

    /* Callback providing google maps object showing map is ready to use after getMapAsync*/
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        googleMapHome = googleMap
        googleMapHome?.clear()
        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE) {
            googleMapHome?.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json))
        }

        if (ConfigPOJO.is_darkMap == "true") {
            googleMapHome?.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.dark_map_style))
        }
        googleMapHome?.uiSettings?.isCompassEnabled = false
//        googleMapHome?.setOnMarkerClickListener(this)
        val mapType = SharedPrefs.with(this).getInt(PREF_MAP_VIEW, GoogleMap.MAP_TYPE_NORMAL)
        googleMapHome?.mapType = mapType
        googleMapHome?.uiSettings?.isTiltGesturesEnabled = false // Disables the gesture to view 3D view of the google maps
        googleMapHome?.uiSettings?.isMyLocationButtonEnabled = false // Hides the default current locator button of google maps
        locationProvider = LocationProvider.LocationUpdatesBuilder(this).build()
        locationProvider.getLastKnownLocation(OnSuccessListener { location ->
            /* Gets the last known location of the user*/
            location?.let {
//                homeApiCall(location.latitude.toString(), location.longitude.toString())
                this@HomeActivity.location = location
                googleMapHome?.isMyLocationEnabled = true
                updateDataApiCall((location.latitude).toString(), (location.longitude).toString())
                googleMapHome?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 14f))
                locationProvider.getAddressFromLatLng(location.latitude, location.longitude, object : LocationProvider.OnAddressListener {
                    override fun getAddress(address: String, result: List<Address>) {
                        /* gets the address from the current user's location*/
                        val currentLocation = CurrentLocationModel(location.latitude, location.longitude, address)
                        SharedPrefs.with(this@HomeActivity).save(PrefsConstant.CURRENT_LOCATION, currentLocation)
                        if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
                            selectVehicleTypeFragmentManager?.onMapReady(googleMap)
                        } else {
                            servicesFragment?.onMapReady(googleMap)
                        }
                    }
                })
            }
        })
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker?.equals(srcMarker) == true) {
            srcMarker?.showInfoWindow()
        } else if (marker?.equals(destMarker) == true) {
            destMarker?.showInfoWindow()
        }
        return true

    }

    private fun homeApiCall(lat: String, lng: String) {
        val map = HashMap<String, String>()
        if (ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
            serviceRequestModel.category_id = 7

        } else {
            if (ConfigPOJO.TEMPLATE_CODE == Constants.NO_TEMPLATE) {
                logoutSuccess()
                Toast.makeText(this, R.string.session_expired_please_login_again, Toast.LENGTH_SHORT).show()
            } else {
                serviceRequestModel.category_id = 4
            }
        }
//        }

        map["category_id"] = serviceRequestModel.category_id.toString()

        map["latitude"] = lat.toString()
        map["longitude"] = lng.toString()
        if (ConfigPOJO.is_asap == "true") {
            map["distance"] = (ConfigPOJO.distance_search_start.toInt() + (ConfigPOJO.search_count.toInt() * ConfigPOJO.distance_search_increment.toInt())).toString()
        } else {
            map["distance"] = "500000"
        }
        if (CheckNetworkConnection.isOnline(this)) {
            servicePresenter.homeApi(map)
        }
    }

    override fun onHomeApiSuccess(response: ServiceDetails?) {
        serviceDetails = response
    }

    override fun onPolygonDataSuccess(response: ArrayList<GeoFenceData>?) {
        if (response?.size ?: 0 > 0) {
            polygonData.clear()
            polygonData.addAll(response?: emptyList())

            for (i in polygonData.indices) {
                polygonData[i].alreadyAdded = ""
            }
        }
    }

    /* Api call to get the support services listing */
    private fun setSupportServices() {
        if (CheckNetworkConnection.isOnline(this)) {
            presenter.getSupportList()
        }
    }

    /* Api call to update the user's latest data like location and fcm_id */
    fun updateDataApiCall(lat: String, lng: String) {
        if (!CheckNetworkConnection.isOnline(this)) {
            return
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                // Get new Instance ID token
                val token = it.result
                SharedPrefs.with(this).save(FCM_TOKEN, token)
                val map = HashMap<String, String>()
                map["timezone"] = TimeZone.getDefault().id
                map["latitude"] = lat
                map["longitude"] = lng
                map["fcm_id"] = token ?: ""
                map["language_id"] = getLanguageId(LocaleManager.getLanguage(this)).toString()
                presenter.updateDataApi(map)
                Logger.e("getFirebaseInstanceId", token ?: "")
            }
        }
    }

    /* Api success for logout user api.*/
    override fun logoutSuccess() {
        /* Logout successful. Clears*/
        var languageCode = SharedPrefs.get().getString(LANGUAGE_CODE, "")
        SharedPrefs.with(this@HomeActivity).removeAllSharedPrefsChangeListeners()
        SharedPrefs.with(this@HomeActivity).removeAll()
        SharedPrefs.get().save(LANGUAGE_CODE, languageCode)

        EventBus.getDefault().post("success")

        finish()
        AppSocket.get().disconnect()
        AppSocket.get().off()
        AppSocket.get().socket = null
    }

    /* Api success for update data api.*/
    override fun onApiSuccess(login: LoginModel?) {
        val loginDetail=SharedPrefs.with(this).getObject(PROFILE,AppDetail::class.java)

        login?.AppDetail?.user?.user_id=loginDetail?.user?.user_id
        login?.AppDetail?.user?.name=loginDetail?.user?.name
        login?.AppDetail?.user?.email=loginDetail?.user?.email
        login?.AppDetail?.user?.phone_number=loginDetail?.user?.phone_number
        login?.AppDetail?.profile_pic_url=loginDetail?.profile_pic_url

        SharedPrefs.with(this).save(PROFILE, login?.AppDetail)
        Log.e("asasasasasaS", login?.AppDetail?.km_earned.toString())
        serviceRequestModel?.km_earned = login?.AppDetail?.km_earned.toString()
        // SharedPrefs.with(this).save(SERVICES, Gson().toJson(login?.services))
        setHeaderView()
    }

    /* Api success for get Support options listing*/
    @SuppressLint("WrongConstant")
    override fun onSupportListApiSuccess(response: List<Service>?) {
    }

    // for Royo Ride flow
    override fun onApiSuccess(response: ServiceDetails?) {
        SharedPrefs.get().save("catData", response)
        var isForceUpdate = this?.let { showForcedUpdateDialog(it, response?.Versioning?.ANDROID?.user?.force.toString(), response?.Versioning?.ANDROID?.user?.normal.toString(), AppVersionConstants.VERSION_NAME) }
        serviceDetails = response

//        listener?.getDriverData(response?.drivers as ArrayList<HomeDriver>)
        if (isForceUpdate == false) {
            /*if (response?.currentOrders?.isNotEmpty() == true) {
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
            } else {*/

//                serviceDetails = response
            if (response?.categories?.isNotEmpty() == true) {
                serviceRequestModel?.category_id = response.categories[0].category_id
                serviceRequestModel?.category_brand_id = response.categories[0].category_brand_id
//                    startDriverTimer(0)
                /* for (driver in response.drivers as ArrayList) {
                     createMarker(driver.latitude, driver.longitude, R.drawable.ic)
                 }*/
                selectVehicleTypeFragmentManager?.let { it1 ->
                    it1.setListener(this)
                    supportFragmentManager.beginTransaction().add(R.id.container, it1).addToBackStack("backstack").commitAllowingStateLoss()
                }
                mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map) as SupportMapFragment
            } else if (response?.lastCompletedOrders?.isNotEmpty() == true) {
                selectVehicleTypeFragmentManager?.removeAllDriverMarkers()
            }

//                if (qrCodeData != null) {
//                    apiInProgress = false
//                    QRScanResultAction()
//                }
//            }
//            apiInProgress = false
        }
    }

    override fun onApiSuccess(response: TerminologyData?) {
        TODO("Not yet implemented")
    }


    override fun snappedDrivers(snappedDrivers: List<HomeDriver>?) {
        selectVehicleTypeFragmentManager?.setDriversOnMap(snappedDrivers)
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        /* Show error if needed */
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            /* User's authorisation expired. Logout and ask user to login again.*/
            AppUtils.logout(this)
        }
    }

    /* Get google maps instance*/
    fun getMapAsync() {
        mapFragment?.getMapAsync(this)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    /**
     * Show markers for both source and destination LatLng and draw polyline path
     * between source and destination
     *
     * @param source source LatLng provided by the user.
     * @param destination destination LatLng provided by the user.
     * */
    fun showMarker(source: LatLng?, destination: LatLng?) {
        //getMapAsync()

        /*add marker for both source and destination*/
        if (srcMarker != null) {
            srcMarker?.remove()
            blackPolyLine?.remove()
            greyPolyLine?.remove()
            googleMapHome?.clear()
        }
        val sourceText = TextView(this)
//        // sourceText.text = getString(R.string.pickup_location)
//        sourceText.setTextColor(ContextCompat.getColor(this, R.color.black))
//        sourceText.setShadowLayer(16f, 16f, 16f, ContextCompat.getColor(this, R.color.white))
//        sourceText.setTypeface(sourceText.typeface, Typeface.BOLD)
//        sourceText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_pickup, 0, 0)
//        AppUtils.setTextViewDrawableColor(sourceText, Color.parseColor(ConfigPOJO.primary_color))

//        val iconGenerator = IconGenerator(this)
//        iconGenerator.setContentView(sourceText)
//        iconGenerator.setBackground(ContextCompat.getDrawable(this, android.R.color.transparent))
//        srcMarker = googleMapHome?.addMarker(source?.let {
//            MarkerOptions().position(it)
//        })

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val inflatedFrame = inflater.inflate(R.layout.map_marker_layout, null)
        var title = getString(R.string.pickup)
        val tvPrice = inflatedFrame.findViewById<TextView>(R.id.text_viewsss)
        tvPrice.setText(title)
        val bitmap = createBitmapFromView(inflatedFrame.findViewById(R.id.framelayout))

        val markerOptions = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .position(source?: LatLng(0.0,0.0))
                .title(title)
        markerOptions.visible(true)
        srcMarker = googleMapHome?.addMarker(markerOptions)
        srcMarker?.isVisible = true

        tvPrice.setText(getString(R.string.drop_off))
        val bitmap1 = createBitmapFromView(inflatedFrame.findViewById(R.id.framelayout))
        val markerOptions1 = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap1))
                .position(destination?:LatLng(0.0,0.0))
                .title(getString(R.string.drop_off))
        markerOptions1.visible(true)
        destMarker = googleMapHome?.addMarker(markerOptions1)
        destMarker?.isVisible = true

        if (ConfigPOJO.is_asap == "true") {
            when (serviceRequestModel?.category_brand_id) {
                24 -> {
                    destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_auto_icon)))
                }
                25 -> {
                    destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_car_icon)))
                }
                23 -> {
                    destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_bike_icon)))
                }
                27 -> {
                    destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_cycle_icon)))
                }
                else -> {
                    destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_car_icon)))
                }
            }
        }

        if (Constants.SECRET_DB_KEY == "eecf5a33e8575b1a860cc17dd778ea6f" || Constants.SECRET_DB_KEY == "456049b71e28127ccd109b3fa9392fdb") {
            when (serviceRequestModel?.category_id) {
                4 -> {
                    destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_del)))
                }
                7 -> {
                    destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_bike)))
                }
                12 -> {
                    destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_tri)))
                }
                14 -> {
                    destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_auto)))
                }
                else -> {
                    destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_car_cab)))
                }
            }
        }

        for (item in serviceRequestModel.stops.indices) {

            val sourceText = TextView(this)
            sourceText.setShadowLayer(16f, 16f, 16f, ContextCompat.getColor(this, R.color.white))
            sourceText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_drplocation, 0, 0)

            when (item) {
                0 -> {
                    AppUtils.setTextViewDrawableColor(sourceText, resources.getColor(R.color.transporter_base_color))
                }
                1 -> {
                    AppUtils.setTextViewDrawableColor(sourceText, resources.getColor(R.color.green_gradient_1))
                }
                2 -> {
                    AppUtils.setTextViewDrawableColor(sourceText, resources.getColor(R.color.payment))
                }
                4 -> {
                    AppUtils.setTextViewDrawableColor(sourceText, resources.getColor(R.color.mat_grey))
                }
            }


            val iconGenerator = IconGenerator(this)
            iconGenerator.setContentView(sourceText)
            iconGenerator.setBackground(ContextCompat.getDrawable(this, android.R.color.transparent))

            googleMapHome?.addMarker(MarkerOptions().position(LatLng(serviceRequestModel.stops.get(item).latitude
                    ?: 0.0, serviceRequestModel.stops.get(item).longitude ?: 0.0))
                    .icon((BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))))
                    ?.setAnchor(0.5f, 0.5f)
        }

        if (serviceRequestModel.stops.isNotEmpty()) {
            presenter.drawPolyLineWithWayPoints(serviceRequestModel, source, destination)
        } else {
            presenter.drawPolyLine(source, destination, language = LocaleManager.getLanguage(this))
        }
    }

    fun showMarkerNearByPlaces(source: LatLng?, destination: LatLng?) {
        presenter.nearByPlaces(source, destination, language = LocaleManager.getLanguage(this))
    }

    override fun nearByResponse(jsonRootObject: JSONObject, sourceLatLng: LatLng?, destLatLng: LatLng?) {
        val routeArray = jsonRootObject.getJSONArray("results")
        Log.e("routeArray", "" + routeArray)
        googleMapHome?.clear()
        if (srcMarker != null) {
            srcMarker?.remove()
        }

        val sourceText = TextView(this)
        sourceText.text = getString(R.string.pickup)
        sourceText.setTextColor(ContextCompat.getColor(this, R.color.black))
        sourceText.setShadowLayer(16f, 16f, 16f, ContextCompat.getColor(this, R.color.white))
        sourceText.setTypeface(sourceText.typeface, Typeface.BOLD)
        sourceText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_pickup, 0, 0)
        AppUtils.setTextViewDrawableColor(sourceText, Color.parseColor(ConfigPOJO.primary_color))

//        val iconGenerator = IconGenerator(this)
//        iconGenerator.setContentView(sourceText)
//        iconGenerator.setBackground(ContextCompat.getDrawable(this, android.R.color.transparent))

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val inflatedFrame = inflater.inflate(R.layout.map_marker_layout, null)
        var title = getString(R.string.pickup)
        val tvPrice = inflatedFrame.findViewById<TextView>(R.id.text_viewsss)
        tvPrice.setText(title)
        val bitmap = createBitmapFromView(inflatedFrame.findViewById(R.id.framelayout))

        val markerOptions = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .position(sourceLatLng?:LatLng(0.0,0.0))
                .title(title)
        markerOptions.visible(true)
        srcMarker = googleMapHome?.addMarker(markerOptions)
        srcMarker?.isVisible = true

//        srcMarker = googleMapHome?.addMarker(sourceLatLng?.let {
//            MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(it).title("Pics").visible(true).snippet("pic")
//        })
//        srcMarker?.showInfoWindow()
//        srcMarker?.setIcon((BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())))
//        srcMarker?.setAnchor(0.5f, 0.5f)

        for (i in 0 until routeArray.length()) {
            val actor = routeArray.getJSONObject(i)
            val overviewPolylines = actor.getJSONObject("geometry")
            val latlng = overviewPolylines.getJSONObject("location")
            googleMapHome?.addMarker(MarkerOptions().position(LatLng(latlng.getDouble("lat"), latlng.getDouble("lng")))
                    .title(actor.getString("name"))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pickup_location_mrkr)))
                    ?.setAnchor(0.5f, 0.5f)
        }

        if (srcMarker != null) {
            val cu = CameraUpdateFactory.newLatLngZoom(srcMarker?.position, 15F)
            googleMapHome?.animateCamera(cu)
        }
    }

    fun createBitmapFromView(v: View): Bitmap {
        v.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)
        val bitmap = Bitmap.createBitmap(v.measuredWidth,
                v.measuredHeight,
                Bitmap.Config.ARGB_8888)

        val c = Canvas(bitmap)
        v.layout(v.left, v.top, v.right, v.bottom)
        v.draw(c)
        return bitmap
    }
    /* Callback for the google direction api and to draw the polyline between provided
       source and destination location of the user */

    override fun polyLine(jsonRootObject: JSONObject, sourceLatLng: LatLng?, destLatLng: LatLng?) {
        // var line: Polyline? = null
        var lineOptions: PolylineOptions? = null

        blackPolyLine?.remove()
        val routeArray = jsonRootObject.getJSONArray("routes")

        val pathArray = ArrayList<String>()
        if (routeArray.length() == 0) {
            /* No route found */
            return
        }
        var routes: JSONObject? = null
        for (i in routeArray.length() - 1 downTo 0) {
            routes = routeArray.getJSONObject(i)
            val overviewPolylines = routes.getJSONObject("overview_polyline")
            val encodedString = overviewPolylines.getString("points")
            serviceRequestModel.directional_path = Uri.encode(encodedString)
            pathArray.add(encodedString)
            list = decodePoly(encodedString)
            val listSize = list?.size
            sourceLatLng?.let { list?.add(0, it) }
//            list?.add(1,LatLng(serviceRequestModel.stops[0].latitude?:0.0,serviceRequestModel.stops[0].longitude?:0.0))
//            list?.add(2,LatLng(serviceRequestModel.stops[1].latitude?:0.0,serviceRequestModel.stops[1].longitude?:0.0))0
            destLatLng?.let { listSize?.plus(1)?.let { it1 -> list?.add(it1, it) } }
            if (i == 0) {

                lineOptions = PolylineOptions()
                        .width(8f)
                        .addAll(list)
                        .color(if (ConfigPOJO.TEMPLATE_CODE == SUMMER_APP_BASE) Color.parseColor(ConfigPOJO.secondary_color)
                        else Color.parseColor(ConfigPOJO.primary_color))
                        .startCap(SquareCap())
                        .endCap(SquareCap())
                        .jointType(ROUND)
                        .geodesic(true)


                blackPolyLine = googleMapHome?.addPolyline(lineOptions)

                val greyOptions = PolylineOptions()
                greyOptions.width(8f)
                greyOptions.color(Color.parseColor(ConfigPOJO.secondary_color))
                greyOptions.startCap(SquareCap())
                greyOptions.endCap(SquareCap())
                greyOptions.jointType(ROUND)
                greyPolyLine = googleMapHome?.addPolyline(greyOptions)

                animatePolyLine()
            }

            val builder = LatLngBounds.Builder()
            if (srcMarker != null) {
                val arr = ArrayList<Marker?>()
                arr.add(srcMarker)
                arr.add(destMarker)
                for (marker in arr) {
                    builder.include(marker?.position)
                }
                val bounds = builder.build()
                val cu = CameraUpdateFactory.newLatLngBounds(bounds,
                        Utils.getScreenWidth(this),
                        Utils.getScreenWidth(this).minus(Utils.dpToPx(24).toInt()),
                        Utils.dpToPx(56).toInt())
                googleMapHome?.animateCamera(cu)
            }
        }

        val estimatedDistance = (((routes?.get("legs") as JSONArray).get(0) as JSONObject).get("distance") as JSONObject).get("value") as Int
        val eta = (((routes.get("legs") as JSONArray).get(0) as JSONObject).get("duration") as JSONObject).get("value") as Int

        try {
            serviceRequestModel.order_distance = estimatedDistance / 1000f
            serviceRequestModel.eta = eta / 60
        } catch (e: Exception) {
            serviceRequestModel.order_distance = 0f
            serviceRequestModel.eta = 0
        }
    }

    /* decode the encoded path we got from the direction api to draw polyline*/
    private fun decodePoly(encoded: String): ArrayList<LatLng> {
        val poly = java.util.ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }


    private fun animatePolyLine() {
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 2500
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { anim ->
            if (blackPolyLine != null) {
                val latLngList = blackPolyLine?.points
                val initialPointSize = latLngList?.size
                val animatedValue = anim.animatedValue as Int
                val newPoints = animatedValue * (list?.size?:0) / 100

                if (initialPointSize?:0 < newPoints) {
                    list?.subList(initialPointSize?:0, newPoints)?.let { latLngList?.addAll(it) }
                    blackPolyLine?.points = latLngList
                }
            }

        }

        animator.addListener(polyLineAnimationListener)
        animator.start()
    }


    internal var polyLineAnimationListener: Animator.AnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animator: Animator) {


        }

        override fun onAnimationEnd(animator: Animator) {
            if (blackPolyLine != null) {
                val blackLatLng = blackPolyLine?.points
                val greyLatLng = greyPolyLine?.points

                greyLatLng?.clear()
                greyLatLng?.addAll(blackLatLng?: mutableListOf())
                blackLatLng?.clear()

                blackPolyLine?.points = blackLatLng
                greyPolyLine?.points = greyLatLng

                blackPolyLine?.zIndex = 2f

                animator.start()

            }
        }

        override fun onAnimationCancel(animator: Animator) {

        }

        override fun onAnimationRepeat(animator: Animator) {


        }
    }

    override fun getDriverData(driversList: ArrayList<HomeDriver>) {
        selectVehicleTypeFragmentManager?.setDriversOnMap(driversList)
    }

    override fun driverDistanceTimeSuccess(jsonRootObject: JSONObject) {

    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Log.e("asasasaSAS", p0.toString() + "  " + p1.toString())
    }

    override fun onPaymentSuccess(p0: String?) {
        var order = SharedPrefs.get().getObject(ORDER, Order::class.java)
        var gson = Gson()
        Log.e("asasasajsa", gson.toJson(order))
        var map = HashMap<String, Any>()
        map["order_id"] = order?.order_id?.toString() ?: ""
        map["payment_id"] = p0.toString()
        presenter?.saveRazorPaymentData(map)
    }

    override fun RazorPaymentApiSuccess(apiResponseNew: ApiResponseNew?) {
        if (apiResponseNew?.success == 1) {
            openRatingFragment()
        }
    }

    override fun DataThawaniSuccess(apiResponseNew: ApiResponseNew?) {
        if (apiResponseNew?.success == 1) {
            openRatingFragment()
        }
    }

    private fun openRatingFragment() {
        fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val fragment = RatingFragment()
        val bundle = Bundle()
        bundle.putString(ORDER, Gson().toJson(SharedPrefs.get().getObject(ORDER, Order::class.java)))
        fragment.arguments = bundle
        supportFragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
    }

    fun tintMenuIcon(context: Context, item: MenuItem, color: Int) {
        val normalDrawable: Drawable = item.icon
        val wrapDrawable: Drawable = DrawableCompat.wrap(normalDrawable)
        DrawableCompat.setTint(wrapDrawable, color)
        item.setIcon(wrapDrawable)
    }

    override fun grtPayment(transId: String, orderData: String) {
        val order = Gson().fromJson(orderData, Order::class.java)
        if(transId?.isEmpty()) return

        val map = HashMap<String, Any>()
        map["access_token"] = SharedPrefs.get().getString(ACCESS_TOKEN_KEY, "")
        map["order_id"] = order?.order_id.toString()
        map["transaction_id"] = transId ?: ""
        presenter.saveDatatranseData(map)
    }
}
