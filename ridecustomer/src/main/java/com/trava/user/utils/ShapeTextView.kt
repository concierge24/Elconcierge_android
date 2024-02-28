package com.trava.user.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import com.trava.user.R

class ShapeTextView : AppCompatTextView {
    companion object {
        private const val SHAPE_SOLID = 0
        private const val SHAPE_GRADIENT = 1

        private const val GRADIENT_ORIENTATION_LEFT_TO_RIGHT = 0
        private const val GRADIENT_ORIENTATION_TOP_TO_BOTTOM = 1
        private const val GRADIENT_ORIENTATION_TOP_LEFT_TO_BOTTOM_RIGHT = 2
        private const val GRADIENT_ORIENTATION_TOP_RIGHT_TO_BOTTOM_LEFT = 3

        private const val DEFAULT_RIPPLE_COLOR = 0x40000000
    }

    private var shapeType = SHAPE_SOLID
    private var radius = 0f
    private var topLeftRadius = 0f
    private var topRightRadius = 0f
    private var bottomLeftRadius = 0f
    private var bottomRightRadius = 0f
    var solidBackgroundColor = 0
    private var setOvalShape = false
    private var setRippleEnabled = false
    private var rippleColor = DEFAULT_RIPPLE_COLOR
    private var strokeWidth = 0
    var strokeColor = 0

    private var gradientStartColor = 0
    private var gradientEndColor = 0
    private var gradientOrientation = GRADIENT_ORIENTATION_LEFT_TO_RIGHT

    private val cornerRadii: FloatArray

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeTextView)

        // Common attributes for all shapes
        shapeType = typedArray.getInt(R.styleable.ShapeTextView_shapeType, SHAPE_SOLID)
        radius = typedArray.getDimensionPixelSize(R.styleable.ShapeTextView_radius, 0).toFloat()
        if (radius == 0f) {
            topLeftRadius = typedArray.getDimensionPixelSize(R.styleable.ShapeTextView_topLeftRadius, 0).toFloat()
            topRightRadius = typedArray.getDimensionPixelSize(R.styleable.ShapeTextView_topRightRadius, 0).toFloat()
            bottomLeftRadius = typedArray.getDimensionPixelSize(R.styleable.ShapeTextView_bottomLeftRadius, 0).toFloat()
            bottomRightRadius =
                    typedArray.getDimensionPixelSize(R.styleable.ShapeTextView_bottomRightRadius, 0).toFloat()
        }
        solidBackgroundColor = typedArray.getColor(R.styleable.ShapeTextView_solidBackgroundColor, 0)
        setOvalShape = typedArray.getBoolean(R.styleable.ShapeTextView_setOvalShape, false)
        setRippleEnabled = typedArray.getBoolean(R.styleable.ShapeTextView_setRippleEnabled, false)
        if (setRippleEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rippleColor = typedArray.getColor(R.styleable.ShapeTextView_rippleColor, DEFAULT_RIPPLE_COLOR)
        }
        strokeWidth = typedArray.getDimensionPixelSize(R.styleable.ShapeTextView_strokeWidth, 0)
        strokeColor = typedArray.getColor(R.styleable.ShapeTextView_strokeColor, 0)

        // Only used when shape type is Gradient
        gradientStartColor = typedArray.getColor(R.styleable.ShapeTextView_gradientStartColor, 0)
        gradientEndColor = typedArray.getColor(R.styleable.ShapeTextView_gradientEndColor, 0)
        gradientOrientation =
                typedArray.getInt(R.styleable.ShapeTextView_gradientOrientation, GRADIENT_ORIENTATION_LEFT_TO_RIGHT)

        typedArray.recycle()

        // If radius is non-zero then apply it to all corners otherwise use the individual corner radius
        cornerRadii = if (radius != 0f) {
            getCornerRadiiArray(radius, radius, radius, radius)
        } else {
            getCornerRadiiArray(topLeftRadius, topRightRadius, bottomLeftRadius, bottomRightRadius)
        }

        // Set the background of view according to the shape type
        when (shapeType) {
            SHAPE_SOLID -> {
                background = if (strokeColor != 0 && strokeWidth != 0) {
                    getStrokeShapeDrawable()
                } else {
                    getShapeDrawable(setOvalShape, cornerRadii, solidBackgroundColor)
                }
            }

            SHAPE_GRADIENT -> {
                background = getGradientDrawable()
            }
        }

        // Add the ripple if enabled and os version is above Kitkat
        if (setRippleEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Mask will have the same shape as the background of the view
            val mask = getShapeDrawable(setOvalShape, cornerRadii, rippleColor)
            background = RippleDrawable(ColorStateList.valueOf(rippleColor), background, mask)
        }
    }

    private fun getShapeDrawable(setOvalShape: Boolean, cornerRadii: FloatArray, backgroundColor: Int): ShapeDrawable {
        val shape = if (setOvalShape) OvalShape() else RoundRectShape(cornerRadii, null, null)
        val shapeDrawable = ShapeDrawable(shape)
        shapeDrawable.paint.color = backgroundColor
        return shapeDrawable
    }

    private fun getStrokeShapeDrawable(): GradientDrawable {
        val drawable = GradientDrawable()
        drawable.setColor(solidBackgroundColor)
        drawable.shape = if (setOvalShape) GradientDrawable.OVAL else GradientDrawable.RECTANGLE
        drawable.setStroke(strokeWidth, strokeColor)
        drawable.cornerRadii = cornerRadii
        return drawable
    }

    private fun getGradientDrawable(): GradientDrawable {
        val gradientOrientation = getGradientOrientation(gradientOrientation)
        val gradient = GradientDrawable(gradientOrientation, intArrayOf(gradientStartColor, gradientEndColor))
        gradient.shape = if (setOvalShape) GradientDrawable.OVAL else GradientDrawable.RECTANGLE
        gradient.cornerRadii = cornerRadii
        if (strokeWidth != 0 && strokeColor != 0) {
            gradient.setStroke(strokeWidth, strokeColor)
        }
        return gradient
    }

    /**
     * The corners are ordered top-left, top-right, bottom-right, bottom-left.
     * For each corner, the array contains 2 values [X_radius, Y_radius]
     * */
    private fun getCornerRadiiArray(
            topLeftRadius: Float,
            topRightRadius: Float,
            bottomLeftRadius: Float,
            bottomRightRadius: Float
    ): FloatArray {
        val radii = FloatArray(8)
        radii[0] = topLeftRadius
        radii[1] = topLeftRadius
        radii[2] = topRightRadius
        radii[3] = topRightRadius
        radii[4] = bottomRightRadius
        radii[5] = bottomRightRadius
        radii[6] = bottomLeftRadius
        radii[7] = bottomLeftRadius
        return radii
    }

    private fun getGradientOrientation(orientation: Int): GradientDrawable.Orientation {
        return when (orientation) {
            GRADIENT_ORIENTATION_LEFT_TO_RIGHT -> GradientDrawable.Orientation.LEFT_RIGHT

            GRADIENT_ORIENTATION_TOP_TO_BOTTOM -> GradientDrawable.Orientation.TOP_BOTTOM

            GRADIENT_ORIENTATION_TOP_LEFT_TO_BOTTOM_RIGHT -> GradientDrawable.Orientation.TL_BR

            GRADIENT_ORIENTATION_TOP_RIGHT_TO_BOTTOM_LEFT -> GradientDrawable.Orientation.TR_BL

            else -> GradientDrawable.Orientation.LEFT_RIGHT
        }
    }
}
