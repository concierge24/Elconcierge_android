package com.trava.user.ui.home.orderdetails.heavyloads

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.transition.Explode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.ui.home.HomeActivity
import com.trava.user.ui.home.ScheduleFragment
import com.trava.user.ui.home.comfirmbooking.ConfirmBookingFragment
import com.trava.user.webservices.models.homeapi.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trava.driver.webservices.models.WalletBalModel
import com.trava.user.databinding.*
import com.trava.user.ui.home.comfirmbooking.ConfirmBookingContract
import com.trava.user.ui.home.comfirmbooking.ConfirmBookingPresenter
import com.trava.user.ui.home.dropofflocation.SetupDropOffLocationFragment
import com.trava.user.ui.home.orderdetails.omcoproducts.ProductCheckListAdapter
import com.trava.user.ui.home.orderdetails.omcoproducts.ProductListAdapter
import com.trava.user.utils.*
import com.trava.user.utils.PermissionUtils
import com.trava.user.webservices.models.*
import com.trava.user.webservices.models.CheckListItem
import com.trava.user.webservices.models.CheckListModel
import com.trava.user.webservices.models.checkList.CheckListPresenter
import com.trava.user.webservices.models.checkList.CheckListcontractor
import com.trava.user.webservices.models.contacts.RideShareResponse
import com.trava.user.webservices.models.order.Order
import com.trava.user.webservices.models.orderrequest.ResultItem
import com.trava.utilities.*
import com.trava.utilities.Constants.DELIVERY20
import com.trava.utilities.Constants.SECRET_DB_KEY
import com.trava.utilities.constants.PrefsConstant.CURRENT_LOCATION
import com.trava.utilities.webservices.models.CurrentLocationModel
import kotlinx.android.synthetic.main.enter_order_details_5.*
import kotlinx.android.synthetic.main.enter_order_details_5.tvSchedule
import kotlinx.android.synthetic.main.fragment_confirm_booking.*
import kotlinx.android.synthetic.main.fragment_confirm_booking.carpool_switch
import kotlinx.android.synthetic.main.fragment_enter_order_details2.*
import kotlinx.android.synthetic.main.fragment_enter_order_details2.checklist_rv
import kotlinx.android.synthetic.main.fragment_enter_order_details2.ed_price
import kotlinx.android.synthetic.main.fragment_enter_order_details2.ed_product_name
import kotlinx.android.synthetic.main.fragment_enter_order_details2.etDeliverPerson
import kotlinx.android.synthetic.main.fragment_enter_order_details2.etDeliverpersonName
import kotlinx.android.synthetic.main.fragment_enter_order_details2.etPhone
import kotlinx.android.synthetic.main.fragment_enter_order_details2.etTypeOfMaterial
import kotlinx.android.synthetic.main.fragment_enter_order_details2.etWeight
import kotlinx.android.synthetic.main.fragment_enter_order_details2.group
import kotlinx.android.synthetic.main.fragment_enter_order_details2.imageView2
import kotlinx.android.synthetic.main.fragment_enter_order_details2.rootView
import kotlinx.android.synthetic.main.fragment_enter_order_details2.rvCompanies
import kotlinx.android.synthetic.main.fragment_enter_order_details2.tvNext
import kotlinx.android.synthetic.main.fragment_enter_order_details2.tv_total
import kotlinx.android.synthetic.main.fragment_enter_order_details3.*
import kotlinx.android.synthetic.main.fragment_select_ride_type.*
import kotlinx.android.synthetic.main.fragment_enter_order_details6.*
import permissions.dispatcher.*

