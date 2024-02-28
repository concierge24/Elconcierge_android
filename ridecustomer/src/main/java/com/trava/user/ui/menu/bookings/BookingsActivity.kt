package com.trava.user.ui.menu.bookings

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.archit.calendardaterangepicker.customviews.DateRangeCalendarView
import com.trava.user.R
import com.trava.user.databinding.ActivityBookingsBinding
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.*
import com.trava.utilities.constants.ACCESS_TOKEN_KEY
import com.trava.utilities.constants.CATEGORY_ID
import kotlinx.android.synthetic.main.activity_bookings.*
import java.util.*
import kotlin.collections.ArrayList

class BookingsActivity : AppCompatActivity(),BookingContract.View {

    var binding : ActivityBookingsBinding ?= null
    var start_Date : String ?= ""
    var end_Date : String ?= ""
    val presenter = BookingsPresenter()
    var type : String = "UPCOMING"
    var isFiltered : Boolean = false
    private var dialogIndeterminate: DialogIndeterminate? = null

    private val listBookings = ArrayList<Order?>()

    private var skip = 0

    private var isLoading = false

    private var isLastPage = false

    private var adapter: BookingAdapter? = null

    private var catId="0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_bookings)
        binding!!.color = ConfigPOJO.Companion

        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)

        presenter.attachView(this)

        catId=(SharedPrefs.get().getString(CATEGORY_ID, "").toIntOrNull()?:0).toString()

        if(ConfigPOJO.TEMPLATE_CODE == Constants.MOVER){
            binding!!.headerTitle.setText(getString(R.string.tv_my_move))
        } else  if(ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE){
            binding!!.headerTitle.setText(getString(R.string.my_deliveries))
            binding!!.tvError.setText("No Deliveries")
        }

        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE)
        {
            cal_iv.visibility=View.GONE
        }
