package com.trava.user.ui.home.deliverystarts.details

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.location.Address
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hbb20.CountryCodePicker
import com.trava.user.R
import com.trava.user.databinding.FragmentDeliveryStartsDetailsBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.HomeActivity.Companion.fullTrackArray
import com.trava.user.ui.home.chatModule.chatMessage.ChatActivity
import com.trava.user.ui.home.complete_ride.CompleteRideFragment
import com.trava.user.ui.home.deliverystarts.DeliveryStartsContract
import com.trava.user.ui.home.deliverystarts.DeliveryStartsFragment
import com.trava.user.ui.home.deliverystarts.DeliveryStartsPresenter
import com.trava.user.ui.home.deliverystarts.RideShareAdapter
import com.trava.user.ui.menu.emergencyContacts.contacts.ContactsActivity
import com.trava.user.ui.menu.emergencyContacts.contacts.ContactsAdapter
import com.trava.user.ui.menu.emergencyContacts.contacts.ContactsInterface
import com.trava.user.ui.home.deliverystarts.paymentview.PaymentFragment
import com.trava.user.ui.home.dropofflocation.SetupDropOffLocationFragment
import com.trava.user.ui.home.processingrequest.ProcessingRequestFragment
import com.trava.user.ui.home.services.ServicesFragment
import com.trava.user.utils.*
import com.trava.user.utils.PermissionUtils
import com.trava.user.utils.swipeButton.Controller.OnSwipeCompleteListener
import com.trava.user.utils.swipeButton.View.LeftSwipeButtonView
import com.trava.user.utils.swipeButton.View.RightSwipeButtonView
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.TrackingModel
import com.trava.user.webservices.models.contacts.*
import com.trava.user.webservices.models.nearestroad.RoadItem
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.*
import com.trava.utilities.constants.ACCESS_TOKEN
import com.trava.utilities.constants.ORDER
import com.trava.utilities.constants.SUCCESS_CODE
import com.trava.utilities.location.LocationProvider
import com.trava.utilities.webservices.BaseRestClient.Companion.BASE_IMAGE_URL
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.fragment_delivery_starts.*
import kotlinx.android.synthetic.main.fragment_delivery_starts_details.*
import kotlinx.android.synthetic.main.fragment_delivery_starts_details.ivCallDriver
import kotlinx.android.synthetic.main.fragment_delivery_starts_details.ivDriverImage
import kotlinx.android.synthetic.main.fragment_delivery_starts_details.ivVehicleImage
import kotlinx.android.synthetic.main.fragment_delivery_starts_details.ratingBar
import kotlinx.android.synthetic.main.fragment_delivery_starts_details.rootView
import kotlinx.android.synthetic.main.fragment_delivery_starts_details.tvDriverName
import kotlinx.android.synthetic.main.fragment_delivery_starts_details.tvPanic
import kotlinx.android.synthetic.main.fragment_delivery_starts_details.tvRatingCount
import kotlinx.android.synthetic.main.fragment_delivery_starts_details.tvTime
import kotlinx.android.synthetic.main.fragment_delivery_starts_details.tvVehicleNumber
import kotlinx.android.synthetic.main.fragment_delivery_starts_details.tv_vehicleType
import org.json.JSONArray
import org.json.JSONObject
import permissions.dispatcher.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@RuntimePermissions
class DeliveryStartsDetails : BaseFragment(), View.OnClickListener, DeliveryStartsContract.View, ContactsInterface {
    private val presenter = DeliveryStartsPresenter()
    private var dialogIndeterminate: DialogIndeterminate? = null
    private var order: Order? = null

    private var sourceLatLong: LatLng? = null

    private var destLong: LatLng? = null

    private var line: Polyline? = null

    private var mMap: GoogleMap? = null

    private var pathArray = ArrayList<String>()

    private var list = ArrayList<LatLng>()

    private var isHeavyLoadsVehicle = false

    var valueAnimatorMove: ValueAnimator? = null

    private lateinit var locationProvider: LocationProvider

    private var serviceRequestModel: ServiceRequestModel? = null

    private var cancelDialog: Dialog? = null

    private var shareRideDialog: Dialog? = null

    private var isHalfWayOrBreakDown = false

    private var contactsRv: RecyclerView? = null

    private var adapter: ContactsAdapter? = null

    private var rideShareAdapter: RideShareAdapter? = null
    private var totalDistance = 0.0

    private var selectedContactsList = ArrayList<ContactModel>()

    private var phoneNumber: EditText? = null
    private var phoneNumberRl: RelativeLayout? = null

    private var countryCodePicker: CountryCodePicker? = null

    private var tvDriverStatus: TextView? = null


