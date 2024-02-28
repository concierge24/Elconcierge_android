package com.codebrew.clikat.module.essentialHome

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codebrew.clikat.BR
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.RideDataType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.FragmentServiceListBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.LocationUser
import com.codebrew.clikat.modal.PojoSignUp
import com.codebrew.clikat.modal.other.Data
import com.codebrew.clikat.modal.other.English
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.modal.other.TopBanner
import com.codebrew.clikat.module.all_categories.adapter.CategoryListAdapter
import com.codebrew.clikat.module.bottom_navigation.MainScreenActivity
import com.codebrew.clikat.module.change_language.ChangeLanguage
import com.codebrew.clikat.module.dialog_adress.AddressDialogFragment
import com.codebrew.clikat.module.dialog_adress.interfaces.AddressDialogListener
import com.codebrew.clikat.module.home_screen.adapter.BannerListAdapter
import com.codebrew.clikat.module.login.LoginActivity
import com.codebrew.clikat.module.more_setting.MoreSettingFragment
import com.codebrew.clikat.module.splash.SplashActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import com.trava.user.AppVersionConstants
import com.trava.user.DeepLink
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.wallet.UserWalletActivity
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.applabels.AppForunSetting
import com.trava.user.webservices.models.appsettings.SettingItems
import com.trava.utilities.Constants
import com.trava.utilities.SharedPrefs
import com.trava.utilities.constants.ACCESS_TOKEN
import com.trava.utilities.constants.ACCESS_TOKEN_KEY
import com.trava.utilities.constants.PROFILE
import com.trava.utilities.constants.SERVICES
import com.trava.utilities.webservices.models.AppDetail
import com.trava.utilities.webservices.models.Service
import com.trava.utilities.webservices.models.User
import dagger.android.DispatchingAndroidInjector
import kotlinx.android.synthetic.main.fragment_service_list.*
import kotlinx.android.synthetic.main.service_list_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.*
import javax.inject.Inject


