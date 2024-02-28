package com.trava.user.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.trava.user.R
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/*
 * Created by cbl45 on 7/5/16.
 */
object StaticFunction {

    fun pxToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun isValidColorHex(color: String?): Boolean {
        if (color == null) return false
        val colorPattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})")
        val m = colorPattern.matcher(color.toLowerCase())
        return m.matches()
    }

    fun getPrimaryColor(context: Context?): Int {
        val color = ConfigPOJO.primary_color
        val defaultColor = ContextCompat.getColor(context!!, R.color.colorPrimary)
        try {
            return Color.parseColor(color)
        } catch (e: Exception) {
            return defaultColor
        }
    }

    fun getTintDrawable(context: Context, drawableId: Int): Drawable? {
        try {
            val drawable = ContextCompat.getDrawable(context, drawableId)
            DrawableCompat.setTint(drawable!!, Color.parseColor(ConfigPOJO.primary_color))
            return drawable
        } catch (e: Exception) {
            return ContextCompat.getDrawable(context, drawableId)
        }
    }

    fun setStatusBarColor(activity: AppCompatActivity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            if (color == Color.BLACK && window.navigationBarColor == Color.BLACK) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }
            window.statusBarColor = color
        }
    }

    fun changeStrokeColor(color: String): GradientDrawable {
        val gradient = GradientDrawable()
        gradient.shape = GradientDrawable.RECTANGLE
        gradient.setStroke(1, Color.parseColor(color))
        gradient.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
        return gradient
    }


    fun changeBorderColor(color: String, strokeColor: String, shape: Int): GradientDrawable {
        val gradient = GradientDrawable()
        gradient.shape = shape
        if (strokeColor.isEmpty()) {
            gradient.setColor(Color.parseColor(color))
            gradient.setStroke(3, Color.parseColor(color))
        } else
            gradient.setStroke(2, Color.parseColor(strokeColor))


        if (shape == GradientDrawable.RADIAL_GRADIENT) {
            gradient.cornerRadius = 20f
            // gradient.setCornerRadii(new float[]{3, 3, 3, 3, 0, 0, 0, 0});
        } else {
            gradient.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
        }
        return gradient
    }


    fun varientColor(color: String, strokeColor: String, shape: Int): GradientDrawable {
        val gradient = GradientDrawable()
        gradient.shape = shape

        gradient.setColor(Color.parseColor(color))
        gradient.setStroke(3, Color.parseColor(strokeColor))

        if (shape == GradientDrawable.RADIAL_GRADIENT) {
            gradient.cornerRadius = 20f
        } else {
            gradient.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
        }

        return gradient
    }

    fun changeBorderTextColor(color: String,strokeColor: String, shape: Int): GradientDrawable {
        val gradient = GradientDrawable()
        gradient.shape = shape
        gradient.setColor(Color.parseColor(color))
        gradient.setStroke(4, Color.parseColor(strokeColor))
        gradient.cornerRadii = floatArrayOf(5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f)
        return gradient
    }

    fun changeBorderTextColorService(color: String,strokeColor: String, shape: Int): GradientDrawable {
        val gradient = GradientDrawable()
        gradient.shape = shape
        gradient.setColor(Color.parseColor(color))
        gradient.setStroke(8, Color.parseColor(strokeColor))
        gradient.cornerRadii = floatArrayOf(5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f)
        return gradient
    }

    fun changeBorderDashColor(color: String,strokeColor: String, shape: Int): GradientDrawable {
        val gradient = GradientDrawable()
        gradient.shape = shape
        gradient.setColor(Color.parseColor(color))
        gradient.setStroke(2, Color.parseColor(strokeColor),10F,10F)
        gradient.cornerRadii = floatArrayOf(5f, 5f, 5f, 5f, 5f, 5f, 5f, 5f)
        return gradient
    }

    fun changeBorderTextColorHome(color: String,strokeColor: String, shape: Int): GradientDrawable {
        val gradient = GradientDrawable()
        gradient.shape = shape
        gradient.setColor(Color.parseColor(color))
        gradient.setStroke(1, Color.parseColor(strokeColor))
        gradient.cornerRadii = floatArrayOf(25f, 25f, 25f, 25f, 25f, 25f, 25f, 25f)
        return gradient
    }

    fun changeBadgeColor(fillcolor: Int): GradientDrawable {
        val strokeWidth = 1
        /*        int strokeColor = Color.parseColor("#03dc13");
        int fillColor = Color.parseColor("#ff0000");*/
        val gradientDrawable = GradientDrawable()
        gradientDrawable.setColor(fillcolor)
        gradientDrawable.shape = GradientDrawable.OVAL
        gradientDrawable.setStroke(strokeWidth, fillcolor)
        return gradientDrawable
    }

    fun changeLocationStroke(color: String, type: String): GradientDrawable {

        val strokeWidth: Int
        if (type == "rate")
            strokeWidth = 30
        else
            strokeWidth = 20

        val gradient = GradientDrawable()
        gradient.shape = GradientDrawable.RECTANGLE
        gradient.cornerRadius = strokeWidth.toFloat()
        gradient.setColor(Color.parseColor(color))

        return gradient
    }


    fun changeCategoryColor(): GradientDrawable {
        val ButtonColors = intArrayOf(Color.parseColor("#00FFFFFF"), Color.parseColor("#A8000000"))

        val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, ButtonColors)
        gradientDrawable.shape = GradientDrawable.LINEAR_GRADIENT
        gradientDrawable.cornerRadius = 10f

        return gradientDrawable
    }


    fun changeGradientColor(): GradientDrawable {
        val ButtonColors = intArrayOf(Color.parseColor("#1A000000"), Color.parseColor("#1A000000"))

        return GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, ButtonColors)
    }

    fun getCartId(cartIds: List<Int>): String {
        return Gson().toJson(cartIds)
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

    fun convertDateOneToAnother(dateToConvert: String, toDate: String, fromDate: String): String {
        var outputDateStr = ""
        val inputFormat = SimpleDateFormat(toDate, Locale.US)
        val outputFormat = SimpleDateFormat(fromDate, Locale.US)
        val date: Date?
        try {
            date = inputFormat.parse(dateToConvert)
            outputDateStr = outputFormat.format(date!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return outputDateStr
    }

    fun priceFormatter(price: String): String {
        val formatter = DecimalFormat("#,###.00")
        return formatter.format(java.lang.Double.valueOf(price))
    }

    fun loadImage(url: String?, imageView: ImageView, roundedShape: Boolean) {
        var thumbUrl = ""

        if (url != null) {
            thumbUrl = url.substring(0, url.lastIndexOf("/") + 1) + "thumb_" + url.substring(url.lastIndexOf("/") + 1)
        }

        val glide = Glide.with(imageView.context)


        val requestOptions = RequestOptions
                .bitmapTransform(RoundedCornersTransformation(8, 0,
                        RoundedCornersTransformation.CornerType.ALL))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(R.drawable.profile_pic_placeholder)
                .error(R.drawable.profile_pic_placeholder)


        glide.load(url)
                .thumbnail(Glide.with(imageView.context).load(thumbUrl))
                .apply(requestOptions).into(imageView)

    }


    fun loadUserImage(url: String?, imageView: ImageView, roundedShape: Boolean) {
        var thumbUrl = ""

        if (url != null) {
            thumbUrl = url.substring(0, url.lastIndexOf("/") + 1) + "thumb_" + url.substring(url.lastIndexOf("/") + 1)
        }

        val glide = Glide.with(imageView.context)


        val requestOptions = RequestOptions
                .bitmapTransform(RoundedCornersTransformation(8, 0,
                        RoundedCornersTransformation.CornerType.ALL))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(R.drawable.ic_user_placeholder)
                .error(R.drawable.ic_user_placeholder)


        glide.load(url)
                .thumbnail(Glide.with(imageView.context).load(thumbUrl))
                .apply(requestOptions).into(imageView)

    }

    fun makeSelector(color: Int,sel_color: Int): StateListDrawable?
    {
        val res = StateListDrawable()
        res.setExitFadeDuration(400)
        res.addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(sel_color))
        res.addState(intArrayOf(), ColorDrawable(color))
        return res
    }
}