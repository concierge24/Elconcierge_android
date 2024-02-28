package com.trava.user.ui.home.comfirmbooking

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.transition.Explode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.trava.driver.webservices.models.WalletBalModel
import com.trava.user.R
import com.trava.user.databinding.ConfirmDialogBinding
import com.trava.user.databinding.FragmentConfirmBooking2Binding
import com.trava.user.databinding.FragmentConfirmBookingBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.ScheduleFragment
import com.trava.user.ui.home.comfirmbooking.outstanding_payment.OutstandingPaymentFragment
import com.trava.user.ui.home.comfirmbooking.outstanding_payment.OutstandingPaymentInterface
import com.trava.user.ui.home.comfirmbooking.payment.SavedCards
import com.trava.user.ui.home.confirm_pickup.ConfirmPickupActivity
import com.trava.user.ui.home.processingrequest.ProcessingRequestFragment
import com.trava.user.ui.home.promocodes.PromoCodesActivity
import com.trava.user.ui.home.vehicles.VehicleContract
import com.trava.user.ui.home.vehicles.VehiclePresenter
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.MyAnimationUtils
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.cards_model.UserCardData
import com.trava.user.webservices.models.contacts.RideShareResponse
import com.trava.user.webservices.models.homeapi.Product
import com.trava.user.webservices.models.order.Order
import com.trava.user.webservices.models.order.OutstandingChargesModel
import com.trava.user.webservices.models.orderrequest.ResultItem
import com.trava.user.webservices.models.promocodes.CouponsItem
import com.trava.user.webservices.models.travelPackages.PackagesItem
import com.trava.utilities.*
import com.trava.utilities.Constants.PROMO_AMMOUNT
import com.trava.utilities.Constants.PROMO_ID
import com.trava.utilities.Constants.SECRET_DB_KEY
import com.trava.utilities.constants.ORDER
import com.trava.utilities.constants.PROFILE
import com.trava.utilities.constants.STORIED_VIEWES
import com.trava.utilities.webservices.models.AppDetail
import kotlinx.android.synthetic.main.fragment_confirm_booking.*
import kotlinx.android.synthetic.main.fragment_confirm_booking.acDropOffAddress
import kotlinx.android.synthetic.main.fragment_confirm_booking.acPickupAddress
import kotlinx.android.synthetic.main.fragment_confirm_booking.animContainer
import kotlinx.android.synthetic.main.fragment_confirm_booking.ivBack
import kotlinx.android.synthetic.main.fragment_confirm_booking.ivVehicleImage
import kotlinx.android.synthetic.main.fragment_confirm_booking.rbOthers
import kotlinx.android.synthetic.main.fragment_confirm_booking.rlCredits
import kotlinx.android.synthetic.main.fragment_confirm_booking.rlPromo
import kotlinx.android.synthetic.main.fragment_confirm_booking.rootView
import kotlinx.android.synthetic.main.fragment_confirm_booking.sbCredits
import kotlinx.android.synthetic.main.fragment_confirm_booking.switch_ll
import kotlinx.android.synthetic.main.fragment_confirm_booking.tvApplyPromoCode
import kotlinx.android.synthetic.main.fragment_confirm_booking.tvCreditCard
import kotlinx.android.synthetic.main.fragment_confirm_booking.tvCreditsLeft
import kotlinx.android.synthetic.main.fragment_confirm_booking.tvPaymentMethod
import kotlinx.android.synthetic.main.fragment_confirm_booking.tvPriceRange
import kotlinx.android.synthetic.main.fragment_confirm_booking.tvPromoCode
import kotlinx.android.synthetic.main.fragment_confirm_booking.tvSeatingCapcity
import kotlinx.android.synthetic.main.fragment_confirm_booking.tvVehicleName
import kotlinx.android.synthetic.main.fragment_confirm_booking.view8
import kotlinx.android.synthetic.main.fragment_confirm_booking.view9
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class ConfirmBookingFragment : Fragment(), ConfirmBookingContract.View, View.OnClickListener,
    OutstandingPaymentInterface, VehicleContract.View {
    private val presenter = ConfirmBookingPresenter()
    var wallet_min_balance = 0F
    var wallet_actual_balance = ""
    private val RQ_PAYMENT_TYPE = 400
    private val CONFIRM_PICKUP_LOC = 401
    private var dialogIndeterminate: DialogIndeterminate? = null
    private var serviceRequest: ServiceRequestModel? = null
    private var selectedPaymentMethod = PaymentType.CASH
    private var tokenId: Int? = null
    private var isPromoApplied = false
    private var promoData: CouponsItem? = null
    private var savedCardsList = ArrayList<UserCardData>()
    private var isCardsLoaded = false
    private var creditPoints = 0
    private var sbIsChecked = false
    private var afterPromoAmount = 0.0
    private var tmpFinalAmt = 0.0
    lateinit var confirmBookingBinding: FragmentConfirmBookingBinding
    lateinit var confirmBookingBinding2: FragmentConfirmBooking2Binding
    private var dialog: BottomSheetDialog? = null
    var homeActivity: HomeActivity? = null
    var count = 0
    var bonusKmValue: String = "0"

    private var cust_id = ""
    private var initialVialue = 0.0
    private var last_four = ""
    var sp_cards: Spinner? = null

    var confirmView: View? = null
    private var vPresenter = VehiclePresenter()
    var buraqfare_r = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enterTransition = Explode()
            exitTransition = Explode()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeActivity?.getMapAsync()

        // Inflate the layout for this fragment
        confirmBookingBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_confirm_booking, container, false)
        confirmBookingBinding2 =
            DataBindingUtil.inflate(inflater, R.layout.fragment_confirm_booking2, container, false)
        confirmBookingBinding.color = ConfigPOJO.Companion
        confirmBookingBinding2.color = ConfigPOJO.Companion

        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
            confirmView = confirmBookingBinding2.root
        } else {
            confirmView = confirmBookingBinding.root
        }

        if (ConfigPOJO.is_merchant == "true") {
            confirmBookingBinding2.rlBonus.visibility = View.VISIBLE
            confirmBookingBinding.rlBonus.visibility = View.VISIBLE
            confirmBookingBinding.rlPromo.visibility = View.GONE
        } else {
            confirmBookingBinding2.rlBonus.visibility = View.GONE
            confirmBookingBinding.rlBonus.visibility = View.GONE
        }
        return confirmView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        vPresenter.attachView(this)

        sp_cards = view.findViewById(R.id.sp_cards)

        intialize()
        SharedPrefs.with(activity).remove(STORIED_VIEWES)

        //check insurance value
        if (ConfigPOJO.settingsResponse?.key_value?.is_insurance == "true") {
            if (serviceRequest?.pkgData == null) {
                rlInsurance.visibility = View.VISIBLE
                serviceRequest?.insurance_amount = "0"
                tvCreditsAmount.text = ConfigPOJO.currency + " " + ConfigPOJO.settingsResponse?.key_value?.insurance_amount
            }
        }

        /*Manage views according to template*/
        if (ConfigPOJO.TEMPLATE_CODE != Constants.GOMOVE_BASE) {
            if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
                ivBack.visibility = View.GONE
                iv_info.visibility = View.GONE
                rlCredits.visibility = View.GONE
                view9.visibility = View.GONE
                cvToolbar.visibility = View.VISIBLE
                ivBack.visibility = View.GONE
                iv_seating_image.visibility = View.GONE
                tvSeatingCapcity.visibility = View.GONE
                tvCreditCard.visibility = View.VISIBLE
                rr_address.visibility = View.VISIBLE
                iv_seating.text = ""
                if (ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
                    confirmBookingBinding.tvBook.setText(getString(R.string.proceed))
                    confirmBookingBinding.genderRl.visibility = View.GONE
                }
            } else {
                tvVehicleName.setOnClickListener(this)
                cvToolbar.visibility = View.GONE
                ivBack.visibility = View.VISIBLE
                iv_info.visibility = View.GONE
                iv_seating_image.visibility = View.GONE
                iv_seating.text = context?.getString(R.string.seatCapacity)
                tvCreditCard.visibility = View.VISIBLE

                if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
                    confirmBookingBinding.rlPromo.visibility = View.GONE
                }
            }

            if (serviceRequest?.bookingFlow == "1") {
                ll_seating_cap.visibility = View.GONE
            }

            if (ConfigPOJO.gateway_unique_id == "payku" || ConfigPOJO.gateway_unique_id == "paystack"
                || ConfigPOJO.gateway_unique_id == "wipay"
                || ConfigPOJO.gateway_unique_id == "qpaypro"
                || ConfigPOJO.gateway_unique_id == "thawani"
                || ConfigPOJO.gateway_unique_id == "paytab"
                || ConfigPOJO.gateway_unique_id == "braintree"
                || ConfigPOJO.gateway_unique_id == "datatrans"
            ) {
                tv_paytype.text = getString(R.string.pay_online)
            } else {
                tv_paytype.text = getString(R.string.creditCard)
            }

            /* get user credit points */
            if (ConfigPOJO?.settingsResponse?.key_value?.is_wallet == "true") {
                tvUseCreditss.text = getString(R.string.use_wallet)
                if (CheckNetworkConnection.isOnline(context)) {
                    presenter.getWalletBalance(false)
                } else {
                    CheckNetworkConnection.showNetworkError(rootView)
                }
            } else {
                hitCreditPointsApi()
            }

            if (ConfigPOJO.TEMPLATE_CODE == Constants.EAGLE) {
                switch_ll.visibility = View.GONE
                view8.visibility = View.GONE
            }
        }

        if (ConfigPOJO.settingsResponse?.key_value?.is_schedule_ride == "true") {
            confirmBookingBinding.tvSchedule.visibility = View.VISIBLE
            confirmBookingBinding2.tvSchedule.visibility = View.VISIBLE
        } else {
            confirmBookingBinding.tvSchedule.visibility = View.GONE
            confirmBookingBinding2.tvSchedule.visibility = View.GONE
        }

        confirmBookingBinding.tvBook.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.Btn_Colour,
            ConfigPOJO.Btn_Colour,
            GradientDrawable.RECTANGLE
        )
        confirmBookingBinding2.tvBook2.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.Btn_Colour,
            ConfigPOJO.Btn_Colour,
            GradientDrawable.RECTANGLE
        )
        confirmBookingBinding2.tvCurrency.setText(ConfigPOJO.currency)
        confirmBookingBinding.tvCurrency.setText(getString(R.string.booking_charges) + " " + ConfigPOJO.currency)
        confirmBookingBinding.tvBook.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour))
        confirmBookingBinding2.tvBook2.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour))

        confirmBookingBinding.tvCancelBonus.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.primary_color,
            ConfigPOJO.primary_color,
            GradientDrawable.RECTANGLE
        )
        confirmBookingBinding2.tvCancelBonus1.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.primary_color,
            ConfigPOJO.primary_color,
            GradientDrawable.RECTANGLE
        )
        confirmBookingBinding.tvApply.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.primary_color,
            ConfigPOJO.primary_color,
            GradientDrawable.RECTANGLE
        )
        confirmBookingBinding2.tvApply1.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.primary_color,
            ConfigPOJO.primary_color,
            GradientDrawable.RECTANGLE
        )
        confirmBookingBinding2.tvSchedule.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.white_color,
            ConfigPOJO.secondary_color,
            GradientDrawable.RECTANGLE
        )
        confirmBookingBinding.tvSchedule.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.white_color,
            ConfigPOJO.secondary_color,
            GradientDrawable.RECTANGLE
        )

        setData()
        setListeners()
        showSurchargeData("show")

        if (SECRET_DB_KEY == "a3f699d8c406daaf828f2d2c891eac15110024532f77f082ad972842c8516d60"
            || SECRET_DB_KEY == "fdd4d886d691305c703e8274bbd49ec217a41b4a1bc16322f1345c000a1987f5"
        ) {
            confirmBookingBinding.tvPaytype.text = requireActivity().getString(R.string.credit_card)
            confirmBookingBinding.tvCashPay.text = activity!!.getString(R.string.cash_payment)
        }

        /*Cash Booking dynamic views*/
        if (ConfigPOJO.settingsResponse?.key_value?.is_cash_on_Delivery == "true" ||
            ConfigPOJO.settingsResponse?.key_value?.is_cash_payment_enabled == "true" ||
            ConfigPOJO.TEMPLATE_CODE == Constants.CORSA
        ) {
            confirmBookingBinding.tvPaymentMethod.visibility = View.VISIBLE
            confirmBookingBinding2.tvPaymentMethod.visibility = View.VISIBLE
        } else {
            confirmBookingBinding.tvPaymentMethod.visibility = View.GONE
            confirmBookingBinding2.tvPaymentMethod.visibility = View.GONE
            selectedPaymentMethod = PaymentType.CARD
            tvCreditCard.background = StaticFunction.changeBorderTextColor(
                ConfigPOJO.white_color,
                ConfigPOJO.primary_color,
                GradientDrawable.RECTANGLE
            )
        }

        /*Card Booking dynamic views*/
        if (ConfigPOJO.is_card_payment_enabled == "true" || ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            confirmBookingBinding.tvCreditCard.visibility = View.VISIBLE
            confirmBookingBinding2.tvCreditCard.visibility = View.VISIBLE
        } else {
            confirmBookingBinding.tvCreditCard.visibility = View.GONE
            confirmBookingBinding2.tvCreditCard.visibility = View.GONE
        }

        if (selectedPaymentMethod == PaymentType.CARD) {
            tvPaymentMethod.background = StaticFunction.changeBorderTextColor(
                ConfigPOJO.white_color,
                ConfigPOJO.gray_color,
                GradientDrawable.RECTANGLE
            )
            tvCreditCard.background = StaticFunction.changeBorderTextColor(
                ConfigPOJO.white_color,
                ConfigPOJO.secondary_color,
                GradientDrawable.RECTANGLE
            )
        } else if (selectedPaymentMethod == PaymentType.CASH) {
            tvPaymentMethod.background = StaticFunction.changeBorderTextColor(
                ConfigPOJO.white_color,
                ConfigPOJO.secondary_color,
                GradientDrawable.RECTANGLE
            )
            tvCreditCard.background = StaticFunction.changeBorderTextColor(
                ConfigPOJO.white_color,
                ConfigPOJO.gray_color,
                GradientDrawable.RECTANGLE
            )
        }
    }

    /*Surcharge Value*/
    private fun showSurchargeData(action: String) {
        if (action == "show") {
            if (serviceRequest?.surChargeValue ?: 0.0 == 0.0) {
                confirmBookingBinding.tvSurcharge.visibility = View.GONE
                confirmBookingBinding2.tvSurcharge.visibility = View.GONE
            } else {
                confirmBookingBinding.tvSurcharge.visibility = View.VISIBLE
                confirmBookingBinding2.tvSurcharge.visibility = View.VISIBLE
            }

            confirmBookingBinding.tvSurcharge.text =
                getString(R.string.surcharge_amount) + " - " + ConfigPOJO.currency + " " + serviceRequest?.surChargeValue.toString()
            confirmBookingBinding2.tvSurcharge.text =
                getString(R.string.surcharge_amount) + " - " + ConfigPOJO.currency + " " + serviceRequest?.surChargeValue.toString()
        } else {
            confirmBookingBinding.tvSurcharge.visibility = View.GONE
            confirmBookingBinding2.tvSurcharge.visibility = View.GONE
        }
    }

    private fun intialize() {

        /*Gender Selection Dynamic*/
        if (ConfigPOJO.settingsResponse?.key_value?.is_gender_selection_enabled == "true") {
            confirmBookingBinding.genderRl.visibility = View.VISIBLE
            confirmBookingBinding2.genderRl.visibility = View.VISIBLE
        } else {
            confirmBookingBinding.genderRl.visibility = View.GONE
            confirmBookingBinding2.genderRl.visibility = View.GONE
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            val profile = SharedPrefs.with(context).getObject(PROFILE, AppDetail::class.java)
            if (profile.user?.gender == "Male" || profile.user?.gender == "") {
                confirmBookingBinding.genderRl.visibility = View.GONE
                confirmBookingBinding2.genderRl.visibility = View.GONE
            } else {
                confirmBookingBinding.genderRl.visibility = View.VISIBLE
                confirmBookingBinding2.genderRl.visibility = View.VISIBLE
            }
        } else {
            /* Re-intialized Promo code variables */
            serviceRequest?.isPromoApplied = false
            serviceRequest?.coupenId = 0
            serviceRequest?.afterPromoFinalCharge = 0.0
        }

        serviceRequest = (activity as HomeActivity).serviceRequestModel
        serviceRequest?.bookingType = serviceRequest?.bookingTypeTemp ?: ""
        dialogIndeterminate = DialogIndeterminate(requireActivity())

        /* Hide PromoCodes views at package booking */
        if (serviceRequest?.pkgData != null) {
            rlPromo.hide()
            view8.hide()
        }
    }

    /*Credit points api call*/
    private fun hitCreditPointsApi() {
        if (CheckNetworkConnection.isOnline(context)) {
            presenter.requestUserCreditPoints()
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    /*Gomove initial pricing structure*/
    fun getGomovePickupInitialCharge(product: Product, context: Context): Double {
        var isPickupElevator: Boolean = false
        var isDroppElevator: Boolean = false
        var loadTime: Double? = 0.0
        var unloadTime: Double? = 0.0

        loadTime = product.load_unload_time?.toDouble()
        unloadTime = product.load_unload_time?.toDouble()

        var pickLevel: Int? = 0
        pickLevel = serviceRequest?.pickup_level?.toInt() ?: 0
        var dropLevel: Int? = 0
        dropLevel = serviceRequest?.dropoff_level?.toInt() ?: 0

        var pickuprateVAlue = "0"
        var dropRateValue = "0"
        if (serviceRequest?.elevator_pickup.equals("true")) {
            isPickupElevator = true
        }
        if (serviceRequest?.elevator_dropoff.equals("true")) {
            isDroppElevator = true
        }

        if (isPickupElevator) {
            pickuprateVAlue = ConfigPOJO.settingsResponse?.key_value?.elevator_percentage ?: "0"
        } else {
            pickuprateVAlue = ConfigPOJO.settingsResponse?.key_value?.level_percentage ?: "0"
        }

        if (isDroppElevator) {
            dropRateValue = ConfigPOJO.settingsResponse?.key_value?.elevator_percentage ?: "0"
        } else {
            dropRateValue = ConfigPOJO.settingsResponse?.key_value?.level_percentage ?: "0"
        }

        var timeLoading = 0.0
        var timeUnloading = 0.0
        if (pickuprateVAlue == "") {
            pickuprateVAlue = "0"
        }
        if (dropRateValue == "") {
            dropRateValue = "0"
        }

        var exactPick =
            (loadTime?.times(pickLevel.toDouble())?.times(pickuprateVAlue.toDouble()))?.div(100)
        timeLoading = loadTime?.plus(exactPick ?: 0.0) ?: 0.0

        var exactDrop =
            (unloadTime?.times(dropLevel.toDouble())?.times(dropRateValue.toDouble()))?.div(100)
        timeUnloading = unloadTime?.plus(exactDrop ?: 0.0) ?: 0.0

        var estimatedTime = serviceRequest?.eta?.toDouble()

        var ET = timeLoading.toDouble() + timeUnloading.toDouble() + estimatedTime!!

        var estimatePriceHourly = (estimatedTime.toFloat() * product.price_per_hr) +
                (timeLoading.toFloat() * product.price_per_hr) +
                (timeUnloading.toFloat() * product.price_per_hr) + product.alpha_price?.toDouble()!!
        return estimatePriceHourly
    }

    /*Gomove pricing structure*/
    fun getGomovePickupCharge(product: Product, context: Context): Double {
        var isPickupElevator: Boolean = false
        var isDroppElevator: Boolean = false
        var loadTime: Double? = 0.0
        var unloadTime: Double? = 0.0

        loadTime = product.load_unload_time?.toDouble()
        unloadTime = product.load_unload_time?.toDouble()

        var pickLevel: Int? = 0
        pickLevel = serviceRequest?.pickup_level?.toInt() ?: 0
        var dropLevel: Int? = 0
        dropLevel = serviceRequest?.dropoff_level?.toInt() ?: 0

        var pickuprateVAlue = "0"
        var dropRateValue = "0"
        if (serviceRequest?.elevator_pickup.equals("true")) {
            isPickupElevator = true
        }
        if (serviceRequest?.elevator_dropoff.equals("true")) {
            isDroppElevator = true
        }

        if (isPickupElevator) {
            pickuprateVAlue = ConfigPOJO.settingsResponse?.key_value?.elevator_percentage ?: "0"
        } else {
            pickuprateVAlue = ConfigPOJO.settingsResponse?.key_value?.level_percentage ?: "0"
        }

        if (isDroppElevator) {
            dropRateValue = ConfigPOJO.settingsResponse?.key_value?.elevator_percentage ?: "0"
        } else {
            dropRateValue = ConfigPOJO.settingsResponse?.key_value?.level_percentage ?: "0"
        }

        var timeLoading = 0.0
        var timeUnloading = 0.0
        if (pickuprateVAlue == "") {
            pickuprateVAlue = "0"
        }
        if (dropRateValue == "") {
            dropRateValue = "0"
        }

        var exactPick =
            (loadTime?.times(pickLevel.toDouble())?.times(pickuprateVAlue.toDouble()))?.div(100)
        timeLoading = loadTime?.plus(exactPick ?: 0.0) ?: 0.0

        var exactDrop =
            (unloadTime?.times(dropLevel.toDouble())?.times(dropRateValue.toDouble()))?.div(100)
        timeUnloading = unloadTime?.plus(exactDrop ?: 0.0) ?: 0.0

        var estimatedTime = serviceRequest?.eta?.toDouble()

        var ET = timeLoading.toDouble() + timeUnloading.toDouble() + estimatedTime!!

        var estimatePriceHourly = (estimatedTime.toFloat() * product.price_per_hr) +
                (timeLoading.toFloat() * product.price_per_hr) +
                (timeUnloading.toFloat() * product.price_per_hr) + product.alpha_price?.toDouble()!!
        var buraqfare_r = 0.0
        buraqfare_r = estimatePriceHourly.times(product.buraq_percentage).div(100)
        return estimatePriceHourly.plus(buraqfare_r)
    }

    /*Gomove pricing structure*/
    fun getGomovePickupChargePromo(product: Product, context: Context, disvalue: Double): Double {
        var isPickupElevator: Boolean = false
        var isDroppElevator: Boolean = false
        var loadTime: Double? = 0.0
        var unloadTime: Double? = 0.0

        loadTime = product.load_unload_time?.toDouble()
        unloadTime = product.load_unload_time?.toDouble()

        var pickLevel: Int? = 0
        pickLevel = serviceRequest?.pickup_level?.toInt() ?: 0
        var dropLevel: Int? = 0
        dropLevel = serviceRequest?.dropoff_level?.toInt() ?: 0

        var pickuprateVAlue = "0"
        var dropRateValue = "0"
        if (serviceRequest?.elevator_pickup.equals("true")) {
            isPickupElevator = true
        }
        if (serviceRequest?.elevator_dropoff.equals("true")) {
            isDroppElevator = true
        }

        if (isPickupElevator) {
            pickuprateVAlue = ConfigPOJO.settingsResponse?.key_value?.elevator_percentage ?: "0"
        } else {
            pickuprateVAlue = ConfigPOJO.settingsResponse?.key_value?.level_percentage ?: "0"
        }

        if (isDroppElevator) {
            dropRateValue = ConfigPOJO.settingsResponse?.key_value?.elevator_percentage ?: "0"
        } else {
            dropRateValue = ConfigPOJO.settingsResponse?.key_value?.level_percentage ?: "0"
        }

        var timeLoading = 0.0
        var timeUnloading = 0.0
        if (pickuprateVAlue == "") {
            pickuprateVAlue = "0"
        }
        if (dropRateValue == "") {
            dropRateValue = "0"
        }

        var exactPick =
            (loadTime?.times(pickLevel.toDouble())?.times(pickuprateVAlue.toDouble()))?.div(100)
        timeLoading = loadTime?.plus(exactPick ?: 0.0) ?: 0.0

        var exactDrop =
            (unloadTime?.times(dropLevel.toDouble())?.times(dropRateValue.toDouble()))?.div(100)
        timeUnloading = unloadTime?.plus(exactDrop ?: 0.0) ?: 0.0

        var estimatedTime = serviceRequest?.eta?.toDouble()

        var ET = timeLoading.toDouble() + timeUnloading.toDouble() + estimatedTime!!

        var estimatePriceHourly = (estimatedTime.toFloat() * product.price_per_hr) +
                (timeLoading.toFloat() * product.price_per_hr) +
                (timeUnloading.toFloat() * product.price_per_hr) + product.alpha_price?.toDouble()!! - disvalue
        var buraqfare_r = 0.0
        buraqfare_r = estimatePriceHourly.times(product.buraq_percentage).div(100)
        return estimatePriceHourly.plus(buraqfare_r)
    }

    /*Ride initial charge*/
    fun getInitialCharge(product: Product?, context: Context?): Double {
        var distance_r = 0.0
        var time_r = 0.0
        if (ConfigPOJO.settingsResponse?.key_value?.is_distance == "true")//distance required
        {
            distance_r =
                (context as HomeActivity).serviceRequestModel.order_distance?.toDouble()?.times(
                    product?.price_per_distance
                        ?: 0.0f
                ) ?: 0.0
        }
        if (ConfigPOJO.settingsResponse?.key_value?.is_time == "true")//Time required
        {
            time_r = (context as HomeActivity).serviceRequestModel.eta?.toDouble()?.times(
                product?.price_per_hr
                    ?: 0.0f
            ) ?: 0.0
        }


        val totalPrice = distance_r.plus(time_r).plus(product?.alpha_price ?: 0.0f)
        var helper_percentage = 0.0
        /*if (ConfigPOJO?.settingsResponse?.key_value?.is_helper_for_driver == "true") {
            if (serviceRequest?.helper =="true") {
                helper_percentage = totalPrice.times(serviceRequest?.helper_percentage?:0F).div(100)
            }
        }*/
        return totalPrice.plus(helper_percentage)
    }

    /*Ride final charge*/
    fun getFinalCharge(product: Product, context: Context): Double {
        var distance_r = 0.0
        var time_r = 0.0
        var buraqfare_r = 0.0
        var load_upload_r = 0.0
        var actualAmount = 0.0
        var scheduleCharge = 0.0
        if (ConfigPOJO.settingsResponse?.key_value?.is_distance == "true")//distance required
        {
            distance_r =
                (context as HomeActivity).serviceRequestModel.order_distance?.toDouble()?.times(
                    product.price_per_distance
                        ?: 0.0f
                ) ?: 0.0
        }
        if (ConfigPOJO.settingsResponse?.key_value?.is_time == "true")//Time required
        {
            time_r = (context as HomeActivity).serviceRequestModel.eta?.toDouble()?.times(
                product.price_per_hr
                    ?: 0.0f
            ) ?: 0.0
        }
        if (ConfigPOJO.settingsResponse?.key_value?.is_load_unload_charge == "true") //load unload required
        {
            load_upload_r = product.load_unload_charges?.toDouble() ?: 0.0
        }

        var helper_percentage = 0.0
        /* if (ConfigPOJO?.settingsResponse?.key_value?.is_helper_for_driver == "true") {
             if (serviceRequest?.helper =="true") {
                 helper_percentage = (distance_r.plus(time_r).plus(product.alpha_price ?: 0.0f).plus(load_upload_r)).times(serviceRequest?.helper_percentage?:0F).div(100)
             }
         }
 */
        //normal
        val totalPrice =
            distance_r.plus(time_r).plus(product.alpha_price ?: 0.0f).plus(load_upload_r)
                .plus(helper_percentage)



        if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
        {
            buraqfare_r = totalPrice.times(product.buraq_percentage).div(100)
        }

        Log.e("asasasas", buraqfare_r.toString())
        actualAmount = totalPrice.plus(buraqfare_r)

        var final_charge = actualAmount.plus(product.surchargeValue.toDouble())

        if (ConfigPOJO.settingsResponse?.key_value?.schedule_charge == "true") {
            scheduleCharge = if (product.schedule_charge_type == "value") {
                product.schedule_charge.toDouble()
            } else {
                final_charge.times(product.schedule_charge).div(100)
            }
        }

        var airport = 0.0
        if (serviceRequest?.airportCharges != "") {
            airport = serviceRequest?.airportCharges?.toDouble() ?: 0.0
        }

        var exactAmount = final_charge.plus(scheduleCharge).plus(airport)
        if (context.getString(R.string.app_name) == "Wasila") {
            if (ConfigPOJO.OMAN_PHONECODE == "+968") {
                exactAmount = exactAmount.times(ConfigPOJO.OMAN_CURRENCY)
            }
        }


        if (serviceRequest?.pkgData != null) {
            var distance_r = 0.0
            var time_r = 0.0

            var product: PackagesItem? = serviceRequest?.pkgData

            distance_r =
                if ((context as HomeActivity).serviceRequestModel.order_distance?.toDouble() ?: 0.0 >
                    product?.packageData?.distanceKms ?: 0.0
                ) {
                    val distance =
                        (context as HomeActivity).serviceRequestModel.order_distance?.toDouble()
                            ?.minus(product?.packageData?.distanceKms ?: 0.0)
                    product?.packageData?.packagePricings?.get(0)?.distancePriceFixed
                        ?.plus(
                            distance?.times(
                                product.packageData!!.packagePricings.get(0).pricePerKm
                                    ?: 0.0
                            ) ?: 0.0
                        ) ?: 0.0
                } else {
                    product?.packageData?.packagePricings?.get(0)?.distancePriceFixed ?: 0.0
                }

            time_r = if (product?.packageData?.packageType == "1") {
                if ((context as HomeActivity).serviceRequestModel.eta?.toDouble() ?: 0.0 > 60.0) {
                    val timePrice =
                        (context as HomeActivity).serviceRequestModel.eta?.toDouble()?.minus(60.0)
                    product?.packageData?.packagePricings?.get(0)?.timeFixedPrice
                        ?.plus(
                            timePrice?.times(
                                product.packageData!!.packagePricings.get(0).pricePerMin
                                    ?: 0.0
                            ) ?: 0.0
                        ) ?: 0.0
                } else {
                    product.packageData!!.packagePricings.get(0).timeFixedPrice ?: 0.0
                }
            } else {
                product?.packageData?.packagePricings?.get(0)?.timeFixedPrice ?: 0.0
            }

            val totalPrice = distance_r.plus(time_r)
            exactAmount = totalPrice
        } else {
            exactAmount = exactAmount
        }
        var checklistamt = 0.0
        if (serviceRequest?.check_lists!!.size > 0) {
            for (i in serviceRequest?.check_lists!!.indices) {
                checklistamt = checklistamt.plus(
                    serviceRequest?.check_lists?.get(i)?.price?.toDouble()
                        ?: 0.0
                )
            }
        }
        tmpFinalAmt = exactAmount.plus(checklistamt)
        return exactAmount.plus(checklistamt)
    }

    /*Ride final charge after promo*/
    fun getFinalChargePromo(product: Product, context: Context, disvalue: Double): Double {
        var distance_r = 0.0
        var time_r = 0.0
        var buraqfare_r = 0.0
        var load_upload_r = 0.0
        var actualAmount = 0.0
        var scheduleCharge = 0.0
        if (ConfigPOJO.settingsResponse?.key_value?.is_distance == "true")//distance required
        {
            distance_r =
                (context as HomeActivity).serviceRequestModel.order_distance?.toDouble()?.times(
                    product.price_per_distance
                        ?: 0.0f
                ) ?: 0.0
        }
        if (ConfigPOJO.settingsResponse?.key_value?.is_time == "true")//Time required
        {
            time_r = (context as HomeActivity).serviceRequestModel.eta?.toDouble()?.times(
                product.price_per_hr
                    ?: 0.0f
            ) ?: 0.0
        }
        if (ConfigPOJO.settingsResponse?.key_value?.is_load_unload_charge == "true") //load unload required
        {
            load_upload_r = product.load_unload_charges?.toDouble() ?: 0.0
        }

        var helper_percentage = 0.0
        /* if (ConfigPOJO?.settingsResponse?.key_value?.is_helper_for_driver == "true") {
             if (serviceRequest?.helper =="true") {
                 helper_percentage = (distance_r.plus(time_r).plus(product.alpha_price ?: 0.0f).plus(load_upload_r)).times(serviceRequest?.helper_percentage?:0F).div(100)
             }
         }*/

        //normal
        val totalPrice =
            distance_r.plus(time_r).plus(product.alpha_price ?: 0.0f).plus(load_upload_r)
                .plus(helper_percentage).minus(disvalue)
        if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
        {
            buraqfare_r = totalPrice.times(product.buraq_percentage).div(100)
        }

        actualAmount = totalPrice.plus(buraqfare_r)

        var final_charge = actualAmount.plus(product.surchargeValue.toDouble())

        if (ConfigPOJO.settingsResponse?.key_value?.schedule_charge == "true") {
            scheduleCharge = if (product.schedule_charge_type == "value") {
                product.schedule_charge.toDouble()
            } else {
                final_charge.times(product.schedule_charge).div(100)
            }
        }

        var airport = 0.0
        if (serviceRequest?.airportCharges != "") {
            airport = serviceRequest?.airportCharges?.toDouble() ?: 0.0
        }

        var exactAmount = final_charge.plus(scheduleCharge).plus(airport)
        if (context.getString(R.string.app_name) == "Wasila") {
            if (ConfigPOJO.OMAN_PHONECODE == "+968") {
                exactAmount = exactAmount.times(ConfigPOJO.OMAN_CURRENCY)
            }
        }

        if (serviceRequest?.pkgData != null) {
            var distance_r = 0.0
            var time_r = 0.0

            var product: PackagesItem? = serviceRequest?.pkgData

            distance_r =
                if ((context as HomeActivity).serviceRequestModel.order_distance?.toDouble() ?: 0.0 >
                    product?.packageData?.distanceKms ?: 0.0
                ) {
                    val distance =
                        (context as HomeActivity).serviceRequestModel.order_distance?.toDouble()
                            ?.minus(product?.packageData?.distanceKms ?: 0.0)
                    product?.packageData?.packagePricings?.get(0)?.distancePriceFixed
                        ?.plus(
                            distance?.times(
                                product.packageData!!.packagePricings.get(0).pricePerKm
                                    ?: 0.0
                            ) ?: 0.0
                        ) ?: 0.0
                } else {
                    product?.packageData?.packagePricings?.get(0)?.distancePriceFixed ?: 0.0
                }

            time_r = if (product?.packageData?.packageType == "1") {
                if ((context as HomeActivity).serviceRequestModel.eta?.toDouble() ?: 0.0 > 60.0) {
                    val timePrice =
                        (context as HomeActivity).serviceRequestModel.eta?.toDouble()?.minus(60.0)
                    product?.packageData?.packagePricings?.get(0)?.timeFixedPrice
                        ?.plus(
                            timePrice?.times(
                                product.packageData!!.packagePricings.get(0).pricePerMin
                                    ?: 0.0
                            ) ?: 0.0
                        ) ?: 0.0
                } else {
                    product.packageData!!.packagePricings.get(0).timeFixedPrice ?: 0.0
                }
            } else {
                product?.packageData?.packagePricings?.get(0)?.timeFixedPrice ?: 0.0
            }

            val totalPrice = distance_r.plus(time_r)
            exactAmount = totalPrice
        } else {
            exactAmount = exactAmount
        }
        var checklistamt = 0.0
        if (serviceRequest?.check_lists!!.size > 0) {
            for (i in serviceRequest?.check_lists!!.indices) {
                checklistamt = checklistamt.plus(
                    serviceRequest?.check_lists?.get(i)?.price?.toDouble()
                        ?: 0.0
                )
            }
        }
        return exactAmount.plus(checklistamt)
    }

    /*Pool ride pricing structure*/
    fun getFinalChargePool(product: Product, riders: Int, context: Context): Double {
        var distance_r = 0.0
        var time_r = 0.0
        var buraqfare_r = 0.0
        var actualAmount = 0.0
        if (ConfigPOJO.settingsResponse?.key_value?.is_distance == "true")//distance required
        {
            distance_r =
                (context as HomeActivity).serviceRequestModel.order_distance?.toDouble()?.times(
                    product.pool_price_per_distance
                        ?: 0.0f
                ) ?: 0.0
        }

        if (ConfigPOJO.settingsResponse?.key_value?.is_time == "true")//Time required
        {
            time_r = (context as HomeActivity).serviceRequestModel.eta?.toDouble()?.times(
                product.pool_price_per_hr
                    ?: 0.0f
            ) ?: 0.0
        }


        //normal
        val poolPrice = (distance_r.plus(time_r)) * riders

        var helper_percentage = 0.0
        /* if (ConfigPOJO?.settingsResponse?.key_value?.is_helper_for_driver == "true") {
             if (serviceRequest?.helper =="true") {
                 helper_percentage = (poolPrice.plus(product.pool_alpha_price ?: 0.0f)).times(serviceRequest?.helper_percentage?:0F).div(100)
             }
         }*/

        val totalPrice = poolPrice.plus(product.pool_alpha_price ?: 0.0f).plus(helper_percentage)

        if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
        {
            buraqfare_r = totalPrice.times(product.buraq_percentage).div(100)
        }

        actualAmount = totalPrice.plus(buraqfare_r)
        var exactAmount = actualAmount
        if (context.getString(R.string.app_name) == "Wasila") {
            if (ConfigPOJO.OMAN_PHONECODE == "+968") {
                exactAmount = exactAmount.times(ConfigPOJO.OMAN_CURRENCY)
            }
        }
        if (serviceRequest?.pkgData != null) {
            var distance_r = 0.0
            var time_r = 0.0

            var product: PackagesItem? = serviceRequest?.pkgData

            distance_r =
                if ((context as HomeActivity).serviceRequestModel.order_distance?.toDouble() ?: 0.0 >
                    product?.packageData?.distanceKms ?: 0.0
                ) {
                    val distance =
                        (context as HomeActivity).serviceRequestModel.order_distance?.toDouble()
                            ?.minus(product?.packageData?.distanceKms ?: 0.0)
                    product?.packageData?.packagePricings?.get(0)?.distancePriceFixed
                        ?.plus(
                            distance?.times(
                                product.packageData!!.packagePricings.get(0).pricePerKm
                                    ?: 0.0
                            ) ?: 0.0
                        ) ?: 0.0
                } else {
                    product?.packageData?.packagePricings?.get(0)?.distancePriceFixed ?: 0.0
                }

            time_r = if (product?.packageData?.packageType == "1") {
                if ((context as HomeActivity).serviceRequestModel.eta?.toDouble() ?: 0.0 > 60.0) {
                    val timePrice =
                        (context as HomeActivity).serviceRequestModel.eta?.toDouble()?.minus(60.0)
                    product?.packageData?.packagePricings?.get(0)?.timeFixedPrice
                        ?.plus(
                            timePrice?.times(
                                product.packageData!!.packagePricings.get(0).pricePerMin
                                    ?: 0.0
                            ) ?: 0.0
                        ) ?: 0.0
                } else {
                    product.packageData!!.packagePricings.get(0).timeFixedPrice ?: 0.0
                }
            } else {
                product?.packageData?.packagePricings?.get(0)?.timeFixedPrice ?: 0.0
            }

            val totalPrice = (distance_r.plus(time_r)) * riders
            exactAmount = totalPrice
        } else {
            exactAmount = exactAmount
        }
        var checklistamt = 0.0
        if (serviceRequest?.check_lists!!.size > 0) {
            for (i in serviceRequest?.check_lists!!.indices) {
                checklistamt = checklistamt.plus(
                    serviceRequest?.check_lists?.get(i)?.price?.toDouble()
                        ?: 0.0
                )
            }
        }
        tmpFinalAmt = exactAmount.plus(checklistamt)
        return exactAmount.plus(checklistamt)
    }

    /*Pool ride pricing structure*/
    fun getFinalChargePoolPromo(
        product: Product,
        riders: Int,
        context: Context,
        disvalue: Double
    ): Double {
        var distance_r = 0.0
        var time_r = 0.0
        var buraqfare_r = 0.0
        var actualAmount = 0.0
        if (ConfigPOJO.settingsResponse?.key_value?.is_distance == "true")//distance required
        {
            distance_r =
                (context as HomeActivity).serviceRequestModel.order_distance?.toDouble()?.times(
                    product.pool_price_per_distance
                        ?: 0.0f
                ) ?: 0.0
        }

        if (ConfigPOJO.settingsResponse?.key_value?.is_time == "true")//Time required
        {
            time_r = (context as HomeActivity).serviceRequestModel.eta?.toDouble()?.times(
                product.pool_price_per_hr
                    ?: 0.0f
            ) ?: 0.0
        }

        //normal
        val poolPrice = (distance_r.plus(time_r)) * riders
        var helper_percentage = 0.0
        /*if (ConfigPOJO?.settingsResponse?.key_value?.is_helper_for_driver == "true") {
            if (serviceRequest?.helper =="true") {
                helper_percentage = (poolPrice.plus(product.pool_alpha_price ?: 0.0f).minus(disvalue)).times(serviceRequest?.helper_percentage?:0F).div(100)
            }
        }
*/
        val totalPrice =
            poolPrice.plus(product.pool_alpha_price ?: 0.0f).plus(helper_percentage).minus(disvalue)

        if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
        {
            buraqfare_r = totalPrice.times(product.buraq_percentage).div(100)
        }

        actualAmount = totalPrice.plus(buraqfare_r)
        var exactAmount = actualAmount
        if (context.getString(R.string.app_name) == "Wasila") {
            if (ConfigPOJO.OMAN_PHONECODE == "+968") {
                exactAmount = exactAmount.times(ConfigPOJO.OMAN_CURRENCY)
            }
        }
        if (serviceRequest?.pkgData != null) {
            var distance_r = 0.0
            var time_r = 0.0

            var product: PackagesItem? = serviceRequest?.pkgData

            distance_r =
                if ((context as HomeActivity).serviceRequestModel.order_distance?.toDouble() ?: 0.0 >
                    product?.packageData?.distanceKms ?: 0.0
                ) {
                    val distance =
                        (context as HomeActivity).serviceRequestModel.order_distance?.toDouble()
                            ?.minus(product?.packageData?.distanceKms ?: 0.0)
                    product?.packageData?.packagePricings?.get(0)?.distancePriceFixed
                        ?.plus(
                            distance?.times(
                                product.packageData!!.packagePricings.get(0).pricePerKm
                                    ?: 0.0
                            ) ?: 0.0
                        ) ?: 0.0
                } else {
                    product?.packageData?.packagePricings?.get(0)?.distancePriceFixed ?: 0.0
                }

            time_r = if (product?.packageData?.packageType == "1") {
                if ((context as HomeActivity).serviceRequestModel.eta?.toDouble() ?: 0.0 > 60.0) {
                    val timePrice =
                        (context as HomeActivity).serviceRequestModel.eta?.toDouble()?.minus(60.0)
                    product?.packageData?.packagePricings?.get(0)?.timeFixedPrice
                        ?.plus(
                            timePrice?.times(
                                product.packageData!!.packagePricings.get(0).pricePerMin
                                    ?: 0.0
                            ) ?: 0.0
                        ) ?: 0.0
                } else {
                    product.packageData!!.packagePricings.get(0).timeFixedPrice ?: 0.0
                }
            } else {
                product?.packageData?.packagePricings?.get(0)?.timeFixedPrice ?: 0.0
            }

            val totalPrice = (distance_r.plus(time_r)) * riders
            exactAmount = totalPrice
        } else {
            exactAmount = exactAmount
        }
        var checklistamt = 0.0
        if (serviceRequest?.check_lists!!.size > 0) {
            for (i in serviceRequest?.check_lists!!.indices) {
                checklistamt = checklistamt.plus(
                    serviceRequest?.check_lists?.get(i)?.price?.toDouble()
                        ?: 0.0
                )
            }
        }
        return exactAmount.plus(checklistamt)
    }

    /*Pool ride pricing structure*/
    fun getFinalChargePoolInitial(product: Product, riders: Int, context: Context): Double {
        var distance_r = 0.0
        var time_r = 0.0
        if (ConfigPOJO.settingsResponse?.key_value?.is_distance == "true")//distance required
        {
            distance_r =
                (context as HomeActivity).serviceRequestModel.order_distance?.toDouble()?.times(
                    product.pool_price_per_distance
                        ?: 0.0f
                ) ?: 0.0
        }

        if (ConfigPOJO.settingsResponse?.key_value?.is_time == "true")//Time required
        {
            time_r = (context as HomeActivity).serviceRequestModel.eta?.toDouble()?.times(
                product.pool_price_per_hr
                    ?: 0.0f
            ) ?: 0.0
        }

        //normal
        val poolPrice = (distance_r.plus(time_r)) * riders
        val totalPrice = poolPrice.plus(product.pool_alpha_price ?: 0.0f)
        var helper_percentage = 0.0
        /*if (ConfigPOJO?.settingsResponse?.key_value?.is_helper_for_driver == "true") {
            if (serviceRequest?.helper =="true") {
                helper_percentage = (totalPrice).times(serviceRequest?.helper_percentage?:0F).div(100)
            }
        }
*/
        return totalPrice.plus(helper_percentage)
    }

    @SuppressLint("SetTextI18n")
    private fun setData() {
        var airport_charge = 0.0
        if (serviceRequest?.airportChargesPickup != "") {
            tv_airport_charges.visibility = View.VISIBLE
            airport_charge = serviceRequest?.airportChargesPickup?.toDouble() ?: 0.0
        }
        if (serviceRequest?.airportChargesDrop != "") {
            tv_airport_charges.visibility = View.VISIBLE
            airport_charge += serviceRequest?.airportChargesDrop?.toDouble() ?: 0.0
        }
        if (airport_charge != 0.0) {
            tv_airport_charges.text =
                getString(R.string.airport_charges_applied) + " " + airport_charge + " " + ConfigPOJO.currency
        }

        if (ConfigPOJO?.settingsResponse?.key_value?.is_helper_for_driver == "true") {
            if (serviceRequest?.helper == "true") {
                tv_airport_charges.visibility = View.VISIBLE
                tv_airport_charges.text =
                    serviceRequest?.helper_percentage.toString() + "% helper charges will be apply on final invoice."
            }
        }

        serviceRequest?.airportCharges = airport_charge.toString()

        initialVialue = getInitialCharge(serviceRequest?.selectedProduct, context)

        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
            initialVialue =
                getGomovePickupInitialCharge(serviceRequest?.selectedProduct!!, context!!)
            serviceRequest?.final_charge =
                getGomovePickupCharge(serviceRequest?.selectedProduct!!, context!!)
        } else {
            if (ConfigPOJO.TEMPLATE_CODE != Constants.CORSA) {
                if (afterPromoAmount != 0.0) {
                    promoData = serviceRequest?.promoData
                    tvPromoCode.setTextColor(Color.parseColor(ConfigPOJO.primary_color))
                    tvApplyPromoCode.text = getString(R.string.remove)
                    tvApplyPromoCode.setTextColor(
                        ContextCompat.getColor(
                            context as Activity,
                            R.color.pink_gradient_2
                        )
                    )
                    promoCalculation()
                    serviceRequest?.final_charge = afterPromoAmount
                } else {
                    serviceRequest?.final_charge =
                        getFinalCharge(serviceRequest?.selectedProduct!!, context!!)

                }
            }

            tvinitialPrice.text =
                "Initial Fare - " + String.format("%.2f", initialVialue) + " " + ConfigPOJO.currency
            acPickupAddress.text = serviceRequest?.pickup_address
            acDropOffAddress.text = serviceRequest?.dropoff_address

            if (ConfigPOJO.is_merchant == "true") {
                tvBonusValue.text =
                    serviceRequest?.km_earned.toString() + " " + getString(R.string.km)
                if (serviceRequest?.km_earned != null && serviceRequest?.km_earned!!.toInt() > 0)
                    sbBonus.max = serviceRequest?.km_earned?.toInt() ?: 0
                else
                    sbBonus.max = 0

                confirmBookingBinding.sbBonus.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        tvAppliedBonus?.text = "${progress.toString()}${getString(R.string.km)}"
                        bonusKmValue = progress.toString()
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        Log.e("Asasas", "assas")
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        Log.e("Asasas", "assas")
                    }
                })
            }

            //Orscom project base
            if (SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14242c4f98380b537e89446cd49b0022f7" ||
                SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14bb88ffcd9d48b45b6cd0b34d5a3554e5"
            ) {
                ivVehicleImage.setImageUrl(serviceRequest?.brandImage)
                rbOthers.visibility = View.GONE
            } else {
                ivVehicleImage.setImageUrl(serviceRequest?.selectedProduct?.image_url)
            }
            tvSeatingCapcity.text = serviceRequest?.seating_capacity.toString()
            tvVehicleName.text = serviceRequest?.selectedProduct?.name
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE) {
            rbOthers.text = "Prefer not to say"
        }

        if (serviceRequest?.future != ServiceRequestModel.SCHEDULE) {
            if (serviceRequest?.selectedProduct?.pool == "true" && ConfigPOJO.settingsResponse?.key_value?.is_Car_pooling == "true") {
                confirmBookingBinding.carpoolSwitch.visibility = View.VISIBLE
                confirmBookingBinding.tvSurcharge.visibility = View.GONE

                if (ConfigPOJO.is_merchant == "true") {
                    confirmBookingBinding.carpoolSwitch.text = getString(R.string.share)
                }

                // Nsahlo project base
                if (SECRET_DB_KEY == "a072d31bb28d08fc2d8de7f21f1bd38e") {
                    confirmBookingBinding.carpoolSwitch.isChecked = true
                    count = 1
                    rl_pool.visibility = View.VISIBLE
                    serviceRequest?.bookingType = BOOKING_TYPE.CARPOOL
                    initialVialue = getFinalChargePoolInitial(
                        serviceRequest?.selectedProduct!!,
                        count,
                        context!!
                    )
                    serviceRequest?.final_charge =
                        getFinalChargePool(serviceRequest?.selectedProduct!!, count, context!!)
                    val amount = serviceRequest?.final_charge?.times(10)?.div(100)
                    val start_price = serviceRequest?.final_charge?.minus(amount ?: 0.0)
                    val end_price = serviceRequest?.final_charge?.plus(amount ?: 0.0)
                    tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)
                    tv_total_pool_amount.text = "$" + AppUtils.getFormattedDecimal(
                        serviceRequest?.final_charge
                            ?: 0.0
                    )
                    tv_total_passenger.text = "for $count passengers"
                    showSurchargeData("hide")
                }
            }
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
            tvPriceRange.text = String.format("%.2f", serviceRequest?.final_charge)
        } else {
            val amount = serviceRequest?.final_charge?.times(10)?.div(100)

            var start_price = 0.0
            if (Constants.SECRET_DB_KEY=="5025ff88429c4cb7b796cb3ee7128009"){
                start_price = serviceRequest?.final_charge!!

                if (serviceRequest!!.selectedProduct!!.actual_value!! > serviceRequest?.final_charge!!.toFloat()){
                    start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()
                }
                if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
                {
                    buraqfare_r = start_price.times(serviceRequest!!.selectedProduct?.buraq_percentage ?: 0F).div(100)
                }
                start_price =  start_price.plus(buraqfare_r)
                tvPriceRange.text = String.format("%.2f ", start_price)

            }
            else{
              start_price = serviceRequest?.final_charge?.minus(amount ?: 0.0)!!
                val end_price = serviceRequest?.final_charge?.plus(amount ?: 0.0)
                tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)
            }

        }

        if (serviceRequest?.future == ServiceRequestModel.BOOK_NOW) {
            //  tvBook.setText(R.string.book_now)
        } else {
            if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
                if (!TextUtils.isEmpty(serviceRequest?.order_timings_text)) {
                    tv_instruct.visibility = View.VISIBLE
                    tv_instruct.setBackgroundColor(Color.parseColor(ConfigPOJO.secondary_color))
                    tv_instruct.setText(serviceRequest?.order_timings_text + "\n" + getString(R.string.schedule_fees_payment))
                }
                confirmBookingBinding.tvBook.text = getString(R.string.confirm_booking)
            } else {
                confirmBookingBinding.tvInstruct.visibility = View.GONE
                confirmBookingBinding.tvBook.text =
                    getString(R.string.confirm_schedule) + "\n" + serviceRequest?.order_timings_text
            }
            confirmBookingBinding2.tvBook2.text =
                getString(R.string.confirm_schedule) + "\n" + serviceRequest?.order_timings_text
        }
    }

    override fun onCreditPointsSucess(response: RideShareResponse) {

        if (ConfigPOJO.TEMPLATE_CODE != Constants.SUMMER_APP_BASE || ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            if (ConfigPOJO.TEMPLATE_CODE == Constants.MOBY || ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
                rlCredits.visibility = GONE
            } else {

                if (response.creditPoints == 0) {
                    rlCredits.visibility = GONE
                } else {
                    MyAnimationUtils.animateShowHideView(animContainer, rlCredits, true)
                    view9.show()
                }
            }
        }

        creditPoints = 0
        creditPoints = response.creditPoints ?: 0
        tvCreditsLeft.text = String.format(
            "%s",
            "${(response.creditPoints ?: "0") ?: "0"} ${getString(R.string.credits)}"
        )
        savedCardsList.clear()
        savedCardsList.addAll(response.cards)
        isCardsLoaded = true
        if (sbCredits.isChecked) {
            if (creditPoints.toDouble() > serviceRequest?.final_charge ?: 0.0) {
                val diff = creditPoints.toDouble().minus(serviceRequest?.final_charge ?: 0.0)
                tvPriceRange.text = String.format(
                    Locale.US,
                    "%.2f",
                    serviceRequest?.final_charge?.minus(creditPoints.minus(diff))
                )
            } else {
                tvPriceRange.text = String.format(
                    Locale.US,
                    "%.2f",
                    serviceRequest?.final_charge?.minus(creditPoints.toDouble())
                )
            }
        }
    }

    override fun onRequestApiSuccess(response: ResultItem) {
        Log.e("hfhgghfhgfhg", "hghghj")
    }

    private fun setListeners() {
        confirmBookingBinding.tvApply.setOnClickListener(this)
        confirmBookingBinding2.tvApply1.setOnClickListener(this)
        confirmBookingBinding.tvCancelBonus.setOnClickListener(this)
        confirmBookingBinding2.tvCancelBonus1.setOnClickListener(this)
        confirmBookingBinding.tvApplyBonus.setOnClickListener(this)
        confirmBookingBinding2.tvApplyBonus1.setOnClickListener(this)
        confirmBookingBinding.ivBack.setOnClickListener(this)
        confirmBookingBinding.cvToolbar.setOnClickListener(this)
        confirmBookingBinding.tvBook.setOnClickListener(this)
        confirmBookingBinding.tvPaymentMethod.setOnClickListener(this)
        confirmBookingBinding.tvApplyPromoCode.setOnClickListener(this)
        confirmBookingBinding.tvCreditCard.setOnClickListener(this)
        confirmBookingBinding.tvSchedule.setOnClickListener(this)
        confirmBookingBinding2.tvSchedule.setOnClickListener(this)
        confirmBookingBinding2.tvBook2.setOnClickListener(this)
        confirmBookingBinding2.ivBack.setOnClickListener(this)
        confirmBookingBinding2.tvPaymentMethod.setOnClickListener(this)
        confirmBookingBinding2.tvApplyPromoCode.setOnClickListener(this)
        confirmBookingBinding2.tvCreditCard.setOnClickListener(this)
        confirmBookingBinding.sbCredits.setOnCheckedChangeListener(null)
        confirmBookingBinding.carpoolSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                count = 1
                rl_pool.visibility = View.VISIBLE
                serviceRequest?.bookingType = BOOKING_TYPE.CARPOOL
                initialVialue =
                    getFinalChargePoolInitial(serviceRequest?.selectedProduct!!, count, context!!)
                serviceRequest?.final_charge =
                    getFinalChargePool(serviceRequest?.selectedProduct!!, count, context!!)
                val amount = serviceRequest?.final_charge?.times(10)?.div(100)
                val end_price = serviceRequest?.final_charge?.plus(amount ?: 0.0)

                var start_price = 0.0
                if (Constants.SECRET_DB_KEY=="5025ff88429c4cb7b796cb3ee7128009"){
                    start_price = serviceRequest?.final_charge!!

                    if (serviceRequest!!.selectedProduct!!.actual_value!! > serviceRequest?.final_charge!!.toFloat()){
                        start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()
                    }
                    if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
                    {
                        buraqfare_r = start_price.times(serviceRequest!!.selectedProduct?.buraq_percentage ?: 0F).div(100)
                    }
                    start_price =  start_price.plus(buraqfare_r)
                    tvPriceRange.text = String.format("%.2f ", start_price)

                }
                else{
                    start_price = serviceRequest?.final_charge?.minus(amount ?: 0.0)!!
                    tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)

                }

                tv_total_pool_amount.text = "$" + AppUtils.getFormattedDecimal(
                    serviceRequest?.final_charge
                        ?: 0.0
                )
                tv_total_passenger.text = "for $count passengers"
                showSurchargeData("hide")
            } else {
                if (SECRET_DB_KEY == "a072d31bb28d08fc2d8de7f21f1bd38e") {
                    confirmBookingBinding.carpoolSwitch.isChecked = true
                } else {
                    rl_pool.visibility = View.GONE
                    count = 0
                    afterPromoAmount = 0.0
                    tvSeatingcounts.text = "1"
                    serviceRequest?.bookingType = serviceRequest?.bookingTypeTemp ?: ""
                    serviceRequest?.final_charge =
                        getFinalCharge(serviceRequest?.selectedProduct!!, context!!)
                    val amount = serviceRequest?.final_charge?.times(10)?.div(100)
                    val end_price = serviceRequest?.final_charge?.plus(amount ?: 0.0)

                    var start_price = 0.0
                    if (Constants.SECRET_DB_KEY=="5025ff88429c4cb7b796cb3ee7128009"){
                        start_price = serviceRequest?.final_charge!!
                        if (serviceRequest!!.selectedProduct!!.actual_value!! > serviceRequest?.final_charge!!.toFloat()){
                            start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()
                        }
                        if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
                        {
                            buraqfare_r = start_price.times(serviceRequest!!.selectedProduct?.buraq_percentage ?: 0F).div(100)
                        }
                        start_price =  start_price.plus(buraqfare_r)
                        tvPriceRange.text = String.format("%.2f ", start_price)

                    }
                    else{
                        start_price = serviceRequest?.final_charge?.minus(amount ?: 0.0)!!
                        tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)

                    }
                    showSurchargeData("show")
                }
            }
        }
        confirmBookingBinding.sbInsurance.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                serviceRequest?.insurance_amount =
                    ConfigPOJO.settingsResponse?.key_value?.insurance_amount
            } else {
                serviceRequest?.insurance_amount = "0"
            }
        }
        confirmBookingBinding.cashSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                serviceRequest?.payment_type = PaymentType.CASH
            } else {
                serviceRequest?.payment_type = PaymentType.CASH
            }
        }

        setCarpoolSeatClickListener()
        creditListner() // apply credit points
        confirmBookingBinding2.tvSchedule.setOnClickListener {
            serviceRequest?.future = ServiceRequestModel.SCHEDULE
            fragmentManager?.beginTransaction()?.replace(R.id.container, ScheduleFragment())
                ?.addToBackStack("backstack")?.commit()
        }
        confirmBookingBinding.tvSchedule.setOnClickListener {
            serviceRequest?.future = ServiceRequestModel.SCHEDULE
            fragmentManager?.beginTransaction()?.replace(R.id.container, ScheduleFragment())
                ?.addToBackStack("backstack")?.commit()
        }
    }

    /*Check credit points
    * thess points user earned from last request and referral code*/
    private fun creditListner() {
        confirmBookingBinding.sbCredits.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (ConfigPOJO?.settingsResponse?.key_value?.is_wallet != "true") {
                if (selectedPaymentMethod == PaymentType.CARD) {
                    if (sbIsChecked) {
                        confirmBookingBinding.sbCredits.isChecked = false
                        sbIsChecked = false
                        val amount = serviceRequest?.final_charge?.times(10)?.div(100)
                        val end_price = serviceRequest?.final_charge?.plus(amount ?: 0.0)

                        var start_price = 0.0
                        if (Constants.SECRET_DB_KEY=="5025ff88429c4cb7b796cb3ee7128009"){
                            start_price = serviceRequest?.final_charge!!
                            if (serviceRequest!!.selectedProduct!!.actual_value!! > serviceRequest?.final_charge!!.toFloat()){
                                start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()
                            }
                            if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
                            {
                                buraqfare_r = start_price.times(serviceRequest!!.selectedProduct?.buraq_percentage ?: 0F).div(100)
                            }
                            start_price =  start_price.plus(buraqfare_r)
                            tvPriceRange.text = String.format("%.2f ", start_price)

                        }
                        else{
                            start_price = serviceRequest?.final_charge?.minus(amount ?: 0.0)!!
                            tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)

                        }
                    } else {
                        sbIsChecked = true
                        confirmBookingBinding.sbCredits.isChecked = true
                        if (creditPoints.toDouble() > serviceRequest?.final_charge ?: 0.0) {
                            val diff = creditPoints.toDouble().minus(
                                serviceRequest?.final_charge
                                    ?: 0.0
                            )
                            confirmBookingBinding.tvPriceRange.text = String.format(
                                Locale.US,
                                "%.2f",
                                serviceRequest?.final_charge?.minus(creditPoints.minus(diff))
                            )
                        } else {
                            confirmBookingBinding.tvPriceRange.text = String.format(
                                Locale.US,
                                "%.2f",
                                serviceRequest?.final_charge?.minus(creditPoints.toDouble())
                            )
                        }
                    }
                } else {
                    sbIsChecked = false
                    sbCredits.isChecked = false
                    Toast.makeText(context, getString(R.string.cantUseCredits), Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                if (sbIsChecked) {
                    confirmBookingBinding.sbCredits.isChecked = false
                    sbIsChecked = false
                } else {
                    if (wallet_actual_balance.toFloatOrNull()?:0f >= wallet_min_balance?:0f) {
                        if (wallet_actual_balance.toFloatOrNull()?:0f >= serviceRequest?.final_charge ?: 0.0) {
                            sbIsChecked = true
                            selectedPaymentMethod = PaymentType.WALLET
                            tvPaymentMethod.background = StaticFunction.changeBorderTextColor(
                                ConfigPOJO.white_color,
                                ConfigPOJO.gray_color,
                                GradientDrawable.RECTANGLE
                            )
                            tvCreditCard.background = StaticFunction.changeBorderTextColor(
                                ConfigPOJO.white_color,
                                ConfigPOJO.gray_color,
                                GradientDrawable.RECTANGLE
                            )
                            confirmBookingBinding.sbCredits.isChecked = true
                        } else {
                            sbIsChecked = false
                            confirmBookingBinding.sbCredits.isChecked = false
                            Toast.makeText(
                                context,
                                getString(R.string.wallet_balance_low),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        sbIsChecked = false
                        confirmBookingBinding.sbCredits.isChecked = false
                        Toast.makeText(context, getString(R.string.min_balance), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        confirmBookingBinding2.sbCredits.setOnCheckedChangeListener(null)
        confirmBookingBinding2.sbCredits.setOnClickListener {
            if (selectedPaymentMethod == PaymentType.CARD) {
                if (sbIsChecked) {
                    sbCredits.isChecked = false
                    sbIsChecked = false
                    val amount = serviceRequest?.final_charge?.times(10)?.div(100)
                    val end_price = serviceRequest?.final_charge?.plus(amount ?: 0.0)

                    var start_price = 0.0
                    if (Constants.SECRET_DB_KEY=="5025ff88429c4cb7b796cb3ee7128009"){
                        start_price = serviceRequest?.final_charge!!
                        if (serviceRequest!!.selectedProduct!!.actual_value!! > serviceRequest?.final_charge!!.toFloat()){
                            start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()
                        }
                        if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
                        {
                            buraqfare_r = start_price.times(serviceRequest!!.selectedProduct?.buraq_percentage ?: 0F).div(100)
                        }
                        start_price =  start_price.plus(buraqfare_r)
                        tvPriceRange.text = String.format("%.2f ", start_price)

                    }
                    else{
                        start_price = serviceRequest?.final_charge?.minus(amount ?: 0.0)!!
                        tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)

                    }
                } else {
                    sbIsChecked = true
                    sbCredits.isChecked = true
                    if (creditPoints.toDouble() > serviceRequest?.final_charge ?: 0.0) {
                        val diff = creditPoints.toDouble().minus(
                            serviceRequest?.final_charge
                                ?: 0.0
                        )
                        confirmBookingBinding2.tvPriceRange.text = String.format(
                            Locale.US,
                            "%.2f",
                            serviceRequest?.final_charge?.minus(creditPoints.minus(diff))
                        )
                    } else {
                        confirmBookingBinding2.tvPriceRange.text = String.format(
                            Locale.US,
                            "%.2f",
                            serviceRequest?.final_charge?.minus(creditPoints.toDouble())
                        )
                    }
                }
            } else {
                sbIsChecked = false
                sbCredits.isChecked = false
                Toast.makeText(context, getString(R.string.cantUseCredits), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        confirmBookingBinding2.sbCredits.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (selectedPaymentMethod == PaymentType.CARD) {
                sbCredits.isChecked = true
            } else {
                sbCredits.isChecked = false
                Toast.makeText(context, getString(R.string.cantUseCredits), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun updatePriceRange() {
        serviceRequest?.km_bonus = bonusKmValue
        tvBonus.text = bonusKmValue + "KM Applied"
        serviceRequest?.order_distance =
            (serviceRequest?.order_distance!!.toInt() - bonusKmValue.toInt()).toFloat()
        serviceRequest?.final_charge = getFinalCharge(serviceRequest?.selectedProduct!!, activity!!)
        val amount = serviceRequest?.final_charge?.times(10)?.div(100)
        val end_price = serviceRequest?.final_charge?.plus(amount ?: 0.0)
        var start_price = 0.0
        if (Constants.SECRET_DB_KEY=="5025ff88429c4cb7b796cb3ee7128009"){
            start_price = serviceRequest?.final_charge!!
            if (serviceRequest!!.selectedProduct!!.actual_value!! > serviceRequest?.final_charge!!.toFloat()){
                start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()
            }
            if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
            {
                buraqfare_r = start_price.times(serviceRequest!!.selectedProduct?.buraq_percentage ?: 0F).div(100)
            }
            start_price =  start_price.plus(buraqfare_r)
            tvPriceRange.text = String.format("%.2f ", start_price)

        }
        else{
            start_price = serviceRequest?.final_charge?.minus(amount ?: 0.0)!!
            tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)

        }
    }

    /*Increase and decrease pool seating capacity*/
    private fun setCarpoolSeatClickListener() {
        iv_left?.setOnClickListener {
            if (count == 1) {
                tvSeatingcounts.text = "1"
            } else {
                count -= 1
                tvSeatingcounts.text = "" + count
            }
            initialVialue =
                getFinalChargePoolInitial(serviceRequest?.selectedProduct!!, count, context!!)
            serviceRequest?.final_charge =
                getFinalChargePool(serviceRequest?.selectedProduct!!, count, context!!)
            var final_char = getFinalChargePool(serviceRequest?.selectedProduct!!, count, context!!)
            val amount = final_char.times(10)?.div(100)
            val end_price = final_char.plus(amount ?: 0.0)
            var start_price = 0.0
            if (Constants.SECRET_DB_KEY=="5025ff88429c4cb7b796cb3ee7128009"){
                start_price = serviceRequest?.final_charge!!
                if (serviceRequest!!.selectedProduct!!.actual_value!! > serviceRequest?.final_charge!!.toFloat()){
                    start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()
                }
                if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
                {
                    buraqfare_r = start_price.times(serviceRequest!!.selectedProduct?.buraq_percentage ?: 0F).div(100)
                }
                start_price =  start_price.plus(buraqfare_r)
                tvPriceRange.text = String.format("%.2f ", start_price)

            }
            else{
                start_price = serviceRequest?.final_charge?.minus(amount ?: 0.0)!!
                tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)

            }
            tv_total_pool_amount.text = "$" + AppUtils.getFormattedDecimal(final_char)
            tv_total_passenger.text = "for $count passengers"
        }

        iv_right?.setOnClickListener {
            if (count < (serviceRequest?.selectedProduct?.seating_capacity ?: 0)) {
                count += 1
                tvSeatingcounts.text = "" + count
                serviceRequest?.final_charge =
                    getFinalChargePool(serviceRequest?.selectedProduct!!, count, context!!)
                initialVialue =
                    getFinalChargePoolInitial(serviceRequest?.selectedProduct!!, count, context!!)
                var final_char =
                    getFinalChargePool(serviceRequest?.selectedProduct!!, count, context!!)
                val amount = final_char.times(10)?.div(100)
                val end_price = final_char.plus(amount ?: 0.0)
                var start_price = 0.0
                if (Constants.SECRET_DB_KEY=="5025ff88429c4cb7b796cb3ee7128009"){
                    start_price = final_char
                    if (serviceRequest!!.selectedProduct!!.actual_value!! > final_char){
                        start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()
                    }
                    if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
                    {
                        buraqfare_r = start_price.times(serviceRequest!!.selectedProduct?.buraq_percentage ?: 0F).div(100)
                    }
                    start_price =  start_price.plus(buraqfare_r)
                    tvPriceRange.text = String.format("%.2f ", start_price)

                }
                else{
                    start_price = final_char.minus(amount ?: 0.0)
                    tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)

                }
                tv_total_pool_amount.text = "$" + AppUtils.getFormattedDecimal(final_char)
                tv_total_passenger.text = "for $count passengers"
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack, R.id.cvToolbar -> {
                activity?.onBackPressed()
            }
            R.id.tvBook -> {
                /*Check booking type request Road pickup and Sinple request*/
                if (selectedPaymentMethod == PaymentType.CARD) {
                    if (cust_id == "") {
                        rootView.showSnack(getString(R.string.pls_select_card))
                    } else {
                        if (serviceRequest?.isOutStandingCharges == false) {
                            if (serviceRequest?.bookingType == BOOKING_TYPE.ROAD_PICKUP) {
                                bookingApiCall(0.0)
                            } else {
                                startActivityForResult(
                                    Intent(
                                        (context as HomeActivity),
                                        ConfirmPickupActivity::class.java
                                    )
                                        .putExtra("REQ_DATA", serviceRequest)
                                        .putExtra(Constants.LAT, serviceRequest?.pickup_latitude)
                                        .putExtra(Constants.LNG, serviceRequest?.pickup_longitude),
                                    CONFIRM_PICKUP_LOC
                                )
                            }
                        } else {
                            bookingApiCall(0.0)
                        }
                    }
                } else {
                    if (serviceRequest?.isOutStandingCharges == false) {
                        if (serviceRequest?.bookingType == BOOKING_TYPE.ROAD_PICKUP || serviceRequest?.bookingType == BOOKING_TYPE.GIFT) {
                            bookingApiCall(0.0)
                        } else {
                            if (ConfigPOJO.is_childrenTraveling == "true") {
                                showChildrenDialog()
                            } else {
                                startActivityForResult(
                                    Intent(
                                        (context as HomeActivity),
                                        ConfirmPickupActivity::class.java
                                    )
                                        .putExtra("REQ_DATA", serviceRequest)
                                        .putExtra(Constants.LAT, serviceRequest?.pickup_latitude)
                                        .putExtra(Constants.LNG, serviceRequest?.pickup_longitude),
                                    CONFIRM_PICKUP_LOC
                                )
                            }
                        }
                    } else {
                        bookingApiCall(0.0)
                    }
                }
            }
            R.id.tvBook2 -> {
                if (serviceRequest?.isOutStandingCharges == false) {
                    setUpConfirmBottomSheetDialog()
                }
            }
            R.id.tvApplyPromoCode -> {
                if (isPromoApplied) {
                    isPromoApplied = false
                    tvPromoCode.text = getString(R.string.promo)
                    tvPromoCode.setTextColor(
                        ContextCompat.getColor(
                            context as Activity,
                            R.color.black_a8
                        )
                    )
                    tvApplyPromoCode.text = getString(R.string.applyPromoCode)
                    tvApplyPromoCode.setTextColor(Color.parseColor(ConfigPOJO.primary_color))
                    serviceRequest?.afterPromoFinalCharge = 0.0
                    serviceRequest?.final_charge = tmpFinalAmt
                    val amount = serviceRequest?.final_charge?.times(10)?.div(100)
                    val end_price = serviceRequest?.final_charge?.plus(amount ?: 0.0)

                    var start_price = 0.0
                    if (Constants.SECRET_DB_KEY=="5025ff88429c4cb7b796cb3ee7128009"){
                        start_price = serviceRequest?.final_charge!!
                        if (serviceRequest!!.selectedProduct!!.actual_value!! > serviceRequest?.final_charge!!){
                            start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()
                        }
                        if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
                        {
                            buraqfare_r = start_price.times(serviceRequest!!.selectedProduct?.buraq_percentage ?: 0F).div(100)
                        }
                        start_price =  start_price.plus(buraqfare_r)
                        tvPriceRange.text = String.format("%.2f ", start_price)

                    }
                    else{
                        start_price = serviceRequest?.final_charge?.minus(amount ?: 0.0)!!
                        tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)

                    }
                    if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                        tvPriceRange.text = String.format("%.2f", serviceRequest?.final_charge)
                    }
                   /* else {
                        tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)
                    }*/
                    afterPromoAmount = 0.0
                    serviceRequest?.isPromoApplied = false
                    serviceRequest?.coupenId = 0
                } else {
                    var intent = Intent(context, PromoCodesActivity::class.java)
                    intent.putExtra("amount", initialVialue.toString())
                    intent.putExtra(
                        "actual_value",
                        serviceRequest?.selectedProduct?.actual_value.toString()
                    )
                    startActivityForResult(intent, Constants.PROMOCODE_REQUEST_CODE)
                }
            }
            R.id.tvCreditCard -> {
                if (ConfigPOJO?.settingsResponse?.key_value?.is_wallet == "true") {
                    sbIsChecked = true
                    confirmBookingBinding.sbCredits.isChecked = false
                }
                if (ConfigPOJO?.gateway_unique_id == "stripe" || ConfigPOJO.gateway_unique_id.toLowerCase()
                        .equals("peach")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("epayco") ||
                    ConfigPOJO.gateway_unique_id.toLowerCase().equals("conekta")
                ) {
                    val intent = Intent(activity, SavedCards::class.java)
                    startActivityForResult(intent, RQ_PAYMENT_TYPE)
                } else if (ConfigPOJO.gateway_unique_id.equals("payku")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("paystack")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("qpaypro")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("wipay")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("braintree")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("razorpay")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("paytab")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("datatrans")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("paymaya")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("thawani")
                ) {
                    tvPaymentMethod.background = StaticFunction.changeBorderTextColor(
                        ConfigPOJO.white_color,
                        ConfigPOJO.gray_color,
                        GradientDrawable.RECTANGLE
                    )
                    tvCreditCard.background = StaticFunction.changeBorderTextColor(
                        ConfigPOJO.white_color,
                        ConfigPOJO.primary_color,
                        GradientDrawable.RECTANGLE
                    )
                    selectedPaymentMethod = PaymentType.CARD
                    cust_id = "0"
                } else {
                    rootView.showSnack(getString(R.string.card_payment_not_available))
                }
            }
            R.id.tvPaymentMethod -> {
                if (ConfigPOJO?.settingsResponse?.key_value?.is_wallet == "true") {
                    sbIsChecked = true
                    confirmBookingBinding.sbCredits.isChecked = false
                } else {
                    confirmBookingBinding.sbCredits.isChecked = false
                    sbIsChecked = false
                    val amount = serviceRequest?.final_charge?.times(10)?.div(100)
                    val end_price = serviceRequest?.final_charge?.plus(amount ?: 0.0)

                    var start_price = 0.0
                    if (Constants.SECRET_DB_KEY=="5025ff88429c4cb7b796cb3ee7128009"){
                        start_price = serviceRequest?.final_charge!!
                        if (serviceRequest!!.selectedProduct!!.actual_value!! > serviceRequest?.final_charge!!){
                            start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()
                        }
                        if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
                        {
                            buraqfare_r = start_price.times(serviceRequest!!.selectedProduct?.buraq_percentage ?: 0F).div(100)
                        }
                        start_price =  start_price.plus(buraqfare_r)
                        tvPriceRange.text = String.format("%.2f ", start_price)

                    }
                    else{
                        start_price = serviceRequest?.final_charge?.minus(amount ?: 0.0)!!
                        tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)

                    }
                }
                tvPaymentMethod.background = StaticFunction.changeBorderTextColor(
                    ConfigPOJO.white_color,
                    ConfigPOJO.primary_color,
                    GradientDrawable.RECTANGLE
                )
                tvCreditCard.background = StaticFunction.changeBorderTextColor(
                    ConfigPOJO.white_color,
                    ConfigPOJO.gray_color,
                    GradientDrawable.RECTANGLE
                )
                selectedPaymentMethod = PaymentType.CASH
                cust_id = ""
                if (ConfigPOJO.TEMPLATE_CODE != Constants.GOMOVE_BASE) {
                    tvSelectedCard.visibility = View.GONE
                }
            }
            R.id.tvApply -> {
                confirmBookingBinding.llBonusData.visibility = View.GONE
                if (bonusKmValue == "0") {
                    tvBonus.text = activity?.getString(R.string.bonus)
                    Toast.makeText(
                        activity!!,
                        getString(R.string.you_dont_have_enough_km),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    updatePriceRange()
                }
            }
            R.id.tvApply1 -> {
                confirmBookingBinding2.llBonusData.visibility = View.GONE
                if (bonusKmValue == "0") {
                    tvBonus.text = activity?.getString(R.string.bonus)
                    Toast.makeText(
                        activity!!,
                        getString(R.string.you_dont_have_enough_km),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    updatePriceRange()
                }
            }
            R.id.tvApplyBonus1 -> {
                confirmBookingBinding2.llBonusData.visibility = View.VISIBLE
            }
            R.id.tvApplyBonus -> {
                confirmBookingBinding.llBonusData.visibility = View.VISIBLE
            }
            R.id.tvCancelBonus -> {
                confirmBookingBinding.llBonusData.visibility = View.GONE
            }
            R.id.tvCancelBonus1 -> {
                confirmBookingBinding2.llBonusData.visibility = View.GONE
            }
        }
    }

    /*Confirm Booking elements/Parameters*/
    private fun bookingApiCall(outStandingPayment: Double) {
        if (CheckNetworkConnection.isOnline(activity)) {
            val requestModel = (activity as HomeActivity).serviceRequestModel

            if (confirmBookingBinding.genderRl.visibility == View.VISIBLE || confirmBookingBinding2.genderRl.visibility == View.VISIBLE) {
                requestModel.gender =
                    if (setDriverGender() == "No Preference" || setDriverGender() == "Prefer not to say") "" else setDriverGender()
            } else {
                requestModel.gender = ""
            }

            if (requestModel.future == ServiceRequestModel.BOOK_NOW) {
                requestModel.order_timings = getCurentDateStringUtc()
            }
            requestModel.payment_type = selectedPaymentMethod
            if (selectedPaymentMethod == PaymentType.CARD) {
                if (cust_id == "") {
                    Toast.makeText(
                        activity!!,
                        activity!!.resources.getString(R.string.please_select_card),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                } else {
                    requestModel.user_card_id = cust_id.toInt()
                }
            } else {
                requestModel.user_card_id = 0
            }

            requestModel.distance = 50000

            if (selectedPaymentMethod == PaymentType.E_TOKEN) {
                requestModel.organisation_coupon_user_id = tokenId
            } else {
                requestModel.organisation_coupon_user_id = null
            }

            if (sbCredits?.isChecked == true) {
                if (creditPoints.toDouble() > serviceRequest?.final_charge ?: 0.0) {
                    val diff = creditPoints.toDouble().minus(serviceRequest?.final_charge ?: 0.0)
                    requestModel.credit_point_used = creditPoints.minus(diff).toInt()
                } else {
                    requestModel.credit_point_used = creditPoints
                }
            } else {
                requestModel.credit_point_used = 0
            }

            requestModel.cancellation_charges = outStandingPayment
            requestModel.pool = count.toString()
            presenter.requestServiceApiCall(requestModel)
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    private fun setDriverGender(): String {
        var gender = ""
        if (confirmBookingBinding.genderRl.visibility == VISIBLE) {
            val selectedId = confirmBookingBinding.rbGroup.checkedRadioButtonId
            val radioButton = view?.findViewById<RadioButton>(selectedId)
            gender = radioButton?.text.toString()
        } else {
            val selectedId2 = confirmBookingBinding2.rbGroup.checkedRadioButtonId
            val radioButton2 = view?.findViewById<RadioButton>(selectedId2)
            gender = radioButton2?.text.toString()
        }
        return gender
    }

    private fun requestAssignFromAdmin() {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setContentView(R.layout.message_alert)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog?.findViewById<TextView>(R.id.tvMsg)?.text = getString(R.string.unassiged_message)

        dialog?.findViewById<TextView>(R.id.tvCall)?.setOnClickListener {
            activity!!.finish()
            startActivity(Intent(activity!!, HomeActivity::class.java))
            dialog.dismiss()
        }

        dialog?.show()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    /*Booking request response*/
    override fun onApiSuccess(response: Order?) {
        serviceRequest?.isOutStandingCharges = false
        if (serviceRequest?.category_id == 12 && ConfigPOJO.is_gift == "true") {
            activity!!.finish()
            startActivity(Intent(activity!!, HomeActivity::class.java))
        } else {
            if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                if (response?.order_status == OrderStatus.UNASSIGNED) {
                    requestAssignFromAdmin()
                } else {
                    val fragment = ProcessingRequestFragment()
                    val bundle = Bundle()
                    bundle.putString(ORDER, Gson().toJson(response))
                    fragment.arguments = bundle
                    fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)
                        ?.addToBackStack("backstack")?.commitAllowingStateLoss()
                    clearPromoCodesPref()
                }
            } else {
                val fragment = ProcessingRequestFragment()
                val bundle = Bundle()
                bundle.putString(ORDER, Gson().toJson(response))
                fragment.arguments = bundle
                fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)
                    ?.addToBackStack("backstack")?.commitAllowingStateLoss()
                clearPromoCodesPref()
            }

        }
    }

    /*previous charges response*/
    override fun onOutstandingCharges(response: Order?) {
        val outStandingModel = OutstandingChargesModel(
            response?.payment_id, response?.cancellation_charges, selectedPaymentMethod,
            response?.updated_at
        )
        val fragment = OutstandingPaymentFragment()
        val bundle = Bundle()
        bundle.putParcelable("data", outStandingModel)
        bundle.putParcelableArrayList(Constants.CARDS_DATA, savedCardsList)
        fragment.setInterfaceListener(this)
        fragment.arguments = bundle
        serviceRequest?.isOutStandingCharges = true
        fragmentManager?.beginTransaction()?.add(R.id.container, fragment)
            ?.addToBackStack("backstack")?.commitAllowingStateLoss()
    }

    override fun onWalletBalSuccess(response: WalletBalModel?) {
        MyAnimationUtils.animateShowHideView(animContainer, rlCredits, true)
        view9.show()
        wallet_min_balance = response?.details?.minBalance ?: 0F
        wallet_actual_balance = response?.amount ?: ""

        tvCreditsLeft.text =
            String.format("%s", "${response?.amount ?: "0"} ${ConfigPOJO.currency}")
    }

    private fun hitOrderDistanceApi() {
        if (CheckNetworkConnection.isOnline(context)) {
            vPresenter.getOrderDistance(
                LatLng(
                    serviceRequest?.pickup_latitude
                        ?: 0.0, serviceRequest?.pickup_longitude ?: 0.0
                ),
                LatLng(
                    serviceRequest?.dropoff_latitude
                        ?: 0.0, serviceRequest?.dropoff_longitude
                        ?: 0.0
                ), "en"
            )
        }
    }

    override fun getOutstandingPayment(
        paymentType: String,
        cancellationCharges: Double,
        payment_id: Int
    ) {
        serviceRequest?.cancellation_charges = cancellationCharges
        serviceRequest?.cancellation_payment_id = payment_id.toString()
        bookingApiCall(cancellationCharges)
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

        val estimatedDistance =
            (((routes?.get("legs") as JSONArray).get(0) as JSONObject).get("distance") as JSONObject).get(
                "value"
            ) as Int
        try {
            serviceRequest?.order_distance = estimatedDistance / 1000f
        } catch (e: Exception) {
            serviceRequest?.order_distance = 0.0f
        }

        val eta =
            (((routes.get("legs") as JSONArray).get(0) as JSONObject).get("duration") as JSONObject).get(
                "text"
            ) as String
        if (eta.isNotEmpty()) {
            val etaSplit = eta.split(" ")
            if (etaSplit.size == 2) {
                serviceRequest?.eta = etaSplit[0].toInt()
            }
        }

        if (ConfigPOJO.TEMPLATE_CODE != Constants.CORSA) {
            if (afterPromoAmount != 0.0) {
                tvApplyPromoCode.text = getString(R.string.remove)
                serviceRequest?.final_charge = afterPromoAmount
            } else {
                if (count == 1) {
                    serviceRequest?.final_charge =
                        getFinalChargePool(serviceRequest?.selectedProduct!!, count, context!!)
                } else {
                    serviceRequest?.final_charge =
                        getFinalCharge(serviceRequest?.selectedProduct!!, context!!)
                }
            }
        }
        val amount = serviceRequest?.final_charge?.times(10)?.div(100)
        val end_price = serviceRequest?.final_charge?.plus(amount ?: 0.0)

        var start_price = 0.0
        if (Constants.SECRET_DB_KEY=="5025ff88429c4cb7b796cb3ee7128009"){
            start_price = serviceRequest?.final_charge!!
            if (serviceRequest!!.selectedProduct!!.actual_value!! > serviceRequest?.final_charge!!){
                start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()
            }
            if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
            {
                buraqfare_r = start_price.times(serviceRequest!!.selectedProduct?.buraq_percentage ?: 0F).div(100)
            }
            start_price =  start_price.plus(buraqfare_r)
            tvPriceRange.text = String.format("%.2f ", start_price)

        }
        else{
            start_price = serviceRequest?.final_charge?.minus(amount ?: 0.0)!!
            tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)

        }

        bookingApiCall(0.0)
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        rootView.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(activity as Activity)
        } else {
            if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                requestAssignFromAdmin()
            } else {
                rootView.showSnack(error.toString())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RQ_PAYMENT_TYPE && resultCode == 101) {
            cust_id = data?.getStringExtra("card_id") ?: ""
            last_four = data?.getStringExtra("last_four") ?: ""
            rootView.showSnack("Card Selected")
            if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                confirmBookingBinding2.selectedCard.text = " **** **** **** $last_four"
            } else {
                tvSelectedCard.text = "Selected Card - **** **** **** $last_four"
                tvSelectedCard.visibility = View.VISIBLE
            }

            tvPaymentMethod.background = StaticFunction.changeBorderTextColor(
                ConfigPOJO.white_color,
                ConfigPOJO.gray_color,
                GradientDrawable.RECTANGLE
            )
            tvCreditCard.background = StaticFunction.changeBorderTextColor(
                ConfigPOJO.white_color,
                ConfigPOJO.primary_color,
                GradientDrawable.RECTANGLE
            )
            selectedPaymentMethod = PaymentType.CARD

        } else if (requestCode == CONFIRM_PICKUP_LOC && resultCode == Activity.RESULT_OK && data != null) {
            /* Confirm Pickup Result Data */
            if (data?.hasExtra("user_detail_id")) {
                serviceRequest?.user_detail_id = data.getStringExtra("user_detail_id")?.toInt()
            }
            serviceRequest?.pickup_latitude = data.getDoubleExtra(Constants.LAT, 0.0)
            serviceRequest?.pickup_longitude = data.getDoubleExtra(Constants.LNG, 0.0)
            if (data.getStringExtra(Constants.ADDRESS)!!.isNotEmpty()) {
                serviceRequest?.pickup_address = data.getStringExtra(Constants.ADDRESS)
            }
            (activity as HomeActivity).showMarker(
                LatLng(
                    serviceRequest?.pickup_latitude ?: 0.0,
                    serviceRequest?.pickup_longitude
                        ?: 0.0
                ), LatLng(
                    serviceRequest?.dropoff_latitude
                        ?: 0.0, serviceRequest?.dropoff_longitude ?: 0.0
                )
            )

            hitOrderDistanceApi()
        } else if (requestCode == Constants.PROMOCODE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            promoData = data.getParcelableExtra<CouponsItem>(Constants.PROMO_DATA)
            serviceRequest?.promoData = promoData
            if (serviceRequest?.final_charge ?: 0.0 > (promoData?.minimum_value
                    ?: "0.0").toDouble()
            ) {

                tvPromoCode.setTextColor(Color.parseColor(ConfigPOJO.primary_color))
                tvApplyPromoCode.text = getString(R.string.remove)
                tvApplyPromoCode.setTextColor(
                    ContextCompat.getColor(
                        context as Activity,
                        R.color.pink_gradient_2
                    )
                )

                if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                    when (promoData?.couponType) {
                        COUPEN_TYPE.VALUE -> {
                            tvPromoCode.text = String.format(
                                "%s",
                                "${promoData?.code} ${getString(R.string.applied)} ${
                                    getFormattedDecimal(
                                        promoData?.amountValue
                                    )
                                } ${ConfigPOJO.currency}"
                            )
                            afterPromoAmount = getGomovePickupChargePromo(
                                serviceRequest?.selectedProduct!!,
                                context!!,
                                promoData?.amountValue?.toDouble()
                                    ?: 0.0
                            )
                        }
                        COUPEN_TYPE.PERCENTAGE -> {
                            var disval = initialVialue?.times(
                                promoData?.amountValue?.toDouble()
                                    ?: 0.0
                            )?.div(100) ?: 0.0
                            tvPromoCode.text = String.format(
                                "%s",
                                "${promoData?.code} ${getString(R.string.applied)} ${
                                    String.format(
                                        "%.2f",
                                        disval
                                    )
                                } ${ConfigPOJO.currency}"
                            )
                            afterPromoAmount = getGomovePickupChargePromo(
                                serviceRequest?.selectedProduct!!,
                                context!!,
                                disval
                            )
                        }
                    }
                    tvPriceRange.text = String.format("%.2f", afterPromoAmount)
                } else {
                    promoCalculation()

                    val amount = afterPromoAmount.times(10).div(100)
                    val end_price = afterPromoAmount.plus(amount)

                    var start_price = 0.0
                    if (Constants.SECRET_DB_KEY=="5025ff88429c4cb7b796cb3ee7128009"){
                        start_price = afterPromoAmount
                        if (serviceRequest!!.selectedProduct!!.actual_value!! > afterPromoAmount){
                            start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()
                        }
                        if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
                        {
                            buraqfare_r = start_price.times(serviceRequest!!.selectedProduct?.buraq_percentage ?: 0F).div(100)
                        }
                        start_price =  start_price.plus(buraqfare_r)
                        tvPriceRange.text = String.format("%.2f ", start_price)

                    }
                    else{
                        start_price = afterPromoAmount.minus(amount)
                        tvPriceRange.text = String.format("%.2f %s %.2f", start_price, "-", end_price)

                    }
                }
                serviceRequest?.isPromoApplied = true
                serviceRequest?.couponcode = promoData?.code
                serviceRequest?.afterPromoFinalCharge = afterPromoAmount
                isPromoApplied = true
                this.promoData = null
            } else {
                serviceRequest?.isPromoApplied = false
                isPromoApplied = false
                this.promoData = null
            }
        }
    }

    private fun promoCalculation() {
        if (count == 0) {
            when (promoData?.couponType) {
                COUPEN_TYPE.VALUE -> {
                    tvPromoCode.text = String.format(
                        "%s",
                        "${promoData?.code} ${getString(R.string.applied)} ${
                            getFormattedDecimal(
                                promoData?.amountValue
                            )
                        } ${ConfigPOJO.currency}"
                    )
                    afterPromoAmount = getFinalChargePromo(
                        serviceRequest?.selectedProduct!!,
                        context!!,
                        promoData?.amountValue?.toDouble()
                            ?: 0.0
                    )
                }
                COUPEN_TYPE.PERCENTAGE -> {
                    var disval = initialVialue?.times(
                        promoData?.amountValue?.toDouble()
                            ?: 0.0
                    )?.div(100) ?: 0.0
                    var dis = 0.0
                    if (disval < promoData?.maximum_discount?.toDouble() ?: 0.0) {
                        dis = disval
                    } else {
                        dis = promoData?.maximum_discount?.toDouble() ?: 0.0
                    }

                    tvPromoCode.text = String.format(
                        "%s",
                        "${promoData?.code} ${getString(R.string.applied)} ${
                            String.format(
                                "%.2f",
                                dis
                            )
                        } ${ConfigPOJO.currency}"
                    )
                    afterPromoAmount =
                        getFinalChargePromo(serviceRequest?.selectedProduct!!, context!!, dis)
                }
            }
        } else {
            when (promoData?.couponType) {
                COUPEN_TYPE.VALUE -> {
                    tvPromoCode.text = String.format(
                        "%s",
                        "${promoData?.code} ${getString(R.string.applied)} ${
                            getFormattedDecimal(
                                promoData?.amountValue
                            )
                        } ${ConfigPOJO.currency}"
                    )
                    afterPromoAmount = getFinalChargePoolPromo(
                        serviceRequest?.selectedProduct!!,
                        count,
                        context!!,
                        promoData?.amountValue?.toDouble()
                            ?: 0.0
                    )
                }
                COUPEN_TYPE.PERCENTAGE -> {
                    var disvalue = initialVialue?.times(
                        promoData?.amountValue?.toDouble()
                            ?: 0.0
                    )?.div(100) ?: 0.0
                    var dis = 0.0
                    if (disvalue < promoData?.maximum_discount?.toDouble() ?: 0.0) {
                        dis = disvalue
                    } else {
                        dis = promoData?.maximum_discount?.toDouble() ?: 0.0
                    }
                    tvPromoCode.text = String.format(
                        "%s",
                        "${promoData?.code} ${getString(R.string.applied)} ${
                            String.format(
                                "%.2f",
                                dis
                            )
                        } ${ConfigPOJO.currency}"
                    )
                    afterPromoAmount = getFinalChargePoolPromo(
                        serviceRequest?.selectedProduct!!,
                        count,
                        context!!,
                        dis
                    )
                }
            }
        }
    }

    private fun getFormattedDecimal(num: String?): String? {
        return String.format(Locale.US, "%.2f", (num ?: "0").toDouble())
    }

    fun clearPromoCodesPref() {
        SharedPrefs.with(context).save(PROMO_ID, "p")
        SharedPrefs.with(context).save(PROMO_AMMOUNT, "a")
    }

    /*Confirm booking alert for Gomove app*/
    private fun setUpConfirmBottomSheetDialog() {
        var dialogBinding: ConfirmDialogBinding? = null
        dialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(getContext()),
            R.layout.confirm_dialog,
            null,
            false
        );
        dialogBinding.color = ConfigPOJO.Companion
        dialogBinding?.tvYes?.background = StaticFunction.changeBorderColor(
            ConfigPOJO.secondary_color,
            ConfigPOJO.secondary_color,
            GradientDrawable.RECTANGLE
        )
        dialogBinding?.tvNo?.background = StaticFunction.changeStrokeColor(ConfigPOJO.primary_color)

        dialog = BottomSheetDialog(context as Activity)
        dialog?.setContentView(dialogBinding.root)
        dialog?.setCancelable(false)
        val bottomSheetInternal =
            dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet) as View?

        bottomSheetInternal?.let {
            BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
        }

        dialogBinding.tvYes.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.white_color,
            ConfigPOJO.secondary_color,
            GradientDrawable.RECTANGLE
        )
        dialogBinding.tvNo.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.primary_color,
            ConfigPOJO.primary_color,
            GradientDrawable.RECTANGLE
        )

        dialogBinding.tvNo.setOnClickListener {
            dialog?.dismiss()
        }

        dialogBinding.tvYes.setOnClickListener {
            confirmBooking()
            dialog?.dismiss()
        }

        dialog?.show()
    }

    private fun confirmBooking() {
        (activity as HomeActivity).showMarker(
            LatLng(
                serviceRequest?.pickup_latitude ?: 0.0,
                serviceRequest?.pickup_longitude
                    ?: 0.0
            ), LatLng(
                serviceRequest?.dropoff_latitude
                    ?: 0.0, serviceRequest?.dropoff_longitude ?: 0.0
            )
        )

        bookingApiCall(0.0)
    }

    private fun showChildrenDialog() {
        val layoutDialog = View.inflate(context, R.layout.dialog_children_travelling, null)
        var dialog = Dialog(context as Activity)
        dialog?.setContentView(layoutDialog)
        val tvNo = (layoutDialog.findViewById<TextView>(R.id.tvNo))
        val tvYes = (layoutDialog.findViewById<TextView>(R.id.tvYes))
        val tvChildrenDesc = (layoutDialog.findViewById<TextView>(R.id.tvChildrenDesc))
        val tvOk = (layoutDialog.findViewById<TextView>(R.id.tvOk))
        val tvDesc = (layoutDialog.findViewById<TextView>(R.id.tvDesc))
        val llButtons = (layoutDialog.findViewById<LinearLayout>(R.id.llButtons))
        val tvChildren = (layoutDialog.findViewById<TextView>(R.id.tvChildren))
        val ll_lay = (layoutDialog.findViewById<LinearLayout>(R.id.ll_lay))

        ll_lay.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.primary_color,
            ConfigPOJO.primary_color,
            GradientDrawable.RECTANGLE
        )
        tvNo.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.white_color,
            ConfigPOJO.black_color,
            GradientDrawable.RECTANGLE
        )
        tvYes.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.white_color,
            ConfigPOJO.black_color,
            GradientDrawable.RECTANGLE
        )
        tvOk.background = StaticFunction.changeBorderTextColor(
            ConfigPOJO.white_color,
            ConfigPOJO.black_color,
            GradientDrawable.RECTANGLE
        )

        tvNo.setOnClickListener {
            serviceRequest?.is_children = "0"
            dialog?.dismiss()
            startActivityForResult(
                Intent((context as HomeActivity), ConfirmPickupActivity::class.java)
                    .putExtra("REQ_DATA", serviceRequest)
                    .putExtra(Constants.LAT, serviceRequest?.pickup_latitude)
                    .putExtra(Constants.LNG, serviceRequest?.pickup_longitude), CONFIRM_PICKUP_LOC
            )
        }

        tvOk.setOnClickListener {
            dialog?.dismiss()
            startActivityForResult(
                Intent((context as HomeActivity), ConfirmPickupActivity::class.java)
                    .putExtra("REQ_DATA", serviceRequest)
                    .putExtra(Constants.LAT, serviceRequest?.pickup_latitude)
                    .putExtra(Constants.LNG, serviceRequest?.pickup_longitude), CONFIRM_PICKUP_LOC
            )
        }

        tvYes.setOnClickListener {
            tvChildrenDesc.visibility = View.VISIBLE
            llButtons.visibility = View.GONE
            tvDesc.visibility = View.GONE
            tvChildren.visibility = View.GONE
            tvOk.visibility = View.VISIBLE
            serviceRequest?.is_children = "1"
        }

        dialog?.show()
    }
}