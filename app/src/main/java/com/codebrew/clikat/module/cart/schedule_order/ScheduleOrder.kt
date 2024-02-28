package com.codebrew.clikat.module.cart.schedule_order

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.DateTimeUtils
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.data.AppDataManager
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.Currency
import com.codebrew.clikat.data.model.api.ListItem
import com.codebrew.clikat.databinding.FragmentAgentTimeSlotBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.modal.other.TimeSlot
import com.codebrew.clikat.modal.slots.SupplierAvailableDatesItem
import com.codebrew.clikat.modal.slots.SupplierSlots
import com.codebrew.clikat.modal.slots.SupplierTimingsItem
import com.codebrew.clikat.modal.slots.TableSlotsItem
import com.codebrew.clikat.module.agent_time_slot.AgentViewModel
import com.codebrew.clikat.module.selectAgent.adapter.TimeSlotAdapter
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.Utils
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_agent_time_slot.*
import kotlinx.android.synthetic.main.nothing_found.*
import kotlinx.android.synthetic.main.toolbar_app.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ScheduleOrder : BaseActivity<FragmentAgentTimeSlotBinding, AgentViewModel>(), TabLayout.OnTabSelectedListener,
        SwipeRefreshLayout.OnRefreshListener, BaseInterface, TimeSlotAdapter.TimeSlotCallback {


    @Inject
    lateinit var factory: ViewModelProviderFactory


    private lateinit var viewModel: AgentViewModel

    @Inject
    lateinit var dateTimeUtils: DateTimeUtils

    @Inject
    lateinit var dataManger: AppDataManager

    var settingData: SettingModel.DataBean.SettingData? = null


    private var time24Format = ""

    private var dateYYMMDD = ""

    private var timeSlots: MutableList<TimeSlot>? = null

    private var bookingTimeSlot = ""

    private var slotAdapter: TimeSlotAdapter? = null

    private val colorConfig by lazy { Configurations.colors }

    private val tempList = ArrayList<SupplierAvailableDatesItem>()

    @Inject
    lateinit var mDateTime: DateTimeUtils

    @Inject
    lateinit var appUtil: AppUtils

    private var latitude: String? = null
    private var longitude: String? = null
    private var supplierId: String? = null
    private val dateList: MutableList<SupplierAvailableDatesItem> = mutableListOf()

    private var supplierAvailableData: SupplierSlots? = null
    private var tableItem: ListItem? = null
    private var selectedCurrency: Currency? = null
    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_agent_time_slot
    }

    override fun getViewModel(): AgentViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(AgentViewModel::class.java)
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        colorConfig.toolbarColor = colorConfig.appBackground
        colorConfig.toolbarText = colorConfig.textAppTitle

        viewDataBinding.color = colorConfig
        viewDataBinding.drawables = Configurations.drawables
        viewDataBinding.strings = appUtil.loadAppConfig(0).strings

        viewModel.navigator = this
        settingData = dataManger.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
        selectedCurrency = dataManger.getGsonValue(PrefenceConstants.CURRENCY_INF, Currency::class.java)
        refreshLayout.setOnRefreshListener(this)
        tabLayout.addOnTabSelectedListener(this)
        //calculateUserDate()
        initialise()
        setAdapter()
        listeners()

        timeSlotObserver()
        suppliersAvailabilityObserver()
        tableTimeSlotObserver()
        holdSlotsObserver()
        getSupplierAvailabilities()
    }


    private fun initialise() {

        if (intent != null) {
            if (intent.hasExtra("supplierId"))
                supplierId = intent?.getStringExtra("supplierId")
            if (intent.hasExtra("latitude"))
                latitude = intent?.getStringExtra("latitude")
            if (intent.hasExtra("longitude"))
                longitude = intent?.getStringExtra("longitude")
        }

        tb_title.text = getString(R.string.choose_date_time)
        clScheduleOrder?.visibility = View.VISIBLE
        btn_book_agent?.visibility = View.GONE
        rgGroup?.visibility = View.VISIBLE

        time24Format = ""
        dateYYMMDD = ""
        timeSlots = ArrayList()

        if (intent?.hasExtra("dineIn") == true) {
            rgGroup?.visibility = View.GONE
            rbTableBooking?.isChecked = true
            /** neet values are added according to params checked into web inspect mode */
            latitude = "0"
            longitude = "0"
        }

    }

    private fun setAdapter() {
        slotAdapter = TimeSlotAdapter(timeSlots)
        slotAdapter?.settingCallback(this)
        rv_timeslot.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv_timeslot.adapter = slotAdapter
    }

    private fun listeners() {

        btnConfirm.setOnClickListener {
            if (dateYYMMDD.isEmpty() || time24Format.isEmpty()) {
                viewDataBinding.root.onSnackbar(getString(R.string.date_time_msg))
            } else if (settingData?.table_book_mac_theme == "1" && intent.hasExtra("seating_capacity") && intent.hasExtra("supplierBranchId")) {
                holdSlotsApi()
            } else {
                screenSelection()
            }
        }

        btnCancel.setOnClickListener {
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        tb_back.setOnClickListener {
            finish()
        }

        rgGroup?.setOnCheckedChangeListener { _, _ ->
            getSupplierAvailabilities()
        }
    }

    private fun holdSlotsApi() {
        val hashMap = HashMap<String, String>()
        hashMap["supplier_id"] = supplierId ?: ""
        hashMap["slotDate"] = dateYYMMDD
        hashMap["slotTime"] = appUtil.convertDateOneToAnother(time24Format, "hh:mm aa", "HH:mm:ss")
                ?: ""
        hashMap["offset"] = SimpleDateFormat("ZZZZZ", DateTimeUtils.timeLocale).format(System.currentTimeMillis())
        hashMap["branch_id"] = intent?.getIntExtra("supplierBranchId", 0).toString()
        if (isNetworkConnected)
            viewModel.holdSlotApi(hashMap)
    }


    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (tempList.isNotEmpty()) {
            dateYYMMDD = tab?.position?.let { tempList[it].fromDate.toString() } ?: ""
            getAgentSlots(dateYYMMDD)
        }
    }

    override fun onRefresh() {
        refreshLayout.isRefreshing = false
        if (tempList.isNotEmpty()) {
            dateYYMMDD = tabLayout.selectedTabPosition.let { tempList[it].fromDate.toString() }
            getAgentSlots(dateYYMMDD)
        }
    }


    override fun refreshAdapter(type: String?, adapterPosition: Int) {
        slotAdapter?.refreshAdapter(type, adapterPosition)
    }


    private fun screenSelection() {
        val datePosition = tabLayout?.selectedTabPosition
        if (datePosition != null && datePosition != -1 && supplierAvailableData != null) {

            val supplierSlotsData = SupplierSlots()
            supplierSlotsData.supplierSlotsInterval = supplierAvailableData?.supplierSlotsInterval
            supplierSlotsData.supplierTimings = arrayListOf(getSelectedSlot(datePosition))
            supplierSlotsData.startDateTime = mDateTime.convertDateOneToAnother("$bookingTimeSlot $time24Format",
                    "yyyy-MM-dd hh:mm aa", "yyyy-MM-dd HH:mm:ss")
            supplierSlotsData.endDateTime = getEndDate()
            supplierSlotsData.selectedTable = tableItem

            val intent = Intent()
            intent.putExtra("slotDetail", supplierSlotsData)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun getSelectedSlot(datePosition: Int): SupplierTimingsItem? {
        val timingList = if (tempList[datePosition].isWeek == true && tempList[datePosition].dayId != null) {
            supplierAvailableData?.supplierTimings?.filter { it?.dayId == tempList[datePosition].dayId }
        } else
            supplierAvailableData?.supplierTimings?.filter { it?.dateId == tempList[datePosition].id }

        timingList?.forEach {
            val startTime = mDateTime.getDateFormat("$bookingTimeSlot ${it?.startTime}",
                    "yyyy-MM-dd HH:mm:ss")
            val endTime = mDateTime.getDateFormat("$bookingTimeSlot ${it?.endTime}",
                    "yyyy-MM-dd HH:mm:ss")
            val selectedSlot = mDateTime.getDateFormat("$bookingTimeSlot $time24Format",
                    "yyyy-MM-dd hh:mm aa")
            if (endTime?.time ?: 0L >= selectedSlot?.time ?: 0L && startTime?.time ?: 0L <= selectedSlot?.time ?: 0L)
                return it
        }
        return null
    }

    private fun getEndDate(): String {
        val startDate = mDateTime.getCalendarFormat("$bookingTimeSlot $time24Format", "yyyy-MM-dd hh:mm aa")
        startDate?.add(Calendar.MINUTE,supplierAvailableData?.supplierSlotsInterval?:0)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        return format.format(startDate?.time?:Date())
    }

    private fun getSupplierAvailabilities() {
        val type = if (rbDelivery?.isChecked == true) 1 else if (rbPickup?.isChecked == true) 2 else 3
        val param = HashMap<String, String>()
        param["date_order_type"] = type.toString()
        param["supplier_id"] = supplierId ?: ""
        param["longitude"] = longitude ?: ""
        param["latitude"] = latitude ?: ""

        if (isNetworkConnected)
            viewModel.getSupplierAvailabilities(param)
    }

    private fun suppliersAvailabilityObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<SupplierSlots> { resource ->
            supplierAvailableData = resource
            clearData()
            setAgentDates(resource)
            //  setAvailableDates(resource.supplierAvailableDates)
        }
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.supplierAvailabilities.observe(this, catObserver)
    }

    private fun setAvailableDates(supplierAvailableDates: ArrayList<SupplierAvailableDatesItem>?) {
        /*add future dates and status ==1 */
        supplierAvailableDates?.forEach {
            val validDate = dateTimeUtils.getDateFormat(it.fromDate ?: "", "yyyy-MM-dd")

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            if (it.status == 1 && (validDate?.after(Date()) == true || it.fromDate?.equals(date) == true)) {
                tempList.add(it)
                val text = appUtil.convertDateOneToAnother(it.fromDate.toString(), "yyyy-MM-dd", "EEE, dd MMMM")
                        ?: ""
                tabLayout.addTab(tabLayout.newTab().setText(text))
            }

        }
    }

    private fun calculateUserDate() {
        val actualFormat = SimpleDateFormat("yyyy-MM-dd", DateTimeUtils.timeLocale)
        val outputFormat = SimpleDateFormat("EEE, dd MMMM", DateTimeUtils.timeLocale)
        val cal = Calendar.getInstance()

        for (i in 0..30) {
            tempList.add(SupplierAvailableDatesItem(fromDate = actualFormat.format(cal.time)))
            tabLayout.addTab(tabLayout.newTab().setText(outputFormat.format(cal.time)))

            cal.add(Calendar.DATE, 1)
        }
    }

    private fun setAgentDates(dataBeans: SupplierSlots) {

        dateList.clear()

        val cal = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", DateTimeUtils.timeLocale)

        if (dataBeans.supplierAvailableDates?.count() ?: 0 > 0) {
            for (userAvailDatesBean in dataBeans.supplierAvailableDates ?: arrayListOf()) {
                val validDate = dateTimeUtils.getDateFormat(userAvailDatesBean.fromDate
                        ?: "", "yyyy-MM-dd")
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                if (userAvailDatesBean.status == 1 && (validDate?.after(Date()) == true || userAvailDatesBean.fromDate?.equals(date) == true)) {
                    userAvailDatesBean.date = dateFormat.parse(userAvailDatesBean.fromDate
                            ?: "")
                    dateList.add(userAvailDatesBean)
                }
            }
        }

        val date = Date()
        if (dataBeans.weeksData?.count() ?: 0 > 0) {
            for (userWeekAvailBean in dataBeans.weeksData ?: arrayListOf()) {
                if (userWeekAvailBean.status == 1) {
                    cal.clear()
                    cal.time = date
                    for (i in 0..4) {
                        cal[Calendar.DAY_OF_WEEK] = userWeekAvailBean.dayId?.plus(1) ?: 0
                        if (date.before(cal.time) || date == cal.time) {
                            userWeekAvailBean.date = cal.time
                            userWeekAvailBean.fromDate = dateFormat.format(cal.time)
                            dateList.add(SupplierAvailableDatesItem(date = cal.time, fromDate = dateFormat.format(cal.time),
                                    dayId = userWeekAvailBean.dayId, id = userWeekAvailBean.id, isWeek = true))
                        }
                        cal.add(Calendar.DAY_OF_MONTH, 7)
                    }
                }
            }
        }

        dateList.sort()
        for (availabilityDate in dateList) {
            tempList.add(availabilityDate)
            tabLayout.addTab(tabLayout.newTab().setText(appUtil.convertDateOneToAnother(availabilityDate.fromDate
                    ?: "", "yyyy-MM-dd", "EEE, dd MMM")))
        }
    }

    private fun clearData() {
        tempList.clear()
        timeSlots?.clear()
        slotAdapter?.notifyDataSetChanged()
        time24Format = ""
        dateYYMMDD = ""
        bookingTimeSlot = ""
        tabLayout?.removeAllTabs()
    }


    private fun getAgentSlots(date: String) {
        bookingTimeSlot = date
        val type = if (rbDelivery?.isChecked == true) 1 else if (rbPickup?.isChecked == true) 2 else 3
        val param = HashMap<String, String>()
        param["date"] = date
        param["date_order_type"] = type.toString()
        param["supplier_id"] = supplierId ?: ""
        param["longitude"] = longitude ?: ""
        param["latitude"] = latitude ?: ""

        if (isNetworkConnected) {
            if (settingData?.table_book_mac_theme == "1" && intent.hasExtra("seating_capacity") && intent.hasExtra("supplierBranchId")) {
                param["seating_capacity"] = intent?.getIntExtra("seating_capacity", 0).toString()
                param["branch_id"] = intent?.getIntExtra("supplierBranchId", 0).toString()
                viewModel.getTableSlotsList(param)
            } else
                viewModel.getSlotsList(param)
        }
    }


    private fun timeSlotObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<List<String>> { resource ->
            updateAgentSlots(resource)

        }
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.slotsData.observe(this, catObserver)
    }

    private fun tableTimeSlotObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<TableSlotsItem> { resource ->
            if (settingData?.table_book_mac_theme == "1") {
                tableItem = ListItem()
                tableItem?.seatingCapacity = intent?.getIntExtra("seating_capacity", 0)
                tableItem?.table_booking_price = resource.tableBookingPrice

                groupTableFee?.visibility = View.VISIBLE
                tvBookingFee?.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL,
                        Utils.getPriceFormat(tableItem?.table_booking_price
                                ?: 0f, settingData, selectedCurrency))

            } else {
                tableItem = resource?.availableTables?.firstOrNull()
                if (tableItem != null) {
                    groupTableFee?.visibility = View.VISIBLE
                    tvBookingFee?.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL,
                            Utils.getPriceFormat(tableItem?.table_booking_price
                                    ?: 0f, settingData, selectedCurrency))
                } else
                    groupTableFee?.visibility = View.GONE
            }



            updateAgentSlots(resource?.slots)

        }
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.tableSlotsData.observe(this, catObserver)
    }

    private fun holdSlotsObserver() {
        // Create the observer which updates the UI.
        val observer = Observer<Any> { resource ->
            screenSelection()
        }
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.holdSlotsLiveData.observe(this, observer)
    }

    private fun updateAgentSlots(resource: List<String>?) {
        timeSlots?.clear()

        resource?.let {
            settingTimeSlot(it)
        }

        tvText?.text = getString(R.string.no_slots_available)
        noData?.visibility = if (timeSlots?.size == 0) View.VISIBLE else View.GONE
        rv_timeslot.visibility = if ((timeSlots?.size ?: 0) > 0) View.VISIBLE else View.GONE

        slotAdapter?.refreshAdapter("", -1)

    }


    private fun settingTimeSlot(data: List<String>) {

        var dayofWeek = ""

        var timeSlotPeriod: TimeSlot


        val listHashMap = mutableMapOf<String, List<String>>()

        var timeSlotsList: MutableList<String>

        val startCalendar = Calendar.getInstance()
        val currentCalendar = Calendar.getInstance()

        if (settingData?.display_slot_with_difference == "1")
            currentCalendar.add(Calendar.HOUR, supplierAvailableData?.scheduleBufferTime ?: 0)

        for (timeSlot in data) {
            startCalendar.clear()
            try {
                startCalendar.time = mDateTime.getCalendarFormat("$bookingTimeSlot $timeSlot", "yyyy-MM-dd HH:mm:ss")?.time
                        ?: Date()
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (settingData?.table_book_mac_theme == "1") {
                when (startCalendar.get(Calendar.HOUR_OF_DAY)) {
                    in 0..12 -> dayofWeek = getString(R.string.breakfast)
                    in 13..20 -> dayofWeek = getString(R.string.lunch)
                    in 21..23 -> dayofWeek = getString(R.string.dinner)
                }
            } else {
                when (startCalendar.get(Calendar.HOUR_OF_DAY)) {
                    in 0..11 -> dayofWeek = getString(R.string.morning)
                    in 12..15 -> dayofWeek = getString(R.string.aftenoon)
                    in 16..20 -> dayofWeek = getString(R.string.evening)
                    in 21..23 -> dayofWeek = getString(R.string.night)
                }
            }


            timeSlotsList = ArrayList()


            if (currentCalendar.time.before(startCalendar.time)) {

                if (listHashMap.containsKey(dayofWeek)) {

                    listHashMap[dayofWeek]?.let { timeSlotsList.addAll(it) }

                    timeSlotsList.add(appUtil.convertDateOneToAnother(timeSlot, "HH:mm:ss", "hh:mm aaa")
                            ?: "")

                    listHashMap[dayofWeek] = timeSlotsList
                } else {

                    timeSlotsList.add(appUtil.convertDateOneToAnother(timeSlot, "HH:mm:ss", "hh:mm aaa")
                            ?: "")

                    listHashMap[dayofWeek] = timeSlotsList
                }
            }
        }

//        if (listHashMap.isEmpty()) {
//            if (appUtil.convertDateOneToAnother(bookingTimeSlot, "yyyy-MM-dd", "EEE, dd MMM") == tabLayout.getTabAt(0)?.text) {
//                tabLayout.removeTabAt(0)
//            }
//
//            getAgentSlots(appUtil.convertDateOneToAnother(tabLayout.getTabAt(0)?.text.toString() + "," + calendar.get(Calendar.YEAR)
//                    , "EEE, dd MMM,yyyy", "yyyy-MM-dd") ?: "")
//        }


        for ((key, value) in listHashMap) {

            timeSlotPeriod = TimeSlot(key, value, true)

            timeSlots?.add(timeSlotPeriod)
        }


    }

    override fun selectTimeSlot(slot: String?) {
        time24Format = slot ?: ""
    }

    override fun onErrorOccur(message: String) {
        viewDataBinding.root.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

}
