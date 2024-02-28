package com.trava.user.ui.home.processingrequest


import android.app.Activity
import android.app.Dialog
import android.content.*
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.*
import android.transition.Explode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.trava.user.R
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.deliverystarts.DeliveryStartsFragment
import com.trava.user.ui.home.services.ServicesFragment
import com.trava.user.utils.AppUtils
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.*
import com.trava.utilities.constants.ACCESS_TOKEN
import com.trava.utilities.constants.ORDER
import com.trava.utilities.constants.SUCCESS_CODE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trava.user.databinding.FragmentProcessingRequestBinding
import com.trava.user.ui.home.complete_ride.CompleteRideFragment
import com.trava.user.ui.home.deliverystarts.paymentview.PaymentFragment
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.utilities.DateUtils.getFormattedDateForUTC
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.fragment_confirm_booking.*
import kotlinx.android.synthetic.main.fragment_processing_request.*
import kotlinx.android.synthetic.main.fragment_processing_request.cvToolbar
import org.json.JSONObject


class ProcessingRequestFragment : Fragment(), ProcessingRequestContract.View {

    var binding: FragmentProcessingRequestBinding? = null

    private val timeLimit = 90000L
    private var tempValue=0
    private lateinit var serviceRequest: ServiceRequestModel

    private val presenter = ProcessingRequestPresenter()

    private var orderDetail: Order? = null

    private var dialogIndeterminate: DialogIndeterminate? = null

    private var isPaused = false

    private var isTimerFinished = false

    private var handlerCheckOrder = Handler()

    private var isServicesAccepted = false

    private var via = ""

    private var orderJson = ""
    var buraqfare_r = 0.0

    private var transferReqDialog: AlertDialog.Builder? = null
    private var scheduleSuccessDialog: AlertDialog.Builder? = null

    private var isTransfer = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            enterTransition = Explode()
            exitTransition = Explode()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_processing_request, container, false)

        binding?.color = ConfigPOJO.Companion

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        dialogIndeterminate = DialogIndeterminate(activity)

        if (ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
            cvToolbar.visibility = View.VISIBLE
        }

        tvCancel.setTextColor(Color.parseColor(ConfigPOJO.primary_color))
        progressBar.progressTintList = ColorStateList.valueOf(Color.parseColor(ConfigPOJO.primary_color))
        setData()
        progressBar.max = timeLimit.toInt()
        if (arguments?.getBoolean("changeTimeOut", false) == true) {
            progressBar?.isIndeterminate = true
            isTimerFinished = true
            checkOrderStatus(0)
        } else {
            startTimer()
        }

        if (arguments?.containsKey("via") == true) {
            via = arguments?.getString("via") ?: ""
            textView13.text = getString(R.string.waitingForAcceptance)
        }
        progressBar.visibility = View.VISIBLE
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        AppSocket.get().on(Socket.EVENT_CONNECT, onConnect)
        isPaused = false
        checkOrderStatus(0)

//        if (!isTimerFinished) {
        startEventSocket()
        context?.let {
            LocalBroadcastManager
                    .getInstance(it)
                    .registerReceiver(notificationBroadcast, IntentFilter(Broadcast.NOTIFICATION))
        }
