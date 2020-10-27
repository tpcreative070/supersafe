package co.tpcreative.supersafe.common.views
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R

class MDCheckBox @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle), Checkable {
    private var checkDrawable: Drawable? = null
    private var bitmapPaint: Paint? = null
    private var bitmapEraser: Paint? = null
    private var checkEraser: Paint? = null
    private var borderPaint: Paint? = null
    private var uncheckPaint: Paint? = null
    private var drawBitmap: Bitmap? = null
    private var checkBitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null
    private var checkCanvas: Canvas? = null
    private var progress = 0f
    private var checkAnim: ObjectAnimator? = null
    private var attachedToWindow = false
    private var isChecked = false
    private var size = 26
    private var bitmapColor = -0xc0ae4b
    private var borderColor = -0x1
    private val uncheckColor = 0x22000000
    private fun init(context: Context, attrs: AttributeSet?) {
        bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bitmapEraser = Paint(Paint.ANTI_ALIAS_FLAG)
        uncheckPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bitmapEraser?.setColor(0)
        bitmapEraser?.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        checkEraser = Paint(Paint.ANTI_ALIAS_FLAG)
        checkEraser?.setColor(0)
        checkEraser?.setStyle(Paint.Style.STROKE)
        checkEraser?.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint?.setStyle(Paint.Style.STROKE)
        borderPaint?.setStrokeWidth(dp(2f).toFloat())
        checkDrawable = ContextCompat.getDrawable(context,R.drawable.baseline_check_white_48)
        visibility = VISIBLE
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == VISIBLE && drawBitmap == null) {
            drawBitmap = Bitmap.createBitmap(dp(size.toFloat()), dp(size.toFloat()), Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(drawBitmap!!)
            checkBitmap = Bitmap.createBitmap(dp(size.toFloat()), dp(size.toFloat()), Bitmap.Config.ARGB_8888)
            checkCanvas = Canvas(checkBitmap!!)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newSpec = MeasureSpec.makeMeasureSpec(dp(size.toFloat()), MeasureSpec.getMode(Math.min(widthMeasureSpec, heightMeasureSpec)))
        super.onMeasure(newSpec, newSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        if (visibility != VISIBLE) {
            return
        }
        checkEraser?.setStrokeWidth(dp(size.toFloat()).toFloat())
        drawBitmap?.eraseColor(0)
        var rad = measuredWidth / 2.toFloat()
        val bitmapProgress = if (progress >= 0.5f) 1.0f else progress / 0.5f
        val checkProgress = if (progress < 0.5f) 0.0f else (progress - 0.5f) / 0.5f
        val p = if (isChecked) progress else 1.0f - progress
        if (p < BOUNCE_VALUE) {
            rad -= dp(2f) * p
        } else if (p < BOUNCE_VALUE * 2) {
            rad -= dp(2f) - dp(2f) * p
        }
        borderPaint?.setColor(borderColor)
        uncheckPaint?.setColor(uncheckColor)
        borderPaint?.let { canvas?.drawCircle(measuredWidth / 2.toFloat(), measuredHeight / 2.toFloat(), rad - dp(1f), it) }
        uncheckPaint?.let { canvas?.drawCircle(measuredWidth / 2.toFloat(), measuredHeight / 2.toFloat(), rad - dp(2f), it) }
        bitmapPaint?.setColor(bitmapColor)
        bitmapPaint?.let { bitmapCanvas?.drawCircle(measuredWidth / 2.toFloat(), measuredHeight / 2.toFloat(), rad, it) }
        bitmapEraser?.let { bitmapCanvas?.drawCircle(measuredWidth / 2.toFloat(), measuredHeight / 2.toFloat(), rad * (1 - bitmapProgress), it) }
        drawBitmap?.let { canvas?.drawBitmap(it, 0f, 0f, null) }
        checkBitmap?.eraseColor(0)
        val w = checkDrawable?.getIntrinsicWidth()
        val h = checkDrawable?.getIntrinsicHeight()
        val x = (measuredWidth - w!!) / 2
        val y = (measuredHeight - h!!) / 2
        checkDrawable?.setBounds(x, y, x + w, y + h)
        checkCanvas?.let { checkDrawable?.draw(it) }
        checkCanvas?.drawCircle(measuredWidth / 2.toFloat(), measuredHeight / 2.toFloat(), rad * (1 - checkProgress), checkEraser!!)
        checkBitmap?.let { canvas?.drawBitmap(it, 0f, 0f, null) }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachedToWindow = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        attachedToWindow = false
    }

    fun setProgress(value: Float) {
        if (progress == value) {
            return
        }
        progress = value
        invalidate()
    }

    fun setCheckBoxColor(bitmapColor: Int) {
        this.bitmapColor = bitmapColor
    }

    fun setCheckBoxSize(size: Int) {
        this.size = size
    }

    fun getProgress(): Float {
        return progress
    }

    fun setCheckedColor(value: Int) {
        bitmapColor = value
    }

    fun setBorderColor(value: Int) {
        borderColor = value
        borderPaint?.setColor(borderColor)
    }

    private fun cancelAnim() {
        if (checkAnim != null) {
            checkAnim?.cancel()
        }
    }

    private fun addAnim(isChecked: Boolean) {
        checkAnim = ObjectAnimator.ofFloat(this, "progress", if (isChecked) 1.0f else 0.0f)
        checkAnim?.setDuration(300)
        checkAnim?.start()
    }

    fun setChecked(checked: Boolean, animated: Boolean) {
        if (checked == isChecked) {
            return
        }
        isChecked = checked
        if (attachedToWindow && animated) {
            addAnim(checked)
        } else {
            cancelAnim()
            setProgress(if (checked) 1.0f else 0.0f)
        }
    }

    fun toggle(animated: Boolean) {
        setChecked(!isChecked, animated)
    }

    override fun toggle() {
        setChecked(!isChecked)
    }

    override fun setChecked(b: Boolean) {
        setChecked(b, true)
    }

    override fun isChecked(): Boolean {
        return isChecked
    }

    fun dp(value: Float): Int {
        if (value == 0f) {
            return 0
        }
        val density = context.resources.displayMetrics.density
        return Math.ceil(density * value.toDouble()).toInt()
    }

    companion object {
        private const val BOUNCE_VALUE = 0.2f
    }

    init {
        if (context != null) {
            init(context, attrs)
        }
    }
}