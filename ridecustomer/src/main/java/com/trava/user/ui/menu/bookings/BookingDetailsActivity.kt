package com.trava.user.ui.menu.bookings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.trava.user.R
import com.trava.user.utils.AppUtils
import com.trava.user.webservices.models.order.Driver
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.*
import com.google.gson.Gson
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.complete_ride.CheckListDetailsFragment
import com.trava.user.ui.home.rating.RatingFragment
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.MyAnimationUtils
import com.trava.user.utils.StaticFunction
import com.trava.utilities.constants.ORDER
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_IMAGE_URL
import kotlinx.android.synthetic.main.activity_booking_details.*
import kotlinx.android.synthetic.main.activity_booking_details.Insurancecharge
import kotlinx.android.synthetic.main.activity_booking_details.Schedulecharge
import kotlinx.android.synthetic.main.activity_booking_details.Zonecharge
import kotlinx.android.synthetic.main.activity_booking_details.airportcharge
import kotlinx.android.synthetic.main.activity_booking_details.helper_charge
import kotlinx.android.synthetic.main.activity_booking_details.ivBack
import kotlinx.android.synthetic.main.activity_booking_details.iv_proof_one
import kotlinx.android.synthetic.main.activity_booking_details.iv_proof_two
import kotlinx.android.synthetic.main.activity_booking_details.iv_signature
import kotlinx.android.synthetic.main.activity_booking_details.levelCharge
import kotlinx.android.synthetic.main.activity_booking_details.ll_order_images
import kotlinx.android.synthetic.main.activity_booking_details.promocharge
import kotlinx.android.synthetic.main.activity_booking_details.surcharge
import kotlinx.android.synthetic.main.activity_booking_details.surchecklist
import kotlinx.android.synthetic.main.activity_booking_details.textView30
import kotlinx.android.synthetic.main.activity_booking_details.textView31
import kotlinx.android.synthetic.main.activity_booking_details.textView33
import kotlinx.android.synthetic.main.activity_booking_details.tollcharge
import kotlinx.android.synthetic.main.activity_booking_details.tpsCharge
import kotlinx.android.synthetic.main.activity_booking_details.tvBookingfeePrice
import kotlinx.android.synthetic.main.activity_booking_details.tvCheckChargesPrice
import kotlinx.android.synthetic.main.activity_booking_details.tvDistanceLabel
import kotlinx.android.synthetic.main.activity_booking_details.tvDistancePrice
import kotlinx.android.synthetic.main.activity_booking_details.tvInsuranceChargesPrice
import kotlinx.android.synthetic.main.activity_booking_details.tvKmBonus
import kotlinx.android.synthetic.main.activity_booking_details.tvLabelPreviousCharges
import kotlinx.android.synthetic.main.activity_booking_details.tvLevelCharge
import kotlinx.android.synthetic.main.activity_booking_details.tvNormalFarePrice
import kotlinx.android.synthetic.main.activity_booking_details.tvPreviousCharges
import kotlinx.android.synthetic.main.activity_booking_details.tvPrice
import kotlinx.android.synthetic.main.activity_booking_details.tvPromoCharge
import kotlinx.android.synthetic.main.activity_booking_details.tvQuantity
import kotlinx.android.synthetic.main.activity_booking_details.tvScheduleChargesPrice
import kotlinx.android.synthetic.main.activity_booking_details.tvServiceChargesPrice
import kotlinx.android.synthetic.main.activity_booking_details.tvServiceName
import kotlinx.android.synthetic.main.activity_booking_details.tvSubtotalPrice
import kotlinx.android.synthetic.main.activity_booking_details.tvSurChargesPrice
import kotlinx.android.synthetic.main.activity_booking_details.tvTimeLabel
import kotlinx.android.synthetic.main.activity_booking_details.tvTimePrice
import kotlinx.android.synthetic.main.activity_booking_details.tvTotal
import kotlinx.android.synthetic.main.activity_booking_details.tvWaitingChargesLabel
import kotlinx.android.synthetic.main.activity_booking_details.tvWaitingChargesPrice
import kotlinx.android.synthetic.main.activity_booking_details.tvZoneChargesPrice
import kotlinx.android.synthetic.main.activity_booking_details.tvairportChargesPrice
import kotlinx.android.synthetic.main.activity_booking_details.tvisrHelperLabel
import kotlinx.android.synthetic.main.activity_booking_details.tvisr_tax
import kotlinx.android.synthetic.main.activity_booking_details.tvisrtaxLabel
import kotlinx.android.synthetic.main.activity_booking_details.tvqCharge
import kotlinx.android.synthetic.main.activity_booking_details.tvtollChargesPrice
import kotlinx.android.synthetic.main.activity_booking_details.tvtpq
import kotlinx.android.synthetic.main.activity_booking_details.tvtps
import kotlinx.android.synthetic.main.fragment_ride_complete.*
import java.util.*

