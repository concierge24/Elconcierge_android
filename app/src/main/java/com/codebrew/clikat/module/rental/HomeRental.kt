package com.codebrew.clikat.module.rental

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.*
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.data.RentalDataType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.AppConstants.Companion.REQUEST_CODE_LOCATION
import com.codebrew.clikat.data.model.others.HomeRentalParam
import com.codebrew.clikat.data.model.others.RentalDayModel
import com.codebrew.clikat.databinding.CalendarDayLayoutBinding
import com.codebrew.clikat.databinding.FragmentHomeRentalBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.Autocomplete.IntentBuilder
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.ScrollMode
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import kotlinx.android.synthetic.main.fragment_home_rental.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject


private const val PLACE_PICKER_REQUEST = 1234
const val DAILY_SLOT = "DAILY"
const val WEEKLY_SLOT = "WEEKLY"
const val MONTHLY_SLOT = "MONTHLY"

class HomeRental : BaseFragment<FragmentHomeRentalBinding, HomeRentalViewModel>(), OnTimeSetListener, HomeRentalNavigator,
        EasyPermissions.PermissionCallbacks, DatePickerDialog.OnDateSetListener {

    lateinit var mAdapter: DaysAdapter

    var daysList: MutableList<RentalDayModel>? = mutableListOf()

    var startMonthName = ""
    var endMonthName = ""

    val startDate by lazy { Calendar.getInstance(DateTimeUtils.timeLocale) }
    val endDate by lazy { Calendar.getInstance(DateTimeUtils.timeLocale) }

    var mType = "pickUp"

    private var pickUptHour = 0
    private var pickUpMinute = 0
    private var dropOffHour = 0
    private var dropOffMinute = 0


    var startLatLng = LatLng(0.0, 0.0)
    var endLatLng = LatLng(0.0, 0.0)


    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var appUtils: AppUtils

    @Inject
    lateinit var permissionUtil: PermissionFile

    @Inject
    lateinit var dateTimeUtils: DateTimeUtils

    private lateinit var viewModel: HomeRentalViewModel

    private var mBinding: FragmentHomeRentalBinding? = null

    private lateinit var timePicker: TimePickerDialog

    // Set the fields to specify which types of place data to return.
    private val fields = listOf(Field.ID, Field.NAME, Field.LAT_LNG)


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null

    private val selectedDates = mutableSetOf<LocalDate>()
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")

    private var mSlotType = WEEKLY_SLOT

    var startSelectionDate: LocalDate? = null
    var endSelectionDate: LocalDate? = null

    private var selectedMonthlyDate = ""

    private val startBackground: GradientDrawable by lazy {
        requireContext().getDrawableCompat(R.drawable.continuous_selected_bg_start) as GradientDrawable
    }

    private val endBackground: GradientDrawable by lazy {
        requireContext().getDrawableCompat(R.drawable.continuous_selected_bg_end) as GradientDrawable
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding = viewDataBinding

        viewDataBinding.color = Configurations.colors

        settingDataLyt()

        calculateDayItem()

        intializePlace()

        intiateSelectionCalendar()

        tv_pickup.setOnClickListener {
            mType = "pickUp"
            openTimePicker(tv_pickup.text.toString())
        }

        tv_dropup.setOnClickListener {
            mType = "dropUp"
            openTimePicker(tv_dropup.text.toString())
        }

        materialCardView.setOnClickListener {
            openPlacePicker("pickUp")
        }

        drop_cardView.setOnClickListener {
            openPlacePicker("dropUp")
        }

        imageView12.setOnClickListener {
            activity?.finish()
        }

        val calendar = Calendar.getInstance()

        tv_start_date.setOnClickListener {
            val dialog = DatePickerDialog(
                    activity ?: requireContext(), this, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            )

            dialog.datePicker.minDate = Date().time

            val mCalendar = Calendar.getInstance()
            mCalendar.add(Calendar.YEAR, 1)
            dialog.datePicker.maxDate = mCalendar.time.time

            dialog.show()
        }

        textView14.text = "Car Rental"

        checkingLocationPermission()

        selectedDates.clear()

        month_radio_grp.setOnCheckedChangeListener { group, checkedId -> // checkedId is the RadioButton selected

            when (checkedId) {
                R.id.rb_daily -> {
                    mSlotType = DAILY_SLOT
                    selectedDates.clear()
                    choose_slot.text = getString(R.string.choose_date)
                    recyclerView.visibility = View.GONE
                    tv_start_date.visibility = View.GONE
                    group_month_year.visibility = View.VISIBLE
                    group_calendar.visibility = View.VISIBLE
                    intiateCustomCalendar()
                }

                R.id.rb_weekly -> {
                    startSelectionDate = null
                    endSelectionDate = null

                    mSlotType = WEEKLY_SLOT
                    choose_slot.text = getString(R.string.choose_slot)
                    legendLayout.visibility = View.VISIBLE
                    calendarView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    tv_start_date.visibility = View.GONE
                    group_month_year.visibility = View.VISIBLE
                    group_calendar.visibility = View.VISIBLE
                    intiateSelectionCalendar()
                }

                R.id.rb_monthly -> {
                    mSlotType = MONTHLY_SLOT
                    choose_slot.text = getString(R.string.choose_slot)
                    legendLayout.visibility = View.GONE
                    calendarView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    group_month_year.visibility = View.GONE
                    tv_start_date.visibility = View.VISIBLE
                    group_calendar.visibility = View.GONE
                }
            }
        }
    }

    private fun intiateSelectionCalendar() {

        legendLayout.visibility = View.VISIBLE
        calendarView.visibility = View.VISIBLE


        mBinding?.calendarView?.post {
            val radius = ((mBinding?.calendarView?.width ?: 0 / 7) / 2).toFloat()
            startBackground.setCornerRadius(topLeft = radius, bottomLeft = radius)
            endBackground.setCornerRadius(topRight = radius, bottomRight = radius)
        }

        val daysOfWeek = daysOfWeekFromLocale()
        mBinding?.legendLayout?.root?.children?.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = daysOfWeek[index].getDisplayName(TextStyle.NARROW, Locale.ENGLISH).toUpperCase(Locale.ENGLISH)
                setTextColorRes(R.color.grey)
            }
        }

        val currentMonth = YearMonth.now()
        mBinding?.calendarView?.setup(currentMonth, currentMonth.plusMonths(12), daysOfWeek.first())
        mBinding?.calendarView?.scrollToMonth(currentMonth)
        mBinding?.calendarView?.orientation = LinearLayout.HORIZONTAL
        mBinding?.calendarView?.scrollMode = ScrollMode.PAGED

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = CalendarDayLayoutBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH && (day.date == today || day.date.isAfter(today))) {
                        val date = day.date
                        if (startSelectionDate != null) {
                            if (date < startSelectionDate || endSelectionDate != null) {
                                startSelectionDate = date
                                endSelectionDate = null
                            } else if (date != startSelectionDate) {
                                endMonthName = "End Month"
                                endSelectionDate = date
                            }
                        } else {
                            startMonthName = "Start Month"
                            startSelectionDate = date
                        }
                        this@HomeRental.mBinding?.calendarView?.notifyCalendarChanged()
                    }
                }
            }
        }

        mBinding?.calendarView?.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.binding.calendarDayText
                val roundBgView = container.binding.exFourRoundBgView

                textView.text = null
                textView.background = null
                roundBgView.makeInVisible()

                val startDate = startSelectionDate
                val endDate = endSelectionDate

                when (day.owner) {
                    DayOwner.THIS_MONTH -> {
                        textView.text = day.day.toString()
                        if (day.date.isBefore(today)) {
                            textView.setTextColorRes(R.color.grey)
                        } else {
                            when {
                                startDate == day.date && endDate == null -> {
                                    textView.setTextColorRes(R.color.white)
                                    roundBgView.makeVisible()
                                    roundBgView.setBackgroundResource(R.drawable.shape_day_oval)
                                }
                                day.date == startDate -> {
                                    textView.setTextColorRes(R.color.white)
                                    textView.background = startBackground
                                }
                                startDate != null && endDate != null && (day.date > startDate && day.date < endDate) -> {
                                    textView.setTextColorRes(R.color.white)
                                    textView.setBackgroundResource(R.drawable.continuous_selected_bg_middle)
                                }
                                day.date == endDate -> {
                                    textView.setTextColorRes(R.color.white)
                                    textView.background = endBackground
                                }
                                day.date == today -> {
                                    textView.setTextColorRes(R.color.black)
                                    roundBgView.makeVisible()
                                    roundBgView.setBackgroundResource(R.drawable.today_bg)
                                }
                                else -> textView.setTextColorRes(R.color.black)
                            }
                        }
                    }
                    // Make the coloured selection background continuous on the invisible in and out dates across various months.
                    DayOwner.PREVIOUS_MONTH ->
                        if (startDate != null && endDate != null && isInDateBetween(day.date, startDate, endDate)) {
                            textView.setBackgroundResource(R.drawable.continuous_selected_bg_middle)
                        }
                    DayOwner.NEXT_MONTH ->
                        if (startDate != null && endDate != null && isOutDateBetween(day.date, startDate, endDate)) {
                            textView.setBackgroundResource(R.drawable.continuous_selected_bg_middle)
                        }
                }
            }
        }

        mBinding?.calendarView?.monthScrollListener = {
            if (mBinding?.calendarView?.maxRowCount == 6) {
                mBinding?.exOneYearText?.text = it.yearMonth.year.toString()
                mBinding?.exOneMonthText?.text = monthTitleFormatter.format(it.yearMonth)
            } else {
                // In week mode, we show the header a bit differently.
                // We show indices with dates from different months since
                // dates overflow and cells in one index can belong to different
                // months/years.
                val firstDate = it.weekDays.first().first().date
                val lastDate = it.weekDays.last().last().date
                if (firstDate.yearMonth == lastDate.yearMonth) {
                    mBinding?.exOneYearText?.text = firstDate.yearMonth.year.toString()
                    mBinding?.exOneMonthText?.text = monthTitleFormatter.format(firstDate)
                } else {
                    mBinding?.exOneMonthText?.text =
                            "${monthTitleFormatter.format(firstDate)} - ${monthTitleFormatter.format(lastDate)}"
                    if (firstDate.year == lastDate.year) {
                        mBinding?.exOneYearText?.text = firstDate.yearMonth.year.toString()
                    } else {
                        mBinding?.exOneYearText?.text = "${firstDate.yearMonth.year} - ${lastDate.yearMonth.year}"
                    }

                }
            }
        }


    }

    private fun intiateCustomCalendar() {

        val daysOfWeek = daysOfWeekFromLocale()
        mBinding?.legendLayout?.root?.children?.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = daysOfWeek[index].getDisplayName(TextStyle.NARROW, Locale.ENGLISH).toUpperCase(Locale.ENGLISH)
                setTextColorRes(R.color.grey)
            }
        }

        val currentMonth = YearMonth.now()
        mBinding?.calendarView?.setup(currentMonth, currentMonth.plusMonths(12), daysOfWeek.first())
        mBinding?.calendarView?.scrollToMonth(currentMonth)
        mBinding?.calendarView?.orientation = LinearLayout.HORIZONTAL
        mBinding?.calendarView?.scrollMode = ScrollMode.PAGED

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = CalendarDayLayoutBinding.bind(view).calendarDayText


            init {
                view.setOnClickListener {
                    if (day.date.isBefore(today)) return@setOnClickListener

                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDates.contains(day.date)) {
                            selectedDates.remove(day.date)
                        } else if (selectedDates.isEmpty() || evaluateDays(day.date, selectedDates)) {
                            selectedDates.add(day.date)
                        }

                        if (selectedDates.isEmpty()) {
                            startMonthName = ""
                            endMonthName = ""
                        } else {
                            startMonthName = "Start Month"
                            endMonthName = "End Month"
                        }

                        mBinding?.calendarView?.notifyDayChanged(day)
                    }
                }
            }

            private fun evaluateDays(currentDay: LocalDate, selectedDates: MutableSet<LocalDate>): Boolean {
                return selectedDates.last().plusDays(1) == currentDay
            }
        }

        mBinding?.calendarView?.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()


                if (day.owner == DayOwner.THIS_MONTH && day.date.isAfter(today)) {
                    when {
                        selectedDates.contains(day.date) -> {
                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.shape_day_oval)
                        }
                        today == day.date -> {
                            textView.setTextColorRes(R.color.black)
                            textView.setBackgroundResource(R.drawable.shape_day_current_oval)
                        }
                        else -> {
                            textView.setTextColorRes(R.color.black)
                            textView.background = null
                        }
                    }
                } else {
                    textView.setTextColorRes(R.color.grey)
                }
            }
        }

        mBinding?.calendarView?.monthScrollListener = {
            if (mBinding?.calendarView?.maxRowCount == 6) {
                mBinding?.exOneYearText?.text = it.yearMonth.year.toString()
                mBinding?.exOneMonthText?.text = monthTitleFormatter.format(it.yearMonth)
            } else {
                // In week mode, we show the header a bit differently.
                // We show indices with dates from different months since
                // dates overflow and cells in one index can belong to different
                // months/years.
                val firstDate = it.weekDays.first().first().date
                val lastDate = it.weekDays.last().last().date
                if (firstDate.yearMonth == lastDate.yearMonth) {
                    mBinding?.exOneYearText?.text = firstDate.yearMonth.year.toString()
                    mBinding?.exOneMonthText?.text = monthTitleFormatter.format(firstDate)
                } else {
                    mBinding?.exOneMonthText?.text =
                            "${monthTitleFormatter.format(firstDate)} - ${monthTitleFormatter.format(lastDate)}"
                    if (firstDate.year == lastDate.year) {
                        mBinding?.exOneYearText?.text = firstDate.yearMonth.year.toString()
                    } else {
                        mBinding?.exOneYearText?.text = "${firstDate.yearMonth.year} - ${lastDate.yearMonth.year}"
                    }

                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (!::fusedLocationClient.isInitialized) return

        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun openPlacePicker(type: String) {
        mType = type
        // Start the autocomplete intent.
        val intent = IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(activity ?: requireActivity())
        startActivityForResult(intent, PLACE_PICKER_REQUEST)
    }


    //*********************Location Request Or Permission Enable*************************
    private fun checkingLocationPermission() {
        if (permissionUtil.hasLocation(activity ?: requireActivity())) {
            createLocationRequest()
        } else {
            permissionUtil.locationTaskFrag(this@HomeRental)
        }
    }
    //***********************************************************************************

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest!!)

        val client = LocationServices.getSettingsClient(activity ?: requireActivity())
        val task = client.checkLocationSettings(builder.build())

        task.addOnCompleteListener { task1 ->
            try {
                val response = task1.getResult(ApiException::class.java)
                // All location settings are satisfied. The client can initialize location
                getCurrentLocation()
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            val resolvable = exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                    activity ?: requireActivity(),
                                    AppConstants.RC_LOCATION_PERM
                            )
                            Log.e("Data", "REQUEST_CHECK_SETTINGS")
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Log.e("Data", "SETTINGS_CHANGE_UNAVAILABLE")
                }
            }
        }
    }

    private fun getCurrentLocation() {

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(activity ?: requireActivity())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (mType == "pickUp") {
                        progressBar?.visibility = View.VISIBLE

                        startLatLng = LatLng(location?.latitude ?: 0.0, location?.longitude ?: 0.0)

                        appUtils.getAddress(location?.latitude ?: 0.0, location?.longitude
                                ?: 0.0)?.getAddressLine(0).let {
                            progressBar.visibility = View.GONE
                            tvPickup.text = it
                        }
                    }
                    locationCallback?.let {
                        fusedLocationClient.removeLocationUpdates(it)
                    }
                    break
                }
            }
        }


        startLocationUpdates()
    }

    private fun startLocationUpdates() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity?.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    activity?.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return
            }
        }


        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null /* Looper */
        )
    }

    private fun intializePlace() {
        if (!Places.isInitialized()) {
            Places.initialize(activity?.applicationContext
                    ?: requireActivity(), appUtils.getMapKey())
        }
    }


    private fun openTimePicker(time: String) {

        val calendar = appUtils.getCalendarFormat(time, "hh:mm aa")

        timePicker.updateTime(calendar?.get(Calendar.HOUR_OF_DAY)
                ?: 11, calendar?.get(Calendar.MINUTE) ?: 12)
        timePicker.show()
    }

    private fun calculateDayItem(dayofMonth: Int? = null, selectedDay: String? = null) {
        if (!::mAdapter.isInitialized) return
        daysList?.clear()

        val cal = Calendar.getInstance()
        pickUptHour = cal.get(Calendar.HOUR_OF_DAY)
        pickUpMinute = cal.get(Calendar.MINUTE)

        if (dayofMonth != null) {
            cal.set(Calendar.DAY_OF_MONTH, dayofMonth)
        }

        tv_pickup.text = appUtils.convertDateOneToAnother(cal.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "hh:mm aa")


        for (i in 0..12) {

            val rental = RentalDayModel()
            rental.dayId = i
            rental.dayName = if (dayofMonth != null) {
                appUtils.convertDateOneToAnother(cal.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "dd\nMMM\nyyyy")
                        ?: ""
            } else {
                appUtils.convertDateOneToAnother(cal.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "MMM\nyyyy")
                        ?: ""
            }

            rental.dayFormt = appUtils.getCalendarFormat(cal.time.toString(), "EEE MMM d HH:mm:ss Z yyyy")
                    ?: Calendar.getInstance()
            daysList?.add(rental)

            cal.add(Calendar.MONTH, 1)
        }

        cal.add(Calendar.HOUR_OF_DAY, 1)
        dropOffHour = cal.get(Calendar.HOUR_OF_DAY)
        dropOffMinute = cal.get(Calendar.MINUTE)
        tv_dropup.text = appUtils.convertDateOneToAnother(cal.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "hh:mm aa")

        if (selectedDay != null) {
            mAdapter.setStartDate(selectedDay)
        }

        mAdapter.submitItemList(daysList)
    }

    private fun settingDataLyt() {
        mAdapter = DaysAdapter()

        timePicker = TimePickerDialog(activity, this, 14, 0, false)
        var dayRefStat: Boolean


        mAdapter.settingCallback(ItemListener {

            if (endMonthName.isNotEmpty()) {
                endMonthName = ""
                startMonthName = ""

                dayRefStat = false

                daysList?.forEachIndexed { index, rentalDayModel ->
                    rentalDayModel.startDate = ""
                    rentalDayModel.dayStatus = false
                    rentalDayModel.endDate = ""
                }
            } else {
                dayRefStat = true
            }


            if (startMonthName.isNotEmpty()) {
                endDate.time = it.dayFormt.time
                if (startDate.before(endDate)) {
                    it.dayStatus = !it.dayStatus
                    endMonthName = it.dayName
                    it.endDate = getString(R.string.end_month)
                } else if (startDate == endDate) {
                    endMonthName = it.dayName
                    it.startDate = getString(R.string.start_end_month, "\n")
                }
            }

            if (dayRefStat && startMonthName.isEmpty()) {
                startDate.time = it.dayFormt.time
                it.dayStatus = !it.dayStatus
                startMonthName = it.dayName
                it.startDate = getString(R.string.start_month)
            } else {
                dayRefStat = true
            }


            if (startMonthName.isNotEmpty() && endMonthName.isNotEmpty()) {
                for (x in daysList?.indexOfFirst { it.startDate.isNotEmpty() }!! until daysList?.indexOfFirst { it.endDate.isNotEmpty() }!!) {
                    val dayModel = daysList?.get(x)
                    dayModel?.dayStatus = true
                    dayModel?.let { it1 -> daysList?.set(x, it1) }
                }
            }
            mAdapter.notifyDataSetChanged()

        })

        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = mAdapter
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            if (place.latLng != null) {
                val lat = place.latLng?.latitude
                val lng = place.latLng?.longitude




                if (mType == "pickUp") {

                    startLatLng = LatLng(lat ?: 0.0, lng ?: 0.0)

                    progressBar.visibility = View.VISIBLE
                    appUtils.getAddress(lat ?: 0.0, lng ?: 0.0)?.getAddressLine(0).let {
                        progressBar.visibility = View.GONE
                        tvPickup.text = it
                    }
                } else {
                    endLatLng = LatLng(lat ?: 0.0, lng ?: 0.0)
                    pbDropup.visibility = View.VISIBLE

                    appUtils.getAddress(lat ?: 0.0, lng ?: 0.0)?.getAddressLine(0).let {
                        pbDropup.visibility = View.GONE
                        tvDropup.text = it
                    }
                }
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(data!!)
            //  Log.i(TAG, status.getStatusMessage());
        } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) { // The user canceled the operation.

        } else if (requestCode == AppConstants.RC_LOCATION_PERM) {
            getCurrentLocation()
        }
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {


        if (mType == "pickUp") {
            pickUptHour = p1
            pickUpMinute = p2
            tv_pickup.text = appUtils.convertDateOneToAnother("$p1:$p2", "HH:mm", "hh:mm aa")
            dropOffHour = p1.plus(1)
            dropOffMinute = p2
            tv_dropup.text = appUtils.convertDateOneToAnother("$p1:$p2", "HH:mm", "hh:mm aa")
        } else {
            dropOffHour = p1
            dropOffMinute = p2
            tv_dropup.text = appUtils.convertDateOneToAnother("$p1:$p2", "HH:mm", "hh:mm aa")
        }
    }


    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home_rental
    }

    override fun getViewModel(): HomeRentalViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(HomeRentalViewModel::class.java)
        return viewModel
    }

    override fun onHomeRental() {
        if (isNetworkConnected && validateHomeData(startDate, endDate, startMonthName, endMonthName)) {

            val mHomeParam = HomeRentalParam(
                    startLatLng.latitude, startLatLng.longitude, endLatLng.latitude, endLatLng.longitude,
                    appUtils.convertDateOneToAnother(startDate.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "yyyy-MM-dd HH:mm:ss")
                            ?: "",
                    appUtils.convertDateOneToAnother(endDate.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "yyyy-MM-dd HH:mm:ss")
                            ?: "",
                    if (radioButton?.isChecked == true) 1 else 0, tvPickup.text.toString(),
                    appUtils.convertDateOneToAnother(startDate.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "MMM dd").plus(" - ")
                            .plus(appUtils.convertDateOneToAnother(endDate.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "MMM dd")
                                    ?: ""),
                    "", "", if (tvDropup.text.toString().equals(getString(R.string.choose_drop_location))) "" else tvDropup.text.toString(),
            )

            mHomeParam.mRentalType = when (mSlotType) {
                DAILY_SLOT -> RentalDataType.Daily
                MONTHLY_SLOT -> RentalDataType.Monthly
                WEEKLY_SLOT -> RentalDataType.Weekly
                else -> RentalDataType.Daily
            }

            val bundle = bundleOf("intputData" to mHomeParam)
            navController(this@HomeRental).navigate(R.id.action_homeRental_to_productListFrag, bundle)
        }
    }

    override fun onErrorOccur(message: String) {
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }


    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

        if (requestCode == REQUEST_CODE_LOCATION) {
            if (isNetworkConnected) {
                createLocationRequest()
            }
        }
    }

    private fun validateHomeData(startDate: Calendar?, endDate: Calendar?, startDayName: String, endDayName: String): Boolean {

        if (mSlotType == DAILY_SLOT && selectedDates.isNotEmpty()) {
            startDate?.time = dateTimeUtils.getCalendarFormat(selectedDates.first().toString(), "yyyy-MM-dd")?.time
                    ?: Date()
            endDate?.time = dateTimeUtils.getCalendarFormat(selectedDates.last().toString(), "yyyy-MM-dd")?.time
                    ?: Date()
        }

        if (mSlotType == WEEKLY_SLOT && startSelectionDate != null && endSelectionDate != null) {
            startDate?.time = dateTimeUtils.getCalendarFormat(startSelectionDate.toString(), "yyyy-MM-dd")?.time
                    ?: Date()
            endDate?.time = dateTimeUtils.getCalendarFormat(endSelectionDate.toString(), "yyyy-MM-dd")?.time
                    ?: Date()
        }


        if (startDayName.isNotEmpty()) {
            startDate?.set(Calendar.HOUR_OF_DAY, pickUptHour)
            startDate?.set(Calendar.MINUTE, pickUpMinute)
        }

        if (endDayName.isNotEmpty()) {
            endDate?.set(Calendar.HOUR_OF_DAY, dropOffHour)
            endDate?.set(Calendar.MINUTE, dropOffMinute)
        }

        return when {
            /*  mSlotType == DAILY_SLOT && selectedDates.isEmpty() -> {
                  mBinding?.root?.onSnackbar(getString(R.string.select_daily_slot))
                  false
              }
              mSlotType== DAILY_SLOT && selectedDates.size==1->{
                  mBinding?.root?.onSnackbar(getString(R.string.pls_add_multiple_date))
                  false
              }*/
            mSlotType == WEEKLY_SLOT && (startSelectionDate == null || endSelectionDate == null) -> {
                mBinding?.root?.onSnackbar(getString(R.string.select_weekly_slot))
                false
            }
            mSlotType == WEEKLY_SLOT && (appUtils.getTotalMinutes(appUtils.convertDateOneToAnother(startDate?.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "yyyy-MM-dd HH:mm:ss"),
                    appUtils.convertDateOneToAnother(endDate?.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "yyyy-MM-dd HH:mm:ss"),
                    RentalDataType.Weekly) >= 30) -> {
                mBinding?.root?.onSnackbar(getString(R.string.pls_select_less_than_30))
                false
            }
            mSlotType == MONTHLY_SLOT && (startMonthName.isEmpty() || endMonthName.isEmpty()) -> {
                mBinding?.root?.onSnackbar(getString(R.string.select_monthly_slot))
                false
            }
            mSlotType == MONTHLY_SLOT && selectedMonthlyDate.isEmpty() -> {
                mBinding?.root?.onSnackbar(getString(R.string.choose_start_date))
                false
            }
            startDayName.isEmpty() -> {
                mBinding?.root?.onSnackbar(getString(R.string.select_start_date))
                false
            }
            endDayName.isEmpty() -> {
                mBinding?.root?.onSnackbar(getString(R.string.select_end_date))
                false
            }
            startDate?.before(endDate) == false -> {
                mBinding?.root?.onSnackbar(getString(R.string.select_valid_date))
                false
            }
            else -> {
                true
            }
        }
    }

    private fun isInDateBetween(inDate: LocalDate, startDate: LocalDate, endDate: LocalDate): Boolean {
        if (startDate.yearMonth == endDate.yearMonth) return false
        if (inDate.yearMonth == startDate.yearMonth) return true
        val firstDateInThisMonth = inDate.plusMonths(1).yearMonth.atDay(1)
        return firstDateInThisMonth >= startDate && firstDateInThisMonth <= endDate && startDate != firstDateInThisMonth
    }

    private fun isOutDateBetween(outDate: LocalDate, startDate: LocalDate, endDate: LocalDate): Boolean {
        if (startDate.yearMonth == endDate.yearMonth) return false
        if (outDate.yearMonth == endDate.yearMonth) return true
        val lastDateInThisMonth = outDate.minusMonths(1).yearMonth.atEndOfMonth()
        return lastDateInThisMonth >= startDate && lastDateInThisMonth <= endDate && endDate != lastDateInThisMonth
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val startDate = "$year-${month + 1}-$dayOfMonth"

        selectedMonthlyDate = dateTimeUtils.convertDateOneToAnother(
                startDate ?: "",
                "yyyy-MM-dd", "MMM dd, yyyy"
        ) ?: ""

        tv_start_date.text = getString(R.string.change_start_date, selectedMonthlyDate)

        calculateDayItem(dayOfMonth, dateTimeUtils.convertDateOneToAnother(
                startDate ?: "",
                "yyyy-MM-dd", "dd\nMMM\nyyyy"
        ))

    }


}