//        }
    }

    override fun onPause() {
        super.onPause()
        AppSocket.get().off(Socket.EVENT_CONNECT, onConnect)
        isPaused = true
        stopSocketEvent()
    }

    private val onConnect = Emitter.Listener {
        //        if (isTimerFinished) {
        checkOrderStatus(0)
//        }
    }

    private val notificationBroadcast = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.getIntExtra("order_id", 0) == orderDetail?.order_id?.toInt() && !isServicesAccepted) {
                checkOrderStatus(0)
            }
        }
    }

    private fun startEventSocket() {
        AppSocket.get().on(Events.ORDER_EVENT) {
            if (JSONObject(it[0].toString()).has("type")) {
                when (JSONObject(it[0].toString()).getString("type")) {
                    OrderEventType.SERVICE_REACHED, OrderEventType.SERVICE_ACCEPT -> {
                        isServicesAccepted = true
                        try {
                            orderJson = JSONObject(it[0].toString()).getJSONArray("order").get(0).toString()
                            val orderModel = Gson().fromJson(orderJson, Order::class.java)
//                            if (orderModel.order_id == orderDetail?.order_id) {
                            timer.cancel()
                            Logger.e(Events.ORDER_EVENT, it[0].toString())
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(activity, "Your ride accept successfully", Toast.LENGTH_SHORT).show()
                                if (orderModel.future == ServiceRequestModel.SCHEDULE) {
                                    showScheduledConfirmationDialog(orderJson)
                                } else {
                                    if (arguments?.containsKey("via") == false) {
                                        if (via != "halfWayStop") {
                                            openBookingConfirmedFragment(orderJson)
                                        }
                                    } else {
                                        openBookingConfirmedFragment(orderJson)
                                    }
                                }
                            }
//                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    OrderEventType.SERVICE_REJECT -> {
                        isServicesAccepted = true
                        Handler(Looper.getMainLooper()).post {
                            if (arguments?.getBoolean("changeTimeOut", false) == false) {
                                openBookingFragment()
                            } else {
                                fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                                val fragment = ServicesFragment()
                                (activity as HomeActivity).serviceRequestModel = ServiceRequestModel()
                                (activity as HomeActivity).servicesFragment = fragment
                                fragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                            }
                        }
                    }

                    OrderEventType.DRIVER_HALFWAY_REJECTED -> {
                        val orderJson = JSONObject(it[0].toString()).getJSONArray("order").get(0).toString()
                        openBookingConfirmedFragment(orderJson)
                    }

                    OrderEventType.SERVICE_COMPLETE -> {
                        val orderJson = JSONObject(it[0].toString()).getJSONArray("order").get(0).toString()
                        val orderModel = Gson().fromJson(orderJson, Order::class.java)
//                        if (orderModel.order_id == order?.order_id && !isPaused) {\
                        if (via != "vehicleBreakDown") {
                            openInvoiceFragment(orderJson)
                        }
                    }

                    OrderEventType.SERVICE_LONG_DISTANCE -> {
                        serviceRequest.isLongDistancePickup = true
                    }

                    OrderEventType.SERVICE_BREAK_DOWN_ACCEPTED -> {
                        val orderJson = JSONObject(it[0].toString()).getJSONArray("order").get(0).toString()
                        orderDetail = Gson().fromJson(orderJson, Order::class.java)

                        Handler(Looper.getMainLooper()).post {
                            isTransfer = true
                            showTransferRequestDialog()
                            textView13.text = getString(R.string.we_are_requesting_your_request)
                        }
                    }

                    OrderEventType.SERVICE_BREAK_DOWN_REJECTED -> {
                        val orderJson = JSONObject(it[0].toString()).getJSONArray("order").get(0).toString()
                        openBookingConfirmedFragment(orderJson)
                    }
                }
            }
        }
    }

    private fun showTransferRequestDialog() {
        if (transferReqDialog == null) {
            transferReqDialog = AlertDialog.Builder(context as HomeActivity)
            transferReqDialog?.setTitle(getString(R.string.transferringReq))
            transferReqDialog?.setMessage(getString(R.string.transferRequest))
            transferReqDialog?.setCancelable(true)
            transferReqDialog?.setPositiveButton(getString(R.string.ok),
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                    })
            transferReqDialog?.show()
        }
    }

    private fun stopSocketEvent() {
        AppSocket.get().off(Events.ORDER_EVENT)
    }

    private fun setData() {
        orderDetail = Gson().fromJson(arguments?.getString(ORDER), Order::class.java)
        serviceRequest = (activity as HomeActivity).serviceRequestModel
        tvQuantity.text = orderDetail?.brand?.name?:""
        tvProductName.text = serviceRequest.brandName

        tvLocation.text = orderDetail?.dropoff_address
        var total = 0.0
        when (orderDetail?.payment?.payment_type) {
            PaymentType.CASH -> {
                total = orderDetail?.payment?.final_charge?.toDouble() ?: 0.0
            }
            PaymentType.CARD -> {
                total = orderDetail?.payment?.final_charge?.toDouble() ?: 0.0
            }
            PaymentType.WALLET -> {
                total = orderDetail?.payment?.final_charge?.toDouble() ?: 0.0
            }
            PaymentType.E_TOKEN -> {
                total = 0.0
            }
        }
        var exactAmount = total
        if (context?.getString(R.string.app_name) == "Wasila") {
            if (ConfigPOJO.OMAN_PHONECODE == "+968") {
                exactAmount = exactAmount.times(ConfigPOJO.OMAN_CURRENCY)
            }
        }
        val amount = exactAmount?.times(10)?.div(100)
        var start_price =serviceRequest.final_charge
        val end_price = exactAmount?.plus(amount?:0.0)
        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE)
        {
            tvTotal.text = String.format("%.2f", exactAmount)
        }
        else
        {

            if (serviceRequest!!.selectedProduct!!.actual_value!! > serviceRequest?.final_charge!!.toFloat()){
                start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()

                if (ConfigPOJO.settingsResponse?.key_value?.is_booking_fee == "true") //buraq percentage required
                {
                    buraqfare_r = start_price.times(serviceRequest!!.selectedProduct?.buraq_percentage ?: 0F).div(100)
                }
                start_price =  start_price.plus(buraqfare_r)
                tvTotal.text = String.format("%.2f", start_price)

            }
            else{
                tvTotal.text = String.format("%.2f %s %.2f", start_price, "-", end_price)

            }
            /*if (serviceRequest!!.selectedProduct!!.actual_value!! > serviceRequest.final_charge!!){
                start_price =  serviceRequest!!.selectedProduct!!.actual_value!!.toDouble()
            }*/

        }