class BookingDetailsActivity : AppCompatActivity(), BookingContract.View,RatingFragment.OnRateDone {

    private val presenter = BookingsPresenter()

    private var order: Order? = null
    private var exactAmount = 1.0
    private val ratingDrawables = listOf(R.drawable.ic_angry_smile, R.drawable.ic_2, R.drawable.ic_3, R.drawable.ic_4, R.drawable.ic_5)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.attachView(this)
        setContentView(R.layout.activity_booking_details)

        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)
        tvCancel.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        tvTrack.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        iv_first_dot.setColorFilter(Color.parseColor(ConfigPOJO.primary_color), PorterDuff.Mode.SRC_IN)
        iv_drop.setColorFilter(Color.parseColor(ConfigPOJO.black_color), PorterDuff.Mode.SRC_IN)
        ratingBar.progressDrawable.setColorFilter(Color.parseColor(ConfigPOJO.primary_color), PorterDuff.Mode.SRC_IN)
        tvBookingStatus.setTextColor(Color.parseColor(ConfigPOJO.primary_color))

        tvCancel.visibility = View.GONE
        ivBack.setOnClickListener {
            onBackPressed()
        }
        if (getString(R.string.app_name) == "Wasila") {
            if (ConfigPOJO.OMAN_PHONECODE == "+968") {
                exactAmount = ConfigPOJO.OMAN_CURRENCY
            }
        }
        val orderData = Gson().fromJson(intent.getStringExtra("data"), Order::class.java)
        setBookingData(orderData)
        setUserData(orderData?.driver)
        setPaymentData(orderData)/*CarPool*/

        setListeners()
    }

    override fun onResume() {
        super.onResume()
        val orderData = Gson().fromJson(intent.getStringExtra("data"), Order::class.java)
        if (CheckNetworkConnection.isOnline(this)) {
            presenter.getOrderDetails(orderData?.order_id?.toLong() ?: 0L)
        } else {
            CheckNetworkConnection.isOnline(this)
        }
    }

    private fun setListeners() {
        tvCancel.setOnClickListener {
            checkCancelRideCondition()
        }

        ivCall.setOnClickListener {
            val phone = order?.driver?.phone_number.toString()
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }

        surchecklist.setOnClickListener {
            val dialog = CheckListDetailsFragment()
            val bundle = Bundle()
            bundle.putParcelable("orderDetails", order)
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, CheckListDetailsFragment.TAG)
        }

        tvTrack.setOnClickListener {
            var intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("from_booking_detail", "from_booking_detail")
            intent.putExtra("order_id", order?.order_id.toString())
            startActivity(intent)
            finish()
        }

        txtRateUser.setOnClickListener {
            openRatingFragment()
        }
    }

    private fun openRatingFragment() {

        val fragment = RatingFragment()
        val bundle = Bundle()
        bundle.putString("detail", "detail")
        bundle.putString(ORDER, Gson().toJson(order))
        fragment.arguments = bundle
        supportFragmentManager?.beginTransaction()?.setCustomAnimations(com.trava.utilities.R.anim.slide_in_top,
                com.trava.utilities.R.anim.slide_in_bottom, com.trava.utilities.R.anim.slide_out_bottom,
                com.trava.utilities.R.anim.slide_out_top)?.add(android.R.id.content,
                fragment, RatingFragment::class.java.name)?.addToBackStack(null)
                ?.commitAllowingStateLoss()
    }

    private fun checkCancelRideCondition() {
        if (order?.accepted_at?.isNotEmpty() == true) {
            val acceptRideMills = convertDateStringToMilliseconds(order?.accepted_at ?: "")

            /* Till 5 hours no cancellation fee will be charged */
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cal.timeInMillis = acceptRideMills
            cal.add(Calendar.HOUR, 5)

            if (System.currentTimeMillis() > cal.timeInMillis) {
                var alert: AlertDialog? = null
                alert = AlertDialogUtil.getInstance().createOkCancelDialog(this,
                        R.string.cancelRide,
                        R.string.cancelRideDesc,
                        R.string.ok,
                        R.string.cancel, false, object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        showCancellationDialog()
                    }

                    override fun onCancelButtonClicked() {
                        alert?.dismiss()
                    }
                })
                if (!alert.isShowing) {
                    alert.show()
                }

            } else {
                showCancellationDialog()
            }
        } else {
            showCancellationDialog()
        }
    }

    private fun setPaymentData(order: Order?) {
        tvQuantity.text = order?.brand?.name
        tvServiceName.text = order?.brand?.brand_name

        setupInvoice(order)

        if (!order?.driver?.name.isNullOrEmpty()) {
            if (order?.order_status == OrderStatus.SERVICE_COMPLETE  || order?.order_status == OrderStatus.SERVICE_HALFWAY_STOP) {
                rlRating.show()
            } else {
                rlRating.hide()
            }
            tvRatingText.text = String.format("%s", "${getString(R.string.yourated)} ${order?.driver?.name}")
        } else {
            rlRating.hide()
        }
        if (order?.ratingByUser?.ratings ?: 0 > 0) {
            ratingBar.rating = order?.ratingByUser?.ratings?.toFloat() ?: 0.0f
            val stars: LayerDrawable = ratingBar.getProgressDrawable() as LayerDrawable
            if ((order?.ratingByUser?.ratings ?: 0) == 5 || (order?.ratingByUser?.ratings
                            ?: 0) > 4) {
                stars.getDrawable(2).setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP)
            } else if ((order?.ratingByUser?.ratings ?: 0) == 3 || (order?.ratingByUser?.ratings
                            ?: 0) == 4) {
                stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP)
            } else if ((order?.ratingByUser?.ratings ?: 0) < 3 && (order?.ratingByUser?.ratings
                            ?: 0) > 0) {
                stars.getDrawable(2).setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
            }
            txtRateUser.visibility=View.GONE
            ratingBar.visibility=View.VISIBLE
        }
        else
        {
            txtRateUser.visibility=View.VISIBLE
            ratingBar.visibility=View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupInvoice(order: Order?) {
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

        if (order?.package_id != 0 && order?.package_id != null) {
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
        var otherChargeAddInNormal=0.0
        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {

            if (order?.payment?.toll_charges != 0.0) {
                toll_parking += order?.payment?.toll_charges ?: 0.0
            }
            if (order?.payment?.parking_charges != 0.0) {
                toll_parking += order?.payment?.parking_charges ?: 0.0
            }

            otherChargeAddInNormal=order?.payment?.waiting_charges?.
            plus(order?.payment?.zone_charges?:0.0)?.
            plus(order?.payment?.schedule_charge?:0.0)?.
            plus(order?.payment?.sur_charge?:0.0)?.
            plus(toll_parking)?.
            plus(order?.payment?.airport_charges?:0.0)?:0.0
        }
        /* Normal Fare Calculation */
        val normalFare = timePrice?.toDouble()?.let { distancePrice?.toDouble()?.let { it1 -> (order?.payment?.product_alpha_charge?.toDouble())?.plus(it)?.plus(it1) } }?.let { getFormattedDecimal(it.times(exactAmount)) }
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
                tvPromoCharge.text="${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.coupon_charge?.times(exactAmount) ?: 0.0)}"

            } else {
                tvPromoCharge.text="${ConfigPOJO.currency} ${getFormattedDecimal(0.0 ?: 0.0)}"

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
            exactPrice = (normalFare?.toDouble())?.plus(order?.payment?.previous_charges ?: 0.0)?.plus(otherChargeAddInNormal)
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

        tvTimeLabel.text = "${getString(R.string.waiting_time)} ${"("}${getFormattedDecimal(order?.payment?.order_time?.toDouble() ?: 0.0)} ${getString(R.string.min)}${")"}"

        tvDistanceLabel.text =  "${getString(R.string.distance)} ${"("}${getString(R.string._km,order?.payment?.order_distance?.toDouble() ?: 0.0)}${")"}"

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

            var baseAmount=0.0
            if (serviceCharges!=0.0)
            {
                baseAmount=serviceCharges?.plus(timePrice.toDouble())?.plus(order?.payment?.admin_charge?.toDouble()
                        ?: 0.0)?:0.0
            }
            else
            {
                baseAmount = order?.payment?.product_alpha_charge?.toDouble()?.plus(timePrice.toDouble())?.plus(order?.payment?.admin_charge?.toDouble()
                        ?: 0.0)?:0.0
            }

            var tps_amt = (baseAmount?.times(ConfigPOJO.settingsResponse?.key_value?.tps?.replace("%","")?.toDouble()
                    ?: 0.0))?.div(100)
            var tvq_amt = (baseAmount?.times(ConfigPOJO.settingsResponse?.key_value?.tvq?.replace("%","")?.toDouble()
                    ?: 0.0))?.div(100)
            tvtps.text = ConfigPOJO.currency + " " + getFormattedDecimal(tps_amt ?: 0.0)
            tvtpq.text = ConfigPOJO.currency + " " + getFormattedDecimal(tvq_amt ?: 0.0)
            tvSubtotalPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.final_charge?.toDouble()?.
            times(exactAmount)?.minus(tps_amt?:0.0)?.minus(tvq_amt?:0.0) ?: 0.0)} "

            tvLevelCharge.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.level_charge?.toDouble()?.times(exactAmount) ?: 0.0)} "
        } else {
            tvLevelCharge.visibility = View.GONE
            levelCharge.visibility = View.GONE
            tpsCharge.visibility = View.GONE
            tvqCharge.visibility = View.GONE
            tvtps.visibility = View.GONE
            tvtpq.visibility = View.GONE
        }

        if (order?.check_lists != null) {
            if (order?.check_lists!!.size > 0) {
                surchecklist.visibility = View.VISIBLE
                tvCheckChargesPrice.visibility = View.VISIBLE
            }
        }

        tvBookingfeePrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.admin_charge?.toDouble()?.times(exactAmount) ?: 0.0)}"
        tvSurChargesPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.sur_charge?.times(exactAmount) ?: 0.0)}"
        tvScheduleChargesPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.schedule_charge?.times(exactAmount) ?: 0.0)}"
        tvInsuranceChargesPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.insurance_amount?.times(exactAmount) ?: 0.0)}"
        tvCheckChargesPrice.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.check_list_total?.times(exactAmount) ?: 0.0)}"
        helper_charge.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.helper_charges?.times(exactAmount) ?: 0.0)}"
        tvTotal.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.final_charge?.toDouble()?.times(exactAmount) ?: 0.0)} "
        tvPaymentTypeAmount.text = "${ConfigPOJO.currency} ${getFormattedDecimal(order?.payment?.final_charge?.toDouble()?.times(exactAmount) ?: 0.0)} "

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

    private fun getFormattedDecimal(num: Double): String? {
        return String.format(Locale.US, "%.2f", num)
    }

    private fun getFormattedPrice(price: String?): String {
        return String.format(Locale.US, "%.2f", price?.toDouble())
    }

    private fun setUserData(driver: Driver?) {
        if (driver?.name == null) {
            rlProfile.visibility = View.GONE
            return
        } else {
//            rlProfile.visibility = View.VISIBLE
        }
        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transforms(CircleCrop())
                .placeholder(R.drawable.profile_pic_placeholder)
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(driver.profile_pic_url).into(imageView)
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(driver.profile_pic_url).into(ivDriverPic)
        tvName.text = driver?.name
        tvComments.text = order?.ratingByUser?.comments
    }

    private fun setBookingData(orderData: Order?) {
        if (orderData?.category_id == 4) {
            tvDropOff.text = getString(R.string.deliver_loc)
        }


        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(8))
        var lat = orderData?.pickup_latitude ?: 0.0
        var lng = orderData?.pickup_longitude ?: 0.0
        val dropLat = orderData?.dropoff_latitude ?: 0.0
        val dropLng = orderData?.dropoff_longitude ?: 0.0

        var list: ArrayList<LatLng>? = null
        list = MapUtils.decodePoly(Uri.decode(orderData?.exact_path))
        if (orderData?.exact_path != "" && orderData?.exact_path != null) {
            Glide.with(this).applyDefaultRequestOptions(requestOptions).load(MapUtils.setStaticPolyLineOnMapPath(orderData?.exact_path
                    ?: ""
                    , ConfigPOJO.SECRET_API_KEY, lat, lng, list[list.size - 1].latitude, list[list.size - 1].longitude)).into(ivMap)
        } else {
            Glide.with(this).applyDefaultRequestOptions(requestOptions).load(MapUtils.setStaticPolyLineOnMapPath(orderData?.exact_path
                    ?: ""
                    , ConfigPOJO.SECRET_API_KEY, lat, lng, dropLat, dropLat)).into(ivMap)
        }

        tvDateTime.text = DateUtils.getFormattedTimeForBooking(orderData?.order_timings ?: "")

        tvBookingId.text = "Id:${orderData?.order_token}"
        tvBookingStatus.text = when (orderData?.order_status) {
            OrderStatus.SERVICE_COMPLETE -> {
                if (order?.future == "1") {
                    tvStartTime.text = DateUtils.getFormattedTimeForBooking(orderData.cRequest?.started_at
                            ?: "")
                } else {
                    tvStartTime.text = DateUtils.getFormattedTimeForBooking(orderData.cRequest?.accepted_at
                            ?: "")
                }
                tvCompletedTime.text = DateUtils.getFormattedTimeForBooking(orderData.cRequest?.updated_at
                        ?: "")
                showDateTimeViews(View.VISIBLE)
                if (order?.booking_type == BOOKING_TYPE.CARPOOL)
                    getString(R.string.car_pool_completed)
                else
                    getString(R.string.completed)
            }
            OrderStatus.SERVICE_HALFWAY_STOP -> {
                getString(R.string.half_way)
            }
            OrderStatus.CUSTOMER_CONFIRMATION_PENDING_ETOKEN, OrderStatus.ONGOING -> {
                showDateTimeViews(View.GONE)
                getString(R.string.ongoing)
            }
            OrderStatus.PENDING -> {
                "Pending"
            }
            OrderStatus.CONFIRMED -> {
                showDateTimeViews(View.GONE)
                getString(R.string.confirmed)
            }
            OrderStatus.REACHED -> {
                showDateTimeViews(View.GONE)
                getString(R.string.reached)
            }
            OrderStatus.UNASSIGNED -> {
                "Unassigned"
            }
            OrderStatus.SCHEDULED,
            OrderStatus.DRIVER_PENDING,
            OrderStatus.DRIVER_APPROVED -> {
                showDateTimeViews(View.GONE)
                getString(R.string.Scheduled)
            }
            else -> {
                tvBookingStatus.setTextColor(ContextCompat.getColor(this, R.color.pink_gradient_2))
                showDateTimeViews(View.GONE)
                getString(R.string.cancelled)
            }
        }

        acDropOffAddress.text = if (orderData?.dropoff_address.isNullOrEmpty()) orderData?.pickup_address else orderData?.dropoff_address
        acPickupAddress.text = orderData?.pickup_address
        if (order?.ride_stops != null) {
            for (item in order?.ride_stops ?: ArrayList()) {
                when (item.priority) {
                    1 -> {
                        ivFirstStop.show()
                        ivFirstStopDot1.show()
                        ivFirstStopDot2.show()
                        tvFirstStop.text = item.address
                        MyAnimationUtils.animateShowHideView(rlLocation, tvFirstStop, true)
                    }

                    2 -> {
                        ivSecondStop.show()
                        ivSecondStopDot1.show()
                        ivSecondStopDot2.show()
                        tvSecondStop.text = item.address
                        MyAnimationUtils.animateShowHideView(rlLocation, tvSecondStop, true)
                    }

                    3 -> {
                        ivThirdStop.show()
                        ivThirdStopDot1.show()
                        ivThirdStopDot2.show()
                        tvThirdStop.text = item.address
                        MyAnimationUtils.animateShowHideView(rlLocation, tvThirdStop, true)
                    }

                    4 -> {
                        ivFourthStop.show()
                        ivFourthStopDot1.show()
                        ivFourthStopDot2.show()
                        tvFourthStop.text = item.address
                        MyAnimationUtils.animateShowHideView(rlLocation, tvFourthStop, true)
                    }
                }
            }
        }
    }

    private fun showDateTimeViews(visibility: Int) {
        tvStartedAt.visibility = visibility
        tvStartTime.visibility = visibility
        tvCompletedAt.visibility = visibility
        tvCompletedTime.visibility = visibility
    }


    private fun getFinalCharge(orderDetail: Order?): String {
        val price = (orderDetail?.payment?.product_quantity
                ?: 1).times(orderDetail?.payment?.product_per_quantity_charge?.toDouble()
                ?: 1.00).plus(orderDetail?.payment?.product_per_distance_charge?.toDouble() ?: 0.00)
                .plus(orderDetail?.payment?.product_per_weight_charge?.toDouble() ?: 0.00)
                .plus(orderDetail?.payment?.product_alpha_charge?.toDouble() ?: 0.00)
        when (orderDetail?.category_id) {
            1 -> {

            }

            2 -> {

            }

            3 -> {

            }
        }
        return String.format("%.2f", price)
    }

    override fun onApiSuccess(listBookings: ArrayList<Order?>) {

    }

    override fun onOrderDetailsSuccess(response: Order?) {
        order = response
        if (response?.order_status == OrderStatus.SCHEDULED
                || response?.order_status == OrderStatus.PENDING
                || response?.order_status == OrderStatus.DRIVER_PENDING
                || response?.order_status == OrderStatus.DRIVER_APPROVED) {
            tvCancel.visibility = View.VISIBLE
        } else {
            tvCancel.visibility = View.GONE

        }
        tvTrack.visibility = View.GONE
        if (ConfigPOJO.multiple_request.toInt() > 1) {
            if (response?.order_status == OrderStatus.REACHED ||
                    response?.order_status == OrderStatus.CONFIRMED
                    || response?.order_status == OrderStatus.ONGOING
                    || response?.order_status == OrderStatus.DRIVER_APPROVED) {
                tvTrack.visibility = View.VISIBLE
            } else {
                tvTrack.visibility = View.GONE
            }
        }

        response?.brand?.name = response?.brand?.product_name
        setUserData(response?.driver)
        setPaymentData(response)
        setBookingData(response)
        /*tvBookingStatus.text = when (response?.order_status) {
            OrderStatus.SERVICE_COMPLETE -> {
                tvStartTime.text = DateUtils.getFormattedTimeForBooking(response.cRequest?.started_at
                        ?: "")
                tvCompletedTime.text = DateUtils.getFormattedTimeForBooking(response.cRequest?.updated_at
                        ?: "")
                showDateTimeViews(View.VISIBLE)
                getString(R.string.completed)
            }
            else -> {
                showDateTimeViews(View.GONE)
                getString(R.string.cancelled)
            }
        }*/
        viewFlipperExtraDetails.displayedChild = 2
        if (response?.order_status == OrderStatus.CUSTOMER_CANCEL || response?.order_status == OrderStatus.DRIVER_CANCELLED) {
            cl_invoice.visibility = View.GONE
        } else {
            cl_invoice.visibility = View.VISIBLE
        }

        if (ConfigPOJO.settingsResponse?.key_value?.is_fair_breakdown_disable=="true")
        {
            cl_invoice.visibility = View.GONE
        }

//        if (response?.ratingByUser == null || response.ratingByUser?.ratings == null || response.ratingByUser?.ratings == 0) {
//            rlProfile.visibility = View.GONE
//        } else {
        val rating = if (response?.ratingByUser?.ratings ?: 0 <= 5) {
            response?.ratingByUser?.ratings ?: 0
        } else {
            5
        }
        if (rating == 0) {
            ivRating.visibility = View.INVISIBLE
        } else {
            ivRating.visibility = View.VISIBLE
            ivRating?.setImageResource(ratingDrawables[rating - 1])
        }

//        }
    }

    override fun showLoader(isLoading: Boolean) {

    }

    override fun apiFailure() {
        viewFlipperExtraDetails.displayedChild = 2
        rootView.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        viewFlipperExtraDetails.displayedChild = 2
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(this)
        } else {
            rootView.showSnack(error ?: "")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun showCancellationDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_cancel_reason)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etMessage = dialog.findViewById(R.id.etMessage) as EditText
        val tvSubmit = dialog.findViewById(R.id.tvSubmit) as TextView
        val cancelGroup = dialog?.findViewById<RadioGroup>(R.id.cancelGroup)
        val btn1 = dialog?.findViewById<RadioButton>(R.id.btn1)
        val btn2 = dialog?.findViewById<RadioButton>(R.id.btn2)
        val btn3 = dialog?.findViewById<RadioButton>(R.id.btn3)
        val btn4 = dialog?.findViewById<RadioButton>(R.id.btn4)

        btn4?.visibility = View.VISIBLE
        btn1?.isChecked = true

        btn1?.setOnClickListener {
            etMessage?.visibility = View.GONE
        }

        btn2?.setOnClickListener {
            etMessage?.visibility = View.GONE
        }

        btn3?.setOnClickListener {
            etMessage?.visibility = View.GONE
        }

        btn4?.setOnClickListener {
            if (btn4.isChecked)
                etMessage?.visibility = View.VISIBLE
            else
                etMessage?.visibility = View.GONE
        }
        tvSubmit.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

        tvSubmit.setOnClickListener {
            /* if (etMessage.text.toString().trim().isNotEmpty()) {
                 val map = HashMap<String, String>()
                 map["order_id"] = order?.order_id.toString()
                 map["cancel_reason"] = etMessage.text.toString().trim()
                 if (CheckNetworkConnection.isOnline(this)) {
                     presenter.requestCancelApiCall(map)
                     dialog.dismiss()
                 } else {
                     CheckNetworkConnection.showNetworkError(rootView)
                 }
             } else {
                 Toast.makeText(this, getString(R.string.cancellation_reason_validation_text), Toast.LENGTH_SHORT).show()
             }*/
            if (cancelGroup?.checkedRadioButtonId.toString().trim().isNotEmpty()) {
                val selectedRdBtn = dialog?.findViewById<RadioButton>(cancelGroup!!.checkedRadioButtonId)
                val map = HashMap<String, String>()
                map["order_id"] = order?.order_id.toString()
                map["cancel_reason"] = (selectedRdBtn)?.text.toString().trim()
                map["order_distance"] = "0"
                if (btn4?.isChecked == true) {
                    if (etMessage?.text.toString().trim().isEmpty()) {
                        Toast.makeText(this, getString(R.string.cancellation_reason_validation_text), Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    map["cancel_reason"] = etMessage?.text.toString().trim()
                }
                if (CheckNetworkConnection.isOnline(this)) {
                    presenter.requestCancelApiCall(map)
                    dialog?.dismiss()
                } else {
                    CheckNetworkConnection.showNetworkError(rootView)
                }
            } else {
                Toast.makeText(this, getString(R.string.cancellation_reason_validation_text), Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    override fun onCancelApiSuccess() {
        Toast.makeText(this, R.string.order_cancelled, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onFilterSuccess() {
        TODO("Not yet implemented")
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun onRated() {
        onResume()
    }
}
