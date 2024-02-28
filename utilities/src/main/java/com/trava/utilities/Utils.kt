package com.trava.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.text.*
import android.text.style.MetricAffectingSpan
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.trava.utilities.webservices.models.ApiErrorModel
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


object Utils {
    fun pxToDp(context: Context, px: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun dpToPx(dp: Int): Float {
        return (dp * Resources.getSystem().displayMetrics.density)
    }

    fun getEditTextFilter(): InputFilter {
        return object : InputFilter {
            override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
                var keepOriginal = true
                val sb = StringBuilder(end - start)
                for (i in start until end) {
                    val c = source.get(i)
                    if (isCharAllowed(c))
                    // put your condition here
                        sb.append(c)
                    else
                        keepOriginal = false
                }
                if (keepOriginal)
                    return null
                else {
                    if (source is Spanned) {
                        val sp = SpannableString(sb)
                        TextUtils.copySpansFrom(source as Spanned, start, sb.length, null, sp, 0)
                        return sp
                    } else {
                        return sb
                    }
                }
            }

            private fun isCharAllowed(c: Char): Boolean {
                val ps = Pattern.compile("^[a-zA-Z ]+$")
                val ms = ps.matcher((c).toString())
                return ms.matches()
            }
        }
    }

    fun acceptOnlyAlphabet(edittext: EditText) {
        edittext.setFilters(arrayOf<InputFilter>(object : InputFilter {
            override fun filter(src: CharSequence, start: Int,
                                end: Int, dst: Spanned, dstart: Int, dend: Int): CharSequence {
                if (src == "") { // for backspace
                    return src
                }
                if (src.toString().matches(("[a-zA-Z ]+").toRegex())) {
                    return src
                }
                return edittext.getText().toString()
            }
        }))
    }

    fun convertDateTimeInMillis(givenDateString: String): Long {
        var timeInMilliseconds: Long = 0
        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            var mDate = sdf.parse(givenDateString)
            timeInMilliseconds = mDate.getTime()
            System.out.println("Date in milli :: " + timeInMilliseconds)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return timeInMilliseconds
    }

    fun isYesterday(calendar: Calendar): Boolean {
        val tempCal = Calendar.getInstance()
        tempCal.add(Calendar.DAY_OF_MONTH, -1)
        return calendar.get(Calendar.DAY_OF_MONTH) == tempCal.get(Calendar.DAY_OF_MONTH)
    }

    fun existsInWeek(calendar: Calendar): Boolean {
        val tempCal = Calendar.getInstance()
        tempCal.add(Calendar.DAY_OF_MONTH, -7)
        tempCal.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
        tempCal.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        tempCal.set(Calendar.SECOND, calendar.get(Calendar.SECOND))
        return calendar.time.after(tempCal.time)
    }

