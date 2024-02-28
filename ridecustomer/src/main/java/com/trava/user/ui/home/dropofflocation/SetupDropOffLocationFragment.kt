package com.trava.user.ui.home.dropofflocation

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.hbb20.CountryCodePicker
import com.trava.user.R
import com.trava.user.databinding.FragmentSetUpDropoffLocationBinding
import com.trava.user.databinding.FragmentSetUpDropoffLocationSummerBinding
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.comfirmbooking.ConfirmBookingFragment
import com.trava.user.ui.home.confirm_pickup.ConfirmPickupActivity
import com.trava.user.ui.home.deliverystarts.DeliveryStartsFragment
import com.trava.user.ui.home.orderdetails.heavyloads.EnterOrderDetailsFragment
import com.trava.user.ui.home.vehicles.SelectVehicleTypeFragment
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.utils.ValidationUtils
import com.trava.user.webservices.models.RecentItem
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.StopsModel
import com.trava.user.webservices.models.add_address_home_work.HomeWorkResponse
import com.trava.user.webservices.models.homeapi.ServiceDetails
import com.trava.user.webservices.models.locationaddress.GetAddressesPojo
import com.trava.user.webservices.models.locationaddress.ResultItem
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.*
import com.trava.utilities.Constants.SECRET_DB_KEY
import com.trava.utilities.constants.ACCESS_TOKEN_KEY
import com.trava.utilities.constants.PROFILE
import com.trava.utilities.webservices.models.AppDetail
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_set_up_dropoff_location.*
import kotlinx.android.synthetic.main.fragment_set_up_dropoff_location.ivBack
import kotlinx.android.synthetic.main.fragment_set_up_dropoff_location.rootView

class SetupDropOffLocationFragment : Fragment(), View.OnClickListener, AddStopsContract.View, RecentLocationinterface, StopsLocationinterface {

    override fun getSelectedRecentLocation(data: StopsModel, position: Int) {
        itemPosition = position
        if (arguments?.containsKey("order") == false) {
            selectAddressDialog(Constants.ADD_STOPS_REQUEST_CODE, 3)
        } else {
            if (data.stop_status == "pending" || data.stop_status == "") {
                showAppStopsWarningDialog()
            }
        }
    }

    override fun getSelectedRecentLocationDelete(data: StopsModel, position: Int) {
        if (data.stop_status == "") {
            serviceRequest?.stops?.removeAt(position)
            onGoing_ride_stops.removeAt(position)
            adapterStops?.notifyDataSetChanged()
            if (serviceRequest?.stops!!.size == 0) {
                tvRecentLocation.show()
                rvRecentLocations.show()
                tvConfirmDone.show()
                rlAddHome.show()
                rlAddWork.show()
                rlMakeChanges.hide()

                if (ConfigPOJO.TEMPLATE_CODE == Constants.EAGLE) {
                    llForMe.visibility = View.GONE
                    rlAddHome.visibility = View.GONE
                    setUpDropoffLocationBinding.viewHome.visibility = View.GONE
                    rlAddWork.visibility = View.GONE
                }
            }
            if (serviceRequest?.stops!!.size < 4) {
                side_view.visibility = View.VISIBLE
                ivAddLocation.visibility = View.VISIBLE
            }
        }
    }

    private var isDropClicked = false
    private var isPickClicked = false
    private var TAG = "SetupLocationFragment"

    private var itemPosition = 0
    private val CONFIRM_PICKUP_LOC = 401
    private var pLat = 0.0
    private var pLng = 0.0
    private var pAddress = ""
    private var dLat = 0.0
    private var dLng = 0.0
    private var dAddress = ""

    private var pLatTemp = 0.0
    private var pLngTemp = 0.0
    private var pAddressTemp = ""
    private var dLatTemp = 0.0
    private var dLngTemp = 0.0
    private var dAddressTemp = ""

