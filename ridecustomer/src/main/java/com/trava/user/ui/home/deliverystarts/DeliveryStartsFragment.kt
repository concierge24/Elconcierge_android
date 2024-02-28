package com.trava.user.ui.home.deliverystarts

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.location.Address
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.ui.IconGenerator
import com.trava.user.utils.audiorecording.AudioCaptureService
import com.trava.user.R
import com.trava.user.databinding.FragmentDeliveryStartsBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.HomeActivity.Companion.fullTrackArray
import com.trava.user.ui.home.chatModule.chatMessage.ChatActivity
import com.trava.user.ui.home.complete_ride.CompleteRideFragment
import com.trava.user.ui.home.deliverystarts.details.DeliveryStartsDetails
import com.trava.user.ui.home.deliverystarts.paymentview.PaymentFragment
import com.trava.user.ui.home.orderbreakdown.FullOrderDetailsFragment
import com.trava.user.ui.home.processingrequest.ProcessingRequestFragment
import com.trava.user.ui.home.services.ServicesFragment
import com.trava.user.ui.home.stories.WatchStories
import com.trava.user.utils.*
import com.trava.user.utils.PermissionUtils
import com.trava.user.utils.swipeButton.Controller.OnSwipeCompleteListener
import com.trava.user.utils.swipeButton.View.LeftSwipeButtonView
import com.trava.user.utils.swipeButton.View.RightSwipeButtonView
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.TrackingModel
import com.trava.user.webservices.models.contacts.RideShareResponse
import com.trava.user.webservices.models.nearestroad.RoadItem
import com.trava.user.webservices.models.order.Order
import com.trava.user.webservices.models.order.helper
import com.trava.utilities.*
import com.trava.utilities.Constants.SECRET_DB_KEY
import com.trava.utilities.constants.*
import com.trava.utilities.location.LocationProvider
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_IMAGE_URL
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_delivery_starts.*
import kotlinx.android.synthetic.main.fragment_delivery_starts.fabMyLocation
import kotlinx.android.synthetic.main.fragment_delivery_starts.ivCallDriver
import kotlinx.android.synthetic.main.fragment_delivery_starts.ivDriverImage
import kotlinx.android.synthetic.main.fragment_delivery_starts.ivVehicleImage
import kotlinx.android.synthetic.main.fragment_delivery_starts.ratingBar
import kotlinx.android.synthetic.main.fragment_delivery_starts.rlHeader
import kotlinx.android.synthetic.main.fragment_delivery_starts.rootView
import kotlinx.android.synthetic.main.fragment_delivery_starts.tvDriverName
import kotlinx.android.synthetic.main.fragment_delivery_starts.tvDriverStatus
import kotlinx.android.synthetic.main.fragment_delivery_starts.tvMapNormal
import kotlinx.android.synthetic.main.fragment_delivery_starts.tvPanic
import kotlinx.android.synthetic.main.fragment_delivery_starts.tvRatingCount
import kotlinx.android.synthetic.main.fragment_delivery_starts.tvSatellite
import kotlinx.android.synthetic.main.fragment_delivery_starts.tvTime
import kotlinx.android.synthetic.main.fragment_delivery_starts.tvVehicleNumber
import kotlinx.android.synthetic.main.fragment_delivery_starts.tv_vehicleType
import kotlinx.android.synthetic.main.fragment_delivery_starts.tv_watch_earn
import kotlinx.android.synthetic.main.fragment_services.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import permissions.dispatcher.*

@RuntimePermissions
class DeliveryStartsFragment : BaseFragment(), DeliveryStartsContract.View {
    private val presenter = DeliveryStartsPresenter()

    private var line: Polyline? = null
    private var mMap: GoogleMap? = null
    private var order: Order? = null
    private var pathArray = ArrayList<String>()
    private var list = ArrayList<LatLng>()
    private var totalDistance = 0.0
    private var sourceLatLong: LatLng? = null
    private var d_lat = 0.0
    private var d_lng = 0.0
    private var destLong: LatLng? = null
    private var dialogStartRecording: Dialog? = null
    private var recordingDialogShown: Boolean = false

    /* Checks if all events are idle fo the time */
    private var checkIdleTimer = Timer()
    private var isPaused = false
    private var isTempTrackStatus = true
    private var isTempTrack = 0
    private var isHeavyLoadsVehicle = false
    var valueAnimatorMove: ValueAnimator? = null
    private var track: TrackingModel? = null
    private var panicDialog: Dialog? = null
    private lateinit var mediaProjectionManager: MediaProjectionManager

