package com.trava.utilities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.util.TimeUnit
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private var dpd: DatePickerDialog? = null

    fun openDateDialog(context: Context, onDateSelectedListener: OnDateSelectedListener, isTimeRequired: Boolean, dateAlreadySelected:Calendar) {
        if (dpd?.isShowing == true) {
            dpd?.dismiss()
        }
        val c = dateAlreadySelected
        val yearCal = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        dpd = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            val selectedCal = Calendar.getInstance()
            selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedCal.set(Calendar.MONTH, monthOfYear)
            selectedCal.set(Calendar.YEAR, year)
            if (isTimeRequired)
                openTimePicker(context, selectedCal, onDateSelectedListener , dateAlreadySelected)
            else
                onDateSelectedListener.dateTimeSelected(selectedCal)

        }, yearCal, month, day)
        dpd?.datePicker?.minDate = System.currentTimeMillis() - 1000
        dpd?.show()
    }

    interface OnDateSelectedListener {
        fun dateTimeSelected(dateCal: Calendar)
        fun timeSelected(dateCal: Calendar)
    }


    fun openTimePicker(context: Context, cal: Calendar, onDateSelectedListener: OnDateSelectedListener, dateAlreadySelected: Calendar) {
        val mCurrentTime = dateAlreadySelected
        val hour = mCurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mCurrentTime.get(Calendar.MINUTE)
        val mTimePicker: TimePickerDialog
        mTimePicker = TimePickerDialog(context,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    val mCurrentTimeSelected = Calendar.getInstance()
                    mCurrentTimeSelected.set(Calendar.HOUR_OF_DAY, selectedHour)
                    mCurrentTimeSelected.set(Calendar.MINUTE, selectedMinute)
                    mCurrentTimeSelected.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
                    mCurrentTimeSelected.set(Calendar.MONTH, cal.get(Calendar.MONTH))
                    mCurrentTimeSelected.set(Calendar.YEAR, cal.get(Calendar.YEAR))
                    onDateSelectedListener.timeSelected(mCurrentTimeSelected)
                }, hour, minute, false)
        mTimePicker.setTitle(context.getString(R.string.dialog_titlte_select_time))
        mTimePicker.show()
    }

    fun getFormattedDateForUTC(dateCal: Calendar): String {
        var formattedDate = ""
        try {
            val dateFormat1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            dateFormat1.timeZone = TimeZone.getTimeZone("UTC")
            formattedDate = dateFormat1.format(dateCal.time)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return formattedDate
    }


    fun getFormattedDateForUTC(dateCal: String): String {
        var formattedDate = ""
        try {
            val dateFormat1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val dateFormat2 = SimpleDateFormat("dd-MMM-yyy, HH.mm aa", Locale.ENGLISH)
            dateFormat1.timeZone = TimeZone.getTimeZone("UTC")
            val date =dateFormat1.parse(dateCal)
            formattedDate = dateFormat2.format(date)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return formattedDate
    }

    fun getFormattedDateForUTCLocale(dateCal: String): String {
        var formattedDate = ""
        try {
            val dateFormat1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val dateFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat1.timeZone = TimeZone.getTimeZone("UTC")
            val date =dateFormat1.parse(dateCal)
            formattedDate = dateFormat2.format(date)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return formattedDate
    }


    fun getFormattedTime(dateCal: Calendar): String {
        var cal =""
        try {
            val dateFormat1 = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            cal = dateFormat1.format(dateCal.time)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cal
    }

    fun getFormattedTime(dateCal: String): String {
        var calString=""
        try {
            val dateFormat1 = SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.US)
            val dateFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = dateFormat1.parse(dateCal)
            calString = dateFormat2.format(cal)


        } catch (e: Exception) {
            e.printStackTrace()
        }
        return calString
    }

    fun getFormattedTimeSymbol(dateCal: String): String {
        var calString=""
        try {
            val dateFormat1 = SimpleDateFormat("kk:mm:ss", Locale.US)
            val dateFormat2 = SimpleDateFormat("aaa", Locale.US)
            val cal = dateFormat1.parse(dateCal)
            calString = dateFormat2.format(cal)


        } catch (e: Exception) {
            e.printStackTrace()
        }
        return calString
    }

    fun getFormattedTimeForTrip(dateCal: Calendar): String {
        var cal =""
        try {
            val dateFormat1 = SimpleDateFormat("hh:mm aaa", Locale.US)
            cal = dateFormat1.format(dateCal.time)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cal
    }


    fun getFormattedDateForPlanTrip(dateCal: Calendar): String {
        var formattedDate = ""
        try {
            val dateFormat1 = SimpleDateFormat("EEE, MMM dd", Locale.US)
            formattedDate = dateFormat1.format(dateCal.time)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return formattedDate
    }

    fun getFormattedTimeForBooking(dateCal: String): String {
        var cal =""
        try {
            val dateFormat1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            dateFormat1.timeZone = TimeZone.getTimeZone("UTC")
            val dateFormat2 = SimpleDateFormat("MMM dd · hh:mm aaa", Locale.US)
            dateFormat2.timeZone = TimeZone.getDefault()
            val date = dateFormat1.parse(dateCal)
            cal = dateFormat2.format(date)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cal
    }

    fun getFormattedTimeForBooking2(dateCal: String): String {
        var cal =""
        try {
            val dateFormat1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            dateFormat1.timeZone = TimeZone.getTimeZone("UTC")
            val dateFormat2 = SimpleDateFormat("MMM dd · hh:mm aaa", Locale.ENGLISH)
            dateFormat2.timeZone = TimeZone.getDefault()
            val date = dateFormat1.parse(dateCal)
            cal = dateFormat2.format(date)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cal
    }

    fun matchTimeWithCurrent(dateCal: String): Boolean {
        var cal = false
        try {
            val dateFormat1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            dateFormat1.timeZone = TimeZone.getTimeZone("UTC")
            val date = dateFormat1.parse(dateCal)
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            if((date.time - calendar.timeInMillis) <= (30*60*1000)){
                cal= true
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cal
    }

    fun matchTimeExecuted(dateCal: String): Int {
        var cal = 0
        try {
            val dateFormat1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            dateFormat1.timeZone = TimeZone.getTimeZone("UTC")
            val date = dateFormat1.parse(dateCal)
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val secondsExecuted = (calendar.timeInMillis - date.time)/1000
            cal = secondsExecuted.toInt()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cal
    }

    fun getFormattedTimeForHistory(dateCal: String): String {
        var cal =""
        try {
            val dateFormat1 = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val dateFormat2 = SimpleDateFormat("MMM", Locale.US)
            val date = dateFormat1.parse(dateCal)
            cal = dateFormat2.format(date)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cal
    }

    fun getFormattedTimeForPromoCodes(dateCal: String): String {
        var cal =""
        try {
            val dateFormat1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            dateFormat1.timeZone = TimeZone.getTimeZone("UTC")
            val dateFormat2 = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
            dateFormat2.timeZone = TimeZone.getDefault()
            val date = dateFormat1.parse(dateCal)
            cal = dateFormat2.format(date)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cal
    }

    fun timeUnitToFullTime(time:Long, timeUnit: java.util.concurrent.TimeUnit):String {
        val day = timeUnit.toDays(time)
        val hour = timeUnit.toHours(time) % 24
        val minute = timeUnit.toMinutes(time) % 60
        val second = timeUnit.toSeconds(time) % 60
        if (day > 0)
        {
            return String.format("%dd %02dh", day, hour)
        }
        else if (hour > 0)
        {
            return String.format("%dh %02dm", hour, minute)
        }
        else if (minute > 0)
        {
            return String.format("%dmin", minute)
        }
        else
        {
            return String.format("%s","1 min")
        }
    }


}