@RuntimePermissions
class EnterOrderDetailsFragment : Fragment(), AddImageAdapter.AddImageCallback, View.OnClickListener, CheckListcontractor.View,
        CheckListAdapter.OnInfocallBack, ProductListAdapter.ListClickCallback, ProductCheckListAdapter.CheckListClickCallback, ConfirmBookingContract.View {

    private val presenter = ConfirmBookingPresenter()

    private val imageList = ArrayList<String>()
    private var dialogIndeterminate: DialogIndeterminate? = null

    private var adapter: AddImageAdapter? = null
    var checkListAdapter: CheckListAdapter? = null
    private var serviceRequest: ServiceRequestModel? = null
    private var checkListPresenter = CheckListPresenter()
    var OrderView: View? = null
    private lateinit var delivery_type_Adapter: ArrayAdapter<String>
    private lateinit var dropLeveAdapter: ArrayAdapter<String>
    private lateinit var pickUpAdapter: ArrayAdapter<String>
    var currentLocation: CurrentLocationModel? = null
    var isPickupElevator: Boolean = true
    var isDropOffElevator: Boolean = true
    var isFragile: Boolean = false
    var spDropLevelValue: String? = ""
    var spPickupLevelValue: String? = ""
    var typeSelected: String? = ""

    private var pickupLevel = ArrayList<String>()
    private var dropLevel = ArrayList<String>()
    private var delivery_type = ArrayList<String>()
    private var package_material = ArrayList<String>()
    private var receiver_school = ArrayList<String>()
    var isCheckList: Boolean = false
    var checkListModelList = ArrayList<CheckListModel>()
    var checkListModel: CheckListModel? = null

    var productList = ArrayList<ProductListModel>()
    var productListModel: ProductListModel? = null
    private var productListAdapter: ProductListAdapter? = null

    var productCheckList = ArrayList<ProductCheckListModel>()
    var productcheckListModel: ProductCheckListModel? = null
    private var productCheckListAdapter: ProductCheckListAdapter? = null

    private lateinit var mProduct: Product
    lateinit var enterOrderDetails2Binding: FragmentEnterOrderDetails2Binding
    lateinit var enterOrderDetails3Binding: FragmentEnterOrderDetails3Binding
    lateinit var enterOrderDetails4Binding: FragmentEnterOrderDetails4Binding
    lateinit var enterOrderDetails5Binding: EnterOrderDetails5Binding
    lateinit var enterOrderDetails6Binding: FragmentEnterOrderDetails6Binding
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
        enterOrderDetails2Binding = DataBindingUtil.inflate(inflater, R.layout.fragment_enter_order_details2, container, false)
        enterOrderDetails3Binding = DataBindingUtil.inflate(inflater, R.layout.fragment_enter_order_details3, container, false)
        enterOrderDetails4Binding = DataBindingUtil.inflate(inflater, R.layout.fragment_enter_order_details4, container, false)
        enterOrderDetails5Binding = DataBindingUtil.inflate(inflater, R.layout.enter_order_details_5, container, false)
        enterOrderDetails6Binding = DataBindingUtil.inflate(inflater, R.layout.fragment_enter_order_details6, container, false)

        presenter.attachView(this)
        dialogIndeterminate = DialogIndeterminate(requireActivity())
        enterOrderDetails2Binding.color = ConfigPOJO.Companion
        enterOrderDetails3Binding.color = ConfigPOJO.Companion
        enterOrderDetails4Binding.color = ConfigPOJO.Companion
        serviceRequest = (activity as HomeActivity).serviceRequestModel
        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
            OrderView = enterOrderDetails3Binding.root
            setInitialViewData()
        } else if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
            OrderView = enterOrderDetails4Binding.root
        } else if (ConfigPOJO.TEMPLATE_CODE == Constants.DELIVERY20) {
            OrderView = enterOrderDetails5Binding.root
        }  else {
            OrderView = enterOrderDetails2Binding.root
        }

        if (getString(R.string.app_name) == "Hood User") {
            enterOrderDetails2Binding.etTypeOfMaterial.setHint(getString(R.string.car_plate_no))
            enterOrderDetails2Binding.etWeight.setHint(getString(R.string.car_type))
        }

        if(SECRET_DB_KEY == "d190fc6d825399e5276673ee8467d415cebf52a7d89e2a2f532b4d8d1268fbc7"){
            enterOrderDetails2Binding.checkTv.text = getString(R.string.purchase_list)
            enterOrderDetails2Binding.tvItem.text = getString(R.string.enter_purchase_list_items)
        }

        return OrderView
    }

    private fun validation(): Boolean {
        if (etTypeOfMaterial.text.toString().trim().isEmpty()) {
            rootView.showSnack(getString(R.string.enter_type_of_material))
            return false
        }

        if (ConfigPOJO.TEMPLATE_CODE != Constants.EAGLE) {
            if (etPhone.text.toString().trim().isEmpty()) {
                rootView.showSnack(getString(R.string.enter_phone_number))
                return false
            }
        }

        if (etDeliverpersonName.text.toString().trim().isEmpty()) {
            rootView.showSnack(getString(R.string.enter_name_of_person_to_deliver))
            return false
        }

        if (etDeliverPerson.text.toString().trim().isEmpty()) {
            rootView.showSnack(getString(R.string.enter_the_sender_s_name))
            return false
        }
        return true
    }

    private fun setInitialViewData() {
        enterOrderDetails3Binding.rlTab.visibility = GONE
        enterOrderDetails3Binding.nestedScroll.visibility = VISIBLE
        enterOrderDetails3Binding.rlChecklist.visibility = GONE
        dropLevel = ArrayList<String>()
        pickupLevel = ArrayList<String>()

        dropLevel.add("What floor is the drop off?")
        dropLevel.add("Ground Floor")

        pickupLevel.add("What floor is the pick up?")
        pickupLevel.add("Ground Floor")

        for (i in 1..10) {
            dropLevel.add("Floor $i")
            pickupLevel.add("Floor $i")
        }

        dropLeveAdapter = ArrayAdapter(activity!!, R.layout.layout_spinner_capacity, dropLevel)
        dropLeveAdapter.setDropDownViewResource(R.layout.item_capacity)
        enterOrderDetails3Binding!!.spDropLevel.adapter = dropLeveAdapter

        pickUpAdapter = ArrayAdapter(activity!!, R.layout.layout_spinner_capacity, pickupLevel)
        pickUpAdapter.setDropDownViewResource(R.layout.item_capacity)
        enterOrderDetails3Binding!!.spPickupLevel.adapter = pickUpAdapter

        setCheckListAdapterTrava()
        enterOrderDetails3Binding.checkTv.setOnClickListener {
            enterOrderDetails3Binding.nestedScroll.visibility = GONE
            enterOrderDetails3Binding.rlChecklist.visibility = VISIBLE
            isCheckList = true
            enterOrderDetails3Binding.checkTv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.white_color, GradientDrawable.RECTANGLE)
            enterOrderDetails3Binding.orderTv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        }

        enterOrderDetails3Binding.orderTv.setOnClickListener {
            enterOrderDetails3Binding.nestedScroll.visibility = VISIBLE
            enterOrderDetails3Binding.rlChecklist.visibility = GONE
            isCheckList = false
            enterOrderDetails3Binding.checkTv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails3Binding.orderTv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.white_color, GradientDrawable.RECTANGLE)
        }
    }

    private fun setCheckListAdapterTrava() {
        if (serviceRequest?.check_lists!!.size > 0) {
            checkListModelList.clear()
            checkListModelList.addAll(serviceRequest?.check_lists!!)

            var total: Int? = 0
            if (checkListModelList.size > 0) {

                for (i in 0..checkListModelList.size - 1) {
                    total = total!!.toInt() + checkListModelList.get(i).price!!.toInt()
                }
            }
            tv_total.setText("Total : " + ConfigPOJO.currency + " " + total.toString())
        }

        checklist_rv?.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        checkListAdapter = CheckListAdapter(activity, checkListModelList, this)
        checklist_rv?.adapter = checkListAdapter!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkListPresenter?.attachView(this)

        if(SECRET_DB_KEY != "c0cb897ef919b1c32ee9f672d74421228890d20962b7623535f9ab29d3e9f88a" || SECRET_DB_KEY != "b25dcb3ed072a432e0b76634d9d7ae4d"){
            enterOrderDetails2Binding.tvAddMore.setOnClickListener(this)
            enterOrderDetails2Binding.tvAddMore.performClick()
            enterOrderDetails3Binding.tvAddMore.setOnClickListener(this)
            enterOrderDetails3Binding.tvAddMore.performClick()
        }

        if (ConfigPOJO.TEMPLATE_CODE == DELIVERY20) {
            enterOrderDetails2Binding.tvAddMore.visibility = View.GONE
            enterOrderDetails2Binding.imageView2.visibility = View.GONE
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.EAGLE) {
            enterOrderDetails2Binding.etAdditionalInfo.visibility = View.GONE
            enterOrderDetails2Binding.etWeight.visibility = View.GONE
            enterOrderDetails2Binding.etPhone.visibility = View.GONE
            enterOrderDetails2Binding.imageView2.visibility = View.GONE
            enterOrderDetails2Binding.tvAddMore.visibility = View.GONE
            group?.visibility = View.VISIBLE
        } else {
            group?.visibility = View.GONE
        }

        if(SECRET_DB_KEY == "6ae915e1698d88c4e7dda839852fa5fd" ||
                SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14242c4f98380b537e89446cd49b0022f7"||
                SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14bb88ffcd9d48b45b6cd0b34d5a3554e5"){
            enterOrderDetails2Binding.textView7.text = activity!!.getString(R.string.enter_details_of_orderr)
            enterOrderDetails2Binding.tvAddMore.visibility = View.GONE
            enterOrderDetails2Binding.imageView2.visibility = View.GONE
            enterOrderDetails2Binding.etTypeOfMaterial.setHint(getString(R.string.enter_type_of_material_data))
            enterOrderDetails2Binding.etPhone.setHint(getString(R.string.phone_number))
            enterOrderDetails2Binding.etDeliverPerson.setHint(getString(R.string.sender_name))
        }

        if(ConfigPOJO.is_hood_app == "1"){
            enterOrderDetails2Binding.textView7.text = requireActivity().getString(R.string.enter_details_of_orderr)
            enterOrderDetails2Binding.textView3.text = requireActivity().getString(R.string.upload_vehicle_image_registration)
            enterOrderDetails2Binding.etWeight.visibility=View.GONE
            enterOrderDetails2Binding.etDeliverpersonName.setHint(requireActivity().getString(R.string.enter_delivery_destination_reciever))
            enterOrderDetails2Binding.etPhone.setHint(requireActivity().getString(R.string.delivery_contact))


            enterOrderDetails2Binding.tvAddMore.setOnClickListener(this)
            enterOrderDetails2Binding.tvAddMore.performClick()
            enterOrderDetails2Binding.tvAddMore.visibility = View.GONE
            enterOrderDetails2Binding.imageView2.visibility = View.GONE
            enterOrderDetails2Binding.etTypeOfMaterial.setHint(getString(R.string.enter_vehicle_details))
        }

        if(ConfigPOJO.settingsResponse?.key_value?.is_enable_business_user =="true"){
            enterOrderDetails2Binding.tvAddMore.setOnClickListener(this)
            enterOrderDetails2Binding.tvAddMore.performClick()
            enterOrderDetails2Binding.tvAddMore.visibility = View.GONE
            enterOrderDetails2Binding.imageView2.visibility = View.GONE
            enterOrderDetails2Binding.etDeliverPerson.visibility = View.VISIBLE
            enterOrderDetails2Binding.textView7.text = activity!!.getString(R.string.enter_bill_of_lading)
            enterOrderDetails2Binding.etTypeOfMaterial.hint = getString(R.string.enter_how_many_pieces)
            enterOrderDetails2Binding.etDeliverPerson.hint = getString(R.string.sender_name)
        }

        if(SECRET_DB_KEY == "c0cb897ef919b1c32ee9f672d74421228890d20962b7623535f9ab29d3e9f88a" || SECRET_DB_KEY == "b25dcb3ed072a432e0b76634d9d7ae4d"){
            enterOrderDetails2Binding.tvAddMore.visibility = View.GONE
            enterOrderDetails2Binding.imageView2.visibility = View.GONE
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER || ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {

            enterOrderDetails4Binding.etTypeOfMaterial.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails4Binding.etWeight.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails4Binding.etAdditionalInfo.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails4Binding.etDeliverpersonName.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails4Binding.etPhone.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails4Binding.etDeliverPerson.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

            enterOrderDetails3Binding!!.etInvoice.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails3Binding.etAdditionalInfo.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails3Binding.etParking.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails3Binding.spPickupLevel.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails3Binding.spDropLevel.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails3Binding.edPrice.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails3Binding.edProductName.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails3Binding.addIvv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        }

        serviceRequest = (activity as HomeActivity).serviceRequestModel
        rvCompanies.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        adapter = AddImageAdapter(imageList)
        adapter?.settingCallback(this)
        rvCompanies.adapter = adapter

        intializeProdItem()
        enterOrderDetails2Binding.tvSchedule.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        enterOrderDetails2Binding.tvNext.background = StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour, GradientDrawable.RECTANGLE)
        enterOrderDetails5Binding.tvNext.background = StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour, GradientDrawable.RECTANGLE)
        enterOrderDetails5Binding.tvSchedule.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        enterOrderDetails2Binding.edProductName.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        enterOrderDetails2Binding.addIvv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        enterOrderDetails2Binding.edPrice.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        enterOrderDetails4Binding.tvNext.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        enterOrderDetails2Binding.tvNext.text = getString(R.string.book_now)

        if (ConfigPOJO.settingsResponse?.key_value?.is_schedule_ride == "true") {
            enterOrderDetails2Binding.tvSchedule.visibility = View.VISIBLE
            enterOrderDetails5Binding.tvSchedule.visibility = View.VISIBLE
        } else {
            enterOrderDetails2Binding.tvSchedule.visibility = View.GONE
            enterOrderDetails5Binding.tvSchedule.visibility = View.GONE
        }

        setListeners()
        setCheckListAdapterTrava()

        //Transporter
        if (SECRET_DB_KEY == "3d7b9c3c1ee5f4d45e391122d21c20298c2e2f089ba3e9b1fde322041c5023a2") {
            enterOrderDetails2Binding.checkTv.text = "Shopping List"
            enterOrderDetails2Binding.tvItem.text = "Enter Shopping List Items"
        }

        if (SECRET_DB_KEY == "f068ea614d6e726084d6660cdfc4dc14bb88ffcd9d48b45b6cd0b34d5a3554e5" ||SECRET_DB_KEY=="f068ea614d6e726084d6660cdfc4dc14242c4f98380b537e89446cd49b0022f7"|| SECRET_DB_KEY == "a3f699d8c406daaf828f2d2c891eac15110024532f77f082ad972842c8516d60"  ||Constants.SECRET_DB_KEY  == "fdd4d886d691305c703e8274bbd49ec217a41b4a1bc16322f1345c000a1987f5") {
            enterOrderDetails2Binding.textView7.text = "Enter details of order"
            enterOrderDetails2Binding.etTypeOfMaterial.hint = "Enter type of material"
            if (SECRET_DB_KEY == "a3f699d8c406daaf828f2d2c891eac15110024532f77f082ad972842c8516d60"  || Constants.SECRET_DB_KEY  == "fdd4d886d691305c703e8274bbd49ec217a41b4a1bc16322f1345c000a1987f5") {
                enterOrderDetails2Binding.tvAddMore.visibility = View.GONE
                enterOrderDetails2Binding.imageView2.visibility = View.GONE
            }
        }

        enterOrderDetails2Binding.ivBack.setOnClickListener{
            activity!!.onBackPressed()
        }

        if (SECRET_DB_KEY == "456454ae09ad1d988be2677c9d477385"||
                SECRET_DB_KEY == "8f12ee7aaf0fa196926a8590472154a9") {
            enterOrderDetails2Binding.etTypeOfMaterial.hint=getString(R.string.bill_invoice)
        }

        //Shipusnow
        if(SECRET_DB_KEY == "c0cb897ef919b1c32ee9f672d74421228890d20962b7623535f9ab29d3e9f88a" || SECRET_DB_KEY == "b25dcb3ed072a432e0b76634d9d7ae4d"){

            enterOrderDetails2Binding.etDropoffAddress.setText(serviceRequest?.pickup_address)
            enterOrderDetails2Binding.etPickupAddress.setText(serviceRequest?.dropoff_address)
            if (serviceRequest?.category_id==13)//Export
            {
                enterOrderDetails2Binding.etDropoffAddress.visibility=View.VISIBLE
                enterOrderDetails2Binding.etPickupAddress.visibility=View.VISIBLE
                enterOrderDetails2Binding.spType.visibility=View.VISIBLE
                enterOrderDetails2Binding.etCommercialvalue.visibility=View.VISIBLE
                enterOrderDetails2Binding.etDeliverPersonPhone.visibility=View.VISIBLE
                etTypeOfMaterial.hint="Item Details"
                etWeight.hint="Item weight (Kg)"

                delivery_type = ArrayList<String>()

                delivery_type.add("Select Delivery Type")
                delivery_type.add("Home delivery")
                delivery_type.add("Airport/park")

                delivery_type_Adapter = ArrayAdapter(activity!!, R.layout.layout_spinner_view, delivery_type)
                delivery_type_Adapter.setDropDownViewResource(R.layout.item_capacity)
                enterOrderDetails2Binding!!.spType.adapter = delivery_type_Adapter
            }
            if (serviceRequest?.category_id==17)//Moving Service
            {
                enterOrderDetails2Binding.textView7.text = "Enter Details"
                enterOrderDetails2Binding.spType.visibility=View.VISIBLE
                enterOrderDetails2Binding.etDropoffAddress.visibility=View.VISIBLE
                enterOrderDetails2Binding.etPickupAddress.visibility=View.VISIBLE
                enterOrderDetails2Binding.etWeight.visibility=View.GONE
                enterOrderDetails2Binding.etPhone.visibility=View.GONE
                enterOrderDetails2Binding.etDeliverPerson.visibility=View.GONE
                enterOrderDetails2Binding.etDeliverPersonPhone.visibility=View.GONE
                enterOrderDetails2Binding.spDropLevel.visibility=View.VISIBLE
                enterOrderDetails2Binding.spPickupLevel.visibility=View.VISIBLE
                enterOrderDetails2Binding.elevatorTv.visibility=View.VISIBLE
                enterOrderDetails2Binding.rbPickupGroup.visibility=View.VISIBLE
                etTypeOfMaterial.hint="Service Details"

                delivery_type = ArrayList<String>()

                delivery_type.add("Select packaging material")
                delivery_type.add("None")
                delivery_type.add("Film Wrap")
                delivery_type.add("Corrugated Paper")

                delivery_type_Adapter = ArrayAdapter(activity!!, R.layout.layout_spinner_view, delivery_type)
                delivery_type_Adapter.setDropDownViewResource(R.layout.item_capacity)
                enterOrderDetails2Binding!!.spType.adapter = delivery_type_Adapter

                dropLevel = ArrayList<String>()
                pickupLevel = ArrayList<String>()

                dropLevel.add("What floor is the drop off?")
                dropLevel.add("Ground Floor")

                pickupLevel.add("What floor is the pick up?")
                pickupLevel.add("Ground Floor")

                for (i in 1..10) {
                    dropLevel.add("Floor $i")
                    pickupLevel.add("Floor $i")
                }

                dropLeveAdapter = ArrayAdapter(activity!!, R.layout.layout_spinner_view, dropLevel)
                dropLeveAdapter.setDropDownViewResource(R.layout.item_capacity)
                enterOrderDetails2Binding!!.spDropLevel.adapter = dropLeveAdapter

                pickUpAdapter = ArrayAdapter(activity!!, R.layout.layout_spinner_view, pickupLevel)
                pickUpAdapter.setDropDownViewResource(R.layout.item_capacity)
                enterOrderDetails2Binding!!.spPickupLevel.adapter = pickUpAdapter
            }
            if (serviceRequest?.category_id==14)//Lagos 2 School
            {
                enterOrderDetails2Binding.etDeliverPerson.visibility=View.GONE
                enterOrderDetails2Binding.etDeliverPersonPhone.visibility=View.GONE
                enterOrderDetails2Binding.etDropoffAddress.visibility=View.VISIBLE
                enterOrderDetails2Binding.spType.visibility=View.VISIBLE
                etTypeOfMaterial.hint="Item Details"
                etWeight.hint="Estimated Weight (kg)"

                delivery_type = ArrayList<String>()

                delivery_type.add("Select Receiver school")
                delivery_type.add("Babcock University")
                delivery_type.add("New School")

                delivery_type_Adapter = ArrayAdapter(activity!!, R.layout.layout_spinner_view, delivery_type)
                delivery_type_Adapter.setDropDownViewResource(R.layout.item_capacity)
                enterOrderDetails2Binding!!.spType.adapter = delivery_type_Adapter
            }
        }

        enterOrderDetails2Binding.checkTv.setOnClickListener {
            enterOrderDetails2Binding.orderDetail.visibility = GONE
            enterOrderDetails2Binding.llChList.visibility = VISIBLE
            isCheckList = true
            enterOrderDetails2Binding.checkTv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.white_color, GradientDrawable.RECTANGLE)
            enterOrderDetails2Binding.orderTv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        }

        enterOrderDetails2Binding.orderTv.setOnClickListener {
            enterOrderDetails2Binding.orderDetail.visibility = VISIBLE
            enterOrderDetails2Binding.llChList.visibility = GONE
            isCheckList = false
            enterOrderDetails2Binding.checkTv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails2Binding.orderTv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.white_color, GradientDrawable.RECTANGLE)
        }

        if (ConfigPOJO.TEMPLATE_CODE != Constants.SUMMER_APP_BASE) {
            if (ConfigPOJO?.TerminologyData?.key_value?.check_list == 0) {
                enterOrderDetails2Binding.rlTab.visibility = VISIBLE
                enterOrderDetails2Binding.orderDetail.visibility = VISIBLE
                enterOrderDetails2Binding.llChList.visibility = GONE

                enterOrderDetails2Binding.checkTv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
                enterOrderDetails2Binding.orderTv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.white_color, GradientDrawable.RECTANGLE)
            }
        }

        if (SECRET_DB_KEY == "80587628ac28c165d2bc0a7c0feed262") {
            enterOrderDetails2Binding.rlTab.visibility = VISIBLE
            enterOrderDetails2Binding.orderDetail.visibility = VISIBLE
            enterOrderDetails2Binding.llChList.visibility = GONE

            enterOrderDetails2Binding.checkTv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            enterOrderDetails2Binding.orderTv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.white_color, GradientDrawable.RECTANGLE)
        }

        if (ConfigPOJO?.settingsResponse?.key_value?.is_helper_for_driver=="true")
        {
            carpool_switch.visibility=View.VISIBLE
            healperheader.visibility=View.VISIBLE

            serviceRequest?.helper="false"
            carpool_switch.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    serviceRequest?.helper="true"
                } else {
                    serviceRequest?.helper="false"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        checkListPresenter?.detachView()
    }

    private fun intializeProdItem() {
        if (arguments == null) return

        if (arguments?.containsKey("productItem") == true) {
            mProduct = arguments?.getParcelable("productItem")!!
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvNext -> {
                setRequestDetails()
                if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
                    currentLocation = Gson().fromJson<CurrentLocationModel>(SharedPrefs.with(activity).getString(CURRENT_LOCATION, ""), object : TypeToken<CurrentLocationModel>() {}.type)
                    val fragment = SetupDropOffLocationFragment()
                    val bundle = Bundle()
                    if (currentLocation != null) {
                        bundle.putDouble(Constants.LATITUDE, currentLocation?.latitude!!)
                        bundle.putDouble(Constants.LONGITUDE, currentLocation?.longitude!!)
                        bundle.putString(Constants.ADDRESS, currentLocation?.address)
                    } else {
                        bundle.putDouble(Constants.LATITUDE, 0.0)
                        bundle.putDouble(Constants.LONGITUDE, 0.0)
                        bundle.putString(Constants.ADDRESS, "")
                    }

                    bundle.putString(Constants.BOOKING_TYPE, BOOKING_TYPE.ROAD_PICKUP)
                    fragment.arguments = bundle
                    fragmentManager?.beginTransaction()?.replace(R.id.container, fragment)?.addToBackStack("backstack")?.commit()

                }
                else {
                    if (ConfigPOJO.TEMPLATE_CODE == Constants.TRAVE_APP_BASE) {
                        if (isCheckList) {
                            if (checkListModelList.size == 0) {
                                rootView.showSnack("Checklist data empty. Please add")
                                return
                            }
                            serviceRequest?.future = ServiceRequestModel.BOOK_NOW
                            fragmentManager?.beginTransaction()?.replace(R.id.container, ConfirmBookingFragment())?.addToBackStack("backstack")?.commit()
                        }
                        else {
                            if(SECRET_DB_KEY == "c0cb897ef919b1c32ee9f672d74421228890d20962b7623535f9ab29d3e9f88a" || SECRET_DB_KEY == "b25dcb3ed072a432e0b76634d9d7ae4d"){
                                if (serviceRequest?.category_id==13|| serviceRequest?.category_id==14|| serviceRequest?.category_id==17)
                                {
                                    if (serviceRequest?.category_id==13)
                                    {
                                        if (enterOrderDetails2Binding.etTypeOfMaterial.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_package_details))
                                            return
                                        }
                                        if (enterOrderDetails2Binding.etWeight.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_approx_weight))
                                            return
                                        }
                                        if (enterOrderDetails2Binding.etCommercialvalue.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.commercial_value_v))
                                            return
                                        }
                                        if (typeSelected=="-1")
                                        {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.v_select_delivery_type))
                                            return
                                        }
                                        if (enterOrderDetails2Binding.etDeliverpersonName.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_receiver_name))
                                            return
                                        }
                                        if (enterOrderDetails2Binding.etPhone.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_receiver_number))
                                            return
                                        }
                                        if (enterOrderDetails2Binding.etDeliverPerson.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_sender_name))
                                            return
                                        }
                                        if (enterOrderDetails2Binding.etDeliverPersonPhone.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.sender_phone))
                                            return
                                        }
                                    }
                                    else if (serviceRequest?.category_id==17)
                                    {
                                        if (enterOrderDetails2Binding.etTypeOfMaterial.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack("Please enter Service details")
                                            return
                                        }
                                        if (spPickupLevelValue == "-1") {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack("Please select pickup level")
                                            return
                                        }

                                        if (spDropLevelValue == "-1") {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack("Please select dropoff level")
                                            return
                                        }
                                        if (typeSelected=="-1")
                                        {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack("Please select package material")
                                            return
                                        }
                                        if (enterOrderDetails2Binding.etDeliverpersonName.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_receiver_name))
                                            return
                                        }
                                    }
                                    else if (serviceRequest?.category_id==14)
                                    {
                                        if (enterOrderDetails2Binding.etTypeOfMaterial.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_package_details))
                                            return
                                        }
                                        if (enterOrderDetails2Binding.etWeight.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_approx_weight))
                                            return
                                        }
                                        if (typeSelected=="-1")
                                        {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack("Please select Receiver school")
                                            return
                                        }
                                        if (enterOrderDetails2Binding.etDeliverpersonName.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_receiver_name))
                                            return
                                        }
                                        if (enterOrderDetails2Binding.etPhone.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_receiver_number))
                                            return
                                        }
                                    }
                                    bookingApiCall()
                                }
                                else
                                {
                                    if(SECRET_DB_KEY == "b25dcb3ed072a432e0b76634d9d7ae4d"
                                            || SECRET_DB_KEY == "c0cb897ef919b1c32ee9f672d74421228890d20962b7623535f9ab29d3e9f88a"
                                            || Constants.SECRET_DB_KEY =="a3f699d8c406daaf828f2d2c891eac15110024532f77f082ad972842c8516d60"
                                            || Constants.SECRET_DB_KEY == "fdd4d886d691305c703e8274bbd49ec217a41b4a1bc16322f1345c000a1987f5")
                                    {
                                        if(SECRET_DB_KEY == "c0cb897ef919b1c32ee9f672d74421228890d20962b7623535f9ab29d3e9f88a" || SECRET_DB_KEY == "b25dcb3ed072a432e0b76634d9d7ae4d") {
                                            if (enterOrderDetails2Binding.etTypeOfMaterial.text.toString().trim().isEmpty()) {
                                                enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_package_details))
                                                return
                                            }
                                            if (enterOrderDetails2Binding.etWeight.text.toString().trim().isEmpty()) {
                                                enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_approx_weight))
                                                return
                                            }
                                        }
                                        if (enterOrderDetails2Binding.etDeliverpersonName.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_receiver_name))
                                            return
                                        }
                                        if (enterOrderDetails2Binding.etPhone.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_receiver_number))
                                            return
                                        }
                                        if (enterOrderDetails2Binding.etDeliverPerson.text.toString().trim().isEmpty()) {
                                            enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_sender_name))
                                            return
                                        }
                                    }

                                    serviceRequest?.future = ServiceRequestModel.BOOK_NOW
                                    fragmentManager?.beginTransaction()?.replace(R.id.container, ConfirmBookingFragment())?.addToBackStack("backstack")?.commit()
                                }
                            }
                            else
                            {
                                if(SECRET_DB_KEY == "c0cb897ef919b1c32ee9f672d74421228890d20962b7623535f9ab29d3e9f88a" ||  SECRET_DB_KEY == "b25dcb3ed072a432e0b76634d9d7ae4d"){
                                    if (enterOrderDetails2Binding.etTypeOfMaterial.text.toString().trim().isEmpty()) {
                                        enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_package_details))
                                        return
                                    }
                                    if (enterOrderDetails2Binding.etWeight.text.toString().trim().isEmpty()) {
                                        enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_approx_weight))
                                        return
                                    }
                                    if (enterOrderDetails2Binding.etDeliverpersonName.text.toString().trim().isEmpty()) {
                                        enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_receiver_name))
                                        return
                                    }
                                    if (enterOrderDetails2Binding.etPhone.text.toString().trim().isEmpty()) {
                                        enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_receiver_number))
                                        return
                                    }
                                    if (enterOrderDetails2Binding.etDeliverPerson.text.toString().trim().isEmpty()) {
                                        enterOrderDetails2Binding.etTypeOfMaterial.showSnack(getString(R.string.please_enter_sender_name))
                                        return
                                    }
                                }

                                serviceRequest?.future = ServiceRequestModel.BOOK_NOW
                                fragmentManager?.beginTransaction()?.replace(R.id.container, ConfirmBookingFragment())?.addToBackStack("backstack")?.commit()

                            }
                        }

                    } else {

                        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
                            if (spPickupLevelValue == "-1") {
                                ll_view.showSnack("Please select pickup level")
                                return
                            }

                            if (spDropLevelValue == "-1") {
                                ll_view.showSnack("Please select dropoff level")
                                return
                            }
                        }

                        serviceRequest?.future = ServiceRequestModel.BOOK_NOW
                        fragmentManager?.beginTransaction()?.replace(R.id.container, ConfirmBookingFragment())?.addToBackStack("backstack")?.commit()
                    }
                }
            }

            R.id.ivBack -> {
                activity?.onBackPressed()
            }

            R.id.tvSchedule -> {
                setRequestDetails()
                if (isCheckList) {
                    if (checkListModelList.size == 0) {
                        rootView.showSnack("Checklist data empty. Please add")
                        return
                    }
                    serviceRequest?.future = ServiceRequestModel.SCHEDULE
                    fragmentManager?.beginTransaction()?.replace(R.id.container, ScheduleFragment())?.addToBackStack("backstack")?.commit()
                } else {
                    if (ConfigPOJO.TEMPLATE_CODE == Constants.TRAVE_APP_BASE) {
                        serviceRequest?.future = ServiceRequestModel.SCHEDULE
                        fragmentManager?.beginTransaction()?.replace(R.id.container, ScheduleFragment())?.addToBackStack("backstack")?.commit()
                        /*if (validation()) {
                        }*/
                    } else {
                        serviceRequest?.future = ServiceRequestModel.SCHEDULE
                        fragmentManager?.beginTransaction()?.replace(R.id.container, ScheduleFragment())?.addToBackStack("backstack")?.commit()
                    }
                }
            }

            R.id.tv_add_more -> {
               /* if (activity!=null)
                {
                    group?.clearAnimation()
                    group?.animate()?.translationY(group.height.toFloat())
                            ?.alpha(if (group?.visibility == View.VISIBLE) 0.0f else 1.0f)
                            ?.setDuration(if (group?.visibility == View.VISIBLE) 600 else 200)
                            ?.setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    super.onAnimationEnd(animation)
                                    group.visibility = if (group?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                                    if (group?.visibility == View.VISIBLE) animateExpand() else animateCollapse()
                                }
                            })
                }*/
            }

            R.id.add_ivv -> {
                if (!ed_product_name.text.toString().trim().isEmpty()) {
                    if (!ed_price.text.isNullOrEmpty()) {
                        checkListModel = CheckListModel(ed_product_name.text.toString(), ed_price.text.toString())
                    } else {
                        checkListModel = CheckListModel(ed_product_name.text.toString(), "0")
                    }
                    checkListModelList.add(checkListModel!!)
                    checkListAdapter?.notifyDataSetChanged()
                    ed_product_name.setText("")
                    ed_price.setText("")

                    var total: Int? = 0
                    if (checkListModelList.size > 0) {
                        for (i in 0..checkListModelList.size - 1) {
                            total = total!!.toInt() + checkListModelList.get(i).price!!.toInt()
                        }
                    }
                    tv_total.text = "Total : " + ConfigPOJO.currency + " " + total.toString()
                }
            }

            R.id.tvChecklistNextt -> {
                tvNext.performClick()
            }

            R.id.tvAdd -> {
                if (!etProductName.text.toString().trim().isEmpty()) {
                    productListModel = ProductListModel(etProductName.text.toString().trim(), etReceiver.text.toString().trim(), etPhoneNumber.text.toString().trim(), "+91", "")
                    productList.add(productListModel!!)
                    productListAdapter?.notifyDataSetChanged()

                    etProductName.setText("")
                    etReceiver.setText("")
                    etPhoneNumber.setText("")
                } else {
                    etProductName.setError(getString(R.string.enter_product_name))
                }
            }

            R.id.tvAddCheckList -> {
                if (!etProductCode.text.toString().trim().isEmpty()) {
                    productcheckListModel = ProductCheckListModel(etProductCode.text.toString().trim(), etphnNumber.text.toString().trim())
                    productCheckList.add(productcheckListModel!!)
                    productCheckListAdapter?.notifyDataSetChanged()

                    etProductCode.setText("")
                    etphnNumber.setText("")
                } else {
                    etProductCode.setError(getString(R.string.enter_product_code))
                }
            }
            R.id.tvRequestNow -> {
                presenter?.requestPickupApi(serviceRequest!!, productList, productCheckList)
            }
            R.id.tvRequestNow1 -> {
                presenter?.requestPickupApi(serviceRequest!!, productList, productCheckList)
            }
        }
    }

    private fun setListeners() {
        enterOrderDetails2Binding.tvNext.setOnClickListener(this)
        enterOrderDetails3Binding.tvNext.setOnClickListener(this)
        enterOrderDetails4Binding.tvNext.setOnClickListener(this)
        enterOrderDetails5Binding.tvNext.setOnClickListener(this)
        enterOrderDetails2Binding.tvSchedule.setOnClickListener(this)
        enterOrderDetails5Binding.tvSchedule.setOnClickListener(this)
        enterOrderDetails2Binding.tvAddMore.setOnClickListener(this)
        enterOrderDetails3Binding.tvAddMore.setOnClickListener(this)
        enterOrderDetails4Binding.tvAddMore.setOnClickListener(this)
        enterOrderDetails4Binding.ivBack.setOnClickListener(this)
        enterOrderDetails3Binding.ivBack.setOnClickListener(this)

        enterOrderDetails3Binding.tvChecklistNextt.setOnClickListener(this)
        enterOrderDetails3Binding.addIvv.setOnClickListener(this)

        enterOrderDetails2Binding.tvChecklistNextt.setOnClickListener(this)
        enterOrderDetails2Binding.addIvv.setOnClickListener(this)

        if (ConfigPOJO.TEMPLATE_CODE == DELIVERY20) {
            enterOrderDetails2Binding.tvAddMore.performClick()
            enterOrderDetails2Binding.tvAddMore.visibility = View.GONE
            enterOrderDetails2Binding.imageView2.visibility = View.GONE

            etDropLocation.setText(serviceRequest?.dropoff_address)
            etPickupLocation.setText(serviceRequest?.pickup_address)
        }

        enterOrderDetails4Binding.fragileCheckbox.setOnClickListener(View.OnClickListener {
            if (enterOrderDetails4Binding.fragileCheckbox.isChecked) {
                isFragile = true;
            } else {
                isFragile = false;
            }
        })

        enterOrderDetails3Binding.rbPickupGroup.setOnCheckedChangeListener { radioGroup, id ->
            when (id) {
                R.id.rbYes -> {
                    isPickupElevator = true
                }

                R.id.rbNo -> {
                    isPickupElevator = false
                }
            }
        }

        enterOrderDetails2Binding.rbPickupGroup.setOnCheckedChangeListener { radioGroup, id ->
            when (id) {
                R.id.rbYes -> {
                    isPickupElevator = true
                }

                R.id.rbNo -> {
                    isPickupElevator = false
                }
            }
        }

        enterOrderDetails3Binding.rbDropGroup.setOnCheckedChangeListener { radioGroup, id ->
            when (id) {
                R.id.rb_drop_Yes -> {
                    isDropOffElevator = true
                }

                R.id.rb_drop_No -> {
                    isDropOffElevator = false
                }
            }
        }

        enterOrderDetails3Binding.fragileGroup.setOnCheckedChangeListener { radioGroup, id ->
            when (id) {
                R.id.rb_drop_Yes -> {
                    isFragile = true
                }

                R.id.rb_drop_No -> {
                    isFragile = false
                }
            }
        }

        enterOrderDetails3Binding!!.spDropLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do Nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0) {
                    spDropLevelValue = (position - 1).toString()
                } else {
                    spDropLevelValue = ""
                }
            }
        }

        enterOrderDetails2Binding!!.spDropLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do Nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0) {
                    spDropLevelValue = (position - 1).toString()
                } else {
                    spDropLevelValue = ""
                }
            }
        }

        enterOrderDetails3Binding!!.spPickupLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do Nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0) {
                    spPickupLevelValue = (position - 1).toString()
                } else {
                    spPickupLevelValue = ""
                }
            }
        }

        enterOrderDetails2Binding!!.spPickupLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do Nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0) {
                    spPickupLevelValue = (position - 1).toString()
                } else {
                    spPickupLevelValue = ""
                }
            }
        }

        if (serviceRequest?.category_id==13|| serviceRequest?.category_id==14||
                serviceRequest?.category_id==17)
        {
            enterOrderDetails2Binding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do Nothing
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position >= 0) {
                        typeSelected = (position - 1).toString()
                    } else {
                        typeSelected = ""
                    }
                }
            }
        }

    }

    private fun animateExpand() {
        val rotate = RotateAnimation(360f, 180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 300
        rotate.fillAfter = true
        imageView2.animation = rotate
    }

    private fun animateCollapse() {
        val rotate = RotateAnimation(180f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 300
        rotate.fillAfter = true
        imageView2.animation = rotate
    }

    private fun setRequestDetails() {

        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
            serviceRequest?.invoice_number = enterOrderDetails3Binding!!.etInvoice.text.toString()
            serviceRequest?.details = enterOrderDetails3Binding.etAdditionalInfo.text.toString()
            serviceRequest?.parking = enterOrderDetails3Binding.etParking.text.toString()

            serviceRequest?.elevator_pickup = isPickupElevator.toString()
            serviceRequest?.elevator_dropoff = isDropOffElevator.toString()
            serviceRequest?.pickup_level = spPickupLevelValue
            serviceRequest?.dropoff_level = spDropLevelValue
            serviceRequest?.fragile = isFragile.toString()
            serviceRequest?.images = imageList
            serviceRequest?.reciptDetail = enterOrderDetails3Binding!!.etInvoice.text.toString()

            serviceRequest?.check_lists?.clear()
            if (!checkListAdapter?.getList()!!.isNullOrEmpty()) {
                serviceRequest?.check_lists?.addAll(checkListAdapter?.getList()!!)
            }
        }
        else if (ConfigPOJO.TEMPLATE_CODE == Constants.MOVER) {
            serviceRequest?.material_details = enterOrderDetails4Binding.etTypeOfMaterial.text.toString()
            if (!enterOrderDetails4Binding.etWeight.text.toString().isNullOrEmpty()) {
                serviceRequest?.product_weight = enterOrderDetails4Binding.etWeight.text.toString().toFloat()
            } else {
                serviceRequest?.product_weight = 0F
            }
            serviceRequest?.details = enterOrderDetails4Binding.etAdditionalInfo.text.toString()
            serviceRequest?.pickup_person_name = enterOrderDetails4Binding.etDeliverpersonName.text.toString()
            serviceRequest?.pickup_person_phone = enterOrderDetails4Binding.etPhone.text.toString()
            serviceRequest?.delivery_person_name = enterOrderDetails4Binding.etDeliverPerson.text.toString()
            serviceRequest?.images = imageList

            serviceRequest?.recieverName = enterOrderDetails4Binding.etDeliverPerson.text.toString()
            serviceRequest?.recieverPhone = enterOrderDetails4Binding.etPhone.text.toString()
            serviceRequest?.fragile = isFragile.toString()

        }
        else if (ConfigPOJO.TEMPLATE_CODE == Constants.DELIVERY20) {
            serviceRequest?.material_details = enterOrderDetails5Binding.etTypeOfMaterial.text.toString()

            if (etWeight.text.isNotBlank()) {
                serviceRequest?.product_weight = enterOrderDetails5Binding.etWeight.text.toString().toFloat()
            } else {
                serviceRequest?.product_weight = 0F
            }

            serviceRequest?.details = enterOrderDetails5Binding.etAdditionalInfo.text.toString()
            serviceRequest?.pickup_person_name = enterOrderDetails5Binding.etDeliverpersonName.text.toString()
            serviceRequest?.pickup_person_phone = enterOrderDetails5Binding.etPhone.text.toString()
            serviceRequest?.invoice_number = "0"
            serviceRequest?.delivery_person_name = enterOrderDetails5Binding.etDeliverPerson.text.toString()
            serviceRequest?.images = imageList

            serviceRequest?.recieverName = enterOrderDetails5Binding.etDeliverPerson.text.toString()
            serviceRequest?.recieverPhone = enterOrderDetails5Binding.etPhone.text.toString()
            serviceRequest?.reciptDetail = "0"

            serviceRequest?.check_lists?.clear()
            if (!checkListAdapter?.getList()!!.isNullOrEmpty()) {
                serviceRequest?.check_lists?.addAll(checkListAdapter?.getList()!!)
            }
        }
        else {
            if(SECRET_DB_KEY == "c0cb897ef919b1c32ee9f672d74421228890d20962b7623535f9ab29d3e9f88a" || SECRET_DB_KEY == "b25dcb3ed072a432e0b76634d9d7ae4d"){
                if (serviceRequest?.category_id==17)//Moving Service
                {
                    serviceRequest?.elevator_pickup = isPickupElevator.toString()
                }
                else
                {
                    serviceRequest?.elevator_pickup = "false"
                }

                serviceRequest?.pickup_level = spPickupLevelValue
                serviceRequest?.dropoff_level = spDropLevelValue
                if (delivery_type.size>0)
                serviceRequest?.delivery_type = typeSelected
            }

            serviceRequest?.delivery_person_phone = enterOrderDetails2Binding.etDeliverPersonPhone.text.toString()
            serviceRequest?.commercial_value = enterOrderDetails2Binding.etCommercialvalue.text.toString()


            serviceRequest?.material_details = enterOrderDetails2Binding.etTypeOfMaterial.text.toString()

            if (etWeight.text.isNotBlank()) {
                serviceRequest?.product_weight = enterOrderDetails2Binding.etWeight.text.toString().toFloat()
            } else {
                serviceRequest?.product_weight = 0F
            }

            serviceRequest?.details = enterOrderDetails2Binding.etAdditionalInfo.text.toString()
            serviceRequest?.pickup_person_name = enterOrderDetails2Binding.etDeliverpersonName.text.toString()
            serviceRequest?.pickup_person_phone = enterOrderDetails2Binding.etPhone.text.toString()
            serviceRequest?.invoice_number = "0"
            serviceRequest?.delivery_person_name = enterOrderDetails2Binding.etDeliverPerson.text.toString()
            serviceRequest?.images = imageList

            serviceRequest?.recieverName = enterOrderDetails2Binding.etDeliverPerson.text.toString()
            serviceRequest?.recieverPhone = enterOrderDetails2Binding.etPhone.text.toString()
            serviceRequest?.reciptDetail = "0"

            serviceRequest?.check_lists?.clear()
            if (!checkListAdapter?.getList()!!.isNullOrEmpty()) {
                serviceRequest?.check_lists?.addAll(checkListAdapter?.getList()!!)
            }
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getStorage() {
        if (imageList.size < 2) {
            ImageUtils.displayImagePicker(this)
        } else {
            rootView.showSnack(getString(R.string.max_images_validation_msg_for_detail))
        }
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showLocationRationale(request: PermissionRequest) {
        activity?.let { PermissionUtils.showRationalDialog(it, R.string.permission_required_to_select_image, request) }
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onNeverAskAgainRationale() {
        activity?.let {
            PermissionUtils.showAppSettingsDialog(it,
                    R.string.permission_required_to_select_image)
        }
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private var imagePath: String? = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ImageUtils.REQ_CODE_CAMERA_PICTURE -> {
                    val file = ImageUtils.getFile(activity)
                    imagePath = file.absolutePath
                    addImage(imagePath)
                }

                ImageUtils.REQ_CODE_GALLERY_PICTURE -> {
                    data?.let {
                        val file = ImageUtils.getImagePathFromGallery(activity, data.data!!)
//                        ivProfilePic.setRoundProfilePic(file)
                        imagePath = file.absolutePath
                        addImage(imagePath)
                    }
                }
            }
        }
    }

    private fun addImage(imagePath: String?) {
        imageList.add(imagePath.toString())
        adapter?.notifyDataSetChanged()
    }


    override fun addImageHeader() {
        getStorageWithPermissionCheck()
    }

    /*Confirm Booking elements/Parameters*/
    private fun bookingApiCall() {
        if (CheckNetworkConnection.isOnline(activity)) {
//            val requestModel = (activity as HomeActivity).serviceRequestModel
            serviceRequest?.order_timings = getCurentDateStringUtc()
            serviceRequest?.gender = ""
            serviceRequest?.payment_type = "Cash"
            serviceRequest?.distance = 50000
            serviceRequest?.cancellation_charges = 0.0
            serviceRequest?.pool = "0"
            presenter.requestServiceApiCall(serviceRequest!!)
        } else {
            CheckNetworkConnection.showNetworkError(rootView)
        }
    }

    override fun onRequestApiSuccess(response: ResultItem) {

    }

    override fun onApiSuccess(response: CheckListItem?) {
        Log.e("asasasasasasas", "sdsdsdsdsdsds")
    }

    override fun onApiSuccess(response: Order?) {
        if (response?.order_status == OrderStatus.UNASSIGNED) {
            requestAssignFromAdmin()
        }
    }

    private fun requestAssignFromAdmin() {
        val dialog = activity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setContentView(R.layout.message_alert)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog?.findViewById<TextView>(R.id.tvMsg)?.text = getString(R.string.unassiged_message)

        dialog?.findViewById<TextView>(R.id.tvCall)?.setOnClickListener {
            activity!!.finish()
            startActivity(Intent(activity!!, HomeActivity::class.java)
                    .putExtra("home_activity", "home_activity")
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            dialog.dismiss()
        }

        dialog?.show()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    override fun onOutstandingCharges(response: Order?) {
        Log.e("asasasasasasas", "sdsdsdsdsdsds")
    }

    override fun onWalletBalSuccess(response: WalletBalModel?) {
        Log.e("asasasasasasas", "sdsdsdsdsdsds")
    }

    override fun onCreditPointsSucess(response: RideShareResponse) {
        Log.e("asasasasasasas", "sdsdsdsdsdsds")
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            /* User's authorisation expired. Logout and ask user to login again.*/
            AppUtils.logout(activity)
        }
    }

    override fun onInfoClickDelete(vi: View, position: Int) {
        checkListModelList.removeAt(position)

        checkListAdapter?.notifyDataSetChanged()
        var total: Int? = 0
        if (checkListModelList.size > 0) {

            for (i in 0..checkListModelList.size - 1) {
                total = total!!.toInt() + checkListModelList.get(i).price!!.toInt()
            }
        }
        tv_total.setText("Total : " + ConfigPOJO.currency + " " + total.toString())
    }

    override fun onInfoClickEdit(tModelList: ArrayList<CheckListModel>) {
        var total: Int? = 0
        if (tModelList.size > 0) {

            for (i in 0..tModelList.size - 1) {
                if (tModelList.get(i).price != "") {
                    total = total!!.toInt() + tModelList.get(i).price!!.toInt()
                }
            }
        }
        tv_total.setText("Total : " + ConfigPOJO.currency + " " + total.toString())
    }

    override fun listClickCallback(vi: View, position: Int) {
        productList?.removeAt(position)
        productListAdapter?.notifyDataSetChanged()
    }

    override fun checkListClickCallback(vi: View, position: Int) {
        productCheckList?.removeAt(position)
        productCheckListAdapter?.notifyDataSetChanged()
    }
}