    fun replaceFragment(fragmentManager: FragmentManager, fragment: Fragment, @IdRes containerId: Int, tag: String) {
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right).replace(containerId, fragment, tag)
                .addToBackStack("backstack").commit()
    }

    fun replaceFragmentNoBackStack(fragmentManager: FragmentManager, fragment: Fragment, @IdRes containerId: Int, tag: String) {
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_top,
                R.anim.slide_in_bottom, R.anim.slide_out_bottom, R.anim.slide_out_top).replace(containerId, fragment, tag)
                .addToBackStack("backstack").commit()
    }

    fun addFragment(fragmentManager: FragmentManager, fragment: Fragment, @IdRes containerId: Int, tag: String) {
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right).add(containerId, fragment, tag)
                .addToBackStack("backstack").commit()
    }

    fun getScreenWidth(activity: Activity): Int {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun getScreenHeight(activity: Activity): Int {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun View.showSnack(msg: String) {

        try {
            val snackBar = Snackbar.make(this, msg, Snackbar.LENGTH_LONG)
            val snackBarView = snackBar.view
            val textView = snackBarView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
            textView.maxLines = 3
            snackBar.setAction(R.string.okay) { snackBar.dismiss() }
            snackBarView.setBackgroundColor(Color.parseColor("#27242b"))
            snackBar.setActionTextColor(Color.parseColor("#0078FF"))
            snackBar.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getLanguageId(language: String): Int {
        return when (language) {
            LocaleManager.LANGUAGE_ENGLISH -> LanguageIds.ENGLISH
            LocaleManager.LANGUAGE_HINDI -> LanguageIds.HINDI
            LocaleManager.LANGUAGE_URDU -> LanguageIds.URDU
            LocaleManager.LANGUAGE_CHINESE -> LanguageIds.CHINESE
            LocaleManager.LANGUAGE_ARABIC -> LanguageIds.ARABIC
            LocaleManager.LANGUAGE_SPANISH -> LanguageIds.SINHALA
            LocaleManager.LANGAGE_TAMIL -> LanguageIds.TAMIL
            else -> LanguageIds.ENGLISH
        }
    }

}

fun ImageView.setRoundImageUrl(url: String?) {
    val requestOptions = RequestOptions()
            .fitCenter()
            .placeholder(R.drawable.ic_weight_inf)
            .error(R.drawable.ic_weight_inf)
            .transform(CircleCrop())
    this.context?.applicationContext?.let { Glide.with(it).load(url).apply(requestOptions).into(this) }
}

fun ImageView.setRoundProfileUrl(url: String?) {
    val requestOptions = RequestOptions()
            .fitCenter()
            .transform(CircleCrop())
            .placeholder(R.drawable.profile_pic_placeholder)
    this.context?.applicationContext?.let { Glide.with(it).load(url).apply(requestOptions).into(this) }
}

fun ImageView.setRoundProfilePic(file: File?) {
    val requestOptions = RequestOptions()
            .fitCenter()
            .transform(CircleCrop())
            .placeholder(R.drawable.profile_pic_placeholder)
    this.context?.applicationContext?.let { Glide.with(it).load(file).apply(requestOptions).into(this) }
}

fun ImageView.setImageUrl(url: String?) {
    val requestOptions = RequestOptions()
            .placeholder(R.color.text_subhead)
    this.context?.applicationContext?.let {
        Glide.with(it).load(url)
                .apply(requestOptions)
                .into(this)
    }
}

fun getApiError(error: String?): ApiErrorModel {
    return Gson().fromJson(error, ApiErrorModel::class.java)
}

fun View.showSnack(msg: String) {

    try {
        val snackBar = Snackbar.make(this, msg, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        val textView = snackBarView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.maxLines = 3
        snackBar.setAction(R.string.okay) { snackBar.dismiss() }
        snackBarView.setBackgroundColor(Color.parseColor("#27242b"))
        snackBar.setActionTextColor(Color.parseColor("#0078FF"))
        snackBar.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.hideKeyboard() {
    val imm = this.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.showSnack(@StringRes id: Int) {
    try {
        val snackBar = Snackbar.make(this, id, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        val textView = snackBarView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.maxLines = 3
        snackBar.setAction(R.string.okay) { snackBar.dismiss() }
        snackBarView.setBackgroundColor(Color.parseColor("#27242b"))
        snackBar.setActionTextColor(Color.parseColor("#0078FF"))
        snackBar.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.showSWWerror() {
    try {
        val snackBar = Snackbar.make(this, R.string.sww_error, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        val textView = snackBarView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.maxLines = 3
        snackBar.setAction(R.string.okay) { snackBar.dismiss() }
        snackBarView.setBackgroundColor(Color.parseColor("#27242b"))
        snackBar.setActionTextColor(Color.parseColor("#0078FF"))
        snackBar.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.showSWWerrorWithError(@StringRes id: Int) {
    try {
        val snackBar = Snackbar.make(this, id, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        val textView = snackBarView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.maxLines = 3
        snackBar.setAction(R.string.okay) { snackBar.dismiss() }
        snackBarView.setBackgroundColor(Color.parseColor("#27242b"))
        snackBar.setActionTextColor(Color.parseColor("#ffffff"))
        snackBar.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getFormatFromDate(date: Date, format: String, locale: Locale): String? {
    val f = SimpleDateFormat(format, locale)
    return f.format(date)
}

fun getFormatFromDateUtc(date: Date, format: String): String? {
    val f = SimpleDateFormat(format, Locale.US)
    f.timeZone = TimeZone.getTimeZone("UTC")
    return f.format(date)
}


fun checkingTimeSlotAfter(start: String): Boolean {
    val pattern = "HH:mm:ss"
    val sdf = SimpleDateFormat(pattern)
    try {
        val date1 = sdf.parse(getCurentDateStringUtcNew())
        val date2 = sdf.parse(start)
        return date1.after(date2)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return false
}

fun checkingTimeSlotBefore(endtime: String): Boolean {
    val pattern = "HH:mm:ss"
    val sdf = SimpleDateFormat(pattern)
    try {
        val date1 = sdf.parse(getCurentDateStringUtcNew())
        val date2 = sdf.parse(endtime)
        return date1.before(date2)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return false
}

fun getCurentDateStringUtcNew(): String? {
    val f = SimpleDateFormat("HH:mm:ss", Locale.US)
    f.timeZone = TimeZone.getTimeZone("UTC")
    return f.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).time)
}

fun getCurentDateStringUtc(): String? {
    val f = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    f.timeZone = TimeZone.getTimeZone("UTC")
    return f.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).time)
}

fun showForcedUpdateDialog(context: Context, versionForced: String, versionNormal: String, appVersion: String): Boolean {
    val builder = AlertDialog.Builder(context)

    return when {
        versionForced.toFloat() > appVersion.toFloat() -> {
            val dialog = builder.setPositiveButton(context.getString(R.string.update)) { _, _ ->

            }
                    .setCancelable(false)
                    .setTitle(context.getString(R.string.update))
                    .setMessage(context.getString(R.string.you_have_to_update_app))
                    .show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                openAppUpdate(context)
                //            val wantToCloseDialog = false
//            //Do stuff, possibly set wantToCloseDialog to true then...
//            if (wantToCloseDialog)
//                dialog.dismiss()
//            //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
            }
            true
        }
        versionNormal > appVersion -> {
            builder.setPositiveButton("Update") { _, _ ->
                openAppUpdate(context)
            }
                    .setNegativeButton(android.R.string.cancel, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            // Do nothing
                        }
                    })
                    .setCancelable(true)
                    .setTitle("Update Available")
                    .setMessage("You can update your app to the latest version to access the new features.")
                    .show()
            false
        }
        else -> false
    }
}

fun showForcedUpdateDialogDriver(context: Context, versionForced: String, versionNormal: String, appVersion: String): Dialog? {
    val builder = AlertDialog.Builder(context)
    return when {
        versionForced.toFloat() > appVersion.toFloat() -> builder.setPositiveButton("Update", { _, _ ->
            openAppUpdate(context)
        })
                .setCancelable(false)
                .setTitle("Update alert")
                .setMessage("You must have to update to latest version of the app .")
                .show()
        versionNormal > appVersion -> builder.setPositiveButton("Update", { _, _ ->
            openAppUpdate(context)
        })
                .setNegativeButton(android.R.string.cancel, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                    }
                })
                .setCancelable(true)
                .setTitle("Update Available")
                .setMessage("You can update your app to the latest version to access the new features.")
                .show()
        else -> null
    }
}


fun openAppUpdate(context: Context) {
    val appPackageName = context.packageName // getPackageName() from Context or Activity object
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)))
    } catch (exc: android.content.ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)))
    }
}

fun getLanguageId(language: String): Int {
    return when (language) {
        LocaleManager.LANGUAGE_ENGLISH -> LanguageIds.ENGLISH
        LocaleManager.LANGUAGE_HINDI -> LanguageIds.HINDI
        LocaleManager.LANGUAGE_URDU -> LanguageIds.URDU
        LocaleManager.LANGUAGE_CHINESE -> LanguageIds.CHINESE
        LocaleManager.LANGUAGE_ARABIC -> LanguageIds.ARABIC
        LocaleManager.LANGUAGE_SPANISH -> LanguageIds.SINHALA
        LocaleManager.LANGAGE_TAMIL -> LanguageIds.TAMIL
        else -> LanguageIds.ENGLISH
    }
}

fun getLanguage(id: Int): String {
    return when (id) {
        LanguageIds.ENGLISH -> LocaleManager.LANGUAGE_ENGLISH
        LanguageIds.HINDI -> LocaleManager.LANGUAGE_HINDI
        LanguageIds.URDU -> LocaleManager.LANGUAGE_URDU
        LanguageIds.CHINESE -> LocaleManager.LANGUAGE_CHINESE
        LanguageIds.ARABIC -> LocaleManager.LANGUAGE_ARABIC
        LanguageIds.SINHALA -> LocaleManager.LANGUAGE_SPANISH
        LanguageIds.TAMIL -> LocaleManager.LANGAGE_TAMIL
        else -> LocaleManager.LANGUAGE_ENGLISH
    }
}


fun getVehicleResId(categoryId: Int?): Int {
    return R.drawable.ic_car_cab
}


fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun getRotateDrawable(d: Drawable?, angle: Float): Drawable {
    val arD = arrayOf(d)
    return object : LayerDrawable(arD) {
        override fun draw(canvas: Canvas) {
            canvas.save()
            canvas.rotate(angle, d?.bounds?.width()?.toFloat() ?: 0F
            / 2, d?.bounds?.height()?.toFloat() ?: 0F / 2)
            super.draw(canvas)
            canvas.restore()
        }
    }
}

fun Context.getScreenWidthInPixels(context: Context): Int {
    return context.resources.displayMetrics.widthPixels
}

fun setRegularCustomFont(context: Context, textView: TextView, text: String) {
    val font = Typeface.createFromAsset(context.assets, "fonts/sf_pro_text_medium.otf")
    textView.typeface = font
    textView.text = text
    textView.setTextColor(ContextCompat.getColor(context, R.color.design_default_color_primary))
}

fun setBoldCustomFont(context: Context, textView: TextView, text: String) {
    val font = Typeface.createFromAsset(context.assets, "fonts/futura_bold.otf")
    textView.typeface = font
    textView.text = text
    textView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
}


//fun Context.handleApiError(error: AppError?) {
//
//    when (error) {
//        is AppError.ApiError -> {
//            showErrorMessage(error.message)
//        }
//        is AppError.ApiFailure -> {
//            showErrorMessage(error.message)
//        }
//        is AppError.ApiUnAuthorized -> {
//            showUnAuthorizedErrorMessage(error.message)
//        }
//        is AppError.ApiCognito -> {
//            showErrorMessage(error.message)
//        }
//        else -> {
//            showErrorMessage(getString(R.string.somethingWent))
//        }
//    }
//}


fun View.showSnackbar(msg: CharSequence) {
    Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).show()
}

//    fun showCustomToastMessage(context: Context, msg: String) {
//        val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
//        val toastView = toast.view  // this will return current view of toast
//        val textView = toastView.findViewById<TextView>(android.R.id.message)  //now you can get the textview of the default view of the toast
//        textView.textSize = 14f
//        textView.setTextColor(ContextCompat.getColor(context, R.color.white))
//        //        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.logo_icon, 0, 0, 0);   //to add image with toast
//        textView.gravity = Gravity.CENTER
//        textView.compoundDrawablePadding = 16
//        toastView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
//        toastView.background = ContextCompat.getDrawable(context, R.drawable.round_corner_toastview)
//        textView.setPadding(20, 10, 20, 10)
//        toast.show()
//    }


fun convertDateStringInISO_Format(dateStr: String): Date? {
    val tz = TimeZone.getTimeZone("UTC")
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    df.timeZone = tz

    try {
        return df.parse(dateStr)
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    return null
}

fun convertDateToString(date: Date): String {
    val tz = TimeZone.getTimeZone("UTC")
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    df.timeZone = tz
    return df.format(date)
}

@SuppressLint("SimpleDateFormat")
fun getDateFromMilliseconds(milliSeconds: Long, dateFormat: String): String {
    // Create a DateFormatter object for displaying date in specified format.
    val formatter = SimpleDateFormat(dateFormat, Locale.US)
    // Create a calendar object that will convert the date and time value in milliseconds to date.
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}

@SuppressLint("SimpleDateFormat")
fun convertDateStringToMilliseconds(dateString: String): Long {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val date = dateFormat.parse(dateString)
    return date.time
}


fun setTextColor(context: Context, textView: TextView, colorId: Int) {
    textView.setTextColor(ContextCompat.getColor(context, colorId))
}

//    fun getUTCtime(timestamp:Long, format : String):String {
//        // print UTC time
//        val utcTimeZone = TimeZone.getTimeZone("UTC")
//        val smsTime = Calendar.getInstance(utcTimeZone)
////    smsTime.timeInMillis = timestamp * 1000
//        val now = Calendar.getInstance()
//        val dateTimeFormatString = format
//        return android.text.format.DateFormat.format(dateTimeFormatString, smsTime).toString()
//    }

class TypefaceSpan(typeface: Typeface) : MetricAffectingSpan() {
    private val typeface: Typeface = typeface
    override fun updateDrawState(tp: TextPaint) {
        tp.typeface = typeface
        tp.flags = tp.flags or Paint.SUBPIXEL_TEXT_FLAG
    }

    override fun updateMeasureState(p: TextPaint) {
        p.typeface = typeface
        p.flags = p.flags or Paint.SUBPIXEL_TEXT_FLAG
    }
}


fun getDistance(lat_a: Double, lng_a: Double, lat_b: Double, lng_b: Double): Double {
    val earthRadius = 3958.75
    val latDiff = Math.toRadians((lat_b - lat_a))
    val lngDiff = Math.toRadians((lng_b - lng_a))
    val a = (Math.sin(latDiff / 2) * Math.sin(latDiff / 2) + (Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
            Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2)))
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    val distance = earthRadius * c
    val meterConversion = 1609
    return (distance * meterConversion)
}

fun Context.sendEmail(email: String) {
    val emailIntent = Intent(Intent.ACTION_SENDTO)
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.helpRequired))
    emailIntent.data = Uri.parse("mailto:$email")
    startActivity(emailIntent)
}

fun Context.shareText(text: String) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.putExtra(Intent.EXTRA_TEXT, text)
    intent.type = "text/plain"
    startActivity(intent)
}


fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}

fun Context.dialPhone(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
    startActivity(intent)
}

fun Context.sendSms(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null))
    startActivity(intent)
}