    /* Checks if all events are idle for the time */
    private var checkIdleTimer = Timer()

    private var isPaused = false

    private var track: TrackingModel? = null

    private var panicDialog: Dialog? = null

    private var orderString = ""
    private var shareLoader = false
    lateinit var deliveryStartsBindingImpl: FragmentDeliveryStartsDetailsBinding

    private val ratingDrawables = listOf(R.drawable.ic_angry_smile,
            R.drawable.ic_2,
            R.drawable.ic_3,
            R.drawable.ic_4,
            R.drawable.ic_5)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        deliveryStartsBindingImpl = DataBindingUtil.inflate(inflater, R.layout.fragment_delivery_starts_details, container, false)
        deliveryStartsBindingImpl.color = ConfigPOJO.Companion
        val view = deliveryStartsBindingImpl.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        tvDriverStatus = view.findViewById(R.id.tvDriverStatus)
        dialogIndeterminate = DialogIndeterminate(requireActivity())
        tvPanic.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

        intialize()
        setListener()
    }

    private fun intialize() {
        locationProvider = LocationProvider.CurrentLocationBuilder(activity, this).build()
        serviceRequestModel = (context as HomeActivity).serviceRequestModel
        orderString = arguments?.getString("order") ?: ""
        order = Gson().fromJson(orderString, Order::class.java)

        if (order?.category_id == 4) {
            tv_header.text = getString(R.string.delivery_in_progress)
            tvRideShare.text = getString(R.string.share_parcel_location)
        }

        if (ConfigPOJO.TerminologyData?.key_value?.breakdown_stop == "0") {
            tvBreakDown.visibility = View.GONE
        }
        if (ConfigPOJO.TerminologyData?.key_value?.halfway_ride_stop == "0") {
            tvHalfWayStop.visibility = View.GONE
        }
        if (ConfigPOJO.TerminologyData?.key_value?.panic_button == "0") {
            tvPanic.visibility = View.GONE
        }

        if(Constants.SECRET_DB_KEY =="a3f699d8c406daaf828f2d2c891eac15110024532f77f082ad972842c8516d60"
                || Constants.SECRET_DB_KEY == "fdd4d886d691305c703e8274bbd49ec217a41b4a1bc16322f1345c000a1987f5"||
                ConfigPOJO?.settingsResponse?.key_value?.is_helper_for_driver == "true")
        {
            tvPanic.visibility = View.GONE
            tvBreakDown.visibility = View.GONE
            tvHalfWayStop.visibility = View.GONE
        }

        setRideShareAdapter()
        presenter.onGoingOrderApi()
        tvDriverName?.text = "....."
        tv_vehicleType?.text = "....."
        tvVehicleNumber?.text = "....."

        (context as HomeActivity).serviceRequestModel.isDriverAccepted = true
    }

    private fun setRideShareAdapter() {
        rideShareAdapter = RideShareAdapter()
        rvRideShare.adapter = rideShareAdapter
    }

    private fun setListener() {
        ivBack.setOnClickListener(this)
        tvBreakDown.setOnClickListener(this)
        tvHalfWayStop.setOnClickListener(this)
        tvRideShare.setOnClickListener(this)
        tvCancelRide.setOnClickListener(this)
        ivCallDriver.setOnClickListener(this)
        ivSupport.setOnClickListener(this)
        tvPanic.setOnClickListener(this)
        tvCancelSharing.setOnClickListener(this)
        tvEditChange.setOnClickListener(this)
//        rvRideShare.visibility = View.GONE
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivBack -> {
                openBookingConfirmedFragment(orderString)
            }

            R.id.tvBreakDown -> {
                if (order?.order_status == OrderStatus.ONGOING) {
                    showCancellationDialog("vehicleBreakDown")
                } else {
                    rootView?.showSnack(getString(R.string.rideNotStarted))
                }
            }

            R.id.tvHalfWayStop -> {
                if (order?.order_status == OrderStatus.ONGOING) {
                    showCancellationDialog("halfWayStop")
                } else {
                    rootView?.showSnack(getString(R.string.rideNotStarted))
                }
            }

            R.id.tvRideShare -> {
                if (order?.order_status == OrderStatus.ONGOING) {
                    selectedContactsList.clear()
                    if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
                        setUpRideShareDialog()
                    } else {
                        val req = ShareRideReqModel()
                        req.order_id = order?.order_id
                        req.shareWith = Gson().toJson(list)
                        shareLoader=true
                        presenter.requestRideShareApiCall(req)
                    }
                } else {
                    rootView?.showSnack(getString(R.string.rideNotStarted))
                }
            }

            R.id.tvCancelRide -> {
                checkCancelRideCondition()
            }

            R.id.ivCallDriver -> {
                val phone = order?.driver?.phone_code.toString()+" "+order?.driver?.phone_number.toString()
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            }

            R.id.ivSupport -> {
                SharedPrefs.with(context).save(ORDER, order)
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra(RECEIVER_ID, order?.driver?.user_id.toString())
                intent.putExtra(USER_NAME, order?.driver?.name)
                intent.putExtra(PROFILE_PIC_URL, order?.driver?.profile_pic_url)
                intent.putExtra(USER_DETAIL_ID, order?.customer_user_detail_id.toString())
                intent.putExtra(ORDER_ID, order?.order_id?.toString())
                startActivity(intent)
            }

            R.id.tvPanic -> {
                confirmPanicRequestDialog()
//                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "112", null))
//                tvPanic.context?.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }

            R.id.tvCancelSharing -> {
                if (CheckNetworkConnection.isOnline(context)) {
                    var alert: AlertDialog? = null
                    alert = AlertDialogUtil.getInstance().createOkCancelDialog(context, R.string.cancelSharing,
                            R.string.are_you_sure_cancleShare,
                            R.string.ok,
                            R.string.cancel, true, object : AlertDialogUtil.OnOkCancelDialogListener {
                        override fun onOkButtonClicked() {
                            order?.order_id?.let { presenter.requestCancelRideShareApiCall(it) }
                        }

                        override fun onCancelButtonClicked() {
                            alert?.dismiss()
                        }
                    })
                    alert?.show()
                } else {
                    CheckNetworkConnection.showNetworkError(rootView)
                }
            }

            R.id.tvEditChange -> {
                openSetDropOffFragment()
            }
        }
    }

    private fun openSetDropOffFragment() {
        val fragment = SetupDropOffLocationFragment()
        val bundle = Bundle()
        bundle.putDouble(Constants.LATITUDE, serviceRequestModel?.pickup_latitude ?: 0.0)
        bundle.putDouble(Constants.LONGITUDE, serviceRequestModel?.pickup_longitude ?: 0.0)
        bundle.putString(Constants.ADDRESS, serviceRequestModel?.pickup_address)
        bundle.putString(Constants.BOOKING_TYPE, serviceRequestModel?.bookingType)
        bundle.putString("order", orderString)
        fragment.arguments = bundle
        fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
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
        panicDialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }


    private fun hitPanicApi() {
        if (CheckNetworkConnection.isOnline(context)) {
            try {
                (activity as HomeActivity).locationProvider.getAddressFromLatLng(track?.latitude
                        ?: 0.0, track?.longitude
                        ?: 0.0, object : LocationProvider.OnAddressListener {
                    override fun getAddress(address: String, result: List<Address>) {
                        val map = HashMap<String, Any>()
                        map["order_id"] = order?.order_id ?: 0
                        map["locationLong"] = track?.latitude ?: 0.0
                        map["locationLat"] = track?.longitude ?: 0.0
                        map["locationAddress"] = address
                        presenter.requestPanicApi(map)
                    }

                })

            } catch (exp: Exception) {
                val map = HashMap<String, Any>()
                map["order_id"] = order?.order_id ?: 0
                map["locationLong"] = track?.latitude ?: 0.0
                map["locationLat"] = track?.longitude ?: 0.0
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
        acceptRideMills = if (track != null) {
            track?.accepted_at ?: ""
        } else {
            order?.accepted_at ?: ""
        }

        val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentDateAndTime: String = simpleDateFormat.format(Date())
        var time = acceptRideMills.split(" ").get(1)
        if (getMinEndRide(time, currentDateAndTime) >= order?.brand?.cancellation_time ?: 0) {
            var alert: AlertDialog? = null
            alert = AlertDialogUtil.getInstance().createOkCancelDialog(context,
                    R.string.cancelRide,
                    R.string.cancelRideDesc,
                    R.string.ok,
                    R.string.cancel, false, object : AlertDialogUtil.OnOkCancelDialogListener {
                override fun onOkButtonClicked() {
                    showCancellationDialog("cancelRide")
                }

                override fun onCancelButtonClicked() {
                    alert?.dismiss()
                }
            })
            if (!alert.isShowing) {
                alert.show()
            }

        } else {
            showCancellationDialog("cancelRide")
        }
    }

    private fun setUpRideShareDialog() {
        shareRideDialog = activity?.let { Dialog(it) }
        shareRideDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        shareRideDialog?.setCancelable(true)
        shareRideDialog?.setCanceledOnTouchOutside(true)
        val layoutDialog = View.inflate(context, R.layout.fragment_ride_share, null)
        shareRideDialog?.setContentView(layoutDialog)
        shareRideDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        shareRideDialog?.setContentView(layoutDialog)
        phoneNumber = (layoutDialog.findViewById<EditText>(R.id.etPhone))
        phoneNumberRl = (layoutDialog.findViewById<RelativeLayout>(R.id.rl_phonenumber))
        countryCodePicker = (layoutDialog.findViewById<CountryCodePicker>(R.id.countryCodePicker))
        val btnContinue = (layoutDialog.findViewById<TextView>(R.id.tvShareRide))
        val cvCountry = (layoutDialog.findViewById<LinearLayout>(R.id.cvCountry))
        contactsRv = (layoutDialog).findViewById<MaxHeightRecyclerView>(R.id.rvContacts)

        btnContinue?.setBackgroundColor(Color.parseColor(ConfigPOJO.primary_color));
        phoneNumberRl!!.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        cvCountry.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)

        /* set up contacts adapter */
        contactsRv?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ContactsAdapter()
        contactsRv?.adapter = adapter

        /* listeners */

        (layoutDialog.findViewById<ImageView>(R.id.ivAddContacts)).setOnClickListener {
            getUserContactsWithPermissionCheck()
        }

        phoneNumber?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (phoneNumber?.text.toString().isNotEmpty()) {
                    (layoutDialog.findViewById<ImageView>(R.id.ivAddContacts)).hide()
                } else {
                    (layoutDialog.findViewById<ImageView>(R.id.ivAddContacts)).show()
                }
            }
        })

        btnContinue.setOnClickListener {
            /* Share Ride Api hit here */
            if (CheckNetworkConnection.isOnline(context)) {
                if (selectedContactsList.isNotEmpty()) {
                    /* User Pick contacts from contact list */

                    val list = ArrayList<ContactRequestModel>()
                    for (item in selectedContactsList) {
                        val model = ContactRequestModel()
                        model.name = item.name
                        val splitPhone = item.phoneNumber?.split(" ")
                        if (splitPhone?.isNotEmpty() == true) {
                            if (splitPhone.size == 2) {
                                model.phone_code = splitPhone[0]
                                model.phone_number = splitPhone[1]
                            } else if (splitPhone.size > 2) {
                                model.phone_code = splitPhone[0]
                                model.phone_number = splitPhone[1] + splitPhone[2]
                            } else {
                                model.phone_number = splitPhone[0]
                            }
                        }
                        list.add(model)
                    }

                    val finalList = ArrayList<ContactRequestModel>()
                    finalList.addAll(list)

                    if (order?.shareWith?.isNotEmpty() == true) {
                        for (item in order?.shareWith ?: ArrayList()) {
                            for (con in finalList) {
                                if (item.phoneNumber == con.phone_number) {
                                    list.remove(con)
                                }
                            }
                        }
                    }

                    if (list.isNotEmpty()) {
                        val req = ShareRideReqModel()
                        req.order_id = order?.order_id
                        req.shareWith = Gson().toJson(list)
                        presenter.requestRideShareApiCall(req)
                    } else {
                        Toast.makeText(context, "You are already shared ride with these contacts", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    /* User type or enter the contact */
                    when {
                        phoneNumber?.text.toString().trim().isEmpty() -> Toast.makeText(btnContinue.context, getString(R.string.phone_empty_validation_message), Toast.LENGTH_SHORT).show()
                        ValidationUtils.isPhoneNumberAllZeros(phoneNumber?.text.toString()) -> Toast.makeText(btnContinue.context, getString(R.string.phone_valid_validation_message), Toast.LENGTH_SHORT).show()
                        else -> {
                            val list = ArrayList<ContactRequestModel>()
                            val model = ContactRequestModel()
                            model.phone_number = phoneNumber?.text.toString().trim()
                            model.phone_code = countryCodePicker?.selectedCountryCodeWithPlus
                            model.name = "Anonymous"
                            list.add(model)
                            val req = ShareRideReqModel()
                            req.order_id = order?.order_id
                            req.shareWith = Gson().toJson(list)
                            presenter.requestRideShareApiCall(req)
                        }
                    }
                }
            } else {
                CheckNetworkConnection.showNetworkError(rootView)
            }
            shareRideDialog?.dismiss()
        }
        shareRideDialog?.show()
        shareRideDialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    override fun deleteContact(position: Int) {
        selectedContactsList.removeAt(position)
        adapter?.setList(selectedContactsList, this)
    }

    private fun launchMultiplePhonePicker() {
        startActivityForResult(Intent(context, ContactsActivity::class.java), Constants.REQUEST_CODE_PICK_CONTACT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_PICK_CONTACT && data != null) {
            selectedContactsList = data.getSerializableExtra("contacts") as ArrayList<ContactModel>
            for (item in selectedContactsList) {
                item.phoneNumber = AppUtils.getCountryCodeFromPhoneNumber(item.phoneNumber
                        ?: "", "0").replace("//s".toRegex(), "")
            }
            adapter?.setList(selectedContactsList, this)
            phoneNumber?.isEnabled = false
            countryCodePicker?.isEnabled = false
        }
    }

    private fun showCancellationDialog(actionType: String) {
        cancelDialog = activity?.let { Dialog(it) }
        cancelDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        cancelDialog?.setCancelable(true)
        cancelDialog?.setCanceledOnTouchOutside(true)
        cancelDialog?.setContentView(R.layout.dialog_cancel_reason)
        cancelDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val etMessage = cancelDialog?.findViewById(R.id.etMessage) as EditText
        val tvSubmit = cancelDialog?.findViewById(R.id.tvSubmit) as TextView
        tvSubmit?.setBackgroundColor(Color.parseColor(ConfigPOJO.primary_color));

        val cancelGroup = cancelDialog?.findViewById<RadioGroup>(R.id.cancelGroup)
        val btn1 = cancelDialog?.findViewById<RadioButton>(R.id.btn1)
        val btn2 = cancelDialog?.findViewById<RadioButton>(R.id.btn2)
        val btn3 = cancelDialog?.findViewById<RadioButton>(R.id.btn3)
        val btn4 = cancelDialog?.findViewById<RadioButton>(R.id.btn4)

        if (actionType == "halfWayStop") {
            cancelDialog?.findViewById<TextView>(R.id.textView10)?.text = getString(R.string.halfWayStopReason)
            btn3?.visibility=View.GONE
        } else if (actionType == "vehicleBreakDown") {
            btn3?.visibility=View.GONE
            cancelDialog?.findViewById<TextView>(R.id.textView10)?.text = getString(R.string.vehicleBreakDownReason)
        }

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
                val selectedRdBtn = cancelDialog?.findViewById<RadioButton>(cancelGroup!!.checkedRadioButtonId)

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

                val map = java.util.HashMap<String, String>()
                map["order_distance"] = totalDistance.toString()
                map["order_id"] = order?.order_id.toString()
                when (actionType) {
                    "cancelRide" -> {
                        map["cancel_reason"] = (selectedRdBtn)?.text.toString().trim()
                    }
                    "halfWayStop" -> {
                        map["half_way_stop_reason"] = (selectedRdBtn)?.text.toString().trim()
                    }
                    "vehicleBreakDown" -> {
                        map["reason"] = (selectedRdBtn)?.text.toString().trim()
                    }
                }

                if (btn4?.isChecked == true) {
                    if (etMessage?.text.toString().trim().isEmpty()) {
                        activity?.let {
                            Toast.makeText(it, getString(R.string.cancellation_reason_validation_text), Toast.LENGTH_SHORT).show()
                        }
                        return@setOnClickListener
                    }
                    when (actionType) {
                        "cancelRide" -> {
                            map["cancel_reason"] = etMessage?.text.toString().trim()
                        }
                        "halfWayStop" -> {
                            map["half_way_stop_reason"] = etMessage?.text.toString().trim()
                        }
                        "vehicleBreakDown" -> {
                            map["reason"] = etMessage?.text.toString().trim()
                        }
                    }
                }

                if (CheckNetworkConnection.isOnline(activity)) {
                    when (actionType) {
                        "cancelRide" -> {
                            presenter.requestCancelApiCall(map)
                        }

                        "halfWayStop" -> {
                            map["latitude"] = sourceLatLong?.latitude.toString()
                            map["longitude"] = sourceLatLong?.longitude.toString()
                            map["payment_type"] = order?.payment?.payment_type.toString()
                            isHalfWayOrBreakDown = true
                            presenter.requestHalfWayStopByUserApiCall(map)
                        }

                        "vehicleBreakDown" -> {
                            map["latitude"] = sourceLatLong?.latitude.toString()
                            map["longitude"] = sourceLatLong?.longitude.toString()
                            isHalfWayOrBreakDown = true
                            presenter.requestVehicleBreakDownApiCall(map)
                        }
                    }

                    cancelDialog?.dismiss()
                } else {
                    CheckNetworkConnection.showNetworkError(rootView)
                }
            } else {
                activity?.let { Toast.makeText(it, getString(R.string.cancellation_reason_validation_text), Toast.LENGTH_SHORT).show() }
            }
        }

        cancelDialog?.show()
        cancelDialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    override fun onHalfWayStopByUserSuccess() {
        openProcessingFragment(order, "halfWayStop")
    }

    private fun openProcessingFragment(order: Order?, type: String) {
        val fragment = ProcessingRequestFragment()
        val bundle = Bundle()
        bundle.putString(ORDER, Gson().toJson(order))
        bundle.putBoolean("changeTimeOut", false)
        bundle.putString("via", type)
        fragment.arguments = bundle
        fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
    }

    override fun onVehiceBreakDownSuccess() {
        openProcessingFragment(order, "vehicleBreakDown")
        Toast.makeText(context, "VehicleBreakDownSuccess", Toast.LENGTH_SHORT).show()
    }

    override fun onRideShareSuccess(data: RideShareResponse) {
        shareLoader=false
        if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            Toast.makeText(context, "RideShareSuccess", Toast.LENGTH_SHORT).show()
            MyAnimationUtils.animateShowHideView(animContainer, rlRideShareLayout, true)
            rideShareAdapter?.setList(data.shareWith)
        } else {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.setType("text/plain")
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                var shareMessage = "\nLet me share the ride details\n\n"
                shareMessage = shareMessage + "  ${data.url} \n\n"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (e: Exception) {
                //e.toString();
            }
        }

    }

    override fun onCancelRideShareSuccess() {
        Toast.makeText(context, "Ride Share Cancel success ", Toast.LENGTH_SHORT).show()
        MyAnimationUtils.animateShowHideView(animContainer, rlRideShareLayout, false)
    }


    private fun setupMap() {
        mMap = (activity as HomeActivity).googleMapHome
        if (mMap != null) {
            mMap?.clear()
        }
//        order = Gson().fromJson(arguments?.getString("order"), Order::class.java)
        sourceLatLong = LatLng(order?.driver?.latitude ?: 0.0, order?.driver?.longitude ?: 0.0)
        isHeavyLoadsVehicle = true
        if (order?.order_status == OrderStatus.CONFIRMED) {
            destLong = LatLng(order?.pickup_latitude ?: 0.0, order?.pickup_longitude ?: 0.0)
        } else {
            destLong = LatLng(order?.dropoff_latitude ?: 0.0, order?.dropoff_longitude
                    ?: 0.0)
        }

        acPickupAddress.text = order?.pickup_address
        acDropOffAddress.text = order?.dropoff_address
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

        tvDateTime.text = DateUtils.getFormattedTimeForBooking(order?.order_timings ?: "")
        val paymentType = getString(AppUtils.getPaymentStringId(order?.payment?.payment_type
                ?: "0"))
        tvPaymentTypeAmount.text = String.format("%s", "${ConfigPOJO.currency} ${AppUtils.getFormattedDecimal(order?.payment?.final_charge?.toDouble()
                ?: 0.0)}")
        tvBookingId.text = String.format("%s", "Id:${order?.order_token}")
        tvBookingStatus.text = getString(R.string.ongoing)
        showMarker()
        reFocusMapCamera()
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

    override fun onApiSuccess(response: List<Order>?) {
        if (response?.isNotEmpty() == true) {
            tvDriverName?.text = response[0].driver?.name
            if (response[0].shareWith.isNotEmpty()) {
                if (ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
                MyAnimationUtils.animateShowHideView(animContainer, rlRideShareLayout, true)
                rideShareAdapter?.setList(response[0].shareWith)
                }
            }
            if (response[0].driver?.rating_count ?: 0 > 0 && response[0].driver?.rating_avg?.toInt() ?: 0 != 0) {
                val rating = if ((response[0].driver?.rating_avg?.toInt() ?: 0) <= 5) {
                    response[0].driver?.rating_avg?.toInt()
                } else {
                    5
                }
//                ratingBar.rating = (ratingDrawables[rating?.minus(1) ?: 0].toFloat())
                ratingBar.rating = (response[0].driver?.rating_avg?: "0").toFloat()
                tvRatingCount.text = " · " + response?.get(0)?.driver?.rating_count.toString()

                if(Constants.SECRET_DB_KEY == "df258aa6db00e79bc182bc5d9ce5e4f6") {
                    val stars: LayerDrawable = ratingBar.getProgressDrawable() as LayerDrawable
                    if ((response[0].driver?.rating_avg
                                    ?: "0").toInt() == 5 || (response[0].driver?.rating_avg
                                    ?: "0").toInt() > 4) {
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
            orderString = Gson().toJson(order)

            if(order?.booking_flow=="1")
            {
                tvPanic.visibility = View.GONE
                tvBreakDown.visibility = View.GONE
                tvHalfWayStop.visibility = View.GONE
            }

            ivDriverImage.setRoundProfileUrl(BASE_IMAGE_URL + response[0].driver?.profile_pic)
            ivVehicleImage.setImageUrl(response[0].driver?.icon_image_url)
            tvCancelRide.text = response[0].brand?.brand_name
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
            /*if (response[0].driver?.vehicle_purchase_year != "" && response[0].driver?.vehicle_purchase_year != null) {
                details.append(response[0].driver?.vehicle_purchase_year ?: "")
            }*/

            tvVehicleNumber.text = details.toString()

            if (order?.order_status == OrderStatus.CONFIRMED) {
                tvDriverStatus?.text = getString(R.string.driver_accepted_request)
                //  textView12.visibility = View.VISIBLE
                tvBookingStatus.text = getString(R.string.confirmed)

            } else if (order?.order_status == OrderStatus.ONGOING) {
                /*if (order?.my_turn == "1") {
                    tvCancelRide.visibility = View.GONE
                    tvDriverStatus?.text = getString(R.string.driver_is_on_the_way)
                    tvBookingStatus.text =getString(R.string.ongoing)
                } else {
                    tvDriverStatus?.text = getString(R.string.driver_completing_nearby_order)
                }*/
                tvCancelRide.visibility = View.GONE
                tvDriverStatus?.text = getString(R.string.driver_is_on_the_way)
                tvBookingStatus.text =getString(R.string.ongoing)
            } else if (order?.order_status == OrderStatus.REACHED) {
                tvDriverStatus?.text = getString(R.string.driver_reached)
                tvBookingStatus.text = getString(R.string.reached)
            }
            setupMap()
        } else {
            fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            val fragment = ServicesFragment()
            (activity as HomeActivity).serviceRequestModel = ServiceRequestModel()
            (activity as HomeActivity).servicesFragment = fragment
            fragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
            Toast.makeText(activity, getString(R.string.order_cancelled), Toast.LENGTH_LONG).show()
        }
    }

    override fun showLoader(isLoading: Boolean) {
        if (shareLoader)
        {
            dialogIndeterminate?.show(isLoading)
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
                .color(ContextCompat.getColor(context as Activity, R.color.colorPrimary))
                .geodesic(true))
        /* To calculate the estimated distance*/
        val estimatedTime = (((routes?.get("legs") as JSONArray).get(0) as JSONObject).get("duration") as JSONObject).get("text") as String
        tvTime.text = estimatedTime
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
    private fun showMarker() {
        /*add marker for both source and destination*/
        carMarker = mMap?.addMarker(this.sourceLatLong?.let { MarkerOptions().position(it) })
        carMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_car_cab)))
        carMarker?.isFlat = true
        carMarker?.setAnchor(0.5f, 0.5f)
        destMarker = mMap?.addMarker(this.destLong?.let { MarkerOptions().position(it) })
        if (isHeavyLoadsVehicle && order?.order_status == OrderStatus.CONFIRMED) {
            destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_pickup_location_mrkr)))
        } else {
            destMarker?.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_nav)))
        }

        this.sourceLatLong?.latitude?.let {
            this.sourceLatLong?.longitude?.let { it1 ->
                this.destLong?.latitude?.let { it2 ->
                    this.destLong?.longitude?.let { it3 ->
                        presenter.drawPolyLine(sourceLat = it, sourceLong = it1,
                                destLat = it2, destLong = it3, language = Locale.US.language)
                    }
                }
            }
        }
    }

    var valueAnimatorRotate: ValueAnimator? = null

    override fun snappedPoints(response: List<RoadItem>?, trackingModel: TrackingModel?) {
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
        openServiceFragment()
    }

    // Socket Listener for orders events
    private val orderEventListener = Emitter.Listener { args ->
        Logger.e("orderEventListener", args[0].toString())
        restartCheckIdleTimer(5000)
        activity?.runOnUiThread {

            when (JSONObject(args[0].toString()).getString("type")) {
                OrderEventType.SERVICE_COMPLETE -> {
                    val orderJson = JSONObject(args[0].toString()).getJSONArray("order").get(0).toString()
                    openInvoiceFragment(orderJson)
                }

                OrderEventType.VEHICLE_BREAK_DOWN -> {
                    val orderJson = JSONObject(args[0].toString()).getJSONArray("order").get(0).toString()
                    val fragment = ProcessingRequestFragment()
                    val bundle = Bundle()
                    bundle.putString("via", "vehicleBreakDown")
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
                            tvDriverStatus?.text = getString(R.string.driver_accepted_request)
                            order?.order_status = OrderStatus.CONFIRMED
                        } else if (track?.order_status == OrderStatus.ONGOING) {
                            /*if (track?.my_turn == "1") {
                                tvCancelRide.visibility = View.GONE
                                tvDriverStatus?.text = getString(R.string.driver_is_on_the_way)
                                order?.order_status = OrderStatus.ONGOING
                            } else {
                                tvDriverStatus?.text = getString(R.string.driver_completing_nearby_order)
                            }*/
                            tvCancelRide.visibility = View.GONE
                            tvDriverStatus?.text = getString(R.string.driver_is_on_the_way)
                            order?.order_status = OrderStatus.ONGOING
                        } else if (track?.order_status == OrderStatus.REACHED) {
                            tvDriverStatus?.text = getString(R.string.driver_reached)
                            order?.order_status = OrderStatus.REACHED

                        }
                    }
                }

                OrderStatus.DRIVER_CANCELLED -> {
                    openServiceFragment()
                    Toast.makeText(activity, getString(R.string.ongoing_request_cancelled_by_driver), Toast.LENGTH_LONG).show()
                }
            }
        }
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

    private fun checkOrderStatus() {
        val request = JSONObject()
        request.put("type", OrderEventType.CUSTOMER_SINGLE_ORDER)
        request.put("access_token", ACCESS_TOKEN)
        request.put("order_token", order?.order_token)
        AppSocket.get().emit(Events.COMMON_EVENT, request, Ack {
            val response = Gson().fromJson<ApiResponse<Order>>(it[0].toString(),
                    object : TypeToken<ApiResponse<Order>>() {}.type)
            if (response?.statusCode == SUCCESS_CODE) {
                val orderModel = response.result
                val orderJson = JSONObject(it[0].toString()).getString("result")
                activity?.runOnUiThread {
                    when (orderModel?.order_status) {

                        OrderStatus.SERVICE_COMPLETE -> {
                            if (orderModel.booking_type != "Breakdown") {
                                openInvoiceFragment(orderJson)
                            }
                        }

                        OrderStatus.REACHED -> {
                            order?.order_status = OrderStatus.REACHED
                            if (isAdded && context != null) {
                                tvDriverStatus?.text = getString(R.string.driver_reached)
                            }
                        }

                        OrderStatus.ONGOING -> {
                            order?.order_status = OrderStatus.ONGOING
                            if (isAdded && context != null) {
                                tvCancelRide.visibility = View.GONE
                                tvDriverStatus?.text = getString(R.string.driver_is_on_the_way)
                            }

                            fullTrackArray.clear()
                            val resultJsonObj = JSONObject(it[0].toString()).getJSONObject("result")
                            if (resultJsonObj.has("order_request")) {
                                val cRequestJson = resultJsonObj.getJSONObject("order_request")
                                val fullTrackJsonString = cRequestJson.getString("full_track")
                                val jsonArray = JSONArray(fullTrackJsonString)
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject = jsonArray.getJSONObject(i)
                                    fullTrackArray.add(LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")))
                                }
                                Log.e("full : ", fullTrackArray.size.toString())
                            }

                        }

                        OrderStatus.DRIVER_CANCELLED -> {
                            openServiceFragment()
                            Toast.makeText(activity, getString(R.string.ongoing_request_cancelled_by_driver), Toast.LENGTH_LONG).show()
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
        if (ConfigPOJO.gateway_unique_id.equals("payku")
                || ConfigPOJO.gateway_unique_id.toLowerCase().equals("wipay")) {
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
        }
        else {
            val fragment = CompleteRideFragment()
            val bundle = Bundle()
            bundle.putString("order", orderJson)
            fragment.arguments = bundle
            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commitAllowingStateLoss()
        }
    }

    private fun openServiceFragment() {
        (context as HomeActivity).serviceRequestModel.pkgData = null
        fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val fragment = ServicesFragment()
        (activity as? HomeActivity)?.servicesFragment = fragment
        fragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
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
        cancelDialog?.cancel()
        shareRideDialog?.cancel()
        AppSocket.get().off(Events.ORDER_EVENT)
        AppSocket.get().off(Events.COMMON_EVENT)
        presenter.detachView()
        super.onDestroyView()
    }

    override fun onNetworkConnected() {

    }

    override fun onNetworkDisconnected() {

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

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    fun getUserContacts() {
        launchMultiplePhonePicker()
    }

    @OnShowRationale(Manifest.permission.READ_CONTACTS)
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(rootView.context, R.string.permission_required_to_select_image, request)
    }

    @OnNeverAskAgain(Manifest.permission.READ_CONTACTS)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(rootView.context,
                R.string.permission_required_to_select_image)
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }
}