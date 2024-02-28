package com.codebrew.clikat.module.home_screen.resturant_home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppToasty
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.loadImage
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.DeliveryType
import com.codebrew.clikat.data.FoodAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.model.api.ListItem
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.FragmentResturantHomeBinding
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.cart.SCHEDULE_REQUEST
import com.codebrew.clikat.module.dialog_adress.AddressDialogFragment
import com.codebrew.clikat.module.dialog_adress.interfaces.AddressDialogListener
import com.codebrew.clikat.module.dialog_adress.v2.AddressDialogFragmentV2
import com.codebrew.clikat.module.home_screen.HomeFragment
import com.codebrew.clikat.module.home_screen.resturant_home.dineIn.DineInResturantFrag
import com.codebrew.clikat.module.home_screen.resturant_home.pickup.PickupResturantFrag
import com.codebrew.clikat.module.home_screen.resturant_home.wagon_pickup.WagonPickUp
import com.codebrew.clikat.module.product.product_listing.DialogListener
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_resturant_home.*
import kotlinx.android.synthetic.main.toolbar_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import ru.nikartm.support.BadgePosition
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
const val REFERAL_REQUEST = 595
const val WALLET_REQUEST = 596
const val DELIVERY_TYPE = "delivery_type"
const val SELF_PICKUP = "self_pickup"

class ResturantHomeFrag : Fragment(), TabLayout.OnTabSelectedListener, AddressDialogListener, DialogListener {


    @Inject
    lateinit var prefHelper: PreferenceHelper


    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var appUtils: AppUtils

    private var clientInform: SettingModel.DataBean.SettingData? = null
    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null


    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null

    private var mAdapter: PagerAdapter? = null

    private val textConfig by lazy { appUtils.loadAppConfig(0).strings }
    private var currentPos: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        screenFlowBean = prefHelper.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        clientInform = prefHelper.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
        bookingFlowBean = prefHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment

