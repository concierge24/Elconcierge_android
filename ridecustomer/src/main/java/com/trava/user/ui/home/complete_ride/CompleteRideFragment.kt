package com.trava.user.ui.home.complete_ride

import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ch.datatrans.payment.Payment
import ch.datatrans.payment.PaymentMethod
import ch.datatrans.payment.PaymentMethodType
import ch.datatrans.payment.PaymentProcessState
import ch.datatrans.payment.android.DisplayContext
import ch.datatrans.payment.android.IPaymentProcessStateListener
import ch.datatrans.payment.android.PaymentProcessAndroid
import ch.datatrans.payment.android.ResourceProvider
import com.braintreepayments.api.BraintreeFragment
import com.braintreepayments.api.dropin.DropInActivity
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.braintreepayments.api.interfaces.BraintreeCancelListener
import com.braintreepayments.api.interfaces.BraintreeErrorListener
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener
import com.braintreepayments.api.models.PaymentMethodNonce
import com.google.gson.Gson
import com.paytabs.paytabs_sdk.payment.ui.activities.PayTabActivity
import com.paytabs.paytabs_sdk.utils.PaymentParams
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.trava.user.R
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.deliverystarts.paymentview.PaymentFragment
import com.trava.user.ui.home.rating.RatingFragment
import com.trava.user.ui.home.services.ServicesFragment
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.ApiResponseNew
import com.trava.user.webservices.models.PayMayaResponse
import com.trava.user.webservices.models.PaypalToken
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.ThawaniData
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.*
import com.trava.utilities.Constants.SECRET_DB_KEY
import com.trava.utilities.constants.ACCESS_TOKEN_KEY
import com.trava.utilities.constants.ORDER
import com.trava.utilities.constants.PROFILE
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_IMAGE_URL
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_URL
import com.trava.utilities.webservices.models.AppDetail
import kotlinx.android.synthetic.main.fragment_ride_complete.*
import kotlinx.android.synthetic.main.fragment_ride_complete.tvAmount
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class CompleteRideFragment : Fragment(), InvoiceRequestContrac.View, PaymentMethodNonceCreatedListener, BraintreeCancelListener, BraintreeErrorListener, PaymentResultListener {
    private var order: Order? = null
    private var orderJson: String = ""
    private var dialogIndeterminate: DialogIndeterminate? = null
    private val presenter = InvoiceRequestPresenter()
    private var paypalTokenValue: String = ""
    private var mBraintreeFragment: BraintreeFragment? = null
    lateinit var paymentProcessStateListener: PaymentProcessStateListener
    private var exactAmount = 1.0

    private var profile:AppDetail?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ride_complete, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profile = SharedPrefs.with(activity).getObject(PROFILE, AppDetail::class.java)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        Checkout.preload(activity!!)

        dialogIndeterminate = DialogIndeterminate(activity)
        rr_invoice.setBackgroundColor(Color.parseColor(ConfigPOJO.primary_color))
        tvCompleteRide.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

        orderJson = arguments?.getString("order") ?: ""
        order = Gson().fromJson(arguments?.getString("order"), Order::class.java)

        if (context?.getString(R.string.app_name) == "Wasila") {
            if (ConfigPOJO.OMAN_PHONECODE == "+968") {
                exactAmount = ConfigPOJO.OMAN_CURRENCY
            }
        }

        tvAmount.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.final_charge?.toDouble()?.times(exactAmount) ?: 0.0)}"

        if (order?.check_lists != null) {
            if (order?.check_lists!!.size > 0) {
                surchecklist.visibility = View.VISIBLE
                tvCheckChargesPrice.visibility = View.VISIBLE
            }
        }


        if (ConfigPOJO.settingsResponse?.key_value?.is_fair_breakdown_disable == "true") {
            constraint_lay.visibility = View.GONE
        }

        //Transporter
        if (SECRET_DB_KEY == "3d7b9c3c1ee5f4d45e391122d21c20298c2e2f089ba3e9b1fde322041c5023a2"
                || SECRET_DB_KEY == "3d7b9c3c1ee5f4d45e391122d21c20298c2e2f089ba3e9b1fde322041c5023a2") {
            surchecklist.text = "Shopping List Charge"
        }

        surchecklist.setOnClickListener {
            val dialog = CheckListDetailsFragment()
            val bundle = Bundle()
            bundle.putParcelable("orderDetails", order)
            dialog.arguments = bundle
            dialog.show(childFragmentManager, CheckListDetailsFragment.TAG)
        }

        /*if (ConfigPOJO.gateway_unique_id.equals("payku")
                || ConfigPOJO.gateway_unique_id.toLowerCase().equals("paystack")) {
            if (order?.payment?.payment_type == "Card") {
                Toast.makeText(activity, "Payment Success", Toast.LENGTH_LONG).show()
            }
        } else*/
        if (ConfigPOJO.gateway_unique_id.equals("braintree", false)) {
            if (order?.payment?.payment_type == "Card") {
                callPaypalTokenApi()
            }
        }

        if (ConfigPOJO?.gateway_unique_id == "stripe") {
            if (order?.payment?.payment_type == "Card") {
                if (ConfigPOJO.TEMPLATE_CODE != Constants.GOMOVE_BASE) {
                    tv_tip.visibility = View.VISIBLE
                }
            }
        }
        if (order?.payment?.payment_type == "Card") {
            if (ConfigPOJO.gateway_unique_id.toLowerCase().equals("paystack")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("braintree")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("razorpay")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("paytab")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("datatrans")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("thawani")
                    || ConfigPOJO.gateway_unique_id.toLowerCase().equals("paymaya")) {
                tvCompleteRide.text = "Payment"
            }
        }

        ivBack.setOnClickListener {
            activity!!.onBackPressed()
        }

        setListener()
        setData()
        if (order?.payment?.payment_type == "Card") {
            if (ConfigPOJO.settingsResponse?.key_value?.gateway_unique_id.equals("datatrans", false)) {
                paymentProcessStateListener = PaymentProcessStateListener(presenter, order!!)
            }
        }
    }

    private fun PayTabPayment() {

        val intent = Intent(activity, PayTabActivity::class.java)
        intent.putExtra(PaymentParams.MERCHANT_EMAIL, ConfigPOJO.settingsResponse?.key_value?.paytab_merchant_email) //this a demo account for testing the sdk
        intent.putExtra(PaymentParams.SECRET_KEY, ConfigPOJO.settingsResponse?.key_value?.paytab_secret_key)//Add your Secret Key Here
        intent.putExtra(PaymentParams.LANGUAGE, PaymentParams.ENGLISH)
        intent.putExtra(PaymentParams.TRANSACTION_TITLE, "CORSA Test Paytabs")
        intent.putExtra(PaymentParams.AMOUNT, order?.payment?.final_charge?.toDouble())
        intent.putExtra(PaymentParams.CURRENCY_CODE, ConfigPOJO.currency)
        intent.putExtra(PaymentParams.CUSTOMER_PHONE_NUMBER, "00971585675699")
        intent.putExtra(PaymentParams.CUSTOMER_EMAIL, profile?.user?.email)
        intent.putExtra(PaymentParams.ORDER_ID, "123456")
        intent.putExtra(PaymentParams.PRODUCT_NAME, order?.brand?.brand_name)
        intent.putExtra(PaymentParams.ADDRESS_BILLING, order?.pickup_address)
        intent.putExtra(PaymentParams.CITY_BILLING, order?.pickup_address)
        intent.putExtra(PaymentParams.STATE_BILLING, order?.pickup_address)
        intent.putExtra(PaymentParams.COUNTRY_BILLING, "BHR")
        intent.putExtra(PaymentParams.POSTAL_CODE_BILLING, "00973")
        intent.putExtra(PaymentParams.ADDRESS_SHIPPING, order?.dropoff_address)
        intent.putExtra(PaymentParams.CITY_SHIPPING, order?.dropoff_address)
        intent.putExtra(PaymentParams.STATE_SHIPPING, order?.dropoff_address)
        intent.putExtra(PaymentParams.COUNTRY_SHIPPING, order?.dropoff_address)
        intent.putExtra(PaymentParams.POSTAL_CODE_SHIPPING, "00973")
        intent.putExtra(PaymentParams.PAY_BUTTON_COLOR, ConfigPOJO.primary_color)
        intent.putExtra(PaymentParams.IS_TOKENIZATION, false)
        intent.putExtra(PaymentParams.IS_PREAUTH, false)
        startActivityForResult(intent, PaymentParams.PAYMENT_REQUEST_CODE)
    }

    fun getFormattedDecimal(num: Double): String? {
        return String.format(Locale.US, "%." + (if (ConfigPOJO.settingsResponse?.key_value?.currency_decimal_places != null)
            ConfigPOJO.settingsResponse?.key_value?.currency_decimal_places else "2") + "f", num)
    }

    private fun callPaypalTokenApi() {
        var map = HashMap<String, Any>()
        map["access_token"] = SharedPrefs.get().getString(ACCESS_TOKEN_KEY, "")
        presenter?.getPayPalToken(map)
    }

    var REQUEST_CODE = 1010
    private fun setListener() {
        tvCompleteRide.setOnClickListener {
            if (order?.payment?.payment_type == "Card") {
                when {
                    ConfigPOJO.settingsResponse?.key_value?.gateway_unique_id.equals("datatrans", false) -> {
                        startTransaction()
                    }
                    ConfigPOJO.settingsResponse?.key_value?.gateway_unique_id.equals("paytab", false) -> {
                        PayTabPayment()
                    }
                    ConfigPOJO.settingsResponse?.key_value?.gateway_unique_id.equals("braintree", false) -> {
                        try {
                            val dropInRequest: DropInRequest = DropInRequest().clientToken(paypalTokenValue)
                            startActivityForResult(dropInRequest.getIntent(activity), REQUEST_CODE)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                    ConfigPOJO.settingsResponse?.key_value?.gateway_unique_id.equals("razorpay", false) -> {
                        startPaymentByRazorpay()
                    }
                    ConfigPOJO.settingsResponse?.key_value?.gateway_unique_id.equals("paymaya", false) -> {
                        if (order?.payment?.payment_type == "Card") {
                            startPaymayaPayment()
                        } else {
                            openRatingFragment()
                        }
                    }
                    ConfigPOJO.settingsResponse?.key_value?.gateway_unique_id?.toLowerCase().equals("thawani", false) -> {
                        if (order?.payment?.payment_type == "Card") {
                            startThawaniPayment()
                        } else {
                            openRatingFragment()
                        }
                    }
                    ConfigPOJO.settingsResponse?.key_value?.gateway_unique_id.equals("paystack", false) -> {
                        if (order?.payment?.payment_type == "Card") {
                            openPaymentFragment()
                        } else {
                            openRatingFragment()
                        }
                    }
                    else -> {
                        openRatingFragment()
                    }
                }
            } else {
                openRatingFragment()
            }
        }

        tv_tip.setOnClickListener {
            showTipDialog()
        }
    }

    private fun openPaymentFragment() {
        val fragment = PaymentFragment()
        val bundle = Bundle()
        if (arguments?.containsKey("payment_body") == true) {
            bundle.putString("payment_body", arguments?.getString("payment_body") ?: "")
        }
        bundle.putString("order", orderJson)
        fragment.arguments = bundle
        fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commitAllowingStateLoss()
    }

    //razorpay payment gateway
    private fun startPaymentByRazorpay() {
        SharedPrefs.with(context).save(ORDER, order)
        val proifle = SharedPrefs.with(activity!!).getObject(PROFILE, AppDetail::class.java)
        val co = Checkout()
        co.setKeyID(ConfigPOJO.razorpay_private_key)
        try {
            val options = JSONObject()
            options.put("name", proifle.user?.name)
            options.put("description", "Demoing Charges")
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            if (ConfigPOJO.currency == "₹") {
                options.put("currency", "INR")
            } else {
                options.put("currency", ConfigPOJO.currency)
            }
            options.put("amount", "" + ((order?.payment?.final_charge?.toDouble()?.roundToInt()?.times(100)
                    ?: 0.0F)))
            val preFill = JSONObject()
            preFill.put("email", proifle.user?.email)
            preFill.put("contact", proifle.user?.phone_number)
            options.put("prefill", preFill)
            co.open(activity!!, options)
        } catch (e: Exception) {
            Toast.makeText(activity!!, "Error in payment: " + e.message, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


    ///call datatrance method
    private fun startTransaction() {
        var amount = order?.payment?.final_charge?.toDouble()?.roundToInt()?.times(100)

        val payment = Payment(ConfigPOJO.settingsResponse?.key_value?.datatrans_merchant_id,
                order?.order_id.toString(),
                ConfigPOJO.settingsResponse?.key_value?.datatrans_currency,
                amount ?: 0,
                ConfigPOJO.settingsResponse?.key_value?.datatrans_sign_key)

        val dc = DisplayContext(ResourceProvider(), activity!!)
        val paymentMethods: MutableList<PaymentMethod> = ArrayList<PaymentMethod>()
        paymentMethods.add(PaymentMethod.createMethod(PaymentMethodType.VISA))
        paymentMethods.add(PaymentMethod.createMethod(PaymentMethodType.MASTERCARD))
        paymentMethods.add(PaymentMethod.createMethod(PaymentMethodType.AMEX))
        paymentMethods.add(PaymentMethod.createMethod(PaymentMethodType.PFCARD))
        paymentMethods.add(PaymentMethod.createMethod(PaymentMethodType.PAYPAL))

        var ppa = PaymentProcessAndroid(dc, payment, paymentMethods)
        ppa.paymentOptions.isRecurringPayment = true

        ppa.paymentOptions.isAutoSettlement = true
        ppa.paymentOptions.appCallbackScheme = "ch.datatrans.android.sample"

        ppa.setTestingEnabled(true)
        ppa.paymentOptions.isCertificatePinning = true
        ppa.addStateListener(paymentProcessStateListener)
        ppa.start()
    }

    class PaymentProcessStateListener(private val presenter: InvoiceRequestPresenter, var order: Order) : IPaymentProcessStateListener {
        override fun paymentProcessStateChanged(paymentProcess: PaymentProcessAndroid?) {
            when (paymentProcess?.state) {
                PaymentProcessState.BEFORE_COMPLETION -> {

                }
                PaymentProcessState.COMPLETED -> {
                    var map = HashMap<String, Any>()
                    map["access_token"] = SharedPrefs.get().getString(ACCESS_TOKEN_KEY, "")
                    map["order_id"] = order.order_id.toString()
                    map["transaction_id"] = paymentProcess?.transactionId ?: ""
                    presenter?.saveDatatranseData(map)
                }
                PaymentProcessState.CANCELED -> {

                }
                PaymentProcessState.ERROR -> {

                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setData() {

        if (order?.delivery_proof1 != null || order?.delivery_proof2 != null) {
            ll_order_images.visibility = View.VISIBLE
            if (order?.delivery_proof1 != null) {
                iv_proof_one?.setImageUrl(BASE_IMAGE_URL + order?.delivery_proof1)
                iv_proof_one.visibility = View.VISIBLE
            }

            if (order?.delivery_proof2 != null) {
                iv_proof_two?.setImageUrl(BASE_IMAGE_URL + order?.delivery_proof2)
                iv_proof_two.visibility = View.VISIBLE
            }
        }

        if (order?.customer_signature != null) {
            ll_order_images.visibility = View.VISIBLE

            if (order?.customer_signature != null) {
                iv_signature?.setImageUrl(BASE_IMAGE_URL + order?.customer_signature)
                iv_signature.visibility = View.VISIBLE
            }
        }

        tvServiceName.text = order?.brand?.brand_name
        tvQuantity.text = order?.brand?.name

        var riders = 1
        if (order?.numberOfRiders != null && order?.numberOfRiders != 0) {
            riders = order?.numberOfRiders ?: 1
        }

        /* Base Fair */
        tvPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.product_alpha_charge?.toDouble()?.times(exactAmount) ?: 0.0)}"

        /* Time and Distance Calculations */
        var timePrice = "0.0"
        var distancePrice = "0.0"

        if (order?.package_id != 0) {
            var distance_r = 0.0
            var time_r = 0.0

            distance_r = if (order?.payment?.order_distance?.toDouble() ?: 0.0 > order?.payment?.distance_kms ?: 0.0) {
                val distance = order?.payment?.order_distance?.toDouble()
                        ?.minus(order?.payment?.distance_kms ?: 0.0)
                order?.payment?.distance_price_fixed?.plus(distance?.times(order?.payment?.price_per_km
                        ?: 0.0) ?: 0.0) ?: 0.0
            } else {
                order?.payment?.distance_price_fixed ?: 0.0
            }

            time_r = if (order?.payment?.package_type == "1") {
                if (order?.payment?.order_time?.toDouble() ?: 0.0 > 60.0) {
                    val timePrice = order?.payment?.order_time?.toDouble()?.minus(60.0)
                    order?.payment?.time_fixed_price?.plus(timePrice?.times(order?.payment?.price_per_min
                            ?: 0.0) ?: 0.0) ?: 0.0
                } else {
                    order?.payment?.time_fixed_price ?: 0.0
                }
            } else {
                order?.payment?.time_fixed_price ?: 0.0
            }

            distance_r = distance_r.times(riders)
            time_r = time_r.times(riders)

            tvTimePrice.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(time_r)}")
            tvDistancePrice.text = String.format("%s", "${ConfigPOJO.currency} ${getFormattedDecimal(distance_r)}")

            tvPrice.visibility = View.GONE
            textView30.visibility = View.GONE
            tvBookingfeePrice.visibility = View.GONE
            textView31.visibility = View.GONE
        } else {
            timePrice = order?.payment?.product_per_hr_charge?.toDouble()?.let { order?.payment?.order_time?.toDouble()?.times(it) }?.let { getFormattedDecimal(it.times(exactAmount).times(riders)) }
                    ?: ""
            distancePrice = order?.payment?.product_per_distance_charge?.toDouble()?.let { order?.payment?.order_distance?.toDouble()?.times(it) }?.let { getFormattedDecimal(it.times(exactAmount).times(riders)) }
                    ?: ""
            tvTimePrice.text = String.format("%s", "${ConfigPOJO.currency} $timePrice")
            tvDistancePrice.text = String.format("%s", "${ConfigPOJO.currency} $distancePrice")
        }
        var toll_parking = 0.0
        var otherChargeAddInNormal = 0.0
        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {

            if (order?.payment?.toll_charges != 0.0) {
                toll_parking += order?.payment?.toll_charges ?: 0.0
            }
            if (order?.payment?.parking_charges != 0.0) {
                toll_parking += order?.payment?.parking_charges ?: 0.0
            }

            otherChargeAddInNormal = order?.payment?.waiting_charges?.plus(order?.payment?.zone_charges
                    ?: 0.0)?.plus(order?.payment?.schedule_charge
                    ?: 0.0)?.plus(order?.payment?.sur_charge
                    ?: 0.0)?.plus(toll_parking)?.plus(order?.payment?.airport_charges ?: 0.0) ?: 0.0
        }

        /* Normal Fare Calculation */
        var normalFare = timePrice?.toDouble()?.let { distancePrice?.toDouble()?.let { it1 -> (order?.payment?.product_alpha_charge?.toDouble())?.plus(it)?.plus(it1) } }?.let { getFormattedDecimal(it.times(exactAmount)) }
        tvNormalFarePrice.text = String.format("%s", "${ConfigPOJO.currency} $normalFare ")

        /* Booking Fee, Service charge, Previous Charges, Waiting Charges Calculations */
        var serviceCharges = 0.0
        var exactPrice = 0.0

        if (order?.payment?.previous_charges ?: 0.0 > 0.0) {
            tvLabelPreviousCharges.show()
            tvPreviousCharges.show()
            tvPreviousCharges.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.previous_charges?.times(exactAmount) ?: 0.0)}"
        }

        if (order?.coupon_detail != null) {
            promocharge.visibility = View.VISIBLE
            tvPromoCharge.visibility = View.VISIBLE
            if ((order?.payment?.coupon_charge ?: 0.0) > 0.0) {
                if (order?.coupon_detail?.couponType == "Value") {
                    promocharge.text = getString(R.string.promo_code_applied) + " " + order?.coupon_detail?.code + " " + getString(R.string.value_of) + " " + ConfigPOJO.currency + "" + order?.coupon_detail?.amountValue
                } else {
                    promocharge.text = getString(R.string.promo_code_applied) + " " + order?.coupon_detail?.code + " " + getString(R.string.value_of) + " " + order?.coupon_detail?.amountValue + "%"
                }
                tvPromoCharge.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.coupon_charge?.times(exactAmount) ?: 0.0)}"
            } else {
                tvPromoCharge.text = "${ConfigPOJO.currency} ${getFormattedDecimal(0.0 ?: 0.0)}"
                promocharge.text = getString(R.string.ride_amount_is_less_than_actual_amount)
            }

        } else {
            promocharge.visibility = View.GONE
            tvPromoCharge.visibility = View.GONE
        }


        if (order?.payment?.waiting_charges == 0.0) {
            tvWaitingChargesPrice.visibility = View.GONE
            tvWaitingChargesLabel.visibility = View.GONE
        }
        if (order?.payment?.isr_mexico_tax == 0.0 || order?.payment?.isr_mexico_tax == null) {
            tvisr_tax.visibility = View.GONE
            tvisrtaxLabel.visibility = View.GONE
        }
        tvisr_tax.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.isr_mexico_tax?.times(exactAmount) ?: 0.0)}"


        tvWaitingChargesLabel.text = String.format("%s", "${getString(R.string.waiting_charges)} ${"("}${getFormattedDecimal(order?.payment?.waiting_time?.toDouble() ?: 0.0)} ${getString(R.string.min)}${")"}")
        tvWaitingChargesPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.waiting_charges?.times(exactAmount) ?: 0.0)}"

        if (normalFare?.toDouble() ?: 0.0 < order?.payment?.product_actual_value?.toDouble() ?: 0.0) {
            exactPrice = (normalFare?.toDouble())?.plus(order?.payment?.previous_charges
                    ?: 0.0)?.plus(otherChargeAddInNormal)
                    ?: 0.0

            serviceCharges = if (exactPrice > order?.payment?.product_actual_value?.toDouble() ?: 0.0) {
                0.0
            } else {
                (order?.payment?.product_actual_value?.toDouble() ?: 0.0).minus(exactPrice ?: 0.0)
            }

            if (serviceCharges == 0.0) {
                tvServiceChargesPrice.visibility = View.GONE
                textView33.visibility = View.GONE
            }

            tvServiceChargesPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(serviceCharges.times(exactAmount))}"
        } else {
            if (ConfigPOJO.TEMPLATE_CODE != Constants.GOMOVE_BASE) {
                tvServiceChargesPrice.visibility = View.GONE
                textView33.visibility = View.GONE
            }
            tvServiceChargesPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(0.0)} "
        }

        tvTimeLabel.text = String.format("%s", "${getString(R.string.waiting_time)} ${"("}${getFormattedDecimal(order?.payment?.order_time?.toDouble() ?: 0.0)} ${getString(R.string.min)}${")"}")
        tvDistanceLabel.text = String.format("%s", "${getString(R.string.distance)} ${"("}${getFormattedDecimal(order?.payment?.order_distance?.toDouble() ?: 0.0)} ${getString(R.string.km)}${")"}")

        if (order?.payment?.km_bonus?.toInt() ?: 0 > 0) {
            tvKmBonus.visibility = View.VISIBLE
            tvKmBonus.text = "${getString(R.string.applied_nonus_on_this)}(${order?.payment?.km_bonus}${getString(R.string.km)})"
        } else {
            tvKmBonus.visibility = View.GONE
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            tvInsuranceChargesPrice.visibility = View.GONE
            Insurancecharge.visibility = View.GONE
            if (order?.payment?.zone_charges != 0.0) {
                Zonecharge.visibility = View.VISIBLE
                tvZoneChargesPrice.visibility = View.VISIBLE
                tvZoneChargesPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.zone_charges ?: 0.0)}"
            }

            if (toll_parking != 0.0) {
                tollcharge.visibility = View.VISIBLE
                tvtollChargesPrice.visibility = View.VISIBLE
                tvtollChargesPrice.text = ConfigPOJO.currency + " " + getFormattedDecimal(toll_parking)
            }

            if (order?.payment?.airport_charges != 0.0) {
                airportcharge.visibility = View.VISIBLE
                tvairportChargesPrice.visibility = View.VISIBLE
                tvairportChargesPrice.text = ConfigPOJO.currency + " " + getFormattedDecimal(order?.payment?.airport_charges
                        ?: 0.0)
            }
        }
        tvSubtotalPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.final_charge?.toDouble()?.times(exactAmount) ?: 0.0)} "

        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
            tvDistancePrice.visibility = View.GONE
            tvDistanceLabel.visibility = View.GONE
            tvLevelCharge.visibility = View.GONE
            levelCharge.visibility = View.GONE
            tpsCharge.text = "TPS(" + ConfigPOJO.settingsResponse?.key_value?.tps + ")"
            tvqCharge.text = "TVQ(" + ConfigPOJO.settingsResponse?.key_value?.tvq + ")"

            var baseAmount = 0.0
            if (serviceCharges != 0.0) {
                baseAmount = serviceCharges?.plus(timePrice.toDouble())?.plus(order?.payment?.admin_charge?.toDouble()
                        ?: 0.0) ?: 0.0
            } else {
                baseAmount = order?.payment?.product_alpha_charge?.toDouble()?.plus(timePrice.toDouble())?.plus(order?.payment?.admin_charge?.toDouble()
                        ?: 0.0) ?: 0.0
            }

            var tps_amt = (baseAmount?.times(ConfigPOJO.settingsResponse?.key_value?.tps?.replace("%", "")?.toDouble()
                    ?: 0.0))?.div(100)
            val tvq_amt = (baseAmount?.times(ConfigPOJO.settingsResponse?.key_value?.tvq?.replace("%", "")?.toDouble()
                    ?: 0.0))?.div(100)
            tvtps.text = ConfigPOJO.currency + " " + getFormattedDecimal(tps_amt ?: 0.0)
            tvtpq.text = ConfigPOJO.currency + " " + getFormattedDecimal(tvq_amt ?: 0.0)
            tvSubtotalPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.final_charge?.toDouble()?.times(exactAmount)?.minus(tps_amt ?: 0.0)?.minus(tvq_amt ?: 0.0) ?: 0.0)} "

            tvLevelCharge.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.level_charge?.toDouble()?.times(exactAmount) ?: 0.0)} "
        } else {
            tvLevelCharge.visibility = View.GONE
            levelCharge.visibility = View.GONE
            tpsCharge.visibility = View.GONE
            tvqCharge.visibility = View.GONE
            tvtps.visibility = View.GONE
            tvtpq.visibility = View.GONE
        }

        tvBookingfeePrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.admin_charge?.toDouble()?.times(exactAmount) ?: 0.0)}"
        tvSurChargesPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.sur_charge?.times(exactAmount) ?: 0.0)}"
        tvScheduleChargesPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.schedule_charge?.times(exactAmount) ?: 0.0)}"
        tvInsuranceChargesPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.insurance_amount?.times(exactAmount) ?: 0.0)}"
        helper_charge.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.helper_charges?.times(exactAmount) ?: 0.0)}"
        tvCheckChargesPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.check_list_total?.times(exactAmount) ?: 0.0)}"

        tvTotal.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.final_charge?.toDouble()?.times(exactAmount) ?: 0.0)} "

        if (order?.payment?.sur_charge?.toDouble() == 0.0) {
            tvSurChargesPrice.visibility = View.GONE
            surcharge.visibility = View.GONE
        }

        if (order?.payment?.helper_charges?.toDouble() == 0.0 || order?.payment?.helper_charges == null) {
            helper_charge.visibility = View.GONE
            tvisrHelperLabel.visibility = View.GONE
        }

        if (order?.payment?.schedule_charge?.toDouble() == 0.0) {
            tvScheduleChargesPrice.visibility = View.GONE
            Schedulecharge.visibility = View.GONE
        }
        if (order?.payment?.insurance_amount?.toDouble() == 0.0) {
            tvInsuranceChargesPrice.visibility = View.GONE
            Insurancecharge.visibility = View.GONE
        }
    }

    private fun openRatingFragment() {
        fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val fragment = RatingFragment()
        val bundle = Bundle()
        bundle.putString(ORDER, Gson().toJson(order))
        fragment.arguments = bundle
        fragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
    }

    private var dialog: Dialog? = null

    private fun showTipDialog() {
        dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setContentView(R.layout.dialog_tips)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etMessage = dialog?.findViewById(R.id.etMessage) as EditText
        val tvSubmit = dialog?.findViewById(R.id.tvSubmit) as TextView
        tvSubmit.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

        tvSubmit.setOnClickListener {
            if (etMessage.text.toString().trim().isNotEmpty()) {
                val map = HashMap<String, Any>()
                map["order_id"] = order?.order_id.toString()
                map["gateway_unique_id"] = ConfigPOJO.gateway_unique_id
                map["tip"] = etMessage.text.toString().trim()
                if (CheckNetworkConnection.isOnline(activity)) {
                    if (etMessage.text.toString().trim().startsWith("0")) {
                        Toast.makeText(activity, getString(R.string.tip_amount), Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    presenter.tipAmountApi(map)
                    etMessage.setText("")
                    dialog?.dismiss()
                } else {
                    CheckNetworkConnection.showNetworkError(rr_invoice)
                }
            } else {
                activity?.let { Toast.makeText(it, getString(R.string.v_enter_tip), Toast.LENGTH_SHORT).show() }
            }
        }

        dialog?.show()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    override fun ApiSuccess(order: ApiResponseNew?) {
        if (order?.success == 1) {
            rr_invoice.showSnack(order.msg ?: "")
            tv_tip.visibility = View.GONE
        }
    }

    override fun PaymentApiSuccess(order: ApiResponseNew?) {
        if (order?.success == 1) {
            rr_invoice.showSnack("Payment Submit Successfully")
            openRatingFragment()
        }
    }

    override fun DatatranseDataSuccess(apiResponseNew: ApiResponseNew?) {
        if (apiResponseNew?.success == 1) {
            rr_invoice.showSnack(apiResponseNew.msg ?: "")
            openRatingFragment()
        }
    }

    override fun RazorPaymentApiSuccess(apiResponseNew: ApiResponseNew?) {
        if (apiResponseNew?.success == 1) {
            rr_invoice.showSnack(apiResponseNew.msg ?: "")
            openRatingFragment()
        }
    }

    override fun PayMayaGeturlSuccess(apiResponseNew: PayMayaResponse?) {
        if (apiResponseNew?.success == 1) {
            val fragment = PaymentFragment()
            val bundle = Bundle()
            bundle.putString("order", orderJson)
            bundle.putString("payment_url", apiResponseNew?.data?.redirectUrl)
            fragment.arguments = bundle
            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commitAllowingStateLoss()
        } else {
            tvCompleteRide.showSnack(apiResponseNew?.message ?: "")
        }
    }

    override fun PayThwaniGeturlSuccess(apiResponseNew: ApiResponse<ThawaniData>?) {
        if (apiResponseNew?.statusCode==200) {
            val fragment = PaymentFragment()
            val bundle = Bundle()
            bundle.putString("order", orderJson)
            bundle.putString("payment_url", apiResponseNew.result?.payment_url)
            fragment.arguments = bundle
            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commitAllowingStateLoss()
        } else {
            tvCompleteRide.showSnack(apiResponseNew?.msg ?: "")
        }
    }

    override fun PaypalTokenApiSuccess(paypalToken: PaypalToken?) {
        if (paypalToken?.success == 1) {
            paypalTokenValue = paypalToken?.clientToken ?: ""
            mBraintreeFragment = BraintreeFragment.newInstance(activity, paypalTokenValue)
        }
    }

    override fun onPaymentMethodNonceCreated(paymentMethodNonce: PaymentMethodNonce?) {
        Log.e("asasasas", paymentMethodNonce.toString())
        var amount = order?.payment?.final_charge?.toDouble()?.roundToInt()?.times(100)

        val map = HashMap<String, Any>()
        map["order_id"] = order?.order_id.toString()
        map["amount"] = amount.toString()
//        map["gateway_unique_id"] = ConfigPOJO.gateway_unique_id
        map["nonce"] = paymentMethodNonce.toString()
        if (CheckNetworkConnection.isOnline(activity)) {
            presenter.applyPaymets(map)
        } else {
            CheckNetworkConnection.showNetworkError(rr_invoice)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val result: DropInResult? = data?.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT)
                Log.e("paymentMethodNonce", "" + result?.paymentMethodNonce?.nonce)
                var amount = order?.payment?.final_charge?.toDouble()

                val map = HashMap<String, Any>()
                map["order_id"] = order?.order_id.toString()
                map["amount"] = amount.toString()
                map["nonce"] = result?.paymentMethodNonce?.nonce.toString()
                if (CheckNetworkConnection.isOnline(activity)) {
                    presenter.applyPaymets(map)
                } else {
                    CheckNetworkConnection.showNetworkError(rr_invoice)
                }
                // use the result to update your UI and send the payment method nonce to your server
            } else if (resultCode == RESULT_CANCELED) {
                // the user canceled
            } else {
                // handle errors here, an exception may be available in
                val error = data?.getSerializableExtra(DropInActivity.EXTRA_ERROR) as java.lang.Exception
            }
        }
        if (resultCode == RESULT_OK && requestCode == PaymentParams.PAYMENT_REQUEST_CODE) {
            Log.e("Tag", data?.getStringExtra(PaymentParams.RESPONSE_CODE) ?: "")
            Log.e("Tag", data?.getStringExtra(PaymentParams.TRANSACTION_ID) ?: "")

            if (data?.hasExtra(PaymentParams.TRANSACTION_ID) ?: "" != "") {
                val map = HashMap<String, Any>()
                map["order_id"] = order?.order_id.toString()
                map["transaction_id"] = data?.getStringExtra(PaymentParams.TRANSACTION_ID) ?: ""
                if (CheckNetworkConnection.isOnline(activity)) {
                    presenter.applyPaymetsPayTab(map)
                } else {
                    CheckNetworkConnection.showNetworkError(rr_invoice)
                }
            }
        }
    }

    override fun onCancel(requestCode: Int) {
        Log.e("cancelllll", requestCode.toString())
    }

    override fun onError(error: Exception?) {
        Log.e("asasasas", error?.message.toString())
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        Log.e("AsasasAS", "asasasasa")
    }

    override fun handleApiError(code: Int?, error: String?) {
        Toast.makeText(activity!!, error.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Log.e("abcde", p1.toString() + "  " + p0)
        Toast.makeText(activity!!, p1.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onPaymentSuccess(p0: String?) {

        if(p0?.isEmpty()==true) return

        val map = HashMap<String, Any>()
        map["access_token"] = SharedPrefs.get().getString(ACCESS_TOKEN_KEY, "")
        map["order_id"] = order?.order_id.toString()
        map["transaction_id"] = p0 ?: ""
        presenter.saveDatatranseData(map)
    }

    private fun startPaymayaPayment() {
        var hashMap = HashMap<String, Any>()
        hashMap["currency"] = ConfigPOJO.currency
        hashMap["amount"] = ((order?.payment?.final_charge?.toDouble() ?: 0.0F)).toString()
        hashMap["userId"] = ""
        hashMap["successUrl"] = "https://billing.royoapps.com/payment-success"
        hashMap["failureUrl"] = "https://billing.royoapps.com/payment-error"
        presenter?.getPaymayaUrl(hashMap)
    }

    private fun startThawaniPayment() {
        SharedPrefs.with(context).save(ORDER, order)
        val hashMap = HashMap<String, Any>()
        hashMap["amount"] = ((order?.payment?.final_charge?.toDouble() ?: 0.0)).times(1000).toString()
        hashMap["email"] = profile?.user?.email?:""
        hashMap["name"] = profile?.user?.name?:""
        hashMap["success_url"] = BASE_URL+"/success"
        hashMap["cancel_url"] = BASE_URL+"/cancel"
        presenter.getThawaniUrl(hashMap)
    }
}