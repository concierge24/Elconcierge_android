package com.trava.user.ui.home.orderbreakdown

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trava.driver.ui.home.orderDetails.OrderImagesAdapter
import com.trava.user.R
import com.trava.user.ui.home.HomeActivity
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.models.CheckListModelUpdate
import com.trava.user.webservices.models.order.CheckListModelArray
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.*
import com.trava.utilities.constants.ACCESS_TOKEN
import com.trava.utilities.constants.SUCCESS_CODE
import io.socket.client.Ack
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.fragment_delivery_starts.*
import kotlinx.android.synthetic.main.layout_full_order_details.*
import kotlinx.android.synthetic.main.layout_full_order_details.check_tv
import kotlinx.android.synthetic.main.layout_full_order_details.order_tv
import kotlinx.android.synthetic.main.layout_full_order_details.tv_total
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class FullOrderDetailsFragment : DialogFragment(), CheckListDetailsAdapter.OnInfocallBack,
        InvoiceRequestContract.View {

    companion object {
        const val TAG = "com.trava.user.ui.home.orderbreakdown.FullOrderDetailsFragment"
    }

    var checkListAdapterCharge: ChargesListAdapter? = null
    private var tollCharges = ArrayList<String>()
    private var parkingCharges = ArrayList<String>()
    var checkListAdapter: CheckListDetailsAdapter? = null
    private lateinit var orderDetail: Order
    private var dialogIndeterminate: DialogIndeterminate? = null
    private var list = ArrayList<CheckListModelUpdate>()
    private var modelList = ArrayList<CheckListModelArray>()
    private var positn = 0
    private var total_amount = 0
    private var orderToken = ""
    private val presenter = CheckRequestPresenter()
    private var checkIdleTimer = Timer()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_full_order_details, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme)
    }

    private fun restartCheckIdleTimer(checkIdleTimeInterval: Long) {
        checkIdleTimer.cancel()
        checkIdleTimer = Timer()
        checkIdleTimer.schedule(object : TimerTask() {
            override fun run() {
                if (CheckNetworkConnection.isOnline(activity)) {
                    checkOrderStatus()
                }
                restartCheckIdleTimer(10000L)
            }
        }, checkIdleTimeInterval)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogIndeterminate = (activity as HomeActivity).dialogIndeterminate
        presenter.attachView(this)
        orderDetail = arguments?.getParcelable("orderDetails")!!
        orderToken = orderDetail?.order_token ?: ""
        presenter.onGoingOrderApi()
        sv_order_details.visibility = View.VISIBLE
        rvOrderChecklist.visibility = View.GONE

        //Transporter
        if (Constants.SECRET_DB_KEY =="3d7b9c3c1ee5f4d45e391122d21c20298c2e2f089ba3e9b1fde322041c5023a2"
                || Constants.SECRET_DB_KEY == "3d7b9c3c1ee5f4d45e391122d21c20298c2e2f089ba3e9b1fde322041c5023a2")
        {
            check_tv.text="Shopping List"
        }

        if (orderDetail.check_lists != null && orderDetail.check_lists.size > 0) {
            rl_tab.visibility = View.VISIBLE
        }

        if (AppSocket.get().isConnected) {
            restartCheckIdleTimer(5000)
        }

        check_tv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        order_tv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.white_color, GradientDrawable.RECTANGLE)
        tvChecklistNexttt.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

        check_tv.setOnClickListener {
            sv_order_details.visibility = View.GONE
            rvOrderChecklist.visibility = View.VISIBLE
            tvChecklistNexttt.visibility = View.VISIBLE
            tv_total.visibility = View.VISIBLE

            check_tv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.white_color, GradientDrawable.RECTANGLE)
            order_tv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        }

        order_tv.setOnClickListener {
            sv_order_details.visibility = View.VISIBLE
            rvOrderChecklist.visibility = View.GONE
            tvChecklistNexttt.visibility = View.GONE
            tv_total.visibility = View.GONE
            check_tv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
            order_tv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.white_color, GradientDrawable.RECTANGLE)
        }

        toolbarAbout.setNavigationOnClickListener {
            dismiss()
        }

        tvChecklistNexttt.setOnClickListener {

            if (tvChecklistNexttt.text.toString() == "Pay") {
                dismiss()
            } else {
                list.clear()
                if (checkListAdapter?.getList()!!.size > 0) {
                    for (i in checkListAdapter?.getList()!!.indices) {
                        list.add(CheckListModelUpdate(checkListAdapter?.getList()?.get(i)?.check_list_id.toString(),
                                checkListAdapter?.getList()?.get(i)?.after_item_price ?: "",
                                checkListAdapter?.getList()?.get(i)?.tax.toString() ?: ""))
                    }
                }

                var arr = JSONArray()
                for (i in list.indices) {
                    var obj = JSONObject()
                    obj.put("check_list_id", list[i].check_list_id.toString())
                    obj.put("price", list[i].price.toString())
                    obj.put("tax", list[i].tax.toString())
                    arr.put(obj)
                }

                presenter.updataReqApi(arr)
            }
        }
        AppSocket.get().on(Events.ORDER_EVENT, orderEventListener)
    }

    // Socket Listener for orders events
    private val orderEventListener = Emitter.Listener { args ->
        Logger.e("orderEventListener", args[0].toString())

        activity?.runOnUiThread {
            when (JSONObject(args[0].toString()).getString("type")) {
                "SerCheckList" -> {
                    val orderJson = JSONObject(args[0].toString()).getJSONObject("order").toString()
                    var order = Gson().fromJson(orderJson, Order::class.java)
                    orderDetail = order
                    setData()
                    setAlert()
                }
            }
        }
    }

    private fun checkOrderStatus() {
        val request = JSONObject()
        request.put("type", OrderEventType.CUSTOMER_SINGLE_ORDER)
        request.put("access_token", ACCESS_TOKEN)
        request.put("order_token", orderToken)
        AppSocket.get().emit(Events.COMMON_EVENT, request, Ack {
            val response = Gson().fromJson<ApiResponse<Order>>(it[0].toString(), object : TypeToken<ApiResponse<Order>>() {}.type)
            if (response?.statusCode == SUCCESS_CODE) {
                val orderModel = response.result
                val orderJson = JSONObject(it[0].toString()).getString("result")
                activity?.runOnUiThread {
                    when (orderModel?.order_status) {

                        OrderStatus.ONGOING, OrderStatus.CONFIRMED, OrderStatus.REACHED -> {
                            val order = Gson().fromJson(orderJson, Order::class.java)
                            orderDetail = order
                            if (ConfigPOJO.TEMPLATE_CODE != Constants.CORSA) {
                                setData()
                            } else {
                                setTollParkingCharges()
                            }
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

    private fun setAlert() {

        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle(getString(R.string.app_name))
        builder.setMessage("Price Including Tax " + ConfigPOJO.currency + "" + total_amount + "\nTotal Price has been updated")
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            dialog?.dismiss()

        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog?.dismiss() }
        builder.show()
    }

    private fun setCheckListAdapter() {
        rvOrderChecklist?.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        checkListAdapter = CheckListDetailsAdapter(activity, modelList, this)
        rvOrderChecklist?.adapter = checkListAdapter!!
    }

    private fun setData() {

        modelList.clear()
        modelList?.addAll(orderDetail?.check_lists)

        var temp = false
        for (i in modelList.indices) {
            if (modelList[i].tax != 0) {
                temp = true
            }
        }

        if (temp) {
            tvChecklistNexttt.text = "Pay"
        }

        if (orderDetail.check_lists != null && orderDetail.check_lists.size > 0) {
            rl_tab.visibility = View.VISIBLE
        }

        setCheckListAdapter()


        var total: Int? = 0
        if (modelList.size > 0) {

            for (i in 0..modelList.size - 1) {
                var tax = 0
                if (modelList.get(i).tax.toString() != "0") {
                    tax = modelList.get(i).tax!!.toInt()
                }
                total = total!!.toInt() + modelList.get(i).after_item_price!!.toInt() + tax
            }
        }
        total_amount = total ?: 0
        tv_total.setText("Total amount: " + ConfigPOJO.currency + " " + total.toString())

        tvTotalPayment.text = getFormattedDecimal(orderDetail?.payment?.final_charge?.toDouble()
                ?: 0.0) + " " + ConfigPOJO.currency
        setValues(tvPaymentType, orderDetail.payment?.payment_type)
        setValues(tvMaterialType, orderDetail.material_details)
        setValues(tvMaterialQuantity, orderDetail?.payment?.product_weight.toString())
        setValues(tvAdditionalInfo, orderDetail.details)
        setValues(tvPersonToDeliver, orderDetail.pickup_person_name)
        setValues(tvPersonDeliverPhone, orderDetail.pickup_person_phone)
        setValues(tvInvoiceNumber, orderDetail.invoice_number)
        setValues(tvDeliveryPerson, orderDetail.delivery_person_name)
    }

    private fun getFormattedDecimal(num: Double): String? {
        return String.format(Locale.US, "%.2f", num)
    }

    private fun setValues(textView: TextView, value: String?) {
        if (!value.isNullOrEmpty()) {
            textView.text = value
        } else {
            textView.text = getString(R.string.dash_six)
        }
    }

    override fun onInfoClickDelete(vi: View, position: Int) {
        positn = position
        presenter.deleteReqApi("[" + modelList[position].check_list_id + "]")
    }

    override fun onInfoClickEdit(tModelList: ArrayList<CheckListModelArray>) {
        var total: Int? = 0
        if (tModelList.size > 0) {

            for (i in 0..tModelList.size - 1) {
                if (tModelList.get(i).after_item_price != "") {
                    total = total!!.toInt() + tModelList.get(i).after_item_price!!.toInt()
                }
            }
        }
        tv_total.setText("Total : " + ConfigPOJO.currency + " " + total.toString())
    }

    override fun onApiSuccess(response: List<Order>?) {
        if (response?.isNotEmpty() == true) {
            orderDetail = response[0]
            ll_load_data.visibility = View.GONE

            if (ConfigPOJO.TEMPLATE_CODE != Constants.CORSA) {
                ll_data.visibility = View.VISIBLE
                ll_toll.visibility = View.GONE
                if (orderDetail.order_images_url != null && orderDetail.order_images_url?.size!! > 0) {
                    rvOrderDetailImages.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
                    rvOrderDetailImages.adapter = OrderImagesAdapter(orderDetail.order_images_url)

                    tvNoOrderImages.visibility = View.GONE
                    rvOrderDetailImages.visibility = View.VISIBLE
                } else {
                    tvNoOrderImages.visibility = View.VISIBLE
                    rvOrderDetailImages.visibility = View.GONE
                }

                setData()
            } else {
                setTollParkingCharges()
            }
        }
    }

    private fun setTollParkingCharges() {
        tollCharges.clear()
        parkingCharges.clear()

        if (orderDetail.payment?.toll_images != "") {
            el_toll.visibility = View.VISIBLE
            var toll_images = JSONArray(orderDetail.payment?.toll_images)
            for (i in 0 until toll_images.length()) {
                tollCharges.add(toll_images[i].toString())
            }
        }

        if (orderDetail.payment?.parking_images != "") {
            rl_parking.visibility = View.VISIBLE
            var parking_images = JSONArray(orderDetail.payment?.parking_images)
            for (i in 0 until parking_images.length()) {
                parkingCharges.add(parking_images[i].toString())
            }
        }

        tvTollAmount.text = orderDetail?.payment?.toll_charges.toString() + " " + ConfigPOJO.currency
        tvAmount.text = orderDetail?.payment?.parking_charges.toString() + " " + ConfigPOJO.currency

        setCheckListChargeAdapter()
        setCheckListParkingChargeAdapter()
        ll_toll.visibility = View.VISIBLE
    }

    private fun setCheckListChargeAdapter() {
        rv_list?.layoutManager = LinearLayoutManager(activity)
        checkListAdapterCharge = ChargesListAdapter(activity, tollCharges)
        rv_list?.adapter = checkListAdapterCharge!!
        rv_list?.isNestedScrollingEnabled = false
    }

    private fun setCheckListParkingChargeAdapter() {
        rv_par_list?.layoutManager = LinearLayoutManager(activity)
        checkListAdapterCharge = ChargesListAdapter(activity, parkingCharges)
        rv_par_list?.adapter = checkListAdapterCharge!!
        rv_par_list?.isNestedScrollingEnabled = false
    }

    override fun ApiSuccessDelete() {
        modelList.removeAt(positn)

        checkListAdapter?.notifyDataSetChanged()
        var total: Int? = 0
        if (modelList.size > 0) {

            for (i in 0..modelList.size - 1) {
                total = total!!.toInt() + modelList.get(i).after_item_price!!.toInt()
            }
        }
        tv_total.setText("Total : " + ConfigPOJO.currency + " " + total.toString())
    }

    override fun ApiSuccessUpdate() {
        Toast.makeText(activity, "Price Updated", Toast.LENGTH_LONG).show()
        presenter.onGoingOrderApi()
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        Log.e("ERROR", "ERROR")
    }

    override fun handleApiError(code: Int?, error: String?) {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }


}