//        setAdapter()
        setAdapter()
        binding?.viewFlipperBooking?.displayedChild = 0

        dialogIndeterminate = DialogIndeterminate(this)

        setListeners()
        ivBack.setOnClickListener {
            onBackPressed()
        }

        cal_iv.setOnClickListener {
            openCalendarViewDialog(this)
        }

        if(ConfigPOJO.multiple_request.toInt()>1){
            type = "ACTIVE"
            upcoming_indicator.visibility = View.GONE
            past_indicator.visibility = View.GONE
            active_indicator.visibility = View.VISIBLE
            resetPagination(type)
        }else{
            type="UPCOMING"
            upcoming_indicator.visibility = View.VISIBLE
            past_indicator.visibility = View.GONE
            active_indicator.visibility = View.GONE
            resetPagination("UPCOMING")
        }
    }

    private fun resetPagination(type: String) {
        skip = 0
        isLastPage = false
        adapter?.setAllItemsLoaded(false)
        bookingHistoryApiCall(type)
        this.type = type
    }

    private fun setListeners() {
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            resetPagination(type)
        }

        past_rl.setOnClickListener {
            past_indicator.visibility = View.VISIBLE
            upcoming_indicator.visibility = View.GONE
            active_indicator.visibility = View.GONE
            resetPagination("PAST")
        }

        up_rl.setOnClickListener {
            upcoming_indicator.visibility = View.VISIBLE
            past_indicator.visibility = View.GONE
            active_indicator.visibility = View.GONE
            resetPagination("UPCOMING")
        }

        act_rl.setOnClickListener {
            upcoming_indicator.visibility = View.GONE
            past_indicator.visibility = View.GONE
            active_indicator.visibility = View.VISIBLE
            resetPagination("ACTIVE")
        }
    }


    fun setAdapter() {
        binding?.rvBookings?.layoutManager = LinearLayoutManager(binding?.rvBookings?.context)
        adapter = BookingAdapter(listBookings)
        binding?.rvBookings?.adapter = adapter
        binding?.rvBookings?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = binding?.rvBookings?.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
//                    if (layoutManager.findLastVisibleItemPosition() == adapter?.itemCount){
//                        bookingHistoryApiCall()
//                    }
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= Constants.PAGE_LIMIT) {
                        bookingHistoryApiCall(type)
                    }
                }
            }
        })

    }

    private fun bookingHistoryApiCall(type : String) {
        if (!CheckNetworkConnection.isOnline(this)) {
            CheckNetworkConnection.showNetworkError(binding?.root!!)
            return
        }
        when (type) {
            "PAST" -> {
                isLoading = true
                presenter.getHistoryList(Constants.PAGE_LIMIT, skip, 1,"","",catId)//type=1
            }

            "UPCOMING" -> {
                isLoading = true
                presenter.getHistoryList(Constants.PAGE_LIMIT, skip, 2,"","",catId)//type=2
            }

            "ACTIVE" ->{
                isLoading = true
                presenter.getHistoryList(Constants.PAGE_LIMIT, skip, 3,"","",catId)//type=2
            }
        }
    }


    private fun openCalendarViewDialog(bookingsActivity: BookingsActivity) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.layout_calendarview)

        var window = dialog.getWindow()
        var wlp = window?.getAttributes()
        wlp?.gravity = Gravity.TOP
        wlp?.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window?.setAttributes(wlp)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val calendar = dialog.findViewById<DateRangeCalendarView>(R.id.calendar)
        val close_iv = dialog.findViewById<ImageView>(R.id.close_iv)
        val tv_apply = dialog.findViewById<TextView>(R.id.tv_apply)
        val rl_back = dialog.findViewById<RelativeLayout>(R.id.rl_back)
        val typeface: Typeface = Typeface.createFromAsset(this.assets, "mont_regular.ttf")
        calendar.setFonts(typeface)
        rl_back.background=StaticFunction.changeBorderTextColor(ConfigPOJO.secondary_color, ConfigPOJO.secondary_color, GradientDrawable.RECTANGLE)
        tv_apply.background=StaticFunction.changeBorderTextColor(ConfigPOJO.primary_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        calendar.setCalendarListener(object : CalendarListener {
            override fun onFirstDateSelected(startDate: Calendar) {
//                Toast.makeText(bookingsActivity, "Start Date: " + startDate.getTime().toString(), Toast.LENGTH_SHORT).show()
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDateRangeSelected(startDate: Calendar, endDate: Calendar) {
//                Toast.makeText(bookingsActivity, "Start Date: " + startDate.getTime().toString().toString() + " End date: " + endDate.getTime().toString(), Toast.LENGTH_SHORT).show()
                 start_Date = DateUtils.getFormattedTime(startDate) + " 00:00:00"
                 end_Date = DateUtils.getFormattedTime(endDate) + " 00:00:00"
            }

        })

        tv_apply.setOnClickListener {
            dialog.dismiss()
            if(!TextUtils.isEmpty(start_Date)){
                when(type){
                    "PAST" -> {
                        filterList(start_Date!!,end_Date!!,"0")
                    }

                    "UPCOMING" -> {
                        filterList(start_Date!!,end_Date!!,"1")
                    }
                }
            }
            else
            {
                Toast.makeText(this,"Please select start and end date", Toast.LENGTH_SHORT).show()
            }
        }

        close_iv.setOnClickListener {
            dialog.dismiss()
            start_Date = ""
            end_Date = ""
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    fun filterList(startDate : String , endDate : String,screen : String){
        if (!CheckNetworkConnection.isOnline(this)) {
            CheckNetworkConnection.showNetworkError(binding?.root!!)
            return
        }
        when (screen) {
            "0" -> {
                isFiltered = true
                presenter.getHistoryList(Constants.PAGE_LIMIT, 0, 1,startDate,endDate,catId)//type=1
            }

            "1" -> {
                isFiltered = true
                presenter.getHistoryList(Constants.PAGE_LIMIT, 0, 2,startDate,endDate,catId)//type=2
            }
        }
    }


    override fun onApiSuccess(listBookings: ArrayList<Order?>) {
        isLoading = false
        binding?.swipeRefreshLayout?.isRefreshing = false

        if(listBookings.size==0){
            this.listBookings.clear()
            this.listBookings.addAll(listBookings)
            adapter?.notifyDataSetChanged()
            binding?.viewFlipperBooking?.displayedChild = 2
        }else{
            if (skip == 0 && listBookings.size == 0) {
                binding?.viewFlipperBooking?.displayedChild = 2
            } else {
                if (skip == 0) {
                    this.listBookings.clear()
                }
                if(isFiltered){
                    this.listBookings.clear()
                }
                if (listBookings.size < Constants.PAGE_LIMIT) {
                    isLastPage = true
                    adapter?.setAllItemsLoaded(true)
                }
                skip += Constants.PAGE_LIMIT
                binding?.viewFlipperBooking?.displayedChild = 1
                this.listBookings.addAll(listBookings)
                adapter?.notifyDataSetChanged()

            }
            isFiltered = false
        }
    }

    override fun onCancelApiSuccess() {
        // Do nothing
        isFiltered = false

    }

    override fun onFilterSuccess() {
        isFiltered = false

    }

    override fun onOrderDetailsSuccess(response: Order?) {
        // Do nothing
        isFiltered = false

    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)

    }

    override fun apiFailure() {
        isLoading = false
        binding?.swipeRefreshLayout?.isRefreshing = false
        binding?.viewFlipperBooking?.displayedChild = 2
        binding?.root?.showSWWerror()
        isFiltered = false
    }

    override fun handleApiError(code: Int?, error: String?) {
        isLoading = false
        binding?.swipeRefreshLayout?.isRefreshing = false
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(this)
        } else {
            binding?.root?.showSnack(error.toString())
        }
        isFiltered = false

    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()

    }


}