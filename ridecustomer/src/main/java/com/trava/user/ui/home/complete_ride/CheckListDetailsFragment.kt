package com.trava.user.ui.home.complete_ride

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.order.CheckListModelArray
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.DialogIndeterminate
import kotlinx.android.synthetic.main.layout_full_order_details.*

class CheckListDetailsFragment : DialogFragment() {

    companion object {
        const val TAG = "com.trava.driver.ui.home.orderDetails.FullOrderDetailsFragment"
    }

    var checkListAdapter: CheckListDetailsAdapter? = null
    private lateinit var orderDetail: Order
    private var dialogIndeterminate: DialogIndeterminate? = null
    private var modelList = ArrayList<CheckListModelArray>()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderDetail = arguments?.getParcelable("orderDetails")!!
        dialogIndeterminate = DialogIndeterminate(activity)
        sv_order_details.visibility = View.GONE
        ll_load_data.visibility = View.GONE
        rvOrderChecklist.visibility = View.VISIBLE
        tv_total.visibility = View.VISIBLE
        ll_data.visibility = View.VISIBLE
        setData()
        check_tv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        order_tv.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.white_color, GradientDrawable.RECTANGLE)
        tvChecklistNexttt.background = StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

        toolbarAbout.setNavigationOnClickListener {
            dismiss()
        }
    }

    private fun setCheckListAdapter() {
        rvOrderChecklist?.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        checkListAdapter = CheckListDetailsAdapter(activity,modelList)
        rvOrderChecklist?.adapter = checkListAdapter!!
    }

    private fun setData() {

        modelList.clear()
        modelList?.addAll(orderDetail?.check_lists)
        setCheckListAdapter()

        var total: Int? = 0
        if (modelList.size > 0) {

            for (i in 0..modelList.size - 1) {
                var tax=0
                if (modelList.get(i).tax != 0) {
                    tax = modelList.get(i).tax!!.toInt()
                }
                total = total!!.toInt() + modelList.get(i).after_item_price!!.toInt() + tax
            }
        }
        tv_total.setText("Total amount : " + ConfigPOJO.currency + " " + total.toString())
    }
}