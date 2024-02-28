package com.trava.user.utils

import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import com.trava.user.R
import com.trava.user.webservices.models.Company
import com.trava.utilities.AppSocket
import com.trava.utilities.PaymentType
import com.trava.utilities.SharedPrefs
import com.trava.utilities.constants.IS_CATEGORY_SELECTED
import org.aviran.cookiebar2.CookieBar
import org.greenrobot.eventbus.EventBus
import java.lang.Exception
import java.lang.StringBuilder
import java.text.DateFormatSymbols
import java.util.*

class AppUtils {
    companion object {
        fun logout(activity: Activity?) {
            val isCategorySelected = SharedPrefs.get().getString(IS_CATEGORY_SELECTED, "")
            AppSocket.get().off()
            AppSocket.get().disconnect()
            AppSocket.get().socket=null
            Toast.makeText(activity, activity?.getString(R.string.session_expired_please_login_again), Toast.LENGTH_LONG).show()
            SharedPrefs.with(activity).removeAllSharedPrefsChangeListeners()
            SharedPrefs.with(activity).removeAll()
            SharedPrefs.get().save(IS_CATEGORY_SELECTED, isCategorySelected)

            EventBus.getDefault().post("success")
            activity?.finish()
        }

        fun setTextViewDrawableColor(textView: TextView, color: Int) {
            for (drawable in textView.getCompoundDrawables()) {
                if (drawable != null) {
                    drawable.setColorFilter(PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN))
                }
            }
        }

        fun getCategoryIcon(categoryId: Int?) {

        }

        fun getPaymentStringId(paymentType: String): Int {
            return when (paymentType) {
                PaymentType.CARD -> R.string.card
                PaymentType.CASH -> R.string.cash
                PaymentType.E_TOKEN -> R.string.e_token
                else -> R.string.card
            }
        }

        fun getServiceDaysString(company: Company?): String {
            val shortDays = DateFormatSymbols.getInstance(Locale.getDefault()).shortWeekdays
            val servingDays = StringBuilder("")
            if (company?.sunday_service == "1") servingDays.append(shortDays[1] + ", ")
            if (company?.monday_service == "1") servingDays.append(shortDays[2] + ", ")
            if (company?.tuesday_service == "1") servingDays.append(shortDays[3] + ", ")
            if (company?.wednesday_service == "1") servingDays.append(shortDays[4] + ", ")
            if (company?.thursday_service == "1") servingDays.append(shortDays[5] + ", ")
            if (company?.friday_service == "1") servingDays.append(shortDays[6] + ", ")
            if (company?.saturday_service == "1") servingDays.append(shortDays[7] + ", ")

            return servingDays.removeSuffix(", ").toString()
        }

        fun getFormattedDecimal(num: Double): String? {
            return String.format(Locale.US, "%.2f", num)
        }

        fun getLabelText(text: String): String {
            var label = ""
            for (i in ConfigPOJO.appLablesList.indices) {
                if (ConfigPOJO.appLablesList[i].key_name == text) {
                    label = ConfigPOJO.appLablesList[i].terminology
                }
            }
            return label
        }

        fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val theta = lon1 - lon2
            var dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + (Math.cos(deg2rad(lat1))
                    * Math.cos(deg2rad(lat2))
                    * Math.cos(deg2rad(theta)))
            dist = Math.acos(dist)
            dist = rad2deg(dist)
            dist = dist * 60.0 * 1.1515
            return dist
        }

        private fun deg2rad(deg: Double): Double {
            return deg * Math.PI / 180.0
        }

        private fun rad2deg(rad: Double): Double {
            return rad * 180.0 / Math.PI
        }

        fun displayAlert(mContext: Context, msg: String?, isSuccess: Boolean) {
            try {
                val title = if (isSuccess) mContext.getString(R.string.success) else mContext.getString(R.string.alert)
                CookieBar.build(mContext as Activity)
                        .setTitle(title)         // String resources are also supported
                        .setTitleColor(R.color.white)
                        .setDuration(2000)
                        .setBackgroundColor(R.color.colorPrimaryDark)
                        .setEnableAutoDismiss(false)
                        .setSwipeToDismiss(false)
                        .setMessage(msg)
                        .show()
            } catch (ex: Exception) {
                Log.e("Excep : ", ex.message ?: "")
            }
        }

        fun displayAlertWithAutoDismiss(mContext: Context, msg: String?, isSuccess: Boolean) {
            try {
                val title = if (isSuccess) mContext.getString(R.string.success) else mContext.getString(R.string.alert)
                CookieBar.build(mContext as Activity)
                        .setTitle(title)         // String resources are also supported
                        .setTitleColor(R.color.white)
                        .setDuration(2000)
                        .setBackgroundColor(R.color.colorPrimaryDark)
                        .setEnableAutoDismiss(true)
                        .setSwipeToDismiss(false)
                        .setMessage(msg)
                        .show()
            } catch (ex: Exception) {
                Log.e("Excep : ", ex.message ?: "")
            }
        }

        fun getCountryCodeFromPhoneNumber(number: String, countryCode: String): String {
            var num = ""
            var isoCode = ""
            val phoneUtil = PhoneNumberUtil.getInstance()
            /*if (countryCode.equals("0")) isoCode = "LK" else isoCode =
                    phoneUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode))*/
            var swissNumberProto: Phonenumber.PhoneNumber? = null
            try {
                swissNumberProto = phoneUtil.parse(number, countryCode)
                num = phoneUtil.format(swissNumberProto!!, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
            } catch (e: Exception) {
                System.err.println("NumberParseException was thrown: " + e.message)
            }
            Log.e("Extension : ", "" + num)
            return num
        }

        fun setImageUrl(url: String?, view: ImageView) {
            view.context.applicationContext?.let { Glide.with(it).load(url).into(view) }
        }
    }
}