    private var isPickupAddress = false
    private var isBookForFriend = false
    private var dialog: BottomSheetDialog? = null
    private var isPhoneNoValid = false
    private var serviceRequest: ServiceRequestModel? = null
    private var serviceDetails: ServiceDetails? = null
    private var bookingType = ""
    private var addressType = 0
    private var priority = 0
    private var flipAddress = true
    private var order: Order? = null
    private val presenter = AddStopsPresenter()
    private var onGoing_ride_stops = ArrayList<StopsModel>()
    private var progressDialog: DialogIndeterminate? = null
    private var isHomeAddress = false
    private var isEditHomeAddress = false
    var homeActivity: HomeActivity? = null
    private var isEditWorkAddress = false
    lateinit var setUpDropoffLocationBinding: FragmentSetUpDropoffLocationBinding
    lateinit var setUpDropoffLocationSummerBinding: FragmentSetUpDropoffLocationSummerBinding
    private var adapter: RecentLocationsAdapter? = null
    private var adapterStops: StopsLocationsAdapter? = null
    var isSelectedFromRecent = false;
    private var addressesList: ArrayList<ResultItem> = ArrayList<ResultItem>()
    private var locationDropdownAdapter: LocationDropdownAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = if (ConfigPOJO.TEMPLATE_CODE == Constants.TRAVE_APP_BASE
                || ConfigPOJO.TEMPLATE_CODE == Constants.EAGLE
                || ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE
                || ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            setUpDropoffLocationBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_set_up_dropoff_location, container, false);
            setUpDropoffLocationBinding.color = ConfigPOJO.Companion
            setUpDropoffLocationBinding.root
        } else {
            setUpDropoffLocationSummerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_set_up_dropoff_location_summer, container, false);
            setUpDropoffLocationSummerBinding.color = ConfigPOJO.Companion
            setUpDropoffLocationSummerBinding.root
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        homeActivity?.getMapAsync()

        intialize()

        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
            tvConfirmDone.text = getString(R.string.next)
            tvDone.text = getString(R.string.next)
        }

        if (SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14242c4f98380b537e89446cd49b0022f7" ||
                SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14bb88ffcd9d48b45b6cd0b34d5a3554e5") {
            tv_flip.visibility = View.GONE
        }

        if(SECRET_DB_KEY == "a3f699d8c406daaf828f2d2c891eac15110024532f77f082ad972842c8516d60" ||
                SECRET_DB_KEY == "fdd4d886d691305c703e8274bbd49ec217a41b4a1bc16322f1345c000a1987f5"){
            ivAddLocation.visibility = View.GONE
            side_view.visibility = View.GONE
        }

        /*Set ride stops fields while request is already created*/
        if (serviceRequest?.stops!!.size > 0) {
            rlMakeChanges.show()
            tvRecentLocation.hide()
            rvRecentLocations.hide()
            rlAddHome.hide()
            rlAddWork.hide()
            tvConfirmDone.hide()
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.EAGLE) {
            llForMe.visibility = View.GONE
            rlAddHome.visibility = View.GONE
            setUpDropoffLocationBinding.viewHome.visibility = View.GONE
            rlAddWork.visibility = View.GONE
        } else if (ConfigPOJO.TEMPLATE_CODE == Constants.MOBY) {
            setUpDropoffLocationSummerBinding.textView7.visibility = VISIBLE
            setUpDropoffLocationSummerBinding.textView6.visibility = GONE
            setUpDropoffLocationSummerBinding.tvConfirmDone.setText("Proceed")
            setUpDropoffLocationSummerBinding.onMapRl.visibility = VISIBLE
            setUpDropoffLocationSummerBinding.rvRecentLocations.visibility = VISIBLE
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.TRAVE_APP_BASE
                || ConfigPOJO.TEMPLATE_CODE == Constants.EAGLE
                || ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE
                || ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
            setUpDropoffLocationBinding.rvStopsLocations.layoutManager = LinearLayoutManager(activity)
            adapterStops = StopsLocationsAdapter(serviceRequest?.stops!!, this)
            setUpDropoffLocationBinding.rvStopsLocations.adapter = adapterStops
        }

        val profile = SharedPrefs.with(context).getObject(PROFILE, AppDetail::class.java)
        ivProfile?.setRoundProfileUrl(profile?.profile_pic_url)
        ivBookFriendProfilee?.setRoundProfileUrl(profile?.profile_pic_url)
        //ivHomeEdit.setColorFilter(Color.parseColor(ConfigPOJO.primary_color), PorterDuff.Mode.SRC_IN)
        tv_main.setColorFilter(Color.parseColor(ConfigPOJO.secondary_color), PorterDuff.Mode.SRC_IN)
        if (Constants.SECRET_DB_KEY == "6ae915e1698d88c4e7dda839852fa5fd") {
            ivDropLocation.setImageResource(R.drawable.ic_drop_location)
        }

        setListener()
    }

    private fun intialize() {
        progressDialog = DialogIndeterminate(context)
        serviceRequest = (activity as HomeActivity).serviceRequestModel
        serviceDetails = (activity as HomeActivity).serviceDetails
        pLat = arguments?.getDouble(Constants.LATITUDE, 0.0) ?: 0.0
        pLng = arguments?.getDouble(Constants.LONGITUDE, 0.0) ?: 0.0
        pAddress = arguments?.getString(Constants.ADDRESS, "") ?: ""
        pLatTemp = arguments?.getDouble(Constants.LATITUDE, 0.0) ?: 0.0
        pLngTemp = arguments?.getDouble(Constants.LONGITUDE, 0.0) ?: 0.0
        pAddressTemp = arguments?.getString(Constants.ADDRESS, "") ?: ""
        bookingType = arguments?.getString(Constants.BOOKING_TYPE) ?: ""
        dLatTemp = 0.0
        dLngTemp = 0.0
        dAddressTemp = ""
        if (requireArguments().containsKey("destination")) {
            dAddress = arguments?.getString("destination") ?: ""
            acDropOffAddress.text = dAddress
        }
        if (serviceRequest?.category_name == "Pickup") {
            acPickupAddress.text = pAddress
        } else if (serviceRequest?.category_name == "Receive") {
            acDropOffAddress.text = pAddress
        } else {
            acPickupAddress.text = pAddress
        }

        if (serviceRequest?.bookingFlow == "1") {
            side_view.visibility = View.GONE
            ivAddLocation.visibility = View.GONE
        }

        if (serviceRequest?.bookingType?.isNotEmpty() == true) {
            tvForMe.text = getString(R.string.forFriend)
        }
        if (serviceRequest?.dropoff_address != "") {
            dLat = serviceRequest?.dropoff_latitude ?: 0.0
            dLng = serviceRequest?.dropoff_longitude ?: 0.0
            acDropOffAddress.text = serviceRequest?.dropoff_address

            pLat = serviceRequest?.pickup_latitude ?: 0.0
            pLng = serviceRequest?.pickup_longitude ?: 0.0
            acPickupAddress.text = serviceRequest?.pickup_address
        }

        if (getString(R.string.app_name) == "Wasila") {
            if (pAddress != "") {
                (activity as HomeActivity).showMarkerNearByPlaces(LatLng(pLat, pLng), LatLng(dLat, dLng))
            }
            rootView.setBackgroundColor(0)
            rl_view.visibility = View.GONE
            ivAddLocation.visibility = View.GONE
            side_view.visibility = View.GONE
        }

        /*Set ride stops data while request is already request created*/
        if (arguments?.containsKey("order") == true) {
            order = Gson().fromJson(arguments?.getString("order"), Order::class.java)
            llForMe.hide()
            tv_flip.visibility = View.GONE
            textView6.text = getString(R.string.changeDestination)
            acPickupAddress.text = order?.pickup_address
            acPickupAddress.isEnabled = false
            acDropOffAddress.text = order?.dropoff_address
            if (order?.ride_stops?.isNotEmpty() == true) {
                tvConfirmDone.hide()
                rlMakeChanges.show()
                rlAddHome.hide()
                rlAddWork.hide()
                rvRecentLocations.hide()
            }
            onGoing_ride_stops.clear()
            serviceRequest?.stops?.clear()
            serviceRequest?.stops?.addAll(order?.ride_stops!!)
            onGoing_ride_stops.addAll(order?.ride_stops!!)

            if (onGoing_ride_stops.size == 4) {
                ivAddLocation.hide()
            }

            if (order?.booking_flow == "1") {
                side_view.visibility = View.GONE
                ivAddLocation.visibility = View.GONE
            }
        }

        if (Constants.SECRET_DB_KEY == "a3f699d8c406daaf828f2d2c891eac15110024532f77f082ad972842c8516d60" || Constants.SECRET_DB_KEY == "fdd4d886d691305c703e8274bbd49ec217a41b4a1bc16322f1345c000a1987f5") {
            setUpDropoffLocationBinding.tvFlip.visibility = View.GONE
            setUpDropoffLocationBinding.llForMe.visibility = View.GONE
            setUpDropoffLocationBinding.ivDropLocation.setImageResource(R.drawable.ic_drop_location)
        }
        setRecentLocationsData()
    }

    /*Set fields of recent locations used for previous rides/request
    * and also set Specific Home and Office address*/
    private fun setRecentLocationsData() {
        if (serviceDetails?.addresses != null) {
            if (serviceDetails?.addresses?.home?.address != null) {
                tvAddHomeTitle?.text = serviceDetails?.addresses?.home?.addressName
                tvHomeAddress?.text = serviceDetails?.addresses?.home?.address
                tvHomeAddress.show()
                ivHomeEdit.show()
            }

            if (serviceDetails?.addresses?.work?.address != null) {
                tvAddWorkTitle?.text = serviceDetails?.addresses?.work?.addressName
                tvWorkAddress?.text = serviceDetails?.addresses?.work?.address
                tvWorkAddress.show()
                ivWorkEdit.show()
            }

            if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE) {
                rvRecentLocations.visibility = View.GONE
            }

            if (serviceDetails?.addresses?.recent?.isNotEmpty() == true) {
                adapter = RecentLocationsAdapter()
                rvRecentLocations.adapter = adapter
                adapter?.refreshList(serviceDetails?.addresses?.recent ?: ArrayList(), this)
            }
        }
    }

    override fun getSelectedRecentLocation(data: RecentItem) {
        isSelectedFromRecent = true;
        showChooseAddressDialog(data)
    }

    /*Alert to select address from recent locations for Pickup and Drop-Off address*/
    private fun showChooseAddressDialog(data: RecentItem) {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setContentView(R.layout.dialog_recent_locations_chooser)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.findViewById<TextView>(R.id.tvLocTitle)?.text = data.addressName
        dialog?.findViewById<TextView>(R.id.tvLocAddress)?.text = data.address
        dialog?.findViewById<View>(R.id.viewRecent)?.hide()

        dialog?.findViewById<TextView>(R.id.tvLocTitle)?.setTextColor(Color.parseColor(ConfigPOJO.black_color))
        dialog?.findViewById<TextView>(R.id.tvLocAddress)?.setTextColor(Color.parseColor(ConfigPOJO.black_color))
        dialog?.findViewById<TextView>(R.id.tvDropoffAddress)?.background =
                StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour,
                        GradientDrawable.RECTANGLE)
        dialog?.findViewById<TextView>(R.id.tvDropoffAddress)?.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour))

        dialog?.findViewById<TextView>(R.id.tvPickupAddress)?.background =
                StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour,
                        GradientDrawable.RECTANGLE)
        dialog?.findViewById<TextView>(R.id.tvPickupAddress)?.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour))

        dialog?.findViewById<TextView>(R.id.tvPickupAddress)?.setOnClickListener {
            isPickupAddress = true
            updateLocationData(data)
            dialog.dismiss()
        }

        dialog?.findViewById<TextView>(R.id.tvDropoffAddress)?.setOnClickListener {
            isPickupAddress = false
            updateLocationData(data)
            dialog.dismiss()
        }

        dialog?.show()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    /*Alert dialog to select address from dragging map and input field for new request*/
    private fun selectAddressDialog(req: Int, type: Int) {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setContentView(R.layout.dialog_set_locations_chooser)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.findViewById<View>(R.id.viewRecent)?.hide()

        dialog?.findViewById<TextView>(R.id.tvLocTitle)?.setTextColor(Color.parseColor(ConfigPOJO.black_color))
        dialog?.findViewById<TextView>(R.id.tvLocAddress)?.setTextColor(Color.parseColor(ConfigPOJO.black_color))
        dialog?.findViewById<TextView>(R.id.tvDropoffAddress)?.background =
                StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour,
                        GradientDrawable.RECTANGLE)
        dialog?.findViewById<TextView>(R.id.tvDropoffAddress)?.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour))

        dialog?.findViewById<TextView>(R.id.tvPickupAddress)?.background =
                StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour,
                        GradientDrawable.RECTANGLE)
        dialog?.findViewById<TextView>(R.id.tvPickupAddress)?.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour))

        dialog?.findViewById<TextView>(R.id.tvPickupAddress)?.setOnClickListener {
            if (getString(R.string.app_name) == "Wasila") {
                activity?.startActivityForResult(Intent(activity, FindLocationActivity::class.java)
                        .putExtra(Constants.LAT, pLat)
                        .putExtra(Constants.LNG, pLng), req)
            } else {
                openPlacePickerIntent(req)
            }
            dialog.dismiss()
        }

        dialog?.findViewById<TextView>(R.id.tvDropoffAddress)?.setOnClickListener {
            startActivityForResult(Intent((context as HomeActivity), ConfirmPickupActivity::class.java)
                    .putExtra(Constants.LAT, pLat)
                    .putExtra(Constants.LNG, pLng)
                    .putExtra("CREATE_RIDE", type)
                    , CONFIRM_PICKUP_LOC)
            dialog.dismiss()
        }

        dialog?.show()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    private fun updateLocationData(data: RecentItem) {
        if (isPickupAddress) {
            /* pickup address */
            pLat = data.addressLatitude ?: 0.0
            pLng = data.addressLongitude ?: 0.0
            pAddress = data.address ?: ""

            pLatTemp = data.addressLatitude ?: 0.0
            pLngTemp = data.addressLongitude ?: 0.0
            pAddressTemp = data.address ?: ""
            acPickupAddress.text = data.addressName
        } else {
            /* dropoff address */
            acDropOffAddress.text = data.addressName
            serviceRequest?.addressName = data.addressName
            serviceRequest?.dropoff_address = data.address
            serviceRequest?.dropoff_latitude = data.addressLatitude
            serviceRequest?.dropoff_longitude = data.addressLongitude
            dLat = data.addressLatitude ?: 0.0
            dLng = data.addressLongitude ?: 0.0
            dAddress = data.address ?: ""

            dLatTemp = data.addressLatitude ?: 0.0
            dLngTemp = data.addressLongitude ?: 0.0
            dAddressTemp = data.address ?: ""
        }
    }

    private fun setListener() {
        acPickupAddress?.setOnClickListener(this)
        acDropOffAddress?.setOnClickListener(this)
        ivBack?.setOnClickListener(this)
        ivAddLocation?.setOnClickListener(this)
        llForMe.setOnClickListener(this)
        llBookForFriend.setOnClickListener(this)
        llBookForMe.setOnClickListener(this)
        rlAddHome.setOnClickListener(this)
        rlAddWork.setOnClickListener(this)
        ivHomeEdit.setOnClickListener(this)
        ivWorkEdit.setOnClickListener(this)
        tvDone.setOnClickListener(this)
        tvConfirmDone.setOnClickListener(this)
        tvSelectPickup.setOnClickListener(this)
        tvSelectdropOff.setOnClickListener(this)
        tv_flip.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                activity?.onBackPressed()
            }
            R.id.tv_flip -> {
                if (flipAddress) {
                    pLat = dLatTemp
                    pLng = dLngTemp
                    pAddress = dAddressTemp
                    acPickupAddress.text = dAddressTemp

                    dLat = pLatTemp
                    dLng = pLngTemp
                    dAddress = pAddressTemp
                    acDropOffAddress.text = pAddressTemp

                    flipAddress = false
                } else {
                    pLat = pLatTemp
                    pLng = pLngTemp
                    pAddress = pAddressTemp
                    acPickupAddress.text = pAddressTemp

                    dLat = dLatTemp
                    dLng = dLngTemp
                    dAddress = dAddressTemp
                    acDropOffAddress.text = dAddressTemp

                    flipAddress = true
                }
            }
            R.id.llForMe -> {
                if (!isBookForFriend) {
                    ivProfile.hide()
                    if (SECRET_DB_KEY == "d190fc6d825399e5276673ee8467d415fd9de35ce5a4fadce32bbaee91cdc036" ||
                            SECRET_DB_KEY == "ad567131d8bf9d931405e8b68956dfa6") {
                        tvForMe.text = "Switch shipper"
                    }
                    else
                    {
                        tvForMe.text = getString(R.string.switchRider)
                    }
                    rlLocation.hide()
                    llBookForFriend.show()
                    isBookForFriend = true
                    if (serviceRequest?.bookingType?.isNotEmpty() == true) {
                        serviceRequest?.bookingType = ""
                        serviceRequest?.bookingFriendName = ""
                        serviceRequest?.bookingFriendCountryCode = ""
                        serviceRequest?.bookingFriendPhoneNumber = ""
                    }
                } else {
                    ivProfile.show()
                    tvForMe.text = getString(R.string.forMe)
                    rlLocation.show()
                    llBookForFriend.hide()
                    isBookForFriend = false
                }
            }

            R.id.llBookForMe -> {
                ivProfile.show()
                tvForMe.text = getString(R.string.forMe)
                rlLocation.show()
                llBookForFriend.hide()
                isBookForFriend = false
            }
            R.id.llBookForFriend -> {
                setUpBottomSheetDialog()
            }
            R.id.acPickupAddress -> {
                isPickupAddress = true
                if (ConfigPOJO.settingsResponse?.key_value?.is_location_selection_alert == "true" ||
                        ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
                    addressType = 1
                    selectAddressDialog(Constants.PLACEAUTOCOMPLETE_REQUESTCODE, addressType)
                } else {
                    openPlacePickerIntent(Constants.PLACEAUTOCOMPLETE_REQUESTCODE)
                }
            }
            R.id.acDropOffAddress -> {
                isPickupAddress = false
                isSelectedFromRecent = false
                if (ConfigPOJO.settingsResponse?.key_value?.is_location_selection_alert == "true" ||
                        ConfigPOJO.TEMPLATE_CODE == Constants.CORSA) {
                    addressType = 2
                    selectAddressDialog(Constants.PLACEAUTOCOMPLETE_REQUESTCODE, addressType)
                } else {
                    openPlacePickerIntent(Constants.PLACEAUTOCOMPLETE_REQUESTCODE)
                }
            }
            R.id.rlAddHome -> {
                if (tvHomeAddress.text.toString().isNotEmpty()) {
                    showChooseAddressDialog(serviceDetails?.addresses?.home ?: RecentItem())
                } else {
                    isHomeAddress = true
                    selectAddressDialog(Constants.ADD_HOME_WORK_ADDRESS_CODE, 5)
                }
            }
            R.id.rlAddWork -> {
                if (tvWorkAddress.text.toString().isNotEmpty()) {
                    serviceDetails?.addresses?.work?.let { showChooseAddressDialog(it) }
                } else {
                    isHomeAddress = false
                    selectAddressDialog(Constants.ADD_HOME_WORK_ADDRESS_CODE, 6)
                }

            }
            R.id.ivHomeEdit -> {
                isHomeAddress = true
                isEditHomeAddress = true
                selectAddressDialog(Constants.ADD_HOME_WORK_ADDRESS_CODE, 5)
            }
            R.id.onMap_rl -> {
                acDropOffAddress.performClick()
            }
            R.id.ivWorkEdit -> {
                isHomeAddress = false
                isEditWorkAddress = true
                selectAddressDialog(Constants.ADD_HOME_WORK_ADDRESS_CODE, 6)
            }
            R.id.ivAddLocation -> {
                if (serviceRequest?.stops!!.size < 4) {
                    priority += 1
                    serviceRequest?.stops?.add(StopsModel(0.0, 0.0, priority, "", "", "", ""))
                    onGoing_ride_stops.add(StopsModel(0.0, 0.0, priority, "", "", "", ""))
                    adapterStops?.notifyDataSetChanged()
                    rlMakeChanges.show()
                    tvRecentLocation.hide()
                    rvRecentLocations.hide()
                    rlAddHome.hide()
                    rlAddWork.hide()
                    tvConfirmDone.hide()

                    if (serviceRequest?.stops!!.size == 4) {
                        side_view.visibility = View.GONE
                        ivAddLocation.visibility = View.GONE
                    }
                }
            }
            R.id.tvDone -> {
                if (arguments?.containsKey("order") == false) {
                    if (validation()) {

                        var count = 0
                        for (i in serviceRequest?.stops!!.indices) {
                            count += 1
                            serviceRequest?.stops?.get(i)?.priority = count
                        }

                        val stopsList = serviceRequest?.stops
                        if (stopsList?.isNotEmpty() == true) {
                            (activity as HomeActivity).showMarker(LatLng(pLat, pLng), LatLng(dLat, dLng))
                            serviceRequest?.pickup_latitude = pLat
                            serviceRequest?.pickup_longitude = pLng
                            serviceRequest?.pickup_address = pAddress
                            serviceRequest?.dropoff_latitude = dLat
                            serviceRequest?.dropoff_longitude = dLng
                            serviceRequest?.dropoff_address = dAddress
                            val bundle = Bundle()
                            if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
                                serviceRequest?.future = ServiceRequestModel.BOOK_NOW
                                fragmentManager?.beginTransaction()?.replace(R.id.container, ConfirmBookingFragment())?.addToBackStack("backstack")?.commit()
                            } else {
                                if (bookingType == BOOKING_TYPE.ROAD_PICKUP) {
                                    val fragment = ConfirmBookingFragment()
                                    bundle.putString(Constants.BOOKING_TYPE, bookingType)
                                    fragment.arguments = bundle
                                    fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                                } else {
                                    val fragment = SelectVehicleTypeFragment()
                                    fragment.arguments = bundle
                                    fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                                }
                            }
                        }
                    }
                } else {
                    if (CheckNetworkConnection.isOnline(context)) {

                        var count = 0
                        for (i in onGoing_ride_stops!!.indices) {
                            count += 1
                            onGoing_ride_stops?.get(i)?.priority = count
                        }

                        var onGoing_ride_stopsNew = ArrayList<StopsModel>()
                        for (i in onGoing_ride_stops!!.indices) {

                            if (onGoing_ride_stops[i].stop_status != "ongoing") {
                                if (onGoing_ride_stops[i].stop_status != "reached") {
                                    onGoing_ride_stopsNew.add(onGoing_ride_stops[i])
                                }
                            }
                        }

                        val map = HashMap<String, String>()
                        map["order_id"] = order?.order_id.toString()
                        map["stops"] = Gson().toJson(onGoing_ride_stopsNew)
                        map["dropoff_latitude"] = order?.dropoff_latitude.toString()
                        map["dropoff_longitude"] = order?.dropoff_longitude.toString()
                        map["dropoff_address"] = order?.dropoff_address.toString()
                        presenter.requestAddStopsDuringRide(map)
                    } else {
                        CheckNetworkConnection.showNetworkError(rootView)
                    }
                }
            }

            R.id.tvConfirmDone -> {
                if (arguments?.containsKey("order") == false) {
                    if (validation()) {
                        if (CheckNetworkConnection.isOnline(activity)) {
                            (activity as HomeActivity).showMarker(LatLng(pLat, pLng), LatLng(dLat, dLng))
                            serviceRequest?.pickup_latitude = pLat
                            serviceRequest?.pickup_longitude = pLng
                            serviceRequest?.pickup_address = pAddress
                            serviceRequest?.dropoff_latitude = dLat
                            serviceRequest?.dropoff_longitude = dLng
                            serviceRequest?.dropoff_address = dAddress
                            serviceRequest?.isCurrentLocation = false
                            val bundle = Bundle()

                            if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
                                serviceRequest?.future = ServiceRequestModel.BOOK_NOW
                                fragmentManager?.beginTransaction()?.replace(R.id.container, ConfirmBookingFragment())?.addToBackStack("backstack")?.commit()
                            } else {
                                if (bookingType == BOOKING_TYPE.ROAD_PICKUP) {
                                    val fragment = ConfirmBookingFragment()
                                    bundle.putString(Constants.BOOKING_TYPE, bookingType)
                                    fragment.arguments = bundle
                                    fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                                } else {

                                    if(SECRET_DB_KEY == "c0cb897ef919b1c32ee9f672d74421228890d20962b7623535f9ab29d3e9f88a" || SECRET_DB_KEY == "b25dcb3ed072a432e0b76634d9d7ae4d"){

                                        if (serviceRequest?.category_id==13|| serviceRequest?.category_id==14||
                                                serviceRequest?.category_id==17)
                                        {
                                            serviceRequest?.category_brand_id = (activity as HomeActivity)?.serviceDetails?.categories?.get(0)?.category_brand_id
                                            serviceRequest?.brandName = (activity as HomeActivity)?.serviceDetails?.categories?.get(0)?.name
                                            serviceRequest?.brandImage = (activity as HomeActivity)?.serviceDetails?.categories?.get(0)?.image_url

                                            serviceRequest?.category_brand_product_id = (activity as HomeActivity)?.serviceDetails?.categories?.get(0)?.products?.get(0)?.category_brand_product_id
                                            serviceRequest?.selectedProduct = (activity as HomeActivity)?.serviceDetails?.categories?.get(0)?.products?.get(0)

                                            val fragment = EnterOrderDetailsFragment()
                                            fragment.arguments = bundle
                                            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                                        }
                                        else
                                        {
                                            val fragment = SelectVehicleTypeFragment()
                                            fragment.arguments = bundle
                                            fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                                        }
                                    }
                                    else
                                    {
                                        val fragment = SelectVehicleTypeFragment()
                                        fragment.arguments = bundle
                                        fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()
                                    }
                                }
                            }
                        } else {
                            CheckNetworkConnection.showNetworkError(rootView)
                        }
                    }
                } else {
                    if (CheckNetworkConnection.isOnline(context)) {
                        val map = HashMap<String, String>()
                        var count = 0

                        for (i in onGoing_ride_stops!!.indices) {
                            count += 1
                            onGoing_ride_stops?.get(i)?.priority = count
                        }

                        var onGoing_ride_stopsNew = ArrayList<StopsModel>()
                        for (i in onGoing_ride_stops!!.indices) {

                            if (onGoing_ride_stops[i].stop_status != "ongoing") {
                                if (onGoing_ride_stops[i].stop_status != "reached") {
                                    onGoing_ride_stopsNew.add(onGoing_ride_stops[i])
                                }
                            }
                        }

                        if (serviceRequest != null && isSelectedFromRecent) {
                            map["stops"] = Gson().toJson(onGoing_ride_stopsNew)
                            map["dropoff_latitude"] = serviceRequest?.dropoff_latitude.toString()
                            map["dropoff_longitude"] = serviceRequest?.dropoff_longitude.toString()
                            map["dropoff_address"] = serviceRequest?.dropoff_address.toString()
                        } else {
                            map["stops"] = Gson().toJson(onGoing_ride_stopsNew)
                            map["dropoff_latitude"] = order?.dropoff_latitude.toString()
                            map["dropoff_longitude"] = order?.dropoff_longitude.toString()
                            map["dropoff_address"] = order?.dropoff_address.toString()
                        }
                        map["order_id"] = order?.order_id.toString()
                        presenter.requestAddStopsDuringRide(map)
                    } else {
                        CheckNetworkConnection.showNetworkError(rootView)
                    }
                }
            }
            R.id.tvSelectdropOff -> {
                spDropOffAddress.performClick()
                spDropOffAddress.visibility = View.VISIBLE
                isDropClicked = true
            }
            R.id.tvSelectPickup -> {
                spPickupAddress.performClick()
                spPickupAddress.visibility = View.VISIBLE
                isPickClicked = true
            }
        }
    }

    private fun validation(): Boolean {
          if (acPickupAddress.text.toString().trim().isEmpty()) {
                if (ConfigPOJO.is_omco == "true") {
                    rootView?.showSnack(getString(R.string.selected_pickup_doesnot_exist))
                } else {
                    rootView?.showSnack(getString(R.string.please_enter_pickup))
                }
                return false
        }
        if (acDropOffAddress?.text.toString().trim().isEmpty()) {
            if (ConfigPOJO.is_omco == "true") {
                rootView?.showSnack(getString(R.string.selected_detsination_does_not_exist))
            } else {
                rootView?.showSnack(getString(R.string.please_enter_dropoff))
            }
            return false
        }
        return true
    }

    private fun showAppStopsWarningDialog() {
        val dialog = AlertDialogUtil.getInstance().createOkCancelDialog(context,
                R.string.attention, R.string.toBeMakeSure, R.string.ok, R.string.cancel, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        dialog?.cancel()
                        selectAddressDialog(Constants.ADD_STOPS_REQUEST_CODE, 3)
                    }

                    override fun onCancelButtonClicked() {
                        dialog?.cancel()
                    }
                })
        dialog?.show()
    }

    override fun onApiSuccess(order: Order) {
        openBookingConfirmedFragment(Gson().toJson(order))
    }

    private fun openBookingConfirmedFragment(orderId: String?) {
        val fragment = DeliveryStartsFragment()
        val bundle = Bundle()
        bundle.putString("order", orderId)
        fragment.arguments = bundle
        try {
            fragmentManager?.popBackStack("backstack", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            fragmentManager?.beginTransaction()?.add(R.id.container, fragment)?.addToBackStack("backstack")?.commitAllowingStateLoss()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun showLoader(isLoading: Boolean) {
        progressDialog?.show(isLoading)
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

    private fun setUpBottomSheetDialog() {
        val layoutDialog = View.inflate(context, R.layout.dialog_where_u_going, null)
        dialog = BottomSheetDialog(context as Activity)
        dialog?.setContentView(layoutDialog)
        val bottomSheetInternal = dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet) as View?
        bottomSheetInternal?.let {
            BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
        }
        val fullName = (layoutDialog.findViewById<EditText>(R.id.etFullName))
        val relationShip = (layoutDialog.findViewById<EditText>(R.id.etRelationStatus))
        val phoneNumber = (layoutDialog.findViewById<EditText>(R.id.etPhone))
        val countryCodePicker = (layoutDialog.findViewById<CountryCodePicker>(R.id.countryCodePicker))
        val btnContinue = (layoutDialog.findViewById<TextView>(R.id.tvContinue))
        val tvCancel = (layoutDialog.findViewById<TextView>(R.id.tvCancel))
        val cvCountry = (layoutDialog.findViewById<LinearLayout>(R.id.cvCountry))
        val ivAddContacts = (layoutDialog.findViewById<ImageView>(R.id.ivAddContacts))

        ivAddContacts.visibility = View.GONE
        tvCancel.visibility = View.GONE

        if (ConfigPOJO.settingsResponse?.key_value?.iso_code?.length == 2) {
            countryCodePicker.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.toLowerCase())
        } else {
            countryCodePicker.setCountryForNameCode(ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                    ?: 0)?.toLowerCase())
        }

        fullName.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        relationShip.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        phoneNumber.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        cvCountry.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        tvCancel.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.Btn_Colour, GradientDrawable.RECTANGLE)
        btnContinue.background = StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour, GradientDrawable.RECTANGLE)
        btnContinue.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour))
        tvCancel.setTextColor(Color.parseColor(ConfigPOJO.primary_color))

        tvCancel.setOnClickListener {
            dialog?.dismiss()
        }

        btnContinue.setOnClickListener {
            countryCodePicker.setPhoneNumberValidityChangeListener(phoneNoValidationChangeListener)
            when {
                fullName.text.toString().trim().isEmpty() -> Toast.makeText(btnContinue.context, getString(R.string.fullname_empty_validation_message), Toast.LENGTH_SHORT).show()
                phoneNumber.text.toString().trim().isEmpty() -> Toast.makeText(btnContinue.context, getString(R.string.phone_empty_validation_message), Toast.LENGTH_SHORT).show()
                ValidationUtils.isPhoneNumberAllZeros(phoneNumber.text.toString()) -> Toast.makeText(btnContinue.context, getString(R.string.phone_valid_validation_message), Toast.LENGTH_SHORT).show()
                relationShip.text.toString().trim().isEmpty() -> Toast.makeText(btnContinue.context, getString(R.string.relationship_empty_validation_message), Toast.LENGTH_SHORT).show()
                else -> {
                    serviceRequest?.bookingType = BOOKING_TYPE.FRIEND
                    serviceRequest?.bookingFriendName = fullName.text.toString().trim()
                    serviceRequest?.bookingFriendCountryCode = countryCodePicker?.selectedCountryCodeWithPlus.toString()
                    serviceRequest?.bookingFriendPhoneNumber = phoneNumber.text.toString().trim()
                    serviceRequest?.bookingFriendRelation = relationShip.text.toString().trim()
                    ivProfile.show()
                    tvForMe.text = getString(R.string.forFriend)
                    rlLocation.show()
                    llBookForFriend.hide()
                    isBookForFriend = false
                    dialog?.dismiss()
                }
            }
        }
        dialog?.show()
    }

    private val phoneNoValidationChangeListener = CountryCodePicker.PhoneNumberValidityChangeListener {
        isPhoneNoValid = it
    }

    private fun openPlacePickerIntent(requestCode: Int) {
        try {
            // Set the fields to specify which types of place data to return.
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
            // Start the autocomplete intent
            var intent: Intent? = null
            if (ConfigPOJO.settingsResponse?.key_value?.is_countrycheck == "true") {

                var country_code=""
                if (ConfigPOJO.settingsResponse?.key_value?.iso_code?.length == 2) {
                   country_code=ConfigPOJO.settingsResponse?.key_value?.iso_code?.toLowerCase()?:""
                } else {
                    country_code=ConfigPOJO.settingsResponse?.key_value?.iso_code?.substring(0, ConfigPOJO.settingsResponse?.key_value?.iso_code?.length?.minus(1)
                            ?: 0)?.toLowerCase()?:""
                }

                intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .setCountry(country_code).build(context as Activity)
            } else {
                intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(context as Activity)
            }

            startActivityForResult(intent, requestCode)
        } catch (e: GooglePlayServicesRepairableException) {
            Log.e(TAG, "Exception : " + e.message)
        } catch (e: GooglePlayServicesNotAvailableException) {
            Log.e(TAG, "Exception : " + e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Exception : " + e.message)
        }
    }

    private fun setStopsAddress(address: String, placeLatLng: LatLng) {
        if (arguments?.containsKey("order") == false) {

            for (i in serviceRequest?.stops!!.indices) {
                if (itemPosition == i) {
                    serviceRequest?.stops?.get(i)?.address = address
                    serviceRequest?.stops?.get(i)?.latitude = placeLatLng?.latitude
                    serviceRequest?.stops?.get(i)?.longitude = placeLatLng?.longitude
                }
            }
            adapterStops?.notifyDataSetChanged()
        } else {
            if (onGoing_ride_stops.size > 0) {
                for (i in onGoing_ride_stops.indices) {
                    if (itemPosition == i) {
                        serviceRequest?.stops?.get(i)?.address = address
                        serviceRequest?.stops?.get(i)?.latitude = placeLatLng?.latitude
                        serviceRequest?.stops?.get(i)?.longitude = placeLatLng?.longitude

                        onGoing_ride_stops.get(i).address = address
                        onGoing_ride_stops.get(i).latitude = placeLatLng?.latitude
                        onGoing_ride_stops.get(i).longitude = placeLatLng?.longitude
                    }
                }
                adapterStops?.notifyDataSetChanged()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.PLACEAUTOCOMPLETE_REQUESTCODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    dialog?.dismiss()

                    if (data?.hasExtra("address") == true) {
                        val address = data?.getStringExtra("address")
                        val placeLatLng = LatLng(data?.getStringExtra("lat")?.toDouble() ?: 0.0,
                                data?.getStringExtra("lng")?.toDouble() ?: 0.0)
                        Log.e(TAG, "Place Lat : " + placeLatLng?.latitude + " Place Lon : " + placeLatLng?.longitude)
                        if (isPickupAddress) {
                            /* pickup address */
                            pLat = placeLatLng?.latitude ?: 0.0
                            pLng = placeLatLng?.longitude ?: 0.0
                            pAddress = address ?: ""
                            pLatTemp = placeLatLng?.latitude ?: 0.0
                            pLngTemp = placeLatLng?.longitude ?: 0.0
                            pAddressTemp = address ?: ""
                            acPickupAddress.text = address
                            if (getString(R.string.app_name) == "Wasila") {
                                (activity as HomeActivity).showMarkerNearByPlaces(LatLng(pLat, pLng), LatLng(dLat, dLng))
                            }
                        } else {
                            serviceRequest?.addressName = address
                            /* drop address */
                            if (arguments?.containsKey("order") == true) {
                                /* In case of change destination during ride */
                                order?.dropoff_address = address
                                order?.dropoff_latitude = placeLatLng?.latitude
                                order?.dropoff_longitude = placeLatLng?.longitude
                                acDropOffAddress.text = address
                            } else {
                                dLat = placeLatLng?.latitude ?: 0.0
                                dLng = placeLatLng?.longitude ?: 0.0
                                dAddress = address ?: ""
                                dLatTemp = placeLatLng?.latitude ?: 0.0
                                dLngTemp = placeLatLng?.longitude ?: 0.0
                                dAddressTemp = address ?: ""
                                acDropOffAddress.text = address
                            }
                        }
                    } else {
                        val place = if (data?.hasExtra("place") ?: false) {
                            data?.getParcelableExtra<Place>("place")
                        } else {
                            Autocomplete.getPlaceFromIntent(data ?: Intent())
                        }
                        val address = place?.address ?: ""
                        val placeLatLng = place?.latLng
                        Log.e(TAG, "Place Lat : " + placeLatLng?.latitude + " Place Lon : " + placeLatLng?.longitude)
                        if (isPickupAddress) {
                            /* pickup address */
                            pLat = placeLatLng?.latitude ?: 0.0
                            pLng = placeLatLng?.longitude ?: 0.0
                            pAddress = address
                            pLatTemp = placeLatLng?.latitude ?: 0.0
                            pLngTemp = placeLatLng?.longitude ?: 0.0
                            pAddressTemp = address
                            acPickupAddress.text = address
                            if (getString(R.string.app_name) == "Wasila") {
                                (activity as HomeActivity).showMarkerNearByPlaces(LatLng(pLat, pLng), LatLng(dLat, dLng))
                            }
                        } else {
                            serviceRequest?.addressName = place?.name.toString()
                            /* drop address */
                            if (arguments?.containsKey("order") == true) {
                                /* In case of change destination during ride */
                                order?.dropoff_address = place?.address
                                order?.dropoff_latitude = place?.latLng?.latitude
                                order?.dropoff_longitude = place?.latLng?.longitude
                                acDropOffAddress.text = address
                            } else {
                                dLat = placeLatLng?.latitude ?: 0.0
                                dLng = placeLatLng?.longitude ?: 0.0
                                dAddress = address
                                dLatTemp = placeLatLng?.latitude ?: 0.0
                                dLngTemp = placeLatLng?.longitude ?: 0.0
                                dAddressTemp = address
                                acDropOffAddress.text = address
                            }
                        }
                        Log.e(TAG, "Place: " + place?.address + ", " + place?.latLng)
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data ?: Intent())
                    Log.e(TAG, status.statusMessage ?: "")
                }
                Activity.RESULT_CANCELED -> // The user canceled the operation.
                    Toast.makeText(context as Activity, getString(R.string.requestCancelled), Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == Constants.ADD_STOPS_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {

                    if (data?.hasExtra("address") == true) {
                        val address = data?.getStringExtra("address")
                        val placeLatLng = LatLng(data?.getStringExtra("lat")?.toDouble() ?: 0.0,
                                data?.getStringExtra("lng")?.toDouble() ?: 0.0)
                        placeLatLng?.let {
                            setStopsAddress(address ?: "", it)
                        }
                    } else {
                        val place = if (data?.hasExtra("place") ?: false) {
                            data?.getParcelableExtra<Place>("place")
                        } else {
                            Autocomplete.getPlaceFromIntent(data ?: Intent())
                        }
                        val address = place?.address ?: ""
                        val placeLatLng = place?.latLng
                        placeLatLng?.let {
                            setStopsAddress(address, it)
                        }
                    }
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(context as Activity, getString(R.string.requestCancelled), Toast.LENGTH_SHORT).show()
                }

                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data ?: Intent())
                    Log.e(TAG, status.statusMessage ?: "")
                }
            }
        } else if (requestCode == Constants.ADD_HOME_WORK_ADDRESS_CODE && resultCode == Activity.RESULT_OK && data != null) {
            when (resultCode) {
                Activity.RESULT_OK -> {

                    if (data?.hasExtra("address") == true) {
                        val address = data?.getStringExtra("address")
                        val placeLatLng = LatLng(data?.getStringExtra("lat")?.toDouble() ?: 0.0,
                                data?.getStringExtra("lng")?.toDouble() ?: 0.0)

                        if (isHomeAddress) {
                            /* Set Home Address Here */
                            tvAddHomeTitle.text = address
                            tvHomeAddress.text = address
                            tvHomeAddress.show()

                            serviceDetails?.addresses?.home?.address = address
                            serviceDetails?.addresses?.home?.addressName = address
                            serviceDetails?.addresses?.home?.addressLatitude = placeLatLng?.latitude
                            serviceDetails?.addresses?.home?.addressLongitude = placeLatLng?.longitude

                            if (CheckNetworkConnection.isOnline(context)) {
                                val hashmap = HashMap<String, String>()
                                hashmap["address"] = address ?: ""
                                hashmap["address_name"] = address.toString()
                                hashmap["address_latitude"] = placeLatLng?.latitude.toString()
                                hashmap["address_longitude"] = placeLatLng?.longitude.toString()
                                hashmap["category"] = ADDRESS_TYPE.HOME
                                if (isEditHomeAddress) {
//                                hashmap["user_address_id"] = serviceRequest?.
                                } else {
                                    presenter.requestHomeWorkAddress(hashmap)
                                }
                            } else {
                                CheckNetworkConnection.showNetworkError(rootView)
                            }
                        } else {
                            /* Set Work Address Here */
                            tvAddWorkTitle.text = address
                            tvWorkAddress.text = address
                            tvWorkAddress.show()
                            serviceDetails?.addresses?.work?.address = address
                            serviceDetails?.addresses?.work?.addressName = address
                            serviceDetails?.addresses?.work?.addressLatitude = placeLatLng?.latitude
                            serviceDetails?.addresses?.work?.addressLongitude = placeLatLng?.longitude

                            if (CheckNetworkConnection.isOnline(context)) {
                                val hashmap = HashMap<String, String>()
                                hashmap["address"] = address ?: ""
                                hashmap["address_name"] = address.toString()
                                hashmap["address_latitude"] = placeLatLng?.latitude.toString()
                                hashmap["address_longitude"] = placeLatLng?.longitude.toString()
                                hashmap["category"] = ADDRESS_TYPE.WORK
                                if (isEditWorkAddress) {
                                    // hashmap["user_address_id"] = serviceRequest?.
                                } else {
                                    presenter.requestHomeWorkAddress(hashmap)
                                }
                            } else {
                                CheckNetworkConnection.showNetworkError(rootView)
                            }
                        }
                    } else {
                        val place = if (data?.hasExtra("place") ?: false) {
                            data?.getParcelableExtra<Place>("place")
                        } else {
                            Autocomplete.getPlaceFromIntent(data)
                        }
                        val address = place?.address ?: ""
                        val placeLatLng = place?.latLng
                        Log.e(TAG, "Place Lat : " + placeLatLng?.latitude + " Place Lon : " + placeLatLng?.longitude)
                        if (isHomeAddress) {
                            /* Set Home Address Here */
                            tvAddHomeTitle.text = place?.name
                            tvHomeAddress.text = address
                            tvHomeAddress.show()

                            serviceDetails?.addresses?.home?.address = place?.address
                            serviceDetails?.addresses?.home?.addressName = place?.address
                            serviceDetails?.addresses?.home?.addressLatitude = place?.latLng?.latitude
                            serviceDetails?.addresses?.home?.addressLongitude = place?.latLng?.longitude

                            if (CheckNetworkConnection.isOnline(context)) {
                                val hashmap = HashMap<String, String>()
                                hashmap["address"] = address
                                hashmap["address_name"] = place?.name.toString()
                                hashmap["address_latitude"] = placeLatLng?.latitude.toString()
                                hashmap["address_longitude"] = placeLatLng?.longitude.toString()
                                hashmap["category"] = ADDRESS_TYPE.HOME
                                if (isEditHomeAddress) {
                                    hashmap["user_address_id"] = serviceDetails?.addresses?.home?.userAddressId.toString()
                                    presenter.requestEditHomeWorkAddress(hashmap)
                                } else {
                                    presenter.requestHomeWorkAddress(hashmap)
                                }
                            } else {
                                CheckNetworkConnection.showNetworkError(rootView)
                            }
                        } else {
                            /* Set Work Address Here */
                            tvAddWorkTitle.text = place?.name
                            tvWorkAddress.text = address
                            tvWorkAddress.show()
                            serviceDetails?.addresses?.work?.address = place?.address
                            serviceDetails?.addresses?.work?.addressName = place?.address
                            serviceDetails?.addresses?.work?.addressLatitude = place?.latLng?.latitude
                            serviceDetails?.addresses?.work?.addressLongitude = place?.latLng?.longitude

                            if (CheckNetworkConnection.isOnline(context)) {
                                val hashmap = HashMap<String, String>()
                                hashmap["address"] = address
                                hashmap["address_name"] = place?.name.toString()
                                hashmap["address_latitude"] = placeLatLng?.latitude.toString()
                                hashmap["address_longitude"] = placeLatLng?.longitude.toString()
                                hashmap["category"] = ADDRESS_TYPE.WORK
                                if (isEditWorkAddress) {
                                    hashmap["user_address_id"] = serviceDetails?.addresses?.work?.userAddressId.toString()
                                    presenter.requestEditHomeWorkAddress(hashmap)
                                } else {
                                    presenter.requestHomeWorkAddress(hashmap)
                                }
                            } else {
                                CheckNetworkConnection.showNetworkError(rootView)
                            }
                            Log.e(TAG, "Place: " + place?.address + ", " + place?.latLng)
                        }
                    }
                }

                Activity.RESULT_CANCELED -> {
                    //Toast.makeText(context as Activity, getString(R.string.requestCancelled), Toast.LENGTH_SHORT).show()
                }

                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data)
                    Log.e(TAG, status.statusMessage ?: "")
                }
            }
        } else if (requestCode == CONFIRM_PICKUP_LOC) {
            if (data?.getIntExtra("CREATE_RIDE", 0) == 1) {
                pLat = data.getDoubleExtra(Constants.LAT, 0.0)
                pLng = data.getDoubleExtra(Constants.LNG, 0.0)
                pAddress = data.getStringExtra(Constants.ADDRESS)!!

                pLatTemp = data.getDoubleExtra(Constants.LAT, 0.0)
                pLngTemp = data.getDoubleExtra(Constants.LNG, 0.0)
                pAddressTemp =  data.getStringExtra(Constants.ADDRESS)!!

                acPickupAddress.text = pAddress
                if (getString(R.string.app_name) == "Wasila") {
                    (activity as HomeActivity).showMarkerNearByPlaces(LatLng(pLat, pLng), LatLng(dLat, dLng))
                }
            } else if (data?.getIntExtra("CREATE_RIDE", 0) == 2) {
                serviceRequest?.addressName = data.getStringExtra(Constants.ADDRESS)
                /* drop address */
                if (arguments?.containsKey("order") == true) {
                    /* In case of change destination during ride */
                    order?.dropoff_address = data.getStringExtra(Constants.ADDRESS)
                    order?.dropoff_latitude = data.getDoubleExtra(Constants.LAT, 0.0)
                    order?.dropoff_longitude = data.getDoubleExtra(Constants.LNG, 0.0)
                    acDropOffAddress.text = data.getStringExtra(Constants.ADDRESS)

                    dLatTemp = data.getDoubleExtra(Constants.LAT, 0.0)
                    dLngTemp =data.getDoubleExtra(Constants.LNG, 0.0)
                    dAddressTemp = data.getStringExtra(Constants.ADDRESS) ?: ""
                } else {
                    dLat = data.getDoubleExtra(Constants.LAT, 0.0)
                    dLng = data.getDoubleExtra(Constants.LNG, 0.0)
                    dAddress = data.getStringExtra(Constants.ADDRESS) ?: ""

                    dLatTemp = data.getDoubleExtra(Constants.LAT, 0.0)
                    dLngTemp =data.getDoubleExtra(Constants.LNG, 0.0)
                    dAddressTemp = data.getStringExtra(Constants.ADDRESS) ?: ""
                    acDropOffAddress.text = dAddress
                }
            } else if (data?.getIntExtra("CREATE_RIDE", 0) == 3) {
                val address = data.getStringExtra(Constants.ADDRESS)
                val placeLatLng = LatLng(data.getDoubleExtra(Constants.LAT, 0.0), data.getDoubleExtra(Constants.LNG, 0.0))
                placeLatLng.let {
                    setStopsAddress(address!!, it)
                }
            } else if (data?.getIntExtra("CREATE_RIDE", 0) == 5) {
                tvAddHomeTitle.text = data.getStringExtra(Constants.ADDRESS)
                tvHomeAddress.text = data.getStringExtra(Constants.ADDRESS)
                tvHomeAddress.show()

                serviceDetails?.addresses?.home?.address = data.getStringExtra(Constants.ADDRESS)
                serviceDetails?.addresses?.home?.addressName = data.getStringExtra(Constants.ADDRESS)
                serviceDetails?.addresses?.home?.addressLatitude = data.getDoubleExtra(Constants.LAT, 0.0)
                serviceDetails?.addresses?.home?.addressLongitude = data.getDoubleExtra(Constants.LNG, 0.0)

                if (CheckNetworkConnection.isOnline(context)) {
                    val hashmap = HashMap<String, String>()
                    hashmap["address"] = data.getStringExtra(Constants.ADDRESS) ?: ""
                    hashmap["address_name"] = data.getStringExtra(Constants.ADDRESS) ?: ""
                    hashmap["address_latitude"] = data.getDoubleExtra(Constants.LAT, 0.0).toString()
                    hashmap["address_longitude"] = data.getDoubleExtra(Constants.LNG, 0.0).toString()
                    hashmap["category"] = ADDRESS_TYPE.HOME
                    if (isEditHomeAddress) {
                        hashmap["user_address_id"] = serviceDetails?.addresses?.home?.userAddressId.toString()
                        presenter.requestEditHomeWorkAddress(hashmap)
                    } else {
                        presenter.requestHomeWorkAddress(hashmap)
                    }
                } else {
                    CheckNetworkConnection.showNetworkError(rootView)
                }
            } else if (data?.getIntExtra("CREATE_RIDE", 0) == 6) {
                tvAddWorkTitle.text = data.getStringExtra(Constants.ADDRESS) ?: ""
                tvWorkAddress.text = data.getStringExtra(Constants.ADDRESS) ?: ""
                tvWorkAddress.show()
                serviceDetails?.addresses?.work?.address = data.getStringExtra(Constants.ADDRESS)
                        ?: ""
                serviceDetails?.addresses?.work?.addressName = data.getStringExtra(Constants.ADDRESS)
                        ?: ""
                serviceDetails?.addresses?.work?.addressLatitude = data.getDoubleExtra(Constants.LAT, 0.0)
                serviceDetails?.addresses?.work?.addressLongitude = data.getDoubleExtra(Constants.LNG, 0.0)

                if (CheckNetworkConnection.isOnline(context)) {
                    val hashmap = HashMap<String, String>()
                    hashmap["address"] = data.getStringExtra(Constants.ADDRESS) ?: ""
                    hashmap["address_name"] = data.getStringExtra(Constants.ADDRESS) ?: ""
                    hashmap["address_latitude"] = data.getDoubleExtra(Constants.LAT, 0.0).toString()
                    hashmap["address_longitude"] = data.getDoubleExtra(Constants.LNG, 0.0).toString()
                    hashmap["category"] = ADDRESS_TYPE.WORK
                    if (isEditWorkAddress) {
                        hashmap["user_address_id"] = serviceDetails?.addresses?.work?.userAddressId.toString()
                        presenter.requestEditHomeWorkAddress(hashmap)
                    } else {
                        presenter.requestHomeWorkAddress(hashmap)
                    }
                } else {
                    CheckNetworkConnection.showNetworkError(rootView)
                }
            }
        }
    }

    override fun onApiHomeWorkSuccess(response: HomeWorkResponse) {
        if (isHomeAddress) {
            ivHomeEdit.show()
        } else {
            ivWorkEdit.show()
        }
    }

    override fun onApiEditHomeWorkSuccess(response: HomeWorkResponse) {
        if (isEditHomeAddress) {
            ivHomeEdit.show()
        }

        if (isEditWorkAddress) {
            ivWorkEdit.show()
        }
    }

    override fun onApiLocationsSuccess(response: GetAddressesPojo) {
        if (response.result!!.size > 0) {
            addressesList = response.result as ArrayList<ResultItem>
            setUpLocationDropdown(addressesList)
        }
    }

    private fun setUpLocationDropdown(addressesList: ArrayList<ResultItem>) {
        if (serviceRequest?.category_name == "Pickup") {
            locationDropdownAdapter = LocationDropdownAdapter(activity!!, addressesList)
            spDropOffAddress.adapter = locationDropdownAdapter
            spDropOffAddress.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (isDropClicked) {
                        dLat = addressesList[position].latitude?.toDouble() ?: 0.0
                        dLng = addressesList[position].longitude?.toDouble() ?: 0.0
                        dAddress = addressesList[position].address + ", " + addressesList[position].city + ", " + addressesList[position].state
                        serviceRequest?.selected_address_id = addressesList[position].id.toString()

                        acDropOffAddress.text = dAddress
                        tvSelectdropOff.text = dAddress
                        tvSelectdropOff.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        } else if (serviceRequest?.category_name == "Receive") {
            locationDropdownAdapter = LocationDropdownAdapter(activity!!, addressesList)
            spPickupAddress.adapter = locationDropdownAdapter
            spPickupAddress.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (isPickClicked) {
                        pLat = addressesList[position].latitude?.toDouble() ?: 0.0
                        pLng = addressesList[position].longitude?.toDouble() ?: 0.0
                        pAddress = addressesList[position].address + ", " + addressesList[position].city + ", " + addressesList[position].state
                        serviceRequest?.selected_address_id = addressesList[position].id.toString()

                        acPickupAddress.text = pAddress
                        tvSelectPickup.text = pAddress
                        tvSelectPickup.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog?.cancel()
        presenter.detachView()
    }
}