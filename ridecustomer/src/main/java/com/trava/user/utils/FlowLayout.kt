package com.trava.user.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.trava.user.R


class FlowLayout(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {
    private var mhorizontalSpacing: Int = 0
    private var mVerticalSpacing: Int = 0
    private val mPaint: Paint

    init {

        val a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout)
        try {
            mhorizontalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_horizontalSpacing + 10, 0)
            mVerticalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_verticalSpacing, 0)
        } finally {
            a.recycle()
        }

        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.color = Color.BLACK
        mPaint.strokeWidth = 2.0f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec) - paddingRight
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)

        val growHeight = widthMode != View.MeasureSpec.UNSPECIFIED

        var width = 0
        var height = paddingTop

        var currentWidth = paddingLeft
        var currentHeight = 0

        var breakLine = false
        var newLine = false
        var spacing = 0

        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            val lp = child.layoutParams as LayoutParams
            spacing = mhorizontalSpacing + 10
            if (lp.horizontalSpacing + 10 >= 0) {
                spacing = lp.horizontalSpacing + 10
            }

            if (growHeight && (breakLine || currentWidth + child.measuredWidth > widthSize)) {
                height += currentHeight + mVerticalSpacing
                currentHeight = 0
                width = Math.max(width, currentWidth - spacing)
                currentWidth = paddingLeft
                newLine = true
            } else {
                newLine = false
            }

            lp.x = currentWidth
            lp.y = height

            currentWidth += child.measuredWidth + spacing
            currentHeight = Math.max(currentHeight, child.measuredHeight)

            breakLine = lp.breakLine
        }

        if (!newLine) {
            height += currentHeight
            width = Math.max(width, currentWidth - spacing)
        }

        width += paddingRight
        height += paddingBottom

        setMeasuredDimension(View.resolveSize(width, widthMeasureSpec),
                View.resolveSize(height, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams
            child.layout(lp.x, lp.y, lp.x + child.measuredWidth, lp.y + child.measuredHeight)
        }
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val more = super.drawChild(canvas, child, drawingTime)
        val lp = child.layoutParams as LayoutParams
        if (lp.horizontalSpacing + 10 + 10 > 0) {
            val x = child.right.toFloat()
            val y = child.top + child.height / 2.0f
            canvas.drawLine(x, y - 4.0f, x, y + 4.0f, mPaint)
            canvas.drawLine(x, y, x + lp.horizontalSpacing.toFloat() + 10f, y, mPaint)
            canvas.drawLine(x + lp.horizontalSpacing.toFloat() + 10f, y - 4.0f, x + lp.horizontalSpacing.toFloat() + 10f, y + 4.0f, mPaint)
        }
        if (lp.breakLine) {
            val x = child.right.toFloat()
            val y = child.top + child.height / 2.0f
            canvas.drawLine(x, y, x, y + 6.0f, mPaint)
            canvas.drawLine(x, y + 6.0f, x + 6.0f, y + 6.0f, mPaint)
        }
        return more
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams {
        return LayoutParams(p.width, p.height)
    }

    class LayoutParams : ViewGroup.LayoutParams {
        internal var x: Int = 0
        internal var y: Int = 0

        internal var horizontalSpacing: Int = 0
        internal var breakLine: Boolean = false

        internal constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout_LayoutParams)
            try {
                horizontalSpacing = a.getDimensionPixelSize(R.styleable.FlowLayout_LayoutParams_layout_horizontalSpacing + 10, -1)
                breakLine = a.getBoolean(R.styleable.FlowLayout_LayoutParams_layout_breakLine, false)
            } finally {
                a.recycle()
            }
        }

        internal constructor(w: Int, h: Int) : super(w, h) {}
    }
}
