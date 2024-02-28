package com.trava.user.utils.bindingadapters

import android.R
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.BindingAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction.changeBorderColor
import com.trava.user.utils.StaticFunction.changeCategoryColor
import com.trava.user.utils.StaticFunction.changeGradientColor
import com.trava.user.utils.StaticFunction.changeLocationStroke
import com.trava.user.utils.StaticFunction.changeStrokeColor
import com.trava.user.utils.StaticFunction.isValidColorHex
import com.trava.user.utils.StaticFunction.loadImage
import com.trava.user.utils.StaticFunction.pxToDp
import com.trava.user.utils.StaticFunction.varientColor
import java.util.*

class BindingAdapters {

    companion object {

        @JvmStatic
        @BindingAdapter(value = ["background", "viewstroke", "viewborder", "gradient"], requireAll = false)
        fun setBackgroundAdapter(view: View, background: String?, viewstroke: String?, viewborder: String?, gradient: String?) {
            if (background != null) view.setBackgroundColor(Color.parseColor(background))
            if (viewstroke != null) {
                view.background = changeLocationStroke(viewstroke, "")
            }
            if (viewborder != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    view.foreground = changeStrokeColor(viewborder)
                }
            }
            if (gradient != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    view.foreground = changeCategoryColor()
                }
            }
        }

        @JvmStatic
        @BindingAdapter(value = ["app:drawableStart", "app:drawableStartTint"], requireAll = true)
        fun setDrawableStartTint(textView: TextView, @DrawableRes drawable: Drawable, @ColorInt color: Int) {
            val mutatedDrawable = drawable.mutate()
            mutatedDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
            textView.setCompoundDrawablesWithIntrinsicBounds(mutatedDrawable, null, null, null)
        }

        @JvmStatic
        @BindingAdapter(value = ["hint", "background", "stroke", "hintcolr", "textcolor"], requireAll = false)
        fun EditText.setEdit(hint: String?, stroke: String?, background: String?, hintcolr: String?, textcolor: String?) {


            if (hint?.isNullOrBlank() == false) {
                setHint(hint)
            }

            if (hintcolr?.isNullOrBlank() == false) {
                setHintTextColor(Color.parseColor(hintcolr))
            }

            if (textcolor?.isNullOrBlank() == false) {
                setTextColor(Color.parseColor(textcolor))
            }


            if (stroke != null && background != null) {
            }
        }

        @JvmStatic
        @BindingAdapter(value = ["background", "textcolor","cursorcolor","linecolor", "hintcolor", "whitetext", "textstroke", "gradient"], requireAll = false)
        fun setTextColor(view: TextView, background: String?, cursorcolor: String?, linecolor: String?, textcolor: String?, hintcolor: String?, whitetext: Boolean, textstroke: String?, gradient: String?) {
            if (textcolor != null) view.setTextColor(Color.parseColor(textcolor))
            if (cursorcolor != null) view.setTextColor(Color.parseColor(cursorcolor))
            if (linecolor != null) view.setTextColor(Color.parseColor(linecolor))
            if (background != null) view.setBackgroundColor(Color.parseColor(background))
            if (background != null) view.setBackgroundColor(Color.parseColor(background))
            if (hintcolor != null) view.setHintTextColor(Color.parseColor(hintcolor))
            if (textstroke != null) view.background = changeLocationStroke(textstroke, "")
            /*if (whitetext) {
                view.setTextColor(Color.parseColor(Configurations.colors.appBackground))
            }*/
            if (gradient != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    view.foreground = changeGradientColor()
                }
            }
        }

        @JvmStatic
        @BindingAdapter("background")
        fun setBackgroundAdapter(view: CardView, color: String?) {
            if (color != null) view.setCardBackgroundColor(Color.parseColor(color))
        }

        @JvmStatic
        @BindingAdapter(value = ["customSrc"])
        fun ImageView.setImageCustomSrc(src: String?) {
            loadImage(src, this, false)
        }

        @JvmStatic
        @BindingAdapter(value = ["src", "background", "changeDrawableColor", "viewborder", "tint"], requireAll = false)
        fun setImageSrc(view: ImageView, src: String?, background: String?, changeDrawableColor: Boolean, viewborder: String?, tint: String?) {
            if (changeDrawableColor && view.background != null && background != null) {
                val shapeDrawable = view.background as GradientDrawable
                shapeDrawable.setColor(Color.parseColor(background))
            } else {
                if (background != null) view.setBackgroundColor(Color.parseColor(background))
            }
            if (viewborder != null) {
                view.background = changeLocationStroke(viewborder, "")
            }
            if (tint != null) {
                if (isValidColorHex(tint)) {
                    view.setColorFilter(Color.parseColor(tint), PorterDuff.Mode.SRC_IN)
                }
            }
        }

        @JvmStatic
        @BindingAdapter(value = ["text", "background", "strokeColor", "changeDrawableColor"], requireAll = false)
        fun changeBackgroundDrawable(view: TextView, text: String?, customBgColor: String?, strokeColor: String?, changeDrawableColor: Boolean) {
            view.text = text
            if (changeDrawableColor && view.background != null && customBgColor != null) {
                val shapeDrawable = view.background as GradientDrawable
                shapeDrawable.setColor(Color.parseColor(customBgColor))
                if (strokeColor != null && !strokeColor.isEmpty()) {
                    shapeDrawable.setStroke(pxToDp(1f, view.context).toInt(), Color.parseColor(strokeColor))
                }
            } else {
                if (customBgColor != null) view.setBackgroundColor(Color.parseColor(customBgColor))
            }
        }

        /*@JvmStatic
        @BindingAdapter(value = ["activatedColor", "deActivatedColor", "background"], requireAll = false)
        fun setTabColors(view: PagerSlidingTabStrip, activatedColor: String?, deActivatedColor: String?, background: String?) {
            if (activatedColor != null) view.indicatorColor = Color.parseColor(activatedColor)
            if (deActivatedColor != null) view.textColor = Color.parseColor(deActivatedColor)
            if (background != null) {
                view.setBackgroundColor(Color.parseColor(background))
            }
        }*/

        @JvmStatic
        @BindingAdapter(value = ["textcolor", "checkedDrawable", "unCheckedDrawable"], requireAll = false)
        fun setImageSrc(radioButton: RadioButton, textcolor: String?, checkedDrawable: String?, unCheckedDrawable: String?) {
            radioButton.setTextColor(Color.parseColor(textcolor))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                radioButton.buttonTintList = ColorStateList.valueOf(Color.parseColor(checkedDrawable))
            }
            radioButton.highlightColor = Color.parseColor(unCheckedDrawable)
        }

        @JvmStatic
        @BindingAdapter(value = ["textcolor", "checkColor", "uncheckColor"], requireAll = false)
        fun setCheckBox(checkBox: CheckBox, textcolor: String?, checkColor: String?, uncheckColor: String?) {
           // checkBox.setTextColor(Color.parseColor(textcolor))
            val colorStateList = ColorStateList(arrayOf(intArrayOf(-R.attr.state_checked), intArrayOf(R.attr.state_checked)), intArrayOf(
                    Color.parseColor(ConfigPOJO.primary_color),
                    Color.parseColor(ConfigPOJO.primary_color)
            ))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                checkBox.buttonTintList = colorStateList
            }
        }

        @JvmStatic
        @BindingAdapter(value = ["textcolor", "checkColor", "uncheckColor"], requireAll = false)
        fun setRadioButton(checkBox: RadioButton, textcolor: String?, checkColor: String?, uncheckColor: String?) {
            checkBox.setTextColor(Color.parseColor(textcolor))
            val colorStateList = ColorStateList(arrayOf(intArrayOf(-R.attr.state_checked), intArrayOf(R.attr.state_checked)), intArrayOf(
                    Color.parseColor(ConfigPOJO.secondary_color),
                    Color.parseColor(ConfigPOJO.secondary_color)
            ))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                checkBox.buttonTintList=colorStateList
            }
        }

        @JvmStatic
        @BindingAdapter(value = ["activatedColor", "colorforeground", "thumbnormal"], requireAll = false)
        fun Switch.setSwitch(activatedColor: String?, colorforeground: String?, thumbnormal: String?) {
            val states = arrayOf(intArrayOf(-R.attr.state_checked), intArrayOf(R.attr.state_checked))

            if (activatedColor != null && colorforeground != null) {
                val thumbColors = intArrayOf(
                        Color.parseColor(activatedColor),
                        Color.parseColor(colorforeground))

                DrawableCompat.setTintList(DrawableCompat.wrap(thumbDrawable), ColorStateList(states, thumbColors))
            }

            if (thumbnormal != null && colorforeground != null) {
                val trackColors = intArrayOf(
                        Color.parseColor(thumbnormal),
                        Color.parseColor(colorforeground))

                DrawableCompat.setTintList(DrawableCompat.wrap(trackDrawable), ColorStateList(states, trackColors))
            }
        }


        /*@JvmStatic
        @BindingAdapter(value = ["progessBackground"], requireAll = false)
        fun ContentLoadingProgressBar.setProgessBar(progessBackground: String?) {
            if (progessBackground != null && this.background!=null) {
                this.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.parseColor(progessBackground), BlendModeCompat.SRC_ATOP)
            }
        }
*/

        @JvmStatic
        @BindingAdapter(value = ["background", "textcolor", "butonstroke"], requireAll = false)
        fun setButton(button: Button, background: String?, textcolor: String?, butonstroke: String?) {
            if (background != null) button.setBackgroundColor(Color.parseColor(background))
            if (textcolor != null) button.setTextColor(Color.parseColor(textcolor))
            if (butonstroke != null) button.background = changeBorderColor(butonstroke, "", GradientDrawable.RECTANGLE)
        }

        @JvmStatic
        @BindingAdapter(value = ["tabSelectedText", "tabTextColr", "tabIndicateColr", "background"], requireAll = false)
        fun TabLayout.setTabLayout(tabSelectedText: String?, tabTextColr: String?, tabIndicateColr: String?, background: String?) {
            setSelectedTabIndicatorColor(Color.parseColor(tabIndicateColr))
            setTabTextColors(Color.parseColor(tabTextColr), Color.parseColor(tabSelectedText))

            setBackgroundColor(Color.parseColor(background))
        }

        @JvmStatic
        @BindingAdapter(value = ["textcolor", "background"], requireAll = false)
        fun MaterialButton.setMaterialButton(textColor: String?, background: String?) {
            setTextColor(Color.parseColor(textColor))
            setBackgroundColor(Color.parseColor(background))
        }

        @JvmStatic
        @BindingAdapter(value = ["ratecolor","deliveryTime"], requireAll = false)
        fun TextView.setRtaingColor(rating: Double,deliveryTime:Int) {


            if(deliveryTime==0)
            {
                val color: String = when (String.format(Locale.getDefault(), "%.0f", rating)) {
                    "1" -> "#c68f9a"
                    "2" -> "#FABC0E"
                    "3" -> "#CDD814"
                    "4", "5" -> "#63AA27"
                    else -> "#8A959B"
                }
                background = changeLocationStroke(color, "")
            }
        }


        @JvmStatic
        @BindingAdapter(value = ["background"], requireAll = false)
        fun TextView.setBackgroundAdapter(background: Boolean?) {
            if (background == true) {
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#01AFFF"))
            } else {
                setTextColor(Color.BLACK)
                setBackgroundColor(Color.WHITE)
            }
        }


       /* @JvmStatic
        @BindingAdapter(value = ["varient"], requireAll = false)
        fun TextView.setVarientItem(varient: VariantValuesBean?) {

            if (varient?.variant_type == 1) {
                setBackground(varientColor(varient.variant_value
                        ?: "", Configurations.colors.primaryColor
                        ?: "", GradientDrawable.RADIAL_GRADIENT))
            } else {
                setText(varient?.variant_value)
            }

        }*/


        @JvmStatic
        @BindingAdapter(value = ["deActivatedColor", "activatedColor"], requireAll = false)
        fun BottomNavigationView.setAdapter(deActivatedColor: String?, activatedColor: String?) {
            if (deActivatedColor != null && activatedColor != null) {
                val states = arrayOf(intArrayOf(-R.attr.state_checked), intArrayOf(R.attr.state_checked), intArrayOf())

                // Fill in color corresponding to state defined in state
                val colors = intArrayOf(
                        Color.parseColor(deActivatedColor),
                        Color.parseColor(activatedColor),
                        Color.parseColor(deActivatedColor))

                val navigationViewColorStateList = ColorStateList(states, colors)

                itemIconTintList = navigationViewColorStateList
                itemTextColor = navigationViewColorStateList
            }
        }


        @JvmStatic
        @BindingAdapter(value = ["background", "textcolor"], requireAll = false)
        fun FloatingActionButton.setAdapter(background: String?, textcolor: String?) {
            if (background != null && textcolor != null) {

                setBackgroundColor(Color.parseColor(background))
            }
        }

    }
}

