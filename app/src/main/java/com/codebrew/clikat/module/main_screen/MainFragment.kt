package com.codebrew.clikat.module.main_screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.DeliveryType
import com.codebrew.clikat.data.FoodAppType
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.modal.other.English
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.modal.other.SupplierInArabicBean
import com.codebrew.clikat.preferences.DataNames
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class MainFragment : Fragment() {


    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null
    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null
    private var clientInform: SettingModel.DataBean.SettingData? = null

    @Inject
    lateinit var prefHelper: PreferenceHelper

    private var bannerItem: English? = null
    private var bundle: Bundle? = null

    private var catId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)


        catId = if (prefHelper.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString().isNotEmpty()) {
            prefHelper.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString().toInt()
        } else {
            0
        }

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        screenFlowBean = prefHelper.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        clientInform = prefHelper.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
        bookingFlowBean = prefHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)

        if (arguments?.containsKey("bannerItem") == true) {
            bannerItem = arguments?.getParcelable("bannerItem")
            bundle = bundleOf("title" to bannerItem?.supplierName,
                    "categoryId" to bannerItem?.id,
                    "subCategoryId" to 0, "supplierId" to bannerItem?.supplier_id, "type" to "banner")
        } else if (arguments?.containsKey("cat_name") == true) {
            bundle = bundleOf("title" to arguments?.getString("cat_name"))
        }

        if (arguments?.containsKey("screenType") == true) {
            when (screenFlowBean?.app_type ?: 0) {
                AppDataType.Food.type -> {

                    if (clientInform?.app_selected_template != null && clientInform?.app_selected_template == "1") {
                        navController(this@MainFragment).navigate(R.id.action_mainFragment_to_restaurantDetailFragNew, bundle)
                    } else {
                        navController(this@MainFragment).navigate(R.id.action_mainFragment_to_restaurantDetailFragment, bundle)
                    }

                }
                AppDataType.HomeServ.type -> {
                    val supplierBean = SupplierInArabicBean()
                    supplierBean.supplier_branch_id = bannerItem?.supplier_branch_id
                    supplierBean.id = bannerItem?.supplier_id

                    bundle?.putBoolean("is_supplier", true)
                    bundle?.putParcelable("supplierData", supplierBean)
                    navController(this@MainFragment).navigate(R.id.action_mainFragment_to_subCategory, bundle)
                }
            }

        } else {
            when (screenFlowBean?.app_type ?: 0) {
                AppDataType.Food.type -> {
                    if ((bookingFlowBean?.is_pickup_order == FoodAppType.Both.foodType || bookingFlowBean?.is_pickup_order == FoodAppType.Pickup.foodType) && screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
                        AppConstants.DELIVERY_OPTIONS = DeliveryType.DeliveryOrder.type
                        navController(this@MainFragment).navigate(R.id.action_mainFragment_to_resturantHomeFrag)
                    } else {
                        navController(this@MainFragment).navigate(R.id.action_mainFragment_to_homeFragment)
                    }
                }
                AppDataType.CarRental.type -> {
                    if (catId == 89) {
                        navController(this@MainFragment).navigate(R.id.action_mainFragment_to_boatRental, bundle)
                    } else {
                        navController(this@MainFragment).navigate(R.id.action_mainFragment_to_homeRental, bundle)
                    }
                }
                else -> {
                    navController(this@MainFragment).navigate(R.id.action_mainFragment_to_homeFragment)
                }
            }
        }

    }
}