    private var longDistanceDialog: androidx.appcompat.app.AlertDialog? = null
    private var cancelRequestDialog: androidx.appcompat.app.AlertDialog? = null
    lateinit var deliveryStartsBinding: FragmentDeliveryStartsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        deliveryStartsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_delivery_starts, container, false)
        deliveryStartsBinding.color = ConfigPOJO.Companion
        val view = deliveryStartsBinding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)


        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE) {
            cvToolbar_header.visibility = View.VISIBLE
            rlHeader.visibility = View.GONE
        } else {
            cvToolbar_header.visibility = View.GONE
            rlHeader.visibility = View.VISIBLE
            tvPanic.background = StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour, GradientDrawable.RECTANGLE)
            tvPanic.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour))
        }

        if (ConfigPOJO.TerminologyData?.key_value?.panic_button == "0") {
            tvPanic.visibility = View.GONE
        }

        if (Constants.SECRET_DB_KEY == "a3f699d8c406daaf828f2d2c891eac15110024532f77f082ad972842c8516d60"
                || Constants.SECRET_DB_KEY == "fdd4d886d691305c703e8274bbd49ec217a41b4a1bc16322f1345c000a1987f5") {
            tvPanic.visibility = View.GONE
        }

        presenter.onGoingOrderApi()
        intialize()
        setListeners()

        if (getString(R.string.app_name) == "Wasila") {
            tvSatellite.visibility = View.VISIBLE
            tvMapNormal.visibility = View.VISIBLE
        }
    }

    private fun intialize() {
        (context as HomeActivity).serviceRequestModel.isDriverAccepted = true
        if ((context as HomeActivity).serviceRequestModel.isLongDistancePickup) {
            showLongDistancePickupAlert()
        }
    }

    private fun showLongDistancePickupAlert() {
        longDistanceDialog = AlertDialogUtil.getInstance().createOkCancelDialog(context, R.string.longDistance,
                R.string.longDistanceDesc,
                R.string.ok,
                R.string.cancel,
                false, object : AlertDialogUtil.OnOkCancelDialogListener {
            override fun onOkButtonClicked() {
                (context as HomeActivity).serviceRequestModel.isLongDistancePickup = false
                longDistanceDialog?.dismiss()
            }

            override fun onCancelButtonClicked() {
                (context as HomeActivity).serviceRequestModel.isLongDistancePickup = false
                checkCancelRideCondition()
            }
        })

        longDistanceDialog?.show()
    }

    override fun onResume() {
        super.onResume()
        isPaused = false
        if (AppSocket.get().isConnected) {
            restartCheckIdleTimer(0)
        }
        AppSocket.get().on(Events.ORDER_EVENT, orderEventListener)
        AppSocket.get().on(Socket.EVENT_CONNECT, onSocketConnected)
        AppSocket.get().on(Socket.EVENT_DISCONNECT, onSocketDisconnected)
    }

    override fun onPause() {
        super.onPause()
        isPaused = true
        checkIdleTimer.cancel()
        AppSocket.get().off(Events.ORDER_EVENT, orderEventListener)
        AppSocket.get().off(Socket.EVENT_CONNECT, onSocketConnected)
        AppSocket.get().off(Socket.EVENT_DISCONNECT, onSocketDisconnected)
    }

    override fun onDestroyView() {
        dialog?.cancel()
        AppSocket.get().off(Events.ORDER_EVENT)
        AppSocket.get().off(Events.COMMON_EVENT)
        presenter.detachView()
        super.onDestroyView()
    }

    private val onSocketConnected = Emitter.Listener {
        activity?.runOnUiThread {
            if (!isPaused) {
                restartCheckIdleTimer(0)
            }
        }
    }

    private val onSocketDisconnected = Emitter.Listener {
        checkIdleTimer.cancel()
    }

    private fun setupMap() {
        mMap = (activity as HomeActivity).googleMapHome
        if (mMap != null) {
            mMap?.clear()
        }

        val mapType = SharedPrefs.with(activity).getInt(PREF_MAP_VIEW, GoogleMap.MAP_TYPE_NORMAL)

        tvMapNormal.isSelected = mapType == GoogleMap.MAP_TYPE_NORMAL
        tvSatellite.isSelected = mapType == GoogleMap.MAP_TYPE_SATELLITE

        order = Gson().fromJson(arguments?.getString("order"), Order::class.java)
        sourceLatLong = LatLng(order?.driver?.latitude ?: 0.0, order?.driver?.longitude ?: 0.0)

        isHeavyLoadsVehicle = true
        if (order?.order_status == OrderStatus.CONFIRMED) {
            destLong = LatLng(order?.pickup_latitude ?: 0.0, order?.pickup_longitude ?: 0.0)
        } else {
            destLong = LatLng(order?.dropoff_latitude ?: 0.0, order?.dropoff_longitude ?: 0.0)
        }
        showMarker(sourceLatLong, destLong)
        reFocusMapCamera()
    }

    @SuppressLint("WrongConstant")
    private fun setListeners() {
        tvSatellite.setOnClickListener {
            tvMapNormal.isSelected = false
            tvSatellite.isSelected = true
            mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            SharedPrefs.with(activity).save(PREF_MAP_VIEW, GoogleMap.MAP_TYPE_SATELLITE)
        }

        tvMapNormal.setOnClickListener {
            tvMapNormal.isSelected = true
            tvSatellite.isSelected = false
            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            SharedPrefs.with(activity).save(PREF_MAP_VIEW, GoogleMap.MAP_TYPE_NORMAL)
        }

        fabMyLocation.setOnClickListener {
            reFocusMapCamera()
        }

        if (ConfigPOJO.is_merchant == "true") {
            tv_watch_earn.visibility = View.VISIBLE
        }

        tv_watch_earn.setOnClickListener {
            startActivity(Intent(activity!!, WatchStories::class.java))
        }

        tvCancel.setOnClickListener {
            checkCancelRideCondition()
        }
        ivDrawer.setOnClickListener {
            activity?.drawer_layout?.openDrawer(Gravity.START)
        }

        ivBack_snd.setOnClickListener {
            activity?.drawer_layout?.openDrawer(Gravity.START)
        }

        if (ConfigPOJO.TEMPLATE_CODE != Constants.SUMMER_APP_BASE) {
            rl_open_view.setOnTouchListener { view, motionEvent ->
                var usassigned = false
                if (SECRET_DB_KEY == "c0cb897ef919b1c32ee9f672d74421228890d20962b7623535f9ab29d3e9f88a" || SECRET_DB_KEY == "b25dcb3ed072a432e0b76634d9d7ae4d") {
                    if (order?.category_id == 13 || order?.category_id == 14 || order?.category_id == 17) {
                        usassigned = true
                    }
                }
                if (!usassigned) {
                    val fragment = DeliveryStartsDetails()
                    val bundle = Bundle()
                    bundle.putString("order", arguments?.getString("order"))
                    fragment.arguments = bundle
                    fragmentManager?.beginTransaction()?.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_top)?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                }
                false
            }
        }

        ivCallDriver.setOnClickListener {
            val phone = order?.driver?.phone_code.toString() + " " + order?.driver?.phone_number.toString()
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }



        ivChat.setOnClickListener {
            SharedPrefs.with(context).save(ORDER, order)
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(RECEIVER_ID, order?.driver?.user_id)
            intent.putExtra(USER_NAME, order?.driver?.name)
            intent.putExtra(PROFILE_PIC_URL, order?.driver?.profile_pic_url)
            intent.putExtra(USER_DETAIL_ID, order?.customer_user_detail_id)
            intent.putExtra(ORDER_ID, order?.order_id?.toString())
            startActivity(intent)
        }

        tvPanic.setOnClickListener {
            confirmPanicRequestDialog()
        }
    }

    private fun confirmPanicRequestDialog() {
        panicDialog = activity?.let { Dialog(it) }
        panicDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        panicDialog?.setCancelable(true)
        panicDialog?.setCanceledOnTouchOutside(true)
        panicDialog?.setContentView(R.layout.dialog_panic_swipe_accept_reject)
        panicDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val accept = panicDialog?.findViewById<LeftSwipeButtonView>(R.id.tvAccept)
        val reject = panicDialog?.findViewById<RightSwipeButtonView>(R.id.tvCancel)

        accept?.setSwipeBackgroundColor(Color.parseColor(ConfigPOJO.primary_color))

        accept?.setOnSwipeCompleteListener_forward_reverse(object : OnSwipeCompleteListener {
            override fun onSwipe_Forward(swipe_button_view: LeftSwipeButtonView?) {
                hitPanicApi()
            }

            override fun onSwipe_RejectForward(swipe_button_view: RightSwipeButtonView?) {
            }

            override fun onSwipe_Reverse(swipe_button_view: LeftSwipeButtonView?) {
            }

            override fun onSwipe_RejectReverse(swipe_button_view: RightSwipeButtonView?) {
                hitPanicApi()
            }
        })

        reject?.setOnSwipeCompleteListener_forward_reverse(object : OnSwipeCompleteListener {
            override fun onSwipe_Forward(swipe_button_view: LeftSwipeButtonView?) {
            }

            override fun onSwipe_RejectForward(swipe_button_view: RightSwipeButtonView?) {
                panicDialog?.dismiss()
            }

            override fun onSwipe_Reverse(swipe_button_view: LeftSwipeButtonView?) {
            }

            override fun onSwipe_RejectReverse(swipe_button_view: RightSwipeButtonView?) {
                panicDialog?.dismiss()
            }
        })

        panicDialog?.show()
        panicDialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun waitForDriverApproval() {
        cancelRequestDialog = AlertDialogUtil.getInstance().createOkCancelDialog(context,
                R.string.cancelRide,
                R.string.wait_for_driver_acceptance,
                0, 0, false, object : AlertDialogUtil.OnOkCancelDialogListener {
            override fun onOkButtonClicked() {
                cancelRequestDialog?.dismiss()
            }

            override fun onCancelButtonClicked() {
                cancelRequestDialog?.dismiss()
            }
        })
        cancelRequestDialog?.show()
    }

    private fun hitPanicApi() {
        if (CheckNetworkConnection.isOnline(context)) {
            try {
                (activity as HomeActivity).locationProvider.getAddressFromLatLng(d_lat
                        ?: 0.0, d_lng
                        ?: 0.0, object : LocationProvider.OnAddressListener {
                    override fun getAddress(address: String, result: List<Address>) {
                        val map = HashMap<String, Any>()
                        map["order_id"] = order?.order_id ?: 0
                        map["locationLong"] = d_lat ?: 0.0
                        map["locationLat"] = d_lng ?: 0.0
                        map["locationAddress"] = address
                        presenter.requestPanicApi(map)
                    }
                })

            } catch (exp: Exception) {
                val map = HashMap<String, Any>()
                map["order_id"] = order?.order_id ?: 0
                map["locationLong"] = d_lat ?: 0.0
                map["locationLat"] = d_lat ?: 0.0
                map["locationAddress"] = order?.pickup_address.toString()
                presenter.requestPanicApi(map)
            }


        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }


    override fun onUserPanicSuccess() {
        panicDialog?.dismiss()
        AppUtils.displayAlertWithAutoDismiss(context as Activity, getString(R.string.panicMsg), true)
    }

    private fun getMinEndRide(start: String, endTime: String): Int {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        val date1 = sdf.parse(start)
        val date2 = sdf.parse(endTime)
        val long = date2.time - date1.time
        val days = (long / (1000 * 60 * 60 * 24));
        val hours = ((long - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
        var min = (long - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)).toInt() / (1000 * 60)
        return min
    }

    private fun checkCancelRideCondition() {
        var acceptRideMills = ""
        acceptRideMills = order?.accepted_at ?: ""
        val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentDateAndTime: String = simpleDateFormat.format(Date())
        var time = acceptRideMills.split(" ").get(1)
        if (getMinEndRide(time, currentDateAndTime) > order?.brand?.cancellation_time ?: 0) {
            var alert: androidx.appcompat.app.AlertDialog? = null
            alert = AlertDialogUtil.getInstance().createOkCancelDialog(context,
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
    }


    fun longLogg(str: String) {
        if (str.length > 4000) {
            Log.d("zxzXXxxxxx212", str.substring(0, 4000))
            longLogg(str.substring(4000))
        } else Log.d("xsxxzxzx1212121", str)
    }

    // Socket Listener for orders events
    private val orderEventListener = Emitter.Listener { args ->
        restartCheckIdleTimer(5000)
        activity?.runOnUiThread {
            longLogg(args[0].toString())
            when (JSONObject(args[0].toString()).getString("type")) {
                OrderEventType.SERVICE_COMPLETE -> {
                    if (cancelRequestDialog != null) {
                        cancelRequestDialog?.dismiss()
                    }
                    val orderJson = JSONObject(args[0].toString()).getJSONArray("order").get(0).toString()
                    val orderModel = Gson().fromJson(orderJson, Order::class.java)
                    openInvoiceFragment(orderJson)
                }
                OrderStatus.DriverCancel -> {
                    if (cancelRequestDialog != null) {
                        cancelRequestDialog?.dismiss()
                    }
                    val orderJson = JSONObject(args[0].toString()).toString()
                    openInvoiceFragment(orderJson)
                }
                OrderEventType.VEHICLE_BREAK_DOWN -> {
                    val orderJson = JSONObject(args[0].toString()).getJSONArray("order").get(0).toString()
                    val fragment = ProcessingRequestFragment()
                    val bundle = Bundle()
                    bundle.putString("via", "vehicleBreakDown")
                    bundle.putString("action", "byDriver")
                    bundle.putString(ORDER, orderJson)
                    fragment.arguments = bundle
                    fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commitAllowingStateLoss()
                }

                OrderEventType.CURRENT_ORDERS -> {
                    track = Gson().fromJson(args[0].toString(), TrackingModel::class.java)
                            ?: null
                    if (track?.order_id == order?.order_id && !isPaused) {

                        sourceLatLong = LatLng(track?.latitude ?: 0.0, track?.longitude ?: 0.0)
                        presenter.getRoadPoints(track)
                        if (track?.order_status == OrderStatus.CONFIRMED) {
                            order?.order_status = OrderStatus.CONFIRMED
                            tvDriverStatus.text = getString(R.string.driver_accepted_request)
                        } else if (track?.order_status == OrderStatus.ONGOING || track?.order_status == OrderStatus.CustCancelReq) {
                            if (track?.stories != null) {
                                if (track?.stories!!.isNotEmpty()) {
                                    if (SharedPrefs.get().getString(STORIED_VIEWES, "") == "") {
                                        SharedPrefs.with(context).save(STORIED_VIEWES, STORIED_VIEWES)
                                        callWatchStoryScreen()
                                    }
                                }
                            }
                            order?.order_status = OrderStatus.ONGOING
                            if (ConfigPOJO.TEMPLATE_CODE != Constants.CORSA)
                                tvCancel.visibility = View.GONE
                            tvDriverStatus.text = getString(R.string.driver_is_on_the_way)
                            if (ConfigPOJO.settingsResponse?.key_value?.is_ride_recording == "true") {
                                showDialogForRecording()
                            }
                        } else if (track?.order_status == OrderStatus.REACHED) {
                            order?.order_status = OrderStatus.REACHED
                            tvDriverStatus.text = getString(R.string.driver_reached)
                        }
                    }
                }
                OrderEventType.HELPER_ACCEPT -> {

                    if (order?.helper != null && order?.helper is helper) {
                        val helper = order?.helper as helper
                        rlTopHelper.visibility = View.VISIBLE
                        ivHelperImage.setRoundProfileUrl(helper.profile_pic_url)
                        tvHelperName.text = helper?.firstName + " " + helper?.lastName + " (Helper)"
                    } else {
                        presenter.onGoingOrderApi()
                    }
                }
                OrderStatus.DRIVER_CANCELLED -> {
                    openServiceFragment()
                    Toast.makeText(activity, getString(R.string.ongoing_request_cancelled_by_driver), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun snappedPoints(response: List<RoadItem>?, trackingModel: TrackingModel?) {
        if ((response?.size ?: 0) > 0) {
            sourceLatLong = LatLng(response?.get(0)?.location?.latitude ?: 0.0,
                    response?.get(0)?.location?.longitude ?: 0.0)
            if (isHeavyLoadsVehicle && trackingModel?.order_status == OrderStatus.CONFIRMED ||
                    trackingModel?.order_status == OrderStatus.ONGOING) {
                if (trackingModel.order_status == OrderStatus.ONGOING) {
                    destLong = LatLng(order?.dropoff_latitude
                            ?: 0.0, order?.dropoff_longitude ?: 0.0)
                    destMarker?.position = LatLng(destLong?.latitude
                            ?: 0.0, destLong?.longitude ?: 0.0)
                    destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_nav)))
                    for (item in order?.ride_stops ?: ArrayList()) {
                        mMap?.addMarker(MarkerOptions().position(LatLng(item.latitude
                                ?: 0.0, item.longitude ?: 0.0))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pick_location)))
                                ?.setAnchor(0.5f, 0.5f)
                    }
                    if (isTempTrackStatus) {
                        isTempTrackStatus = false
                        isTempTrack = 0
                    }
                }

                if (order?.ride_stops?.isNotEmpty() == true) {
                    if (order?.order_status == OrderStatus.ONGOING) {
                        presenter.drawPolyLineWithWayPoints(order, LatLng(trackingModel.latitude
                                ?: 0.0, trackingModel.longitude ?: 0.0), LatLng(destLong?.latitude
                                ?: 0.0, destLong?.longitude ?: 0.0))
                    } else {
                        presenter.drawPolyLine(sourceLat = trackingModel.latitude
                                ?: 0.0, sourceLong = trackingModel.longitude ?: 0.0,
                                destLat = destLong?.latitude ?: 0.0, destLong = destLong?.longitude
                                ?: 0.0, language = Locale.US.language)

                    }
                } else {
                    presenter.drawPolyLine(sourceLat = trackingModel.latitude
                            ?: 0.0, sourceLong = trackingModel.longitude ?: 0.0,
                            destLat = destLong?.latitude ?: 0.0, destLong = destLong?.longitude
                            ?: 0.0, language = Locale.US.language)
                }
                if (sourceLatLong != null) {
                    animateMarker(sourceLatLong, trackingModel.bearing?.toFloat())
                }

                if (isTempTrack < 5) {
                    isTempTrack += 1
                    reFocusMapCamera()
                }
            }
        }
    }

    override fun onApiSuccess(response: List<Order>?) {
        if (response?.isNotEmpty() == true) {
            tvDriverName?.text = response[0].driver?.name
            if (response[0].driver?.rating_count ?: 0 > 0 && response[0].driver?.rating_avg?.toInt() ?: 0 != 0) {
                val rating = if ((response[0].driver?.rating_avg?.toInt() ?: 0) <= 5) {
                    response[0].driver?.rating_avg?.toInt()
                } else {
                    5
                }
                ratingBar.rating = (response[0].driver?.rating_avg ?: "0").toFloat()
                tvRatingCount.text = " · " + response.get(0).driver?.rating_count.toString()

                if (Constants.SECRET_DB_KEY == "df258aa6db00e79bc182bc5d9ce5e4f6") {
                    val stars: LayerDrawable = ratingBar.getProgressDrawable() as LayerDrawable
                    if ((response[0].driver?.rating_avg ?: "0").toInt() == 5) {
                        stars.getDrawable(2).setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP)
                    } else if ((response[0].driver?.rating_avg
                                    ?: "0").toInt() == 3 || (response[0].driver?.rating_avg
                                    ?: "0").toInt() == 4) {
                        stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP)
                    } else if ((response[0].driver?.rating_avg
                                    ?: "0").toInt() < 3 && (response[0].driver?.rating_avg
                                    ?: "0").toInt() > 0) {
                        stars.getDrawable(2).setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
                    }
                }
            } else {
                ratingBar.visibility = View.GONE
                tvRatingCount.visibility = View.GONE
            }
            order = response[0]
            setListeners()

            ivCallHelper.setOnClickListener {

                if (response[0]?.helper != null && response[0]?.helper is helper) {
                    val helper = response[0].helper as helper

                    val phone = helper?.phone_code.toString() + " " + helper.phone_number.toString()
                    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                }
            }


            tvFullOrderDetailss.setOnClickListener {
                val dialog = FullOrderDetailsFragment()
                val bundle = Bundle()
                bundle.putParcelable("orderDetails", order)
                dialog.arguments = bundle
                dialog.show(childFragmentManager, FullOrderDetailsFragment.TAG)
            }

            setupMap()

            if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
                if (response[0]?.order_status == OrderStatus.CustCancelReq) {
                    waitForDriverApproval()
                }
            }

            if (response[0]?.booking_flow == CategoryId.FREIGHT || ConfigPOJO.TEMPLATE_CODE == Constants.CORSA || order?.category_id == 4) {
                tvFullOrderDetailss?.visibility = View.VISIBLE
            }

            if (response[0]?.booking_flow == CategoryId.FREIGHT || order?.category_id == 4) {
                tvPanic.visibility = View.GONE
            }

            if (SECRET_DB_KEY == "c0cb897ef919b1c32ee9f672d74421228890d20962b7623535f9ab29d3e9f88a" || SECRET_DB_KEY == "b25dcb3ed072a432e0b76634d9d7ae4d") {
                if (order?.category_id == 13 || order?.category_id == 14 || order?.category_id == 17) {
                    tvCancel.visibility = View.GONE
                }
            }

            if (ConfigPOJO?.settingsResponse?.key_value?.is_helper_for_driver == "true") {

                if (response[0]?.helper != null && response[0]?.helper is helper) {

                    val helper = response[0].helper as helper
                    rlTopHelper.visibility = View.VISIBLE
                    ivHelperImage.setRoundProfileUrl(helper?.profile_pic_url)
                    tvHelperName.text = helper?.firstName + " " + helper?.lastName + " (Helper)"
                }
            }

            ivDriverImage.setRoundProfileUrl(BASE_IMAGE_URL + response[0].driver?.profile_pic)
            ivVehicleImage.setImageUrl(response[0].driver?.icon_image_url)

            if (ConfigPOJO.settingsResponse?.key_value?.user_otp_check == "true") {
                tvShareOtp.visibility = View.VISIBLE
                tvShareOtp.text = "OTP -  " + order?.otp
            }

            tvCancel.text = response[0].brand?.brand_name
            tv_vehicleType.text = response[0].brand?.name
            val details = StringBuilder("")
            if (response[0].driver?.vehicle_number != "") {
                details.append(getString(R.string.vehicle_no))
                details.append(response[0].driver?.vehicle_number ?: "")
                details.append("\n")
            }
            if (response[0].driver?.vehicle_color != "" && response[0].driver?.vehicle_color != null) {
                details.append(response[0].driver?.vehicle_color ?: "")
                details.append(",")
            }
            if (response[0].driver?.vehicle_model != "" && response[0].driver?.vehicle_model != null) {
                details.append(response[0].driver?.vehicle_model ?: "")
                details.append(",")
            }
            if (response[0].driver?.vehicle_brand != "" && response[0].driver?.vehicle_brand != null) {
                details.append(response[0].driver?.vehicle_brand ?: "")
                details.append(",")
            }
            tvVehicleNumber.text = details.toString()

            if (order?.order_status == OrderStatus.CONFIRMED) {
                tvDriverStatus.text = getString(R.string.driver_accepted_request)
                //  textView12.visibility = View.VISIBLE
                callWatchStoryScreen()
            } else if (order?.order_status == OrderStatus.ONGOING || order?.order_status == OrderStatus.CustCancelReq) {
                order?.order_status = OrderStatus.ONGOING
                if (ConfigPOJO.TEMPLATE_CODE != Constants.CORSA)
                    tvCancel.visibility = View.GONE
                tvDriverStatus.text = getString(R.string.driver_is_on_the_way)
                if (ConfigPOJO.settingsResponse?.key_value?.is_ride_recording == "true") {
                    showDialogForRecording()
                }
                callWatchStoryScreen()
            } else if (order?.order_status == OrderStatus.REACHED) {
                tvDriverStatus.text = getString(R.string.driver_reached)
                order?.order_status = OrderStatus.REACHED
                callWatchStoryScreen()
            }

            if (arguments?.getString("from") == "Notification") {
                SharedPrefs.with(context).save(ORDER, order)
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra(RECEIVER_ID, order?.driver?.user_id.toString())
                intent.putExtra(USER_NAME, order?.driver?.name)
                intent.putExtra(PROFILE_PIC_URL, order?.driver?.profile_pic_url)
                intent.putExtra(USER_DETAIL_ID, order?.customer_user_detail_id.toString())
                intent.putExtra(ORDER_ID, order?.order_id?.toString())
                startActivity(intent)
            }

        } else {
            fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            val fragment = ServicesFragment()
            (activity as HomeActivity).serviceRequestModel = ServiceRequestModel()
            (activity as HomeActivity).servicesFragment = fragment
            fragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
            Toast.makeText(activity, getString(R.string.order_cancelled), Toast.LENGTH_LONG).show()
        }
    }

    private fun callWatchStoryScreen() {
        if (ConfigPOJO?.is_merchant == "true") {
            startActivity(Intent(activity!!, WatchStories::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
    }

    override fun showLoader(isLoading: Boolean) {

    }

    override fun apiFailure() {
        rootView.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(activity as Activity)
        } else {
            rootView.showSnack(error.toString())
        }
    }

    override fun onCancelApiSuccess() {
        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            if (order?.order_status == OrderStatus.ONGOING) {
                waitForDriverApproval()
            } else {
                openServiceFragment()
            }
        } else {
            openServiceFragment()
        }
    }

    override fun polyLine(jsonRootObject: JSONObject) {
        line?.remove()
        val routeArray = jsonRootObject.getJSONArray("routes")
        if (routeArray.length() == 0) {
            return
        }
        var routes: JSONObject?
        routes = routeArray.getJSONObject(0)
        val overviewPolylines = routes.getJSONObject("overview_polyline")
        val encodedString = overviewPolylines.getString("points")
        pathArray.add(encodedString)
        list = decodePoly(encodedString)
        val listSize = list.size
        sourceLatLong?.let { list.add(0, it) }
        destLong?.let { list.add(listSize + 1, it) }
        line = mMap?.addPolyline(PolylineOptions()
                .addAll(list)
                .width(8f)
                .color(Color.parseColor(ConfigPOJO.primary_color))
                .geodesic(true))
        /* To calculate the estimated distance*/
        val estimatedTime = (((routes?.get("legs") as JSONArray).get(0) as JSONObject).get("duration") as JSONObject).get("text") as String
        tvTime.text = estimatedTime
        destMarker?.title = String.format("%s", estimatedTime)
        destMarker?.showInfoWindow()

    }

    private fun reFocusMapCamera() {
        val builder = LatLngBounds.Builder()
        val arr = ArrayList<Marker?>()
        arr.add(carMarker)
        arr.add(destMarker)
        for (marker in arr) {
            builder.include(marker?.position)
        }
        val bounds = builder.build()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds,
                activity?.let { Utils.getScreenWidth(it) } ?: 0,
                activity?.let { Utils.getScreenWidth(it) }?.minus(Utils.dpToPx(24).toInt())
                        ?: 0, Utils.dpToPx(56).toInt())
        mMap?.animateCamera(cu)
    }

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

    var carMarker: Marker? = null
    var destMarker: Marker? = null

    private fun showMarker(sourceLatLong: LatLng?, destLong: LatLng?) {
        /*add marker for both source and destination*/
        carMarker = mMap?.addMarker(this.sourceLatLong?.let { MarkerOptions().position(it) })
        carMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_car_cab)))
        if (ConfigPOJO.is_asap == "true") {
            when (order?.category_brand_id) {
                24 -> {
                    carMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_auto_icon)))
                }
                25 -> {
                    carMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_car_icon)))
                }
                23 -> {
                    carMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_bike_icon)))
                }
                27 -> {
                    carMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_cycle_icon)))
                }
                else -> {
                    carMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_car_icon)))
                }
            }
        }

        if (Constants.SECRET_DB_KEY == "eecf5a33e8575b1a860cc17dd778ea6f" ||
                Constants.SECRET_DB_KEY == "456049b71e28127ccd109b3fa9392fdb") {
            when (order?.category_id) {
                4 -> {
                    carMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_del)))
                }
                7 -> {
                    carMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_bike)))
                }
                12 -> {
                    carMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_tri)))
                }
                14 -> {
                    carMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_auto)))
                }
                else -> {
                    carMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_car_cab)))
                }
            }
        }
        carMarker?.isFlat = true
        carMarker?.setAnchor(0.5f, 0.5f)
        destMarker = mMap?.addMarker(this.destLong?.let { MarkerOptions().position(it) })
        if (isHeavyLoadsVehicle && order?.order_status == OrderStatus.CONFIRMED) {
            destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_nav)))
        } else {
            destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_nav)))
        }

        if (order?.ride_stops != null) {
            for (item in order?.ride_stops!!.indices) {

                val sourceText = TextView(activity)
                sourceText.setShadowLayer(16f, 16f, 16f, ContextCompat.getColor(activity as HomeActivity, R.color.white))
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


                val iconGenerator = IconGenerator(activity)
                iconGenerator.setContentView(sourceText)
                iconGenerator.setBackground(ContextCompat.getDrawable(activity as HomeActivity, android.R.color.transparent))

                mMap?.addMarker(MarkerOptions().position(LatLng(order?.ride_stops?.get(item)?.latitude
                        ?: 0.0, order?.ride_stops?.get(item)?.longitude ?: 0.0))
                        .icon((BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))))
                        ?.setAnchor(0.5f, 0.5f)
            }
        }


        this.sourceLatLong?.latitude?.let {
            this.sourceLatLong?.longitude?.let { it1 ->
                this.destLong?.latitude?.let { it2 ->
                    this.destLong?.longitude?.let { it3 ->
                        if (order?.ride_stops?.isNotEmpty() == true) {
                            if (order?.order_status == OrderStatus.ONGOING) {
                                presenter.drawPolyLineWithWayPoints(order, LatLng(it, it1), LatLng(it2, it3))
                            } else {
                                presenter.drawPolyLine(sourceLat = it, sourceLong = it1,
                                        destLat = it2, destLong = it3, language = Locale.US.language)
                            }
                        } else {
                            presenter.drawPolyLine(sourceLat = it, sourceLong = it1,
                                    destLat = it2, destLong = it3, language = Locale.US.language)
                        }
                    }
                }
            }
        }
    }

    fun animateMarker(latLng: LatLng?, bearing: Float?) {
        if (carMarker != null) {
            val startPosition = carMarker?.position
            val endPosition = LatLng(latLng?.latitude ?: 0.0, latLng?.longitude ?: 0.0)
            val startRotation = carMarker?.rotation
            val latLngInterpolator = LatLngInterpolator.LinearFixed()
            valueAnimatorMove?.cancel()
            valueAnimatorMove = ValueAnimator.ofFloat(0f, 1f)
            valueAnimatorMove?.duration = 1000 // duration 1 second
            valueAnimatorMove?.interpolator = LinearInterpolator()
            valueAnimatorMove?.addUpdateListener { animation ->
                try {
                    val v = animation.animatedFraction
                    val newPosition = latLngInterpolator.interpolate(v, startPosition
                            ?: LatLng(0.0, 0.0), endPosition)
                    carMarker?.position = newPosition
                    //                        carMarker?.setRotation(computeRotation(v, startRotation?:0f, currentBearing))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            valueAnimatorMove?.start()
            rotateMarker(latLng, bearing)
        }
    }

    var valueAnimatorRotate: ValueAnimator? = null
    private fun rotateMarker(latLng: LatLng?, bearing: Float?) {
        if (carMarker != null) {
            val startPosition = carMarker?.position
            val endPosition = LatLng(latLng?.latitude ?: 0.0, latLng?.longitude ?: 0.0)

            val latLngInterpolator = LatLngInterpolator.LinearFixed()
            valueAnimatorRotate?.cancel()
            val startRotation = carMarker?.rotation
            valueAnimatorRotate = ValueAnimator.ofFloat(0f, 1f)
            valueAnimatorRotate?.duration = 5000 // duration 1 second
            valueAnimatorRotate?.interpolator = LinearInterpolator()
            valueAnimatorRotate?.addUpdateListener { animation ->
                try {
                    val v = animation.animatedFraction
                    val newPosition = latLngInterpolator.interpolate(v, startPosition
                            ?: LatLng(0.0, 0.0), endPosition)
                    //                        carMarker?.setPosition(newPosition)
                    carMarker?.rotation = computeRotation(v, startRotation ?: 0f, bearing
                            ?: 0f)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            valueAnimatorRotate?.start()

        }
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
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }

    private var startRotation = 0f


    private var dialog: Dialog? = null
    private fun showCancellationDialog() {
        dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setContentView(R.layout.dialog_cancel_reason)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        val etMessage = dialog?.findViewById(R.id.etMessage) as EditText
        val tvSubmit = dialog?.findViewById(R.id.tvSubmit) as TextView
        tvSubmit.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

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

        tvSubmit.setOnClickListener {

            if (cancelGroup?.checkedRadioButtonId.toString().trim().isNotEmpty()) {
                val selectedRdBtn = dialog?.findViewById<RadioButton>(cancelGroup!!.checkedRadioButtonId)

                val fullTrackList = fullTrackArray
                totalDistance = 0.0
                for (i in fullTrackList.indices) {
                    if (i == fullTrackList.size.minus(1)) {
                        totalDistance += MapUtils.getDistanceBetweenTwoPointsCancel(LatLng(fullTrackList[i].latitude, fullTrackList[i].longitude), LatLng(fullTrackList[i].latitude, fullTrackList[i].longitude))
                    } else {
                        totalDistance += MapUtils.getDistanceBetweenTwoPointsCancel(LatLng(fullTrackList[i].latitude, fullTrackList[i].longitude), LatLng(fullTrackList[i + 1].latitude, fullTrackList[i + 1].longitude))
                    }
                    Log.e("distance : ", totalDistance.toString())
                }

                val map = HashMap<String, String>()
                map["order_id"] = order?.order_id.toString()
                map["cancel_reason"] = (selectedRdBtn)?.text.toString().trim()
                map["order_distance"] = totalDistance.toString()

                if (btn4?.isChecked == true) {
                    if (etMessage?.text.toString().trim().isEmpty()) {
                        activity?.let {
                            Toast.makeText(it, getString(R.string.cancellation_reason_validation_text), Toast.LENGTH_SHORT).show()
                        }
                        return@setOnClickListener
                    }
                    map["cancel_reason"] = etMessage?.text.toString().trim()
                }
                if (CheckNetworkConnection.isOnline(activity)) {
                    presenter.requestCancelApiCall(map)
                    dialog?.dismiss()
                } else {
                    CheckNetworkConnection.showNetworkError(rootView)
                }
            } else {
                activity?.let { Toast.makeText(it, getString(R.string.cancellation_reason_validation_text), Toast.LENGTH_SHORT).show() }
            }
        }

        dialog?.show()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun restartCheckIdleTimer(checkIdleTimeInterval: Long) {
        checkIdleTimer.cancel()
        checkIdleTimer = Timer()
        checkIdleTimer.schedule(object : TimerTask() {
            override fun run() {
                if (CheckNetworkConnection.isOnline(activity)) {
                    checkOrderStatus()
                }
                restartCheckIdleTimer(5000L)
            }
        }, checkIdleTimeInterval)
    }

    fun longLog(str: String) {
        if (str.length > 4000) {
            Log.d("zxzXX", str.substring(0, 4000))
            longLog(str.substring(4000))
        } else Log.d("xsxxzxzx", str)
    }

    private fun checkOrderStatus() {
        val request = JSONObject()
        request.put("type", OrderEventType.CUSTOMER_SINGLE_ORDER)
        request.put("access_token", ACCESS_TOKEN)
        request.put("order_token", order?.order_token)
        AppSocket.get().emit(Events.COMMON_EVENT, request, Ack {
            val response = Gson().fromJson<ApiResponse<Order>>(it[0].toString(),
                    object : TypeToken<ApiResponse<Order>>() {}.type)
            System.out.println(it[0].toString())
            if (response?.statusCode == SUCCESS_CODE) {
                val orderModel = response.result
                val orderJson = JSONObject(it[0].toString()).getString("result")
                longLog(orderJson)
                System.out.println(orderJson)
                activity?.runOnUiThread {
                    when (orderModel?.order_status) {
                        OrderStatus.SERVICE_COMPLETE, OrderStatus.DriverCancel -> {
                            if (orderModel.booking_type != "Breakdown") {
                                if (cancelRequestDialog != null) {
                                    cancelRequestDialog?.dismiss()
                                }
                                openInvoiceFragment(orderJson)
                            }
                        }

                        OrderStatus.REACHED -> {
                            if (tvDriverStatus != null) {
                                order?.order_status = OrderStatus.REACHED
                                tvDriverStatus.text = getString(R.string.driver_reached)
                                order?.order_status = OrderStatus.REACHED
                            }
                        }

                        OrderStatus.ONGOING -> {
                            if (tvDriverStatus != null) {
                                if (ConfigPOJO.TEMPLATE_CODE != Constants.CORSA)
                                    tvCancel.visibility = View.GONE
                                tvDriverStatus.text = getString(R.string.driver_is_on_the_way)
                                order?.order_status = OrderStatus.ONGOING
                                if (ConfigPOJO.settingsResponse?.key_value?.is_ride_recording == "true") {
                                    showDialogForRecording()
                                }
                                fullTrackArray.clear()
                                val resultJsonObj = JSONObject(it[0].toString()).getJSONObject("result")
                                if (resultJsonObj.has("order_request")) {
                                    val cRequestJson = resultJsonObj.getJSONObject("order_request")
                                    d_lat = cRequestJson.getDouble("driver_current_latitude")
                                    d_lng = cRequestJson.getDouble("driver_current_longitude")
                                    val fullTrackJsonString = cRequestJson.getString("full_track")
                                    val jsonArray = JSONArray(fullTrackJsonString)
                                    for (i in 0 until jsonArray.length()) {
                                        val jsonObject = jsonArray.getJSONObject(i)
                                        fullTrackArray.add(LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")))
                                    }
                                    Log.e("full : ", fullTrackArray.size.toString())
                                }
                            }
                        }

                        OrderStatus.DRIVER_CANCELLED -> {
                            openServiceFragment()
                            Toast.makeText(activity, getString(R.string.ongoing_request_cancelled_by_driver), Toast.LENGTH_LONG).show()
                        }
                        OrderEventType.HELPER_ACCEPT -> {
                            if (order?.helper != null && order?.helper is helper) {
                                val helper = order?.helper as helper
                                rlTopHelper.visibility = View.VISIBLE
                                ivHelperImage.setRoundProfileUrl(helper.profile_pic_url)
                                tvHelperName.text = helper?.firstName + " " + helper?.lastName + " (Helper)"
                            } else {
                                presenter.onGoingOrderApi()
                            }
                        }
                        OrderStatus.CUSTOMER_CANCEL -> {
                            openServiceFragment()
                            Toast.makeText(activity, getString(R.string.request_was_cancelled), Toast.LENGTH_LONG).show()
                        }

                        else -> {
                            Logger.e("CUSTOMER_SINGLE_ORDER", "This status not handeled :" + orderModel?.order_status)

                        }
                    }
                }
            } else if (response.statusCode == StatusCode.UNAUTHORIZED) {
                AppUtils.logout(activity)
            }
        })
    }

    private fun openInvoiceFragment(orderJson: String?) {
        var order = Gson().fromJson(orderJson, Order::class.java)
        if (Constants.SECRET_DB_KEY == "c0cb897ef919b1c32ee9f672d74421228890d20962b7623535f9ab29d3e9f88a" || Constants.SECRET_DB_KEY == "b25dcb3ed072a432e0b76634d9d7ae4d") {
            if (order?.category_id == 13 || order?.category_id == 14 || order?.category_id == 17) {
                Toast.makeText(activity, "Ongoing order completed", Toast.LENGTH_LONG).show()
                activity!!.finish()
                startActivity(Intent(activity!!, HomeActivity::class.java))
            } else {
                invoiceData(orderJson)
            }
        } else {
            invoiceData(orderJson)
        }
    }

    private fun invoiceData(orderJson: String?) {
        if (ConfigPOJO.gateway_unique_id == "payku"
                || ConfigPOJO.gateway_unique_id.toLowerCase() == "qpaypro"
                || ConfigPOJO.gateway_unique_id.toLowerCase() == "wipay") {

            if (order?.payment?.payment_type == "Card") {
                val fragment = PaymentFragment()
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
        } else {
            val fragment = CompleteRideFragment()
            val bundle = Bundle()
            bundle.putString("order", orderJson)
            fragment.arguments = bundle
            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commitAllowingStateLoss()
        }
    }

    private fun openServiceFragment() {
        if (activity?.isFinishing == false) {
            (context as HomeActivity).serviceRequestModel.pkgData = null
            fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            val fragment = ServicesFragment()
            (activity as? HomeActivity)?.servicesFragment = fragment
            fragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commitAllowingStateLoss()
        }
    }

    override fun onVehiceBreakDownSuccess() {
        /* Not Used Here */
    }

    override fun onHalfWayStopByUserSuccess() {
        /* Not Used Here */
    }

    override fun onRideShareSuccess(data: RideShareResponse) {
        /* Not Used Here */
    }

    override fun onCancelRideShareSuccess() {
        /* Not Used Here */
    }

    override fun onNetworkConnected() {

    }

    override fun onNetworkDisconnected() {

    }

    private fun showDialogForRecording() {
        dialogStartRecording = Dialog(activity!!)
        dialogStartRecording = AlertDialogUtil.getInstance()?.createOkCancelDialog(activity,
                R.string.start_recording, R.string.want_to_start_recording, R.string.yes, R.string.no, true, object : AlertDialogUtil.OnOkCancelDialogListener {
            override fun onOkButtonClicked() {
                recordingDialogShown = true
                dialogStartRecording!!.dismiss()
                startCapturingWithPermissionCheck()
            }

            override fun onCancelButtonClicked() {
                recordingDialogShown = true
                dialogStartRecording!!.dismiss()
            }
        })

        try {
            if (!recordingDialogShown) {
                dialogStartRecording?.show()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    fun startCapturing() {
        startMediaProjectionRequest()
    }

    @OnNeverAskAgain(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(ivDriverImage.context,
                R.string.permission_required_to_record_the_audio)
    }

    @OnShowRationale(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(ivDriverImage.context, R.string.permission_required_to_record_the_audio, request)
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun stopCapturing() {
        activity!!.startService(Intent(activity!!, AudioCaptureService::class.java).apply {
            action = AudioCaptureService.ACTION_STOP
        })
    }

    private fun startMediaProjectionRequest() {
        mediaProjectionManager =
                activity!!.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                MEDIA_PROJECTION_REQUEST_CODE
        )
    }

    companion object {
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 42
        private const val MEDIA_PROJECTION_REQUEST_CODE = 13
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(
                        activity!!,
                        "MediaProjection permission obtained. Foreground service will be started to capture audio.",
                        Toast.LENGTH_SHORT
                ).show()

                val audioCaptureIntent = Intent(activity!!, AudioCaptureService::class.java).apply {
                    action = AudioCaptureService.ACTION_START
                    putExtra(AudioCaptureService.EXTRA_RESULT_DATA, data!!)
                }
                activity!!.startForegroundService(audioCaptureIntent)
            } else {
                Toast.makeText(
                        activity!!, "Request to obtain MediaProjection denied.",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}