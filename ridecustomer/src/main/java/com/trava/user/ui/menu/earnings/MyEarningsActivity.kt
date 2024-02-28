package com.trava.user.ui.menu.earnings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.trava.user.R
import com.trava.user.ui.home.stories.WatchStories
import com.trava.user.utils.AppUtils
import com.trava.user.webservices.models.earnings.AdsEarningResponse
import com.trava.user.webservices.models.earnings.ResultItem
import com.trava.user.webservices.models.earnings.StartEndDateModel
import com.trava.utilities.*
import com.trava.utilities.constants.ACCESS_TOKEN_KEY
import kotlinx.android.synthetic.main.activity_my_earnings.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MyEarningsActivity : AppCompatActivity(), View.OnClickListener, OnChartValueSelectedListener, WeekAdapter.OnclickWeek, EarningContract.View {
    private var dataSet: BarDataSet? = null
    private var presenter = EarningPresenter()
    private var weeksList: ArrayList<String>? = null
    private var daysList: ArrayList<String>? = null
    private var graphList: ArrayList<String>? = null
    private var earningList : ArrayList<ResultItem> = ArrayList<ResultItem>()
    private var weekAdapter: WeekAdapter ?= null
    private var strDate: String = ""
    private var enDate: String = ""
    private var entries: ArrayList<BarEntry>? = null
    private var year = Calendar.YEAR
    private var month = Calendar.MONTH
    private var startEndDateList: ArrayList<StartEndDateModel>? = null
    var currentWeekDatesLisPos = 0
    var currentWeekDates = ""
    var currentDayDate =""
    private var viewType: String = "Week"
    private var dialog: DialogIndeterminate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_earnings)
        presenter.attachView(this)
        dialog = DialogIndeterminate(this)

        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd")
        currentDayDate = df.format(c.time)
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        currentWeekDates = df.format(c.time)

        setMonthYear()
        setWeekData(month, year, viewType)
        clickListeners()
    }

    private fun setMonthYear() {
        val calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
    }

    private fun getCurrentWeekData(){
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd")
        currentDayDate = df.format(c.time)
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        currentWeekDates = df.format(c.time)
    }

    private fun setWeekData(month: Int, year: Int, type: String) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.US)
        dateFormat.timeZone = TimeZone.getDefault()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val simpleDateFormat = SimpleDateFormat("MMM", Locale.US)
        val apiFormatDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val monthName = simpleDateFormat.format(calendar.time)
        var days: Int
        var start: String = ""
        var end: String = ""

        if (type == "Week") {
            getCurrentWeekData()
            currentWeekDatesLisPos = 0
            val day = Calendar.SUNDAY - calendar.get(Calendar.DAY_OF_WEEK)
            if (day < 0) {
                calendar.add(Calendar.DATE, 7 + day)
            } else {
                calendar.add(Calendar.DATE, day)
            }
            days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            start = dateFormat.format(calendar.time)
            end = "$days $monthName $year"
            setWeekRecyclerView(start, end)
            getstaatDateAndEmdDate(start, end)
        } else if (viewType == "Day") {
            val cal = Calendar.getInstance()
            cal.time = (Calendar.getInstance().time)
            cal[Calendar.DAY_OF_MONTH] = 1
            val myMonth = cal[Calendar.MONTH]
            start = dateFormat.format(calendar.time)
            daysList = ArrayList<String>()
            startEndDateList = ArrayList<StartEndDateModel>()
            while (myMonth == cal[Calendar.MONTH]) {
                Log.e("dadsdadasdaD", "SFSSDFDSF" + dateFormat.format(cal.time))
                daysList!!.add(dateFormat.format(cal.time))
                end = dateFormat.format(cal.time)

                var model = StartEndDateModel()
                model.startDate = apiFormatDate.format(cal.time).toString()  // for startdate of weeek
                model.endDate = apiFormatDate.format(cal.time).toString()  // for enddate week
                startEndDateList?.add(model)

                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
            setUpDaysAdapter()
            getstaatDateAndEmdDate(start, end)
        } else if (viewType == "Month") {
            val cal = Calendar.getInstance()
            cal.time = (Calendar.getInstance().time)
            start = dateFormat.format(calendar.time)
            cal[Calendar.DAY_OF_MONTH] = 1
            val myMonth = cal[Calendar.MONTH]
            daysList = ArrayList<String>()
            daysList!!.add(start)
            /*end = dateFormat.format(calendar.time)


            startEndDateList = ArrayList<StartEndDateModel>()
            var model = StartEndDateModel()
            */
//            daysList = ArrayList<String>()
            /*startEndDateList = ArrayList<StartEndDateModel>()
            while (myMonth == cal[Calendar.MONTH]) {
                Log.e("dadsdadasdaD", "SFSSDFDSF" + dateFormat.format(cal.time))
//                daysList!!.add(dateFormat.format(cal.time))
                end = dateFormat.format(cal.time)

//                var model = StartEndDateModel()
//                model.startDate = apiFormatDate.format(cal.time).toString()  // for startdate of weeek
//                model.endDate = apiFormatDate.format(cal.time).toString()  // for enddate week
//                startEndDateList?.add(model)

                cal.add(Calendar.DAY_OF_MONTH, 1)
            }

            setUpDaysAdapter()
            getstaatDateAndEmdDate(start, end)*/
        }
    }

    private fun setUpDaysAdapter() {
        currentWeekDates = currentDayDate
        for (i in 0 until startEndDateList!!.size) {
            Log.e("abceded",startEndDateList!![i].startDate?:"")
            if (startEndDateList!![i].startDate == currentDayDate) {
                currentWeekDatesLisPos = i
                startEndDateList!![i].isSelected = true
            }
        }

        rvWeekList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        (rvWeekList.layoutManager as LinearLayoutManager).scrollToPosition(currentWeekDatesLisPos)
        weekAdapter= WeekAdapter(daysList, startEndDateList, this)
        rvWeekList.adapter = weekAdapter

        var map = HashMap<String, Any>()
        map["access_token"] = SharedPrefs.get().getString(ACCESS_TOKEN_KEY, "")
        map["start_dt"] = startEndDateList?.get(0)?.startDate.toString() + " 00:00:00"
        map["end_dt"] = startEndDateList?.get(startEndDateList?.size!!.minus(1))?.endDate.toString() + " 00:00:00"
        presenter.requestEarnings(map)
    }

    private fun setWeekRecyclerView(start: String, end: String) {
        weeksList = ArrayList()
        startEndDateList = ArrayList<StartEndDateModel>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.US)
        dateFormat.timeZone = TimeZone.getDefault()

        try {
            val startDate = dateFormat.parse(start)
            val endDate = dateFormat.parse(end)

            calendar.time = startDate
            val calendarFormat = SimpleDateFormat("d MMM", Locale.US)
            val apiFormatDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            while (calendar.time.time <= endDate.time) {
                var model = StartEndDateModel()
                var date = calendarFormat.format(calendar.time).toString() + "-"

                model.startDate = apiFormatDate.format(calendar.time).toString()  // for startdate of weeek

                calendar.add(Calendar.DATE, 6)

                model.endDate = apiFormatDate.format(calendar.time).toString()  // for enddate week

                /*if ((startEndDateList?.size ?: 0) > 0)
                    model.isSelected = false
                else {
                    model.isSelected = true
                }*/
                startEndDateList?.add(model)

                date = date + calendarFormat.format(calendar.time).toString()
                weeksList?.add(date)
                calendar.add(Calendar.DATE, 1)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        for (i in startEndDateList!!.indices) {
            if (startEndDateList!![i].startDate == currentWeekDates) {
                currentWeekDatesLisPos = i
                startEndDateList!![i].isSelected = true
            }
        }

        rvWeekList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        (rvWeekList.layoutManager as LinearLayoutManager).scrollToPosition(currentWeekDatesLisPos)
        weekAdapter = WeekAdapter(weeksList, startEndDateList, this)
        rvWeekList.adapter = weekAdapter

        var map = HashMap<String, Any>()
        map["access_token"] = SharedPrefs.get().getString(ACCESS_TOKEN_KEY, "")
        map["start_dt"] = startEndDateList?.get(currentWeekDatesLisPos)?.startDate.toString() + " 00:00:00"
        map["end_dt"] = startEndDateList?.get(currentWeekDatesLisPos)?.endDate.toString() + " 00:00:00"
        presenter.requestEarnings(map)
    }

    private fun getstaatDateAndEmdDate(start: String, end: String) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.US)
        dateFormat.timeZone = TimeZone.getDefault()

        try {
            val startDate = dateFormat.parse(start)
            val endDate = dateFormat.parse(end)
            calendar.time = startDate
            val calendarFormat = SimpleDateFormat("d MMM", Locale.US)
            val format = SimpleDateFormat("yyyy-MM-dd HH:MM:SS", Locale.US)
            strDate = format.format(calendar.time).toString()

            Log.e("startDate_endDate", strDate)

            while (calendar.time.time <= endDate.time) {
                // var date = format.format(calendar.time).toString() + "-"
                calendar.add(Calendar.DATE, 6)
                enDate = format.format(calendar.time).toString()
                Log.e("enddate", enDate)
                calendar.add(Calendar.DATE, 1)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    private fun clickListeners() {
        tvDaily.setOnClickListener(this)
        tvWeekly.setOnClickListener(this)
        tvMonthly.setOnClickListener(this)
        btnEarnMore.setOnClickListener(this)
        ivBack.setOnClickListener(this)
        ivPrev.setOnClickListener(this)
        ivNext.setOnClickListener(this)

        changeVisibility(tvWeekly,tvDaily,  tvMonthly, viewWeekly, viewDaily, viewMonthly)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvDaily -> {
                viewType = "Day"
                setWeekData(month, year, viewType)
                changeVisibility(tvDaily, tvWeekly, tvMonthly, viewDaily, viewWeekly, viewMonthly)
            }
            R.id.tvWeekly -> {
                viewType = "Week"
                setWeekData(month, year, viewType)
                changeVisibility(tvWeekly, tvMonthly, tvDaily, viewWeekly, viewMonthly, viewDaily)
            }
            R.id.tvMonthly -> {
                viewType = "Month"
                setWeekData(month, year, viewType)
                changeVisibility(tvMonthly, tvWeekly, tvDaily, viewMonthly, viewWeekly, viewDaily)
            }
            R.id.ivBack -> {
                onBackPressed()
            }
            R.id.btnEarnMore -> {
                startActivity(Intent(this, WatchStories::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
            R.id.ivNext -> {
                currentWeekDatesLisPos+=1
                rvWeekList.scrollToPosition(currentWeekDatesLisPos)
//                currentWeekDatesLisPos += 1
//                if (currentWeekDatesLisPos < startEndDateList!!.size) {
//                    tvDurationText.text = startEndDateList!![currentWeekDatesLisPos].startDate.toString()
//                }
            }
            R.id.ivPrev -> {
                currentWeekDatesLisPos-=1
                rvWeekList.scrollToPosition(currentWeekDatesLisPos)
//                currentWeekDatesLisPos -= 1
//                if (currentWeekDatesLisPos > startEndDateList!!.size) {
//                    tvDurationText.text = startEndDateList!![currentWeekDatesLisPos].startDate.toString()
//                }
            }
        }
    }

    private fun changeVisibility(tvMonthly: TextView?, tvWeekly: TextView?, tvDaily: TextView?, viewMonthly: View?, viewDaily: View?, viewWeekly: View?) {
        tvMonthly?.setTextColor(ContextCompat.getColor(this, R.color.white))
        viewMonthly?.visibility = View.VISIBLE
        tvDaily?.setTextColor(ContextCompat.getColor(this, R.color.bt_very_light_gray))
        viewDaily?.visibility = View.GONE
        tvWeekly?.setTextColor(ContextCompat.getColor(this, R.color.bt_very_light_gray))
        viewWeekly?.visibility = View.GONE
    }

    private fun setUpBarGraph(daily: ArrayList<ResultItem>) {
        entries = ArrayList()
        entries!!.clear()
        for (i in daily!!.indices) {
            (entries as ArrayList<BarEntry>).add(BarEntry(i.toFloat(), (daily.get(i)?.earning!!).toFloat()))
        }

        barChart.setVisibleXRangeMaximum(100f);
        barChart.moveViewToX(50f);
        barChart.invalidate()
        setYChartAxis()
        setXChartAxis()
        setBarData()
        setBarDecor()
    }

    private fun setYChartAxis() {
        val yAxis = barChart.axisLeft
        yAxis.setDrawLabels(true)
        yAxis.setDrawAxisLine(false)
        yAxis.setDrawGridLines(true)
        yAxis.axisMinimum = 0f
        yAxis.textColor = ActivityCompat.getColor(this, R.color.white)
    }

    private fun setXChartAxis() {
        if(viewType == "Week") {
            graphList = ArrayList()
            graphList!!.clear()
            graphList = ArrayList()
            graphList?.add("Sun")
            graphList?.add("Mon")
            graphList?.add("Tue")
            graphList?.add("Wed")
            graphList?.add("Thu")
            graphList?.add("Fri")
            graphList?.add("Sat")

            val quarters1 = arrayOfNulls<String>(graphList?.size ?: 0)
            for (i in graphList?.indices ?: 0..0) {
                quarters1[i] = graphList?.get(i) ?: ""
            }
            val formatter1 = object : IndexAxisValueFormatter() {
                override fun getFormattedValue(value: Float, axis: AxisBase): String? {
                    return if (value <= axis.axisMaximum)
                        quarters1!![value.toInt()]
                    else
                        ""
                }
            }



            barChart.axisRight.isEnabled = false
            barChart.resetZoom()
            val xAxis1 = barChart.xAxis
            xAxis1.setDrawGridLines(false)
            xAxis1.valueFormatter = formatter1
            xAxis1.position = XAxis.XAxisPosition.BOTTOM
            xAxis1.setAvoidFirstLastClipping(false)
            xAxis1.textColor = ActivityCompat.getColor(this, R.color.white)
        }else if(viewType == "Day"){
            graphList = ArrayList()
            graphList!!.clear()
            for (i in 0 until daysList!!.size){
                graphList?.add(daysList!![i])
            }

            val quarters1 = arrayOfNulls<String>(graphList?.size ?: 0)
            for (i in graphList?.indices ?: 0..0) {
                quarters1[i] = graphList?.get(i) ?: ""
            }
            val formatter1 = object : IndexAxisValueFormatter() {
                override fun getFormattedValue(value: Float, axis: AxisBase): String? {
                    return if (value <= axis.axisMaximum)
                        quarters1!![value.toInt()]
                    else
                        ""
                }
            }
            barChart.axisRight.isEnabled = false
            barChart.resetZoom()
            val xAxis1 = barChart.xAxis
            xAxis1.setDrawGridLines(false)
            xAxis1.valueFormatter = formatter1
            xAxis1.position = XAxis.XAxisPosition.BOTTOM
            xAxis1.setAvoidFirstLastClipping(false)
            xAxis1.textColor = ActivityCompat.getColor(this, R.color.white)
        }
    }

    private fun setBarData() {
        dataSet = BarDataSet(entries, "")
        dataSet?.color = ContextCompat.getColor(this, R.color.white)
        val data = BarData(dataSet)
        data.setValueTextColor(ContextCompat.getColor(this, R.color.white))
        data.setDrawValues(false)
        data.setBarWidth(.40f)
        barChart.data = data
    }

    private fun setBarDecor() {
        barChart.legend.isEnabled = true
        barChart.description = null
        barChart.animateY(2000)
        barChart.animateX(2000)
        barChart.isDoubleTapToZoomEnabled = true
        barChart.setScaleEnabled(true)
        barChart.setPinchZoom(true)
        barChart.xAxis.granularity = 0.3f
        barChart.setVisibleXRangeMaximum(100f)
        barChart.setOnChartValueSelectedListener(this)
    }

    override fun onNothingSelected() {}

    override fun onValueSelected(e: Entry?, h: Highlight?) {
//        tvKmValue.text  = e!!.data!!.toString()+" ${getString(R.string.km)}"
    }

    override fun onClcikWeekListener(position: Int) {
        currentWeekDatesLisPos = position
        var map = HashMap<String, Any>()
        map["access_token"] = SharedPrefs.get().getString(ACCESS_TOKEN_KEY, "")
        map["start_dt"] = startEndDateList?.get(position)?.startDate.toString() + " 00:00:00"
        map["end_dt"] = startEndDateList?.get(position)?.endDate.toString() + " 00:00:00"
        presenter.requestEarnings(map)
    }

    override fun onEarningAiSuccess(responseData: AdsEarningResponse) {
        // tvTotalEarning.text = ConfigPOJO.currency+" " + String.format("%.2f", responseData.totalEarnings)
        Log.e("ASasasasASASAS",responseData.results?.size.toString()+"ASasas")
        if(responseData.statusCode == 200) {
            if (responseData.results!!.isNotEmpty()) {
                earningList.clear()
                tvTotalEarnings.text = "${responseData.total} ${getString(R.string.km)}"
                tvKmValue.text = "${responseData.total} ${getString(R.string.km)}"
                tvKmValue1.text = "${responseData.total} ${getString(R.string.km)}"
                earningList = responseData.results as ArrayList<ResultItem>
                setUpBarGraph(earningList)
            }
        }
    }

    override fun showLoader(isLoading: Boolean) {
        dialog!!.dismiss()
    }

    override fun apiFailure() {
        rootView.showSWWerror()
    }

    override fun handleApiError(code: Int?, error: String?) {
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(this)
        } else {
            rootView.showSnack(error.toString())
        }
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}