//        tvTotal.text = String.format("%s %.2f", ConfigPOJO.currency, total)
    }


    private fun setListeners() {
        binding?.ivBackSnd?.setOnClickListener {
            tvCancel.performClick()
        }

        tvCancel.setOnClickListener {
            if (CheckNetworkConnection.isOnline(activity)) {
                if (arguments?.containsKey("via") == false) {
                    val map = HashMap<String, String>()
                    map["order_id"] = orderDetail?.order_id.toString()
                    map["cancel_reason"] = Constants.DEFAULT_REASON_CUSTOMER_CANCELLED
                    map["order_distance"] = "0"
                    presenter.requestCancelApiCall(map)
                } else {
                    when (via) {
                        "halfWayStop" -> {
                            orderDetail?.order_id?.let { it1 -> presenter.requestHafWaycancelByUser(it1) }
                        }

                        "vehicleBreakDown" -> {
                            if (arguments?.containsKey("action") == true) {
                                val map = HashMap<String, String>()
                                map["order_id"] = orderDetail?.order_id.toString()
                                map["cancel_reason"] = Constants.DEFAULT_REASON_CUSTOMER_CANCELLED
                                presenter.requestCancelApiCall(map)
                            } else {
                                if (isTransfer) {
                                    val map = HashMap<String, String>()
                                    map["order_id"] = orderDetail?.order_id.toString()
                                    map["cancel_reason"] = Constants.DEFAULT_REASON_CUSTOMER_CANCELLED
                                    presenter.requestCancelApiCall(map)
                                } else {
                                    orderDetail?.order_id?.let { it1 -> presenter.requestVehicleBreakDownCancelByUser(it1) }
                                }

                            }
                        }
                    }
                }
            } else {
                CheckNetworkConnection.showNetworkError(binding?.root!!)
            }

        }
    }

    override fun onCancelHalfWayStopSucess() {
        if (orderJson.isNotEmpty()) {
            openBookingConfirmedFragment(orderJson)
        }
    }

    override fun onCancelVehicleBreakdownSuccess() {
        if (orderJson.isNotEmpty()) {
            openBookingConfirmedFragment(orderJson)
        }
    }


    private val timer = object : CountDownTimer(timeLimit, 16) {
        override fun onFinish() {
            isTimerFinished = true
            if (arguments?.containsKey("via") == true) {
                if (via == "vehicleBreakDown")
                    noDriverFoundDialog()
            }
            if (!isPaused) {
                checkOrderStatus(0)
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            progressBar.progress = timeLimit.toInt() - millisUntilFinished.toInt()
        }

    }

    private fun startTimer() {
        timer.start()
    }

    private fun noDriverFoundDialog() {
        val alertDialog = AlertDialog.Builder(context as HomeActivity)
        alertDialog.setTitle(getString(R.string.noDriverFound))
        alertDialog.setMessage(getString(R.string.currently_no_driver_available))
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton(getString(R.string.ok),
                DialogInterface.OnClickListener { dialog, which ->
                    if (CheckNetworkConnection.isOnline(activity)) {
                        val map = HashMap<String, String>()
                        map["order_id"] = orderDetail?.order_id.toString()
                        map["cancel_reason"] = Constants.DEFAULT_REASON_CUSTOMER_CANCELLED
                        presenter.requestCancelApiCall(map)
                    } else {
                        CheckNetworkConnection.showNetworkError(binding?.root!!)
                    }
                })
        alertDialog.show()
    }

    private fun checkOrderStatus(delay: Long) {
        try {
            handlerCheckOrder.removeCallbacks(runnable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        handlerCheckOrder.postDelayed(runnable, delay)

    }

    private val runnable = Runnable {
        if (isTimerFinished) {
//            if(arguments?.containsKey("via") == false) {
            checkOrderStatus(5000)
//            }
        }
        val request = JSONObject()
        request.put("type", OrderEventType.CUSTOMER_SINGLE_ORDER)
        request.put("access_token", ACCESS_TOKEN)
        request.put("order_token", orderDetail?.order_token)
        AppSocket.get().emit(Events.COMMON_EVENT, request, Ack {
            val response = Gson().fromJson<ApiResponse<Order>>(it[0].toString(),
                    object : TypeToken<ApiResponse<Order>>() {}.type)

            if (response.statusCode == SUCCESS_CODE) {
                val orderModel = response.result
                orderJson = JSONObject(it[0]?.toString()).getString("result")
                Handler(Looper.getMainLooper()).post {
                    when (orderModel?.order_status) {

                        OrderStatus.ONGOING, OrderStatus.REACHED -> {
                            if (arguments?.containsKey("via") == false) {
                                if (via != "halfWayStop" || via != "vehicleBreakDown") {
                                    openBookingConfirmedFragment(orderJson)
                                }
                            }
                        }

                        OrderStatus.CONFIRMED -> {
                            openBookingConfirmedFragment(orderJson)
                        }
                        OrderStatus.SERVICE_HALFWAY_STOP -> {
                            if (response?.result.half_way_status=="Accepted")
                            {
                                openInvoiceFragment(orderJson)
                            }
                            else if (response?.result.half_way_status=="Rejected")
                            {
                                openBookingConfirmedFragment(orderJson)
                            }
                        }
                        OrderStatus.SERVICE_COMPLETE -> {
                            if (via != "vehicleBreakDown") {
                                openInvoiceFragment(orderJson)
                            }
                        }

                        OrderStatus.SEARCHING -> {
                            /* Keep searching */
                        }

                        OrderStatus.SCHEDULED -> {
                            showScheduledConfirmationDialog(orderJson)
                        }
                        OrderStatus.PENDING -> {
                            if (activity != null) {
                                if (tempValue==0)
                                {
                                    requestAssignFromAdmin()
                                    tempValue=1
                                }
                            }
                        }
                        OrderStatus.UNASSIGNED -> {
                            if (activity != null) {

                                if (tempValue==0)
                                {
                                    requestUnAssignFromAdmin()
                                    tempValue=1
                                }
                            }
                        }
                        else -> {
                            if (arguments?.getBoolean("changeTimeOut", false) == false) {
                                openBookingFragment()
                            } else {
                                fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                                val fragment = ServicesFragment()
                                (context as HomeActivity).serviceRequestModel = ServiceRequestModel()
                                (context as HomeActivity).servicesFragment = fragment
                                fragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                            }
                        }
                    }
                }
            } else if (response.statusCode == StatusCode.UNAUTHORIZED) {
                AppUtils.logout(activity as Activity)
            }
        })
    }

    private fun openBookingConfirmedFragment(orderJson: String?) {
        val fragment = DeliveryStartsFragment()
        val bundle = Bundle()
        bundle.putString("order", orderJson)
        fragment.arguments = bundle
        try {
            fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            fragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commitAllowingStateLoss()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun openInvoiceFragment(orderJson: String?) {
        if (ConfigPOJO.gateway_unique_id.equals("payku") ||
                ConfigPOJO.gateway_unique_id.toLowerCase().equals("wipay")) {
            var order = Gson().fromJson(orderJson, Order::class.java)
            if (order?.payment?.payment_type == "Card") {
                val fragment = CompleteRideFragment()
                val bundle = Bundle()
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
        }else if(ConfigPOJO.gateway_unique_id.toLowerCase() == "paystack"){
            val fragment = CompleteRideFragment()
            val bundle = Bundle()
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
    }

    private fun openBookingFragment() {
        if (activity != null) {
            if(ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE){
//              Toast.makeText(activity, "We're sorry, there is no driver around you at the moment available to help you out! Call us at +1 (514) 416-6606 and we'll make sure to appoint someone right away!", Toast.LENGTH_LONG).show()
                DriverNotFoundInGomove()
            }
            else
            {
                Toast.makeText(activity, R.string.currently_no_driver_available, Toast.LENGTH_SHORT).show()
                fragmentManager?.popBackStackImmediate()
            }
        }
//        openHomeFragment()
    }

    private fun DriverNotFoundInGomove() {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setContentView(R.layout.dialog_driver_not_found)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog?.findViewById<TextView>(R.id.tvMsg)?.text=getString(R.string.no_driver_found_gomove)
        dialog?.findViewById<TextView>(R.id.tvCall)?.background =
                StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color,
                        GradientDrawable.RECTANGLE)

        dialog?.findViewById<TextView>(R.id.tvCall)?.setOnClickListener {
            val phone = "514-416-6606"
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            dialog.dismiss()
            fragmentManager?.popBackStackImmediate()
        }

        dialog?.findViewById<TextView>(R.id.tvTryAgain)?.setOnClickListener {
            dialog.dismiss()
            fragmentManager?.popBackStackImmediate()
        }

        dialog?.show()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun requestUnAssignFromAdmin() {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setContentView(R.layout.message_alert)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog?.findViewById<TextView>(R.id.tvMsg)?.text=getString(R.string.unassiged_message)

        dialog?.findViewById<TextView>(R.id.tvCall)?.setOnClickListener {
            openHomeFragment()
            dialog.dismiss()
        }

        dialog?.show()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun requestAssignFromAdmin() {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setContentView(R.layout.message_alert)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog?.findViewById<TextView>(R.id.tvMsg)?.text=getString(R.string.request_approval_pending)

        dialog?.findViewById<TextView>(R.id.tvCall)?.setOnClickListener {
            dialog.dismiss()
            openHomeFragment()
//            fragmentManager?.popBackStackImmediate()
        }

        dialog?.findViewById<TextView>(R.id.tvTryAgain)?.setOnClickListener {
            dialog.dismiss()
            openHomeFragment()
//            fragmentManager?.popBackStackImmediate()
        }

        dialog?.show()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onApiSuccess() {
        timer.cancel()
        Toast.makeText(activity, R.string.request_cancelled_successfully, Toast.LENGTH_SHORT).show()
        fragmentManager?.popBackStackImmediate()
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        binding?.root!!.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(activity as Activity)
        } else {
            binding?.root!!.showSnack(error.toString())
        }
    }

    override fun handleOrderError(order: Order?) {
        when (order?.order_status) {
            OrderStatus.CUSTOMER_CANCEL, OrderStatus.DRIVER_CANCELLED, OrderStatus.DRIVER_SCHEDULED_CANCELLED, OrderStatus.SYS_SCHEDULED_CANCELLED, OrderStatus.SERVICE_TIMEOUT -> {
                timer.cancel()
                Toast.makeText(activity, R.string.request_cancelled_successfully, Toast.LENGTH_SHORT).show()
                fragmentManager?.popBackStackImmediate()
            }

            OrderStatus.SERVICE_COMPLETE -> {
                openInvoiceFragment(Gson().toJson(orderDetail))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer.cancel()
        dialogIndeterminate?.show(false)
        handlerCheckOrder.removeCallbacks(runnable)
        presenter.detachView()
    }

    private fun showScheduledConfirmationDialog(orderJson: String?) {
        var orderData = Gson().fromJson(orderJson, Order::class.java)
        if (scheduleSuccessDialog == null) {
            scheduleSuccessDialog = AlertDialog.Builder(context as HomeActivity)
            scheduleSuccessDialog?.setTitle(getString(R.string.congratulations))
            scheduleSuccessDialog?.setMessage(getString(R.string.service_booked_successfully) + " "
                    + getFormattedDateForUTC(orderData.order_timings ?: ""))
            scheduleSuccessDialog?.setCancelable(true)
            scheduleSuccessDialog?.setPositiveButton(getString(R.string.ok),
                    DialogInterface.OnClickListener { dialog, which ->
                        openHomeFragment()
                    })
            scheduleSuccessDialog?.show()
        }
    }

    private fun openHomeFragment() {
        fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val fragment = ServicesFragment()
        (activity as HomeActivity).serviceRequestModel = ServiceRequestModel()
        (activity as HomeActivity).servicesFragment = fragment
        fragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
    }
}