        val binding = DataBindingUtil.inflate<FragmentResturantHomeBinding>(inflater, R.layout.fragment_resturant_home, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = textConfig

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefHelper.removeValue(PrefenceConstants.FIRST_HOME_SECTION)
        val mlistFrag: MutableList<Fragment>?

        settingTabLayout()

        when {
            clientInform?.is_skip_theme == "1" -> {
                tl_home.visibility = View.GONE
                iv_search?.visibility = View.GONE
                iv_notification?.visibility = View.GONE
                iv_logo?.visibility = View.VISIBLE
                iv_logo.loadImage(clientInform?.logo_url ?: "")
                mlistFrag = mutableListOf(HomeFragment.newInstance())
                ivCart?.visibility = View.VISIBLE
            }

            bookingFlowBean?.is_pickup_order == FoodAppType.Both.foodType || clientInform?.is_table_booking == "1" -> {
                tl_home.visibility = View.VISIBLE
                tl_home.addOnTabSelectedListener(this)

                mlistFrag = when (bookingFlowBean?.is_pickup_order) {
                    FoodAppType.Both.foodType -> {
                        if (clientInform?.is_wagon_app == "1") {
                            mutableListOf(HomeFragment.newInstance(), WagonPickUp.newInstance(FoodAppType.Pickup.foodType.toString(), "pickup"))
                        } else {
                            mutableListOf(HomeFragment.newInstance(), PickupResturantFrag.newInstance())
                        }
                    }
                    FoodAppType.Delivery.foodType -> {
                        mutableListOf(HomeFragment.newInstance())
                    }
                    else -> {
                        mutableListOf(PickupResturantFrag.newInstance())
                    }
                }
                if (clientInform?.is_table_booking == "1") {
                    mlistFrag.add(DineInResturantFrag.newInstance())
                }
            }
            else -> {
                tl_home.visibility = View.GONE
                mlistFrag = mutableListOf(PickupResturantFrag.newInstance())
            }
        }

        location_txt?.text = textConfig?.location_text

        mAdapter = PagerAdapter(this@ResturantHomeFrag, mlistFrag ?: emptyList())
        //viewPager.setPagingEnabled(false)
        viewPager.offscreenPageLimit = mlistFrag.size ?: 0
        viewPager.adapter = mAdapter

        viewPager.isUserInputEnabled = false

        viewPager.isSaveFromParentEnabled = true

        tvTitle?.visibility = if (clientInform?.is_skip_theme == "1") View.VISIBLE else View.GONE

        iv_supplier_logo.setImageResource(R.drawable.ic_back_home)

        iv_supplier_logo.setOnClickListener {

            if (appUtils.getCartList().cartInfos?.count() ?: 0 > 0) {
                appUtils.mDialogsUtil.openAlertDialog(activity
                        ?: requireContext(), getString(R.string.cart_clear_popup),
                        getString(R.string.okay), getString(R.string.cancel), this)
            } else {
                activity?.finish()
            }
        }

        ivCart?.setOnClickListener {
            navController(this@ResturantHomeFrag).navigate(R.id.action_resturantHomeFrag_to_cart)
        }

        iv_rewards.setOnClickListener {
            if (prefHelper.getCurrentUserLoggedIn()) {
                navController(this@ResturantHomeFrag).navigate(R.id.action_resturantHomeFrag_to_manageReferralFrag)
            } else {
                appUtils.checkLoginFlow(requireActivity(), REFERAL_REQUEST)
            }
        }

        iv_logo?.setOnClickListener {
            navController(this@ResturantHomeFrag).navigate(R.id.action_resturantHomeFrag_to_skipOther)
        }

        if (clientInform?.referral_feature == "1" && clientInform?.yummyTheme == "1") {
            iv_rewards.visibility = View.VISIBLE
        } else {
            iv_rewards.visibility = View.GONE
        }

        updateLyt()

        if (currentPos != null) {
            tabSetting(tl_home?.getTabAt(tl_home.selectedTabPosition)?.text.toString(), tl_home?.selectedTabPosition
                    ?: 0)
        } else
            currentPos = tl_home?.selectedTabPosition


        if (BuildConfig.CLIENT_CODE == "bookmytable_0882") {
            iv_search?.visibility = View.GONE
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        AppConstants.DELIVERY_OPTIONS = DeliveryType.DeliveryOrder.type
        EventBus.getDefault().unregister(this)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateLyt() {


        prefHelper.getGsonValue(PrefenceConstants.ADRS_DATA, AddressBean::class.java)?.let {
            tvArea.text = appUtils.getAddressFormat(it)
        }

        val isUserLoggedIn = prefHelper.getCurrentUserLoggedIn()
        tvArea.setOnClickListener {
            if (clientInform?.show_ecom_v2_theme == "1") {
                AddressDialogFragmentV2.newInstance(0).show(childFragmentManager, "dialog")
            } else {
                AddressDialogFragment.newInstance(0).show(childFragmentManager, "dialog")
            }
        }

        iv_search.setOnClickListener {
            if (clientInform?.is_unify_search == "1")
                navController(this@ResturantHomeFrag).navigate(R.id.action_resturantHomeFrag_to_unify_SearchFragment)
            else
                navController(this@ResturantHomeFrag).navigate(R.id.action_resturantHomeFrag_to_searchFragment)
        }
        iv_notification.setOnClickListener {
            if (isUserLoggedIn)
                navController(this@ResturantHomeFrag).navigate(R.id.action_home_to_notificationFrag)
            else
                AppToasty.error(requireContext(), getString(R.string.login_first))
        }

        iv_notification.setBadgeValue(prefHelper.getKeyValue(PrefenceConstants.BADGE_COUNT, PrefenceConstants.TYPE_STRING).toString().toIntOrNull()
                ?: 0)
                .setBadgeOvalAfterFirst(true)
                .setBadgeTextSize(16f)
                .setMaxBadgeValue(999)
                .setBadgeBackground(ContextCompat.getDrawable(activity
                        ?: requireContext(), R.drawable.rectangle_rounded))
                .setBadgePosition(BadgePosition.TOP_RIGHT)
                .setBadgeTextStyle(Typeface.NORMAL)
                .setShowCounter(true)
                .setBadgePadding(4)
    }


    private fun settingTabLayout() {
        when {
            bookingFlowBean?.is_pickup_order == FoodAppType.Both.foodType && clientInform?.is_table_booking == "1" -> {
                tl_home.addTab(tl_home.newTab().setText(textConfig?.delivery_tab.toString()).setIcon(R.drawable.ic_delivery_tag))
                tl_home.addTab(tl_home.newTab().setText(textConfig?.pickup_tab.toString()).setIcon(R.drawable.ic_pickup_tag))
                tl_home.addTab(tl_home.newTab().setText(getString(R.string.dine_in_tag)).setIcon(R.drawable.ic_dine))
            }
            bookingFlowBean?.is_pickup_order == FoodAppType.Both.foodType -> {
                tl_home.addTab(tl_home.newTab().setText(textConfig?.delivery_tab.toString()).setIcon(R.drawable.ic_delivery_tag))
                tl_home.addTab(tl_home.newTab().setText(textConfig?.pickup_tab.toString()).setIcon(R.drawable.ic_pickup_tag))
            }
            bookingFlowBean?.is_pickup_order == FoodAppType.Delivery.foodType && clientInform?.is_table_booking == "1" -> {
                tl_home.addTab(tl_home.newTab().setText(textConfig?.delivery_tab.toString()).setIcon(R.drawable.ic_delivery_tag))
                tl_home.addTab(tl_home.newTab().setText(getString(R.string.dine_in_tag)).setIcon(R.drawable.ic_dine))
            }
            bookingFlowBean?.is_pickup_order == FoodAppType.Delivery.foodType -> {
                tl_home.addTab(tl_home.newTab().setText(textConfig?.delivery_tab.toString()).setIcon(R.drawable.ic_delivery_tag))
            }
            bookingFlowBean?.is_pickup_order == FoodAppType.Pickup.foodType && clientInform?.is_table_booking == "1" -> {
                tl_home.addTab(tl_home.newTab().setText(textConfig?.pickup_tab.toString()).setIcon(R.drawable.ic_pickup_tag))
                tl_home.addTab(tl_home.newTab().setText(getString(R.string.dine_in_tag)).setIcon(R.drawable.ic_dine))
            }
            else -> {
                tl_home.addTab(tl_home.newTab().setText(textConfig?.pickup_tab.toString()).setIcon(R.drawable.ic_pickup_tag))
            }
        }
    }


    override fun onTabSelected(tab: TabLayout.Tab) {
        currentPos = tab.position
        tabSetting(tl_home?.getTabAt(currentPos ?: 0)?.text.toString(), currentPos ?: 0)
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        //   tabSetting(tabNames[tab.position])
        // Log.e("onTabUnselected",tabNames[tab.position])
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        //  Log.e("onTabReselected",tabNames[tab.position])
    }

    override fun onAddressSelect(adrsBean: AddressBean) {

        adrsBean.let {
            appUtils.setUserLocale(it)

            prefHelper.addGsonValue(PrefenceConstants.ADRS_DATA, Gson().toJson(it))
            tvArea.text = appUtils.getAddressFormat(it)
        }

        val fragments = mAdapter?.getItem(viewPager.currentItem)
        if (fragments is HomeFragment) {
            if (fragments.clearViewModelApi()) {
                viewPager.adapter = mAdapter
            }
        } else if (fragments is PickupResturantFrag) {
            fragments.checkAvailableZones()
        } else if (fragments is DineInResturantFrag) {
            fragments.checkAvailableZones()
        }
    }

    override fun onDestroyDialog() {

    }


    private fun tabSetting(tabName: String, pos: Int) {

        val frag = mAdapter?.getItem(pos)
        when (tabName) {
            getString(R.string.delivery_txt) -> {
                AppConstants.DELIVERY_OPTIONS = DeliveryType.DeliveryOrder.type
                viewPager.currentItem = pos
                tl_home.getTabAt(pos)?.select()

                if (frag is DineInResturantFrag)
                    frag.changeToDelivery(true)

                mAdapter?.notifyItemChanged(pos)
            }
            getString(R.string.dine_in_tag) -> {
                AppConstants.DELIVERY_OPTIONS = DeliveryType.DineIn.type
                viewPager.currentItem = pos
                tl_home.getTabAt(pos)?.select()
                if (frag is DineInResturantFrag)
                    frag.changeToDelivery(false)
                mAdapter?.notifyItemChanged(pos)
            }
            else -> {
                AppConstants.DELIVERY_OPTIONS = DeliveryType.PickupOrder.type
                viewPager.currentItem = pos
                tl_home.getTabAt(pos)?.select()
                mAdapter?.notifyItemChanged(pos)
            }
        }


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onResultReceived(result: ListItem?) {
//        val currentPos = viewPager?.currentItem
//        Log.e("currentpos",currentPos.toString())
//        if (currentPos != null && currentPos != -1) {
//            val fragments = mAdapter?.getItem(currentPos)
//            if (fragments is HomeFragment)
//                fragments.onResultReceived(result)
//            else if(fragments is DineInResturantFrag)
//                fragments.onResultReceived(result)
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REFERAL_REQUEST && resultCode == Activity.RESULT_OK) {
            iv_rewards.performClick()
        } else if ((requestCode == SCHEDULE_REQUEST || requestCode == AppConstants.REQUEST_PAYMENT_OPTION) && resultCode == Activity.RESULT_OK) {
            if (currentPos != null && currentPos != -1) {
                val fragments = mAdapter?.getItem(currentPos ?: 0)
                if (fragments is HomeFragment)
                    fragments.onActivityResult(requestCode, resultCode, data)
                else if (fragments is DineInResturantFrag)
                    fragments.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onSucessListner() {
        activity?.finish()
    }

    override fun onErrorListener() {

    }


}// Required empty public constructor
