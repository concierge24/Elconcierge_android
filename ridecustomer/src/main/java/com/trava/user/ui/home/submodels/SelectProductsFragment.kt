package com.trava.user.ui.home.submodels

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.transition.Explode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.trava.user.R
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.comfirmbooking.ConfirmBookingFragment
import com.trava.user.ui.home.orderdetails.heavyloads.EnterOrderDetailsFragment
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.homeapi.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trava.user.databinding.FragmentSelectRideTypeBinding
import com.trava.user.ui.home.ScheduleFragment
import com.trava.user.ui.home.fare_breakdown.FareBreakDownFragment
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.homeapi.HomeDriver
import com.trava.utilities.*
import com.trava.utilities.Constants.SECRET_DB_KEY
import kotlinx.android.synthetic.main.fragment_select_ride_type.*
import kotlinx.android.synthetic.main.fragment_select_ride_type.cvToolbar
import kotlinx.android.synthetic.main.fragment_select_ride_type.ivBack

class SelectProductsFragment : Fragment(), InvoiceInterface {
    private val products: ArrayList<Product?>? = ArrayList()
    private var homeActivity: HomeActivity? = null
    private var selectedPosition: Int? = null
    private var adapter: SelectProductsAdapter? = null
    private lateinit var serviceRequest: ServiceRequestModel
    lateinit var selectRideTypeBinding: FragmentSelectRideTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceRequest = (activity as HomeActivity).serviceRequestModel
        serviceRequest.category_brand_product_id = -1
        serviceRequest.selectedCard = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enterTransition = Explode()
            exitTransition = Explode()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        selectRideTypeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_ride_type, container, false)
        selectRideTypeBinding.color = ConfigPOJO.Companion
        val view = selectRideTypeBinding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeActivity = activity as HomeActivity

        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
            cvToolbar.visibility = View.VISIBLE
            ivBack.visibility = View.GONE
            tvSchedule.visibility = View.GONE
            if (ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
                tvHeader.visibility = VISIBLE
            }
        } else {
            cvToolbar.visibility = View.GONE
            ivBack.visibility = View.VISIBLE
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
            tvBookNow.text = getString(R.string.next)
        }
        if (SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14242c4f98380b537e89446cd49b0022f7"||
                SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14bb88ffcd9d48b45b6cd0b34d5a3554e5"|| ConfigPOJO.is_hood_app=="1") {
            tvBookNow.text = getString(R.string.book_now)
        }
        if (SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14242c4f98380b537e89446cd49b0022f7"||
                SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14bb88ffcd9d48b45b6cd0b34d5a3554e5"  ||  SECRET_DB_KEY == "6ae915e1698d88c4e7dda839852fa5fd" || ConfigPOJO.is_hood_app=="1") {
            tvTitle.text = getString(R.string.select_service)
        } else {
            tvTitle.text = getString(R.string.dialog_select_your_choice) + " " + homeActivity?.serviceRequestModel?.category_name + " " + getString(R.string.tv_type)
        }

        tvSchedule.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        tvBookNow.background = StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour, GradientDrawable.RECTANGLE)

        products?.clear()
        products?.addAll(Gson().fromJson(arguments?.getString("submodels", ""), object : TypeToken<List<Product>>() {}.type)
                ?: ArrayList())

        /*Set particular brand related Products on adapter
        these data we got from home api */
        if (products?.isEmpty() == false) {
            if (homeActivity?.serviceRequestModel?.pkgData?.packageData?.packagePricings?.get(0)?.categoryId != null) {
                products.filter {
                    it?.category_brand_product_id == homeActivity?.serviceRequestModel?.pkgData?.packageData?.packagePricings?.get(0)?.categoryBrandId
                }
                selectedPosition = 0
            } else {
                selectedPosition = getSelectedPosition(serviceRequest.category_brand_product_id
                        ?: 0)
            }

            if (products.size > 0) {
                for (i in products.indices) {
                    products.get(i)?.selected_seating_capacity = 1
                }
            }

            var driversList = (activity as HomeActivity).serviceRequestModel.driverList
            if (driversList.isEmpty()) {
                driversList = ((activity as HomeActivity).serviceDetails?.drivers as? ArrayList)
                        ?: ArrayList()
            }

            Log.e("jhfjgfyuefdfd",driversList.size.toString()+"Sdsdsd")

            if(products.size>0) {
                for (i in 0 until products.size) {
                    for (item in driversList) {
                        if (item.category_brand_product_id == products[i]!!.category_brand_product_id) {
                            products[i]!!.driversList = ArrayList<HomeDriver>()
                            products[i]!!.driversList.add(item)
                        }
                    }
                }
            }

            adapter = SelectProductsAdapter(products, selectedPosition!!, this)
            rvCompanies?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            rvCompanies?.adapter = adapter
        }

        intialize()
        setListeners()
    }

    private fun intialize() {
        if (arguments?.containsKey("type") == true) {
            tvTitle.text = getString(R.string.schBooking)
            tvScheduleDate.show()
            tvScheduleDate.text = serviceRequest.order_timings_text
        }
    }

    private fun setListeners() {
        /*Show view's according to flow requirement (Schedule ride)*/
        if (serviceRequest.bookingFlow == "1") {
            view_1.visibility = View.GONE
            tvSchedule.visibility = View.GONE
        } else {
            if (ConfigPOJO.settingsResponse?.key_value?.is_schedule_ride == "true") {
                view_1.visibility = View.VISIBLE
                tvSchedule.visibility = View.VISIBLE
            } else {
                view_1.visibility = View.GONE
                tvSchedule.visibility = View.GONE
            }
        }

        ivBack.setOnClickListener {
            activity?.onBackPressed()
        }

        cvToolbar.setOnClickListener {
            activity?.onBackPressed()
        }

        tvSchedule.setOnClickListener {
            if (products?.isEmpty() == true) {
                rootView.showSnack(R.string.no_vehicles_available)
                return@setOnClickListener
            }

            serviceRequest.bookingTypeTemp = serviceRequest.bookingType
            serviceRequest.category_brand_product_id = adapter?.getSelectedBrandProductId()
            serviceRequest.load_unload_charges = adapter?.getSelectedBrandProductLoadPrice()
            serviceRequest.productName = adapter?.getSelectedBrandProductName()
            serviceRequest.seating_capacity = adapter?.getSelectedProduct()?.seating_capacity
            serviceRequest.selectedProduct = adapter?.getSelectedProduct()

            serviceRequest.final_charge = getFinalCharge(adapter?.getSelectedProduct()!!, context!!)

            serviceRequest.images = ArrayList()
            serviceRequest.product_weight = 0F
            serviceRequest.material_details = ""
            serviceRequest.details = ""
            serviceRequest.future = ServiceRequestModel.SCHEDULE
            fragmentManager?.beginTransaction()?.replace(R.id.container, ScheduleFragment())?.addToBackStack("backstack")?.commit()
        }

        tvBookNow.setOnClickListener {
            if (products?.isEmpty() == true) {
                rootView.showSnack(R.string.no_vehicles_available)
                return@setOnClickListener
            }
            serviceRequest.bookingTypeTemp = serviceRequest.bookingType
            serviceRequest.category_brand_product_id = adapter?.getSelectedBrandProductId()
            serviceRequest.load_unload_charges = adapter?.getSelectedBrandProductLoadPrice()
            serviceRequest.productName = adapter?.getSelectedBrandProductName()
            serviceRequest.seating_capacity = adapter?.getSelectedProduct()?.seating_capacity
            serviceRequest.selectedProduct = adapter?.getSelectedProduct()
            if (serviceRequest.pkgData != null) {
                val totalPrice = adapter?.getSelectedProduct()?.timeFixedPrice
                serviceRequest.final_charge = totalPrice?.toDouble()
                serviceRequest.price_per_min = adapter?.getSelectedProduct()?.pricePerMin ?: 0.0
                serviceRequest.distance_price_fixed = adapter?.getSelectedProduct()?.distancePriceFixed
                        ?: 0.0
                serviceRequest.time_fixed_price = adapter?.getSelectedProduct()?.timeFixedPrice ?: 0.0
                serviceRequest.price_per_km = adapter?.getSelectedProduct()?.pricePerKm ?: 0.0

            } else {

                serviceRequest.final_charge = getFinalCharge(adapter?.getSelectedProduct()!!, context!!)
            }
            serviceRequest.images = ArrayList()
            serviceRequest.product_weight = 0F
            serviceRequest.material_details = ""
            serviceRequest.details = ""

            if (serviceRequest.check_lists != null) {
                serviceRequest.check_lists.clear()
            }

            /*Check Flow
            * if Booking flow 1 = User move on request detail screen. where user fill all required fields.
            * if Booking flow 0 = No nned to fill extra details for request and direct move on confirm booking screen*/
            if (serviceRequest.bookingFlow == "1") {
                val fragment = EnterOrderDetailsFragment()
                val bundle = Bundle()
                bundle.putParcelable("productItem", adapter?.getSelectedProduct())
                fragment.arguments = bundle
                fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
            } else {
                serviceRequest.future = ServiceRequestModel.BOOK_NOW
                fragmentManager?.beginTransaction()?.replace(R.id.container, ConfirmBookingFragment())?.addToBackStack("backstack")?.commit()
            }
        }
    }

    fun getFinalCharge(product: Product, context: Context): Double {
        var distance_r = 0.0
        var time_r = 0.0
        var buraqfare_r = 0.0
        var actualAmount = 0.0
        var scheduleCharge = 0.0
        var final_amount = 0.0
        var totalPrice=0.0
        var exactAmount=0.0
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

        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            var airport_charge = 0.0
            if ((context as HomeActivity).serviceRequestModel?.airportChargesPickup != "") {
                airport_charge = (context as HomeActivity).serviceRequestModel?.airportChargesPickup?.toDouble() ?: 0.0
            }
            if ((context as HomeActivity).serviceRequestModel?.airportChargesDrop != "") {
                airport_charge += (context as HomeActivity).serviceRequestModel?.airportChargesDrop?.toDouble() ?: 0.0
            }

            var scheduleCharge = 0.0
            if (ConfigPOJO.settingsResponse?.key_value?.schedule_charge == "true") {
                scheduleCharge =product.schedule_charge.toDouble()
            }
            totalPrice = distance_r.plus(time_r).plus(product.alpha_price ?: 0.0f).plus(airport_charge).plus(scheduleCharge)

            var afterPromoAmount = 0.0
            if ((context as HomeActivity).serviceRequestModel?.promoData!=null)
            {
                when ((context as HomeActivity).serviceRequestModel?.promoData?.couponType) {
                    COUPEN_TYPE.VALUE -> {
                        afterPromoAmount = totalPrice?.minus((context as HomeActivity).serviceRequestModel?.promoData?.amountValue?.toDouble() ?: 0.0) ?: 0.0
                    }

                    COUPEN_TYPE.PERCENTAGE -> {
                        var disvalue=totalPrice?.times((context as HomeActivity).serviceRequestModel?.promoData?.amountValue?.toDouble()?: 0.0)?.div(100) ?: 0.0

                        if (disvalue<(context as HomeActivity).serviceRequestModel?.promoData?.maximum_discount?.toDouble()?: 0.0)
                        {
                            afterPromoAmount=totalPrice?.minus(disvalue)
                        }
                        else
                        {
                            afterPromoAmount=totalPrice?.minus((context as HomeActivity).serviceRequestModel?.promoData?.maximum_discount?.toDouble()?: 0.0)
                        }
                    }
                }
            }

            if (afterPromoAmount!=0.0)
            {
                totalPrice=afterPromoAmount
            }
            buraqfare_r = totalPrice.times(product.buraq_percentage).div(100)
            actualAmount = totalPrice.plus(buraqfare_r)
            exactAmount=actualAmount
        }
        else
        {
            totalPrice = distance_r.plus(time_r).plus(product.alpha_price ?: 0.0f)
            if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
            {
                buraqfare_r = totalPrice.times(product.buraq_percentage).div(100)
                actualAmount = totalPrice.plus(buraqfare_r)
                var sur = product.surchargeValue.toDouble()

                final_amount = actualAmount.plus(sur)
                if (ConfigPOJO.settingsResponse?.key_value?.schedule_charge == "true") {
                    scheduleCharge = if (product.schedule_charge_type == "value") {
                        product.schedule_charge.toDouble()
                    } else {
                        final_amount.times(product.schedule_charge).div(100)
                    }
                }
                exactAmount = final_amount.plus(scheduleCharge)
                if (context.getString(R.string.app_name) == "Wasila") {
                    if (ConfigPOJO.OMAN_PHONECODE == "+968") {
                        exactAmount = exactAmount.times(ConfigPOJO.OMAN_CURRENCY)
                    }
                }
            }
        }
        (context as HomeActivity).serviceRequestModel.final_charge=exactAmount
        return (exactAmount)
    }

    private fun getSelectedPosition(categoryBrandId: Int): Int {
        for (i in products.orEmpty().indices) {
            if (products?.get(i)?.category_brand_product_id == categoryBrandId) {
                return i
            }
        }
        return 0
    }

    override fun OnInfoClick(data: Product?) {
        val fragment = FareBreakDownFragment()
        val bundle = Bundle()
        bundle.putParcelable("productItem", adapter?.getSelectedProduct())
        fragment.arguments = bundle
        fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
    }
}