class ServiceListFragment @Inject constructor() :
    BaseFragment<FragmentServiceListBinding, ServiceViewModel>(),
    BaseInterface,
    ServiceHeaderViewAdapter.OnListFragmentInteractionListener, AddressDialogListener,
    CategoryListAdapter.OnCategoryListener, BannerListAdapter.BannerCallback,
    SwipeRefreshLayout.OnRefreshListener {

    private lateinit var viewModel: ServiceViewModel
    private val bannerHandler = Handler()

    var bannerRunnable: Runnable? = null
    var stop = false

    private var mBinding: FragmentServiceListBinding? = null

    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var appUtils: AppUtils

    private var mAdapter: ServiceHeaderViewAdapter? = null

    private val mValues: MutableList<English> = mutableListOf()
    private var mValuesHeader: HashMap<String, List<English>>? = null

    private var clientInform: SettingModel.DataBean.SettingData? = null

    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null

    private var mCategoryName = ""

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    private var handler = Handler()

    private var mRideSetting: SettingItems? = null
    private var mCategoryData: Data? = null

    private var mSettingData: English? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this

        clientInform = prefHelper.getGsonValue(
            DataNames.SETTING_DATA,
            SettingModel.DataBean.SettingData::class.java
        )
        bookingFlowBean = prefHelper.getGsonValue(
            DataNames.BOOKING_FLOW,
            SettingModel.DataBean.BookingFlowBean::class.java
        )

        EventBus.getDefault().register(this)

        categoryObserver()
        rideCategoryObserver()
        appLabelObserver()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding = viewDataBinding
        viewDataBinding.color = Configurations.colors
        viewDataBinding.strings = Configurations.strings

        cl_back.setBackgroundColor(
            if (BuildConfig.CLIENT_CODE == "fastestdeliverynew_0824") {
                Color.parseColor(Configurations.colors.primaryColor)
            } else {
                Color.parseColor("#fafafa")
            }
        )


        swiprRefresh.setOnRefreshListener(this)

        if (isNetworkConnected) {
            viewModel.getRideSetting()
        }

        if (isNetworkConnected) {
            if (viewModel.homeDataLiveData.value == null) {
                viewModel.getCategories(clientInform?.enable_zone_geofence)
            } else {
                loadCategories(viewModel.homeDataLiveData.value)
            }
        }

        tvArea.setOnClickListener {
            AddressDialogFragment.newInstance(0).show(childFragmentManager, "dialog")
        }

        clientInform?.secondary_language?.let {
            if (it == "ar") {
                iv_change_lang2.visibility = View.VISIBLE
            }
        }

        iv_change_lang2.setOnClickListener {
            ChangeLanguage.newInstance().show(childFragmentManager, "change_language")
        }

        addNavigationDrawer()

    }

    private fun addNavigationDrawer() {

        if (clientInform?.is_wagon_app == "1" || clientInform?.is_hood_app == "1") {
            iv_menu_option.visibility = View.VISIBLE
        } else {
            iv_menu_option.visibility = View.GONE
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }

        iv_menu_option.setOnClickListener {
            drawer_layout?.openDrawer(GravityCompat.START)
        }

        nav_view.setNavigationItemSelectedListener { true }

        // insert master fragment into master container (i.e. nav view)
        val masterFragment = MoreSettingFragment.newInstance(true)
        childFragmentManager.beginTransaction()
            .add(R.id.nav_view, masterFragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // The action bar home/up action should open or close the drawer.
        when (item.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        settingToolbar()

        AppGlobal.localeManager?.setLocale(activity ?: requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    private fun getHeaderMap(resource: Data?) {
        val results: HashMap<String, List<English>> = hashMapOf()

        val essentials = mutableListOf<English>()
        val health = mutableListOf<English>()
        val service = mutableListOf<English>()
        val products = mutableListOf<English>()
        val distance = mutableListOf<English>()

        mValues.let {
            for (i in 0 until it.size) {
                if (it[i].type == AppDataType.CarRental.type || it[i].id == 60) {
                    distance.add(it[i])
                } else if (clientInform?.is_wagon_app == "1" && (it[i].type == AppDataType.Food.type || it[i].type == AppDataType.HomeServ.type || it[i].type == AppDataType.Ecom.type)) {
                    essentials.add(it[i])
                } else if (it[i].type == AppDataType.Food.type) {
                    essentials.add(it[i])
                } else if (it[i].type == AppDataType.HomeServ.type) {
                    service.add(it[i])
                } else if (it[i].categoryName?.toLowerCase(Locale.ENGLISH)
                        ?.contains("ambulance") == true || it[i].name == "Pharmacy"
                ) {
                    health.add(it[i])
                } else if (it[i].ride_title != RideDataType.PickUp.type && it[i].ride_title != RideDataType.Cab.type && it[i].ride_title != RideDataType.WaterOrdering.type || it[i].ride_title?.isEmpty() == true) {
                    products.add(it[i])
                }
            }
        }

        if (clientInform?.is_wagon_app == "1" && essentials.size >= 2) {
            Collections.swap(essentials, 0, 1)
        }

        if (essentials.isNotEmpty()) {
            results[getString(R.string.essentials_deliver_tag)] = essentials
        }



        if (clientInform?.show_food_groc == null || clientInform?.show_food_groc == "0") {


            if (products.isNotEmpty()) {
                results[getString(R.string.explore_product_different_brands)] = products
            }


            if (health.isNotEmpty()) {
                results[getString(R.string.health_medicine_tag)] = health
            }

            if (service.isNotEmpty()) {
                results[getString(R.string.service_at_home)] = service
            }


            distance.addAll(mRideSetting?.services?.filter {
                (it.title == RideDataType.PickUp.type || it.title == RideDataType.Cab.type || it.title == RideDataType.WaterOrdering.type) && it.name?.toLowerCase(
                    Locale.ENGLISH
                )?.contains("ambulance") == false
            }?.let {
                it.map {
                    English(
                        categoryName = it.name ?: "", imageId = catIdImage(
                            it.category_id
                                ?: 0
                        ), id = it.category_id, image = it.image
                    )
                }
            }?.toMutableList() ?: mutableListOf())


            if (distance.isNotEmpty()) {
                results[getString(R.string.distance_not)] = distance
            }

            mValuesHeader = if (resource?.english?.isNotEmpty() == true && results.toList()
                    .sortedBy { (key, _) -> -key.length }.toMap() is HashMap<String, List<English>>
            ) {
                results.toList().sortedBy { (key, _) -> -key.length }
                    .toMap() as HashMap<String, List<English>>
            } else {
                results
            }

        } else {
            mValuesHeader = results
        }

        setServiceLIstAdapter(resource)
    }

    private fun setServiceLIstAdapter(resource: Data?) {


        if (clientInform?.custom_vertical_theme == "1") {


            mRideSetting?.services?.forEachIndexed { index, service ->
                mValues.add(
                    English(
                        categoryName = service.name, imageId = catIdImage(
                            service.category_id
                                ?: 0
                        ), id = service.category_id, image = service.image
                    )
                )
            }

            with(rv_categories_header) {
                layoutManager = LinearLayoutManager(context)
                adapter = EssentialServiceAdaptor(mValues, this@ServiceListFragment)
            }

        } else if (clientInform?.app_custom_domain_theme != null && clientInform?.app_custom_domain_theme == "1") {
            grp_banner.visibility = View.VISIBLE

            with(rv_categories_header)
            {
                layoutManager = if (BuildConfig.CLIENT_CODE == "ghaytah_0021") GridLayoutManager(
                    context,
                    3
                ) else GridLayoutManager(context, 4)
                adapter = CategoryListAdapter(mValues, this@ServiceListFragment, appUtils)
            }


            resource?.topBanner?.map {
                it.isWebsite = true
            }

            val bannerAdapter = BannerListAdapter(
                resource?.topBanner?.take(5)
                    ?: listOf(), 2, 0, clientInform, 0
            )

            with(rv_banner_list)
            {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = bannerAdapter
            }

            bannerAdapter.settingCallback(this)

        } else {

            grp_banner.visibility = View.GONE
            deals_tag.visibility = View.GONE


            mAdapter = ServiceHeaderViewAdapter(mValuesHeader ?: hashMapOf(), this, clientInform)
            rv_categories_header.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            rv_categories_header.adapter = mAdapter
        }

        val topBanner = mutableListOf<TopBanner>()

        when {
            clientInform?.is_wagon_app == "1" -> {

                resource?.topBanner?.map {
                    it.isWebsite = true
                }

                topBanner.addAll(resource?.topBanner?.take(5)?.toMutableList() ?: mutableListOf())
                topBanner.map {
                    it.isEnabled = true
                }
            }
            clientInform?.custom_vertical_theme == "1" -> {
                topBanner.addAll(resource?.topBanner ?: mutableListOf())
            }
            //sss
            else -> {
                if (BuildConfig.CLIENT_CODE == "ghaytah_0021") {
                    topBanner.add(
                        TopBanner(
                            staticImage = true,
                            bannerImage = R.drawable.pager_image1
                        )
                    )
                    topBanner.add(
                        TopBanner(
                            staticImage = true,
                            bannerImage = R.drawable.pager_image2
                        )
                    )
                    topBanner.add(
                        TopBanner(
                            staticImage = true,
                            bannerImage = R.drawable.pager_image3
                        )
                    )
                } else {
                    topBanner.add(TopBanner(staticImage = true, bannerImage = R.drawable.banner1))
                    topBanner.add(TopBanner(staticImage = true, bannerImage = R.drawable.banner2))
                    topBanner.add(TopBanner(staticImage = true, bannerImage = R.drawable.banner3))
                }
            }
        }

        val bannerAdapter = BannerListAdapter(topBanner, 0, 0, clientInform, 0)
        bannerAdapter.settingCallback(this)
        with(tvTopBannerList)
        {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = bannerAdapter
            visibility = View.VISIBLE
        }
        stop = false
    }


    private fun settingToolbar() {
        if (prefHelper.getGsonValue(PrefenceConstants.ADRS_DATA, AddressBean::class.java) != null) {
            val adrsData =
                prefHelper.getGsonValue(PrefenceConstants.ADRS_DATA, AddressBean::class.java)!!

            tvArea.text = adrsData.customer_address
        } else {
            val mLocUser = prefHelper.getGsonValue(DataNames.LocationUser, LocationUser::class.java)
                ?: return
            tvArea.text = mLocUser.address ?: ""
        }

        if (clientInform?.custom_vertical_theme == "1") {
            frameTopColor?.visibility = View.VISIBLE
            ivLogo?.visibility = View.VISIBLE
            cvTop?.visibility = View.VISIBLE
            tvArea?.visibility = View.GONE

            tvAddress?.text = tvArea?.text.toString()

            cvTop?.setOnClickListener {
                tvArea?.callOnClick()
            }
        } else {
            frameTopColor?.visibility = View.GONE
            ivLogo?.visibility = View.GONE
            cvTop?.visibility = View.GONE
            tvArea?.visibility = View.VISIBLE

        }
        // iv_marker.loadImage(clientInform?.logo_url ?: "")
        //  iv_marker.visibility = View.VISIBLE
    }


    private fun categoryObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<Data> { resource ->

            mCategoryData = resource

            mRideSetting?.let {
                viewModel.setIsLoading(false)
                loadCategories(resource)
            }
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.homeDataLiveData.observe(this, catObserver)

    }

    private fun rideCategoryObserver() {
        // Create the observer which updates the UI.
        val rideCatObserver = Observer<SettingItems> { resource ->

            mRideSetting = resource

            mCategoryData?.let {
                viewModel.setIsLoading(false)
                loadCategories(mCategoryData)
            }

        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.rideSettingLiveData.observe(this, rideCatObserver)

    }

    private fun appLabelObserver() {
        // Create the observer which updates the UI.
        val appLabelObserver =
            Observer<com.trava.user.webservices.models.applabels.Data> { resource ->

                if (!resource?.app_forun_settings.isNullOrEmpty()) {
                    ConfigPOJO.appLablesList = resource?.app_forun_settings
                        ?: ArrayList<AppForunSetting>()
                }

                activity?.intent?.handleIntent()
            }
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.appLabelLiveData.observe(this, appLabelObserver)

    }

    private fun setSettingData(response: SettingItems?, categoryID: Int) {

        AppVersionConstants.APPLICATION_ID = BuildConfig.APPLICATION_ID

        if (clientInform?.is_hood_app == "1") {
            ConfigPOJO.is_hood_app = "1"
        }

        ConfigPOJO.SECRET_API_KEY =
            if (response?.key_value?.andriod_google_api?.isNotEmpty() == true) {
                response.key_value.andriod_google_api
            } else {
                ConfigPOJO.SECRET_API_KEY
            }

        Places.initialize(activity ?: requireContext(), ConfigPOJO.SECRET_API_KEY)
        if (response?.key_value?.app_template != "") {
            ConfigPOJO.TEMPLATE_CODE = response?.key_value?.app_template?.toInt() ?: 0
        } else {
            ConfigPOJO.TEMPLATE_CODE = 0
        }

        if (response?.key_value?.currency_symbol == null) {
            ConfigPOJO.currency = "₹"
        } else {
            ConfigPOJO.currency = response.key_value.currency_symbol.toString()
        }

        if (response?.key_value?.is_cash_on_Delivery == "true") {
            ConfigPOJO.is_cash_on_Delivery = response?.key_value?.is_cash_on_Delivery
        }
        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            ConfigPOJO.primary_color = response?.key_value?.Primary_colour.toString().trim()
            ConfigPOJO.secondary_color = response?.key_value?.secondary_colour.toString().trim()
            ConfigPOJO.headerColor = response?.key_value?.header_colour.toString().trim()
            ConfigPOJO.header_txt_colour = response?.key_value?.heder_txt_colour.toString().trim()
            ConfigPOJO.Btn_Text_Colour = ConfigPOJO.Btn_Text_Colour.toString().trim()
            ConfigPOJO.Btn_Colour = response?.key_value?.Primary_colour.toString().trim()
        } else {

            ConfigPOJO.primary_color =
                checkNotEmpty(response?.key_value?.Primary_colour.toString().trim())
            ConfigPOJO.secondary_color =
                checkNotEmpty(response?.key_value?.secondary_colour.toString().trim())
            ConfigPOJO.headerColor =
                checkNotEmpty(response?.key_value?.header_colour.toString().trim())
            ConfigPOJO.header_txt_colour =
                checkNotEmpty(response?.key_value?.heder_txt_colour.toString().trim())
            ConfigPOJO.Btn_Text_Colour =
                checkNotEmpty(response?.key_value?.Secondary_Btn_Text_colour.toString().trim())
            ConfigPOJO.Btn_Colour =
                checkNotEmpty(response?.key_value?.Secondary_Btn_Colour.toString().trim())
            ConfigPOJO.white_color = checkNotEmpty("#ffffff")
        }

        ConfigPOJO.settingsResponse = response
        ConfigPOJO.dynamicBar = response?.dynamicbar

        if (response?.key_value?.is_contactus.equals("true", false)) {
            if (response?.supports != null && response.supports?.isNotEmpty() == true) {
                ConfigPOJO.support_email = response.supports?.get(0)?.email ?: ""
                ConfigPOJO.support_number = response.supports?.get(0)?.phone ?: ""
            }
        }

        if (response?.key_value?.is_merchant.equals("true", false)) {
            ConfigPOJO.is_merchant = response?.key_value?.is_merchant ?: ""
        }

        if (response?.key_value?.is_omco.equals("true", false)) {
            ConfigPOJO.is_omco = response?.key_value?.is_omco ?: ""
        }

        if (response?.key_value?.is_darkMap.equals("true", false)) {
            ConfigPOJO.is_darkMap = response?.key_value?.is_darkMap ?: ""
        }

        if (response?.key_value?.is_childrenTravelling.equals("true", false)) {
            ConfigPOJO.is_childrenTraveling = response?.key_value?.is_childrenTravelling ?: ""
        }

        if (response?.key_value?.is_gift.equals("true", false)) {
            ConfigPOJO.is_gift = response?.key_value?.is_gift ?: ""
        }

        if (response?.key_value?.is_play_video_after_splash.equals("true", false)) {
            ConfigPOJO.is_play_video_after_splash = response?.key_value?.is_play_video_after_splash
                ?: ""
            ConfigPOJO.play_video_url = response?.key_value?.play_video_url ?: ""
            ConfigPOJO.play_video_after_splash_images =
                response?.key_value?.play_video_after_splash_images
                    ?: ""
        }
        ConfigPOJO.admin_base_url = response?.key_value?.admin_base_url ?: ""

        if (response?.key_value?.is_card_payment_enabled.equals("true", false)) {
            ConfigPOJO.is_card_payment_enabled = response?.key_value?.is_card_payment_enabled ?: ""
        }

        if (response?.key_value?.is_google_login.equals("true", false)) {
            ConfigPOJO.is_google_login = response?.key_value?.is_google_login ?: ""
        }

        if (response?.key_value?.is_gender_selection_enabled.equals("true", false)) {
            ConfigPOJO.is_gender_selection_enabled =
                response?.key_value?.is_gender_selection_enabled
                    ?: ""
        }

        if (response?.key_value?.is_facebookLogin.equals("true", false)) {
            ConfigPOJO.is_facebookLogin = response?.key_value?.is_facebookLogin ?: ""
        }

        if (response?.key_value?.is_countrycheck.equals("true", false)) {
            ConfigPOJO.is_countrycheck = response?.key_value?.is_countrycheck ?: ""
        }

        if (response?.key_value?.is_asap.equals("true", false)) {
            ConfigPOJO.is_asap = response?.key_value?.is_asap ?: ""
        }

        if (response?.key_value?.is_cash_payment_enabled.equals("true", false)) {
            ConfigPOJO.is_cash_payment_enabled = response?.key_value?.is_cash_payment_enabled ?: ""
        }

        if (response?.key_value?.is_water_platform.equals("true", false)) {
            ConfigPOJO.is_water_platform = response?.key_value?.is_water_platform ?: ""
        }

        ConfigPOJO.multiple_request = response?.key_value?.multiple_request ?: "1"

        if (ConfigPOJO.is_asap == "true" && ConfigPOJO.currency == "INR") {
            ConfigPOJO.currency = "₹"
        }

        if (ConfigPOJO.is_asap == "true") {
            ConfigPOJO.search_count = response?.key_value?.search_count ?: "0"
            ConfigPOJO.distance_search_increment = response?.key_value?.distance_search_increment
                ?: "0"
            ConfigPOJO.distance_search_start = response?.key_value?.distance_search_start ?: "0"
        }

        ConfigPOJO.defaultCoutryCode =
            response?.key_value?.default_country_code!!.toString().trim().replace("\n", "")
        ConfigPOJO.ISO_CODE = response.key_value.iso_code.toString().trim().replace("\n", "")
        if (response?.key_value?.minimum_wallet_balance != null)
            ConfigPOJO.minimum_wallet_balance =
                response.key_value.minimum_wallet_balance.toString().trim().replace("\n", "")
        if (!response.key_value.gateway_unique_id.isNullOrEmpty()) {
            if (response.key_value.gateway_unique_id.equals("stripe")) {
                ConfigPOJO.gateway_unique_id = response.key_value.gateway_unique_id.toString()
                if (response?.key_value?.stripe_public_key != null) {
                    ConfigPOJO.STRIPE_PUBLIC_KEY = response.key_value.stripe_public_key.toString()
                    ConfigPOJO.STRIPE_SECRET_KEY = response.key_value.stripe_secret_key.toString()
                }

            } else if (response.key_value.gateway_unique_id.equals("conekta")) {
                ConfigPOJO.gateway_unique_id = response.key_value.gateway_unique_id.toString()
                ConfigPOJO.conekta_api_key = response.key_value.conekta_api_key.toString()
                ConfigPOJO.conekta_api_version = response.key_value.conekta_api_version.toString()
                ConfigPOJO.conekta_locale = response.key_value.conekta_locale.toString()
            } else if (response.key_value.gateway_unique_id.equals("razorpay")) {
                ConfigPOJO.gateway_unique_id = response.key_value.gateway_unique_id
                ConfigPOJO.razorpay_secret_key = response.key_value.razorpay_secret_key
                ConfigPOJO.razorpay_private_key = response.key_value.razorpay_private_key
            }
            ConfigPOJO.gateway_unique_id = response.key_value.gateway_unique_id.toString()
        }

        if (response.services != null) {
            var categoriesList: ArrayList<Service>? = ArrayList()
            for (i in response.services?.indices!!) {
                if (response.services?.get(i)?.category_id == categoryID) {
                    categoriesList?.add(response.services?.get(i)!!)
                }
            }

            SharedPrefs.with(activity).save(SERVICES, Gson().toJson(categoriesList))
        }

        activity?.intent?.handleIntent()
    }

    private fun checkNotEmpty(value: String): String {
        if (value.isEmpty()) return "#ffffff"
        return value
    }

    private fun Intent.handleIntent() {
        when (action) {
            // When the action is triggered by a deep-link, Intent.Action_VIEW will be used
            Intent.ACTION_VIEW -> handleDeepLink(data)
            // Otherwise start the app as you would normally do.
            else -> updateDefaultView()
        }
    }

    private fun handleDeepLink(data: Uri?) {
        // path is normally used to indicate which view should be displayed
        // i.e https://fit-actions.firebaseapp.com/start?exerciseType="Running" -> path = "start"
        var actionHandled = true
        when (data?.path) {
            DeepLink.BOOK_RIDE -> {
                val destinationData =
                    data.getQueryParameter(DeepLink.Params.ACTIVITY_TYPE).orEmpty()
                        ?: ""
                val intent = Intent(activity, HomeActivity::class.java)
                intent.putExtra("destination", destinationData)
                intent.putExtra("for", "book_ride")
                startActivity(intent)
            }

            DeepLink.OPEN_BOOKINGS -> {
                val intent = Intent(activity, HomeActivity::class.java)
                intent.putExtra("destination", "bookings")
                intent.putExtra("for", "bookings")
                startActivity(intent)
            }

            DeepLink.OPEN_ROYO_RIDES -> {
                updateDefaultView()
            }

            else -> {
                updateDefaultView()
            }
        }
    }


    private fun updateDefaultView() {

        with(arguments)
        {
            if (this != null) {
                var data = ""
                if (getString("data") != null) {
                    val type = JSONObject(getString("data") ?: "")
                    data = type.getString("type")
                }

                if (data == "wallet" || get("type") == "wallet") {
                    startActivity(
                        Intent(
                            activity,
                            UserWalletActivity::class.java
                        )
                            .putExtra("from", "Notification")
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                } else if (getString("type") == "chat" || getString("type") == "chat") {
                    startActivity(
                        Intent(
                            activity,
                            HomeActivity::class.java
                        )
                            .putExtra("order_id", getString("order_id"))
                            .putExtra("from_chat", "Notification")
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                } else {
                    if (getBoolean("hold", true)) {
                        handler.postDelayed(runnable, 0)
                    } else {
                        handler.postDelayed(runnable, 500)
                    }
                }
            } else {
                if (this != null && getBoolean("hold", true)) {
                    handler.postDelayed(runnable, 0)
                } else {
                    handler.postDelayed(runnable, 500)
                }
            }
        }
    }

    private val runnable = Runnable {
        val proifle = SharedPrefs.with(activity).getObject(PROFILE, AppDetail::class.java)
        if (proifle != null) {
            startActivity(
                Intent(activity, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            )
        } else {
            activity?.launchActivity<LoginActivity>(AppConstants.REQUEST_CAB_BOOKING)
        }
    }


    private fun loadCategories(resource: Data?) {

        when {
            clientInform?.custom_vertical_theme == "1" -> {
                grp_banner.visibility = View.GONE
                deals_tag.visibility = View.GONE
            }
            clientInform?.is_hood_app == "1" -> {
                tv_more.visibility = View.GONE
                deals_tag.visibility = View.GONE
            }
            else -> {
                tv_more.visibility = View.VISIBLE
                deals_tag.visibility = View.VISIBLE
            }
        }

        if (resource?.english?.isNotEmpty() == true) {
            mValues.clear()
            mValues.addAll(resource.english)

            if (clientInform?.show_food_groc == null || clientInform?.show_food_groc == "0") {

                mRideSetting?.services?.forEachIndexed { index, service ->
                    mValues.add(
                        English(
                            categoryName = service.name,
                            imageId = catIdImage(
                                service.category_id
                                    ?: 0
                            ),
                            id = service.category_id,
                            ride_title = service.title,
                            image = service.image
                        )
                    )
                }
            }
        }
        getHeaderMap(resource)
    }

    private fun saveCategoryInf(bean: English?) {

        if (bean?.is_laundary == 1) {
            clientInform?.is_laundry_theme = "1"
            clientInform?.hideAgentList = "1"
            clientInform?.laundary_service_flow = "1"

            if (bean.type == AppDataType.HomeServ.type) {
                bookingFlowBean?.cart_flow = 3
            }
        } else {
            clientInform?.is_laundry_theme = "0"
            clientInform?.hideAgentList = "0"
            clientInform?.laundary_service_flow = "0"
            when (bean?.type) {
                AppDataType.HomeServ.type -> {
                    bookingFlowBean?.cart_flow = 1
                }
                else -> {
                    bookingFlowBean?.cart_flow = 3
                }
            }
        }

        if (clientInform?.show_supplier_detail_home_ecom == "1" && (bean?.type == AppDataType.HomeServ.type || bean?.type == AppDataType.Ecom.type)) {
            clientInform?.show_supplier_detail = "1"
        } else {
            clientInform?.show_supplier_detail = "0"
        }

        if (clientInform?.is_wagon_app == "1" && bean?.type == AppDataType.Ecom.type) {
            clientInform?.is_product_rating = "0"
            clientInform?.is_supplier_rating = "0"
        }

        bookingFlowBean?.vendor_status = 0
        prefHelper.setkeyValue(DataNames.BOOKING_FLOW, Gson().toJson(bookingFlowBean))


        clientInform?.payment_after_confirmation = (bean?.payment_after_confirmation
            ?: 0).toString()
        clientInform?.order_instructions = (bean?.order_instructions ?: 0).toString()
        clientInform?.cart_image_upload = (bean?.cart_image_upload ?: 0).toString()
        clientInform?.is_table_booking = (bean?.is_dine ?: 0).toString()
        clientInform?.is_supplier_detail = "0"

        prefHelper.setkeyValue(DataNames.SETTING_DATA, Gson().toJson(clientInform))

        val screenFlowBean = prefHelper.getGsonValue(
            DataNames.SCREEN_FLOW,
            SettingModel.DataBean.ScreenFlowBean::class.java
        )
        screenFlowBean?.app_type = bean?.type ?: 0
        prefHelper.setkeyValue(DataNames.SCREEN_FLOW, Gson().toJson(screenFlowBean))

        prefHelper.addGsonValue(PrefenceConstants.APP_TERMINOLOGY, bean?.terminology ?: "")


    }


    companion object {
        @JvmStatic
        fun newInstance() =
            ServiceListFragment()
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_service_list
    }

    override fun getViewModel(): ServiceViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(ServiceViewModel::class.java)
        return viewModel
    }

    override fun onErrorOccur(message: String) {
        viewModel.setIsLoading(false)
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        viewModel.setIsLoading(false)
        openActivityOnTokenExpire()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: String?) {

        if (event == "success") {
            activity?.launchActivity<SplashActivity>()
            activity?.finishAffinity()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNavigationEvent(event: String?) {

        if (event == "item_click") {
            drawer_layout.closeDrawers()
            hideKeyboard()
        }
    }


    override fun onListFragmentInteraction(item: English?) {

        mSettingData = item


        if (item?.categoryName.isNullOrBlank()) {

            AppConstants.APP_SAVED_SUB_TYPE = item?.type ?: 0
            AppConstants.APP_SUB_TYPE = item?.type ?: 0


            saveCategoryInf(item)

            // prefHelper.setkeyValue(DataNames.SELECTED_LANGUAGE, ClikatConstants.LANGUAGE_ENGLISH)

            if (BuildConfig.CLIENT_CODE != "ored_0751") {
                StaticFunction.clearCart(activity)
                prefHelper.onCartClear()
                prefHelper.setkeyValue(PrefenceConstants.CATEGORY_ID, item?.id.toString())

                if (item?.sub_category?.isNotEmpty() == true) {
                    prefHelper.setkeyValue(
                        PrefenceConstants.SUB_CATEGORY_ID,
                        Gson().toJson(item.sub_category.map { it.id })
                    )
                } else {
                    prefHelper.setkeyValue(PrefenceConstants.SUB_CATEGORY_ID, "")
                }


            }



            activity?.launchActivity<MainScreenActivity> {
                putExtra("cat_id", item?.id)
                putExtra("cat_name", item?.name)
                if (!item?.screenType.isNullOrEmpty() && item?.screenType == "Banner") {
                    putExtra("screenType", item.screenType)
                    putExtra("bannerItem", item)
                }
            }

        } else {

            mCategoryName = item?.categoryName ?: ""
            if (prefHelper.getCurrentUserLoggedIn()) {
                launchRideSection(item)
            } else {
                activity?.launchActivity<LoginActivity>(AppConstants.REQUEST_CAB_BOOKING)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppConstants.REQUEST_CAB_BOOKING && resultCode == Activity.RESULT_OK) {

            launchRideSection(mSettingData)
        }
    }


    private fun launchRideSection(item: English?) {
        val userData = saveUserData()

        prefHelper.setkeyValue(PrefenceConstants.CATEGORY_ID, item?.id.toString())
        SharedPrefs.with(activity).save(ACCESS_TOKEN_KEY, userData?.data?.access_token)
        ACCESS_TOKEN = userData?.data?.access_token ?: ""

        setSettingData(mRideSetting, item?.id ?: 0)
    }


    private fun saveUserData(): PojoSignUp? {

        val userDataModel = prefHelper.getGsonValue(DataNames.USER_DATA, PojoSignUp::class.java)

        val userData = User(
            name = "${userDataModel?.data?.firstname} ,${userDataModel?.data?.lastname}",
            user_id = userDataModel?.data?.id,
            email = userDataModel?.data?.email,
            phone_number = userDataModel?.data?.mobile_no?.replace(" ", "")?.toLong()
        )
        val appDetail = AppDetail(
            profile_pic_url = userDataModel?.data?.user_image,
            access_token = userDataModel?.data?.access_token,
            user = userData
        )

        prefHelper.addGsonValue(PrefenceConstants.PROFILE, Gson().toJson(appDetail))

        return userDataModel
    }

    fun catIdImage(categoryID: Int): Int {
        return when (categoryID) {
            7 -> R.drawable.category_cab
            10 -> R.drawable.category_ambulance
            4 -> R.drawable.category_pickup
            else -> R.drawable.ic_ride_services
        }
    }

    override fun onAddressSelect(adrsBean: AddressBean) {

        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)

        prefHelper.addGsonValue(PrefenceConstants.ADRS_DATA, Gson().toJson(adrsBean))
        if (clientInform?.custom_vertical_theme == "1") {
            tvAddress?.text = adrsBean.customer_address
        } else
            tvArea.text = adrsBean.customer_address
    }

    override fun onDestroyDialog() {

    }


    override fun onCategoryItem(item: English?) {
        onListFragmentInteraction(item)
    }

    override fun onBannerDetail(bannerBean: TopBanner?) {

        val bannerItem = English()
        bannerItem.is_question = bannerBean?.is_question
        bannerItem.is_assign = bannerBean?.is_assign
        bannerItem.payment_after_confirmation = bannerBean?.payment_after_confirmation
        bannerItem.terminology = bannerBean?.terminology
        bannerItem.cart_image_upload = bannerBean?.cart_image_upload
        bannerItem.order_instructions = bannerBean?.order_instructions
        bannerItem.supplierName = bannerBean?.supplier_name
        bannerItem.id = bannerBean?.category_id
        bannerItem.supplier_id = bannerBean?.supplier_id
        bannerItem.supplier_branch_id = bannerBean?.branch_id
        bannerItem.type = bannerBean?.type
        bannerItem.screenType = "Banner"


        onListFragmentInteraction(bannerItem)
    }

    override fun onRefresh() {

        swiprRefresh.isRefreshing = false

        if (isNetworkConnected) {
            viewModel.getRideSetting()
        }

        if (isNetworkConnected) {
            viewModel.getCategories(clientInform?.enable_zone_geofence)
        }
    }

    override fun onPagerScroll(bannerList: List<TopBanner>) {
        if (bannerList.isNotEmpty()) {
            val NUM_PAGES = bannerList.count()
                ?: 0
            var currentPage = 0

            if (bannerRunnable == null) {
                bannerRunnable = Runnable {
                    if (currentPage == NUM_PAGES) {
                        currentPage = 0
                    }
                    if (!stop) {
                        tvTopBannerList.scrollToPosition(currentPage++)
                    }

                }
                val swipeTimer = Timer()
                swipeTimer.schedule(object : TimerTask() {
                    override fun run() {
                        bannerRunnable?.let { bannerHandler.post(it) }


                    }
                }, 3000, 3000)
            } else {
                bannerRunnable?.let { bannerHandler.post(it) }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        //   if (bannerHandler == null && bannerRunnable == null) return

        bannerRunnable?.let { bannerHandler.removeCallbacks(it) }
        //  EventBus.getDefault().unregister(this)
    }

    fun stopData() {
        //   bannerHandler.removeCallbacksAndMessages(null)
        // bannerHandler.removeMessages(0)
        stop = true
        // bannerHandler.removeCallbacks(bannerRunnable!!)
        bannerRunnable?.let { bannerHandler.removeCallbacks(it) }

    }

    override fun onPause() {
        super.onPause()
        stop = true
    }
}
