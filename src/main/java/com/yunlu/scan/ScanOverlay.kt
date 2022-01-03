package com.yunlu.scan

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.animation.DecelerateInterpolator


class ScanOverlay @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbsScanOverlay(context, attrs) {
    private val vertexPaint = Paint().apply {
        strokeWidth = 2f
        color = Color.WHITE
        style = Paint.Style.STROKE
    }
    private var vertexSize = 0f

    private val scanAreaPaint = Paint().apply {
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val gridDensity = 20f
    private var scanAreaSize = 0f
    private val animator = ValueAnimator()
    private var linearGradient: LinearGradient? = null
    private val scanAreaMatrix = Matrix()

    init {
        animator.setFloatValues(1f, 0f)
        animator.duration = 1300
        animator.repeatMode = ValueAnimator.RESTART
        animator.interpolator = DecelerateInterpolator()
        animator.repeatCount = -1
        animator.addUpdateListener {
            if (linearGradient != null) {

                scanAreaMatrix.setTranslate(0f, -measuredHeight * (it.animatedValue as Float))
                linearGradient!!.setLocalMatrix(scanAreaMatrix)
                invalidate()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        vertexSize = measuredWidth.toFloat() / 10

        scanAreaSize = measuredWidth.toFloat() / 3

        linearGradient = LinearGradient(
            0f,
            0f,
            0f,
            measuredWidth + 0.01f * measuredWidth,
            intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, getColorPrimary(), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 0.99f, 1f),
            Shader.TileMode.CLAMP
        )
        scanAreaPaint.shader = linearGradient
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    private fun getColorPrimary(): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        return typedValue.data
    }

    override fun onDraw(canvas: Canvas) {
        drawVertex(canvas)
        drawScanArea(canvas)
    }

    private fun drawScanArea(canvas: Canvas) {
        val path = Path()
        val count = (measuredWidth / gridDensity).toInt()
        val left = measuredWidth.toFloat() % gridDensity
        val leftHalf = left / 2
        canvas.save()
        canvas.translate(leftHalf, leftHalf)
        for (i in 0..count) {
            path.moveTo(0f, i * gridDensity)
            path.lineTo(measuredWidth.toFloat() - left, i * gridDensity)
        }

        for (i in 0..count) {
            path.moveTo(i * gridDensity, 0f)
            path.lineTo(i * gridDensity, measuredHeight.toFloat() - left)
        }

        canvas.drawPath(path, scanAreaPaint)
        canvas.restore()
    }

    private fun drawVertex(canvas: Canvas) {
        val path = Path()
        path.moveTo(0f, vertexSize)
        path.lineTo(0f, 0f)
        path.lineTo(vertexSize, 0f)

        path.moveTo(measuredWidth - vertexSize, 0f)
        path.lineTo(measuredWidth.toFloat(), 0f)
        path.lineTo(measuredWidth.toFloat(), vertexSize)


        path.moveTo(measuredWidth.toFloat(), measuredHeight - vertexSize)
        path.lineTo(measuredWidth.toFloat(), measuredHeight.toFloat())
        path.lineTo(measuredWidth - vertexSize, measuredHeight.toFloat())

        path.moveTo(vertexSize, measuredHeight.toFloat())
        path.lineTo(0f, measuredHeight.toFloat())
        path.lineTo(0f, measuredHeight - vertexSize)

        canvas.drawPath(path, vertexPaint)
    }
}