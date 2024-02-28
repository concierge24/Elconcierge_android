package com.trava.user.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.transition.Explode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.trava.user.R
import com.trava.user.databinding.FragmentScheduleBinding
import com.trava.user.ui.home.comfirmbooking.ConfirmBookingFragment
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.utilities.*
import kotlinx.android.synthetic.main.fragment_schedule.*
import java.util.*

class ScheduleFragment : Fragment(), View.OnClickListener {

    private var serviceRequest: ServiceRequestModel? = null
    private var selectedDate = Calendar.getInstance(Locale.getDefault())
    lateinit var scheduleBinding: FragmentScheduleBinding

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
        scheduleBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule, container, false)
        scheduleBinding.color = ConfigPOJO.Companion
        val view=scheduleBinding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        serviceRequest = (activity as HomeActivity).serviceRequestModel
        selectedDate = Calendar.getInstance(Locale.getDefault())
        selectedDate.add(Calendar.HOUR_OF_DAY, 1)

        if (ConfigPOJO.TEMPLATE_CODE == Constants.SUMMER_APP_BASE) {
            cvToolbar.visibility = View.VISIBLE
            ivBack.visibility = View.GONE
        } else {
            cvToolbar.visibility = View.GONE
            ivBack.visibility = View.VISIBLE
        }
        tvNext.background = StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour, GradientDrawable.RECTANGLE)
        tvNext.setTextColor(Color.parseColor(ConfigPOJO.Btn_Text_Colour))
        setDateTimeViews()
        setListeners()
    }

    private fun setListeners() {
        ivBack.setOnClickListener(this)
        cvToolbar.setOnClickListener(this)
        tvDate.setOnClickListener(this)
        tvTime.setOnClickListener(this)
        tvNext.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack,R.id.cvToolbar -> {
                serviceRequest?.future = ServiceRequestModel.BOOK_NOW
                activity?.onBackPressed()
            }

            R.id.tvDate -> {
                showDatePicker()
            }
            R.id.tvTime -> {
                showTimePicker()
            }
            R.id.tvNext -> {
                val tempCal = Calendar.getInstance()
                tempCal.add(Calendar.HOUR_OF_DAY, 1)
                /*if (selectedDate.before(tempCal)) {
                    Toast.makeText(activity, getString(R.string.schedule_time_selection_validation_msg), Toast.LENGTH_LONG).show()
                } else {
                    serviceRequest?.order_timings = getFormatFromDateUtc(selectedDate.time, "yyyy-MM-dd HH:mm:ss")
                    serviceRequest?.order_timings_text = getFormatFromDate(selectedDate.time, "EEE, MMM d h:mm a")
                    fragmentManager?.beginTransaction()?.replace(R.id.container, ConfirmBookingFragment())?.addToBackStack("backstack")?.commit()
                }*/
                var loc = LocaleManager.getLanguage(activity!!)
                val locale = Locale(loc)

                serviceRequest?.order_timings = getFormatFromDateUtc(selectedDate.time, "yyyy-MM-dd HH:mm:ss")
                serviceRequest?.order_timings_text = getFormatFromDate(selectedDate.time, "EEE, MMM d h:mm a",locale)
                fragmentManager?.beginTransaction()?.replace(R.id.container, ConfirmBookingFragment())?.addToBackStack("backstack")?.commit()

            }
        }
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(activity!!, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            selectedDate.set(Calendar.YEAR, year)
            selectedDate.set(Calendar.MONTH, month)
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val tempCalendar = Calendar.getInstance()
            tempCalendar.add(Calendar.HOUR_OF_DAY, 1)
            if (selectedDate.time.before(tempCalendar.time)) {
                selectedDate = tempCalendar.clone() as Calendar
            }
            setDateTimeViews()
        }, selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH))

        datePicker.setCanceledOnTouchOutside(true)
        val tempCal = Calendar.getInstance()
        datePicker.datePicker.minDate = tempCal.timeInMillis
        datePicker.show()
    }

    private fun showTimePicker() {
        val timePicker = TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            val selectedTimeCalendar = selectedDate.clone() as Calendar
            selectedTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedTimeCalendar.set(Calendar.MINUTE, minute)
            selectedTimeCalendar.set(Calendar.SECOND, 0)
            val tempCalendar = Calendar.getInstance()
            tempCalendar.add(Calendar.HOUR_OF_DAY, 1)
            tempCalendar.add(Calendar.MINUTE, 1)
            tempCalendar.set(Calendar.SECOND, 0)
            /*if (selectedTimeCalendar.time.before(tempCalendar.time)) {
                Toast.makeText(activity, getString(R.string.schedule_time_selection_validation_msg), Toast.LENGTH_LONG).show()
            } else {
                selectedDate = selectedTimeCalendar.clone() as Calendar
                setDateTimeViews()
            }*/
            selectedDate = selectedTimeCalendar.clone() as Calendar
            setDateTimeViews()
        }, selectedDate.get(Calendar.HOUR_OF_DAY),
                selectedDate.get(Calendar.MINUTE),
                false)
        timePicker.setCanceledOnTouchOutside(true)
        timePicker.show()
    }

    private fun setDateTimeViews() {
        var loc = LocaleManager.getLanguage(activity!!)
        val locale = Locale(loc)
        tvDate.text = getFormatFromDate(selectedDate.time, "EEE, dd MMM",locale)
        tvTime.text = getFormatFromDate(selectedDate.time, "h:mm a",locale)
    }
}
