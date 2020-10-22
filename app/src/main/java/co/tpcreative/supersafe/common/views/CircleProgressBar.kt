package co.tpcreative.supersafe.common.views
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import co.tpcreative.supersafe.R

/**
 * Created by M. Emre Davarci on 02.08.2017.
 */
class CircleProgressBar : View {
    private var progressBarPaint: Paint? = null
    private var bacgroundPaint: Paint? = null
    private var textPaint: Paint? = null
    private var mRadius = 0f
    private val mArcBounds: RectF? = RectF()
    var drawUpto = 0f

    constructor(context: Context?) : super(context) {
        // create the Paint and set its color
    }

    private var progressColor = 0
    private var backgroundColor = 0
    private var strokeWidth = 0f
    private var backgroundWidth = 0f
    private var roundedCorners = false
    private var maxValue = 0f
    private var progressTextColor = Color.BLACK
    private var textSize = 18f
    private var text: String? = ""
    private var suffix: String? = ""
    private var prefix: String? = ""
    var defStyleAttr = 0

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.defStyleAttr = defStyleAttr
        initPaints(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        initPaints(context, attrs)
    }

    private fun initPaints(context: Context, attrs: AttributeSet?) {
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar, defStyleAttr, 0)
        progressColor = ta.getColor(R.styleable.CircleProgressBar_progressColor, Color.BLUE)
        backgroundColor = ta.getColor(R.styleable.CircleProgressBar_backgroundColor, Color.GRAY)
        strokeWidth = ta.getFloat(R.styleable.CircleProgressBar_strokeWidths, 10f)
        backgroundWidth = ta.getFloat(R.styleable.CircleProgressBar_backgroundWidth, 10f)
        roundedCorners = ta.getBoolean(R.styleable.CircleProgressBar_roundedCorners, false)
        maxValue = ta.getFloat(R.styleable.CircleProgressBar_maxValue, 100f)
        progressTextColor = ta.getColor(R.styleable.CircleProgressBar_progressTextColor, Color.BLACK)
        textSize = ta.getDimension(R.styleable.CircleProgressBar_textSize, 18f)
        suffix = ta.getString(R.styleable.CircleProgressBar_suffix)
        prefix = ta.getString(R.styleable.CircleProgressBar_prefix)
        text = ta.getString(R.styleable.CircleProgressBar_progressText)
        progressBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        progressBarPaint?.setStyle(Paint.Style.FILL)
        progressBarPaint?.setColor(progressColor)
        progressBarPaint?.setStyle(Paint.Style.STROKE)
        progressBarPaint?.setStrokeWidth(strokeWidth * resources.displayMetrics.density)
        if (roundedCorners) {
            progressBarPaint?.setStrokeCap(Paint.Cap.ROUND)
        } else {
            progressBarPaint?.setStrokeCap(Paint.Cap.BUTT)
        }
        val pc = String.format("#%06X", 0xFFFFFF and progressColor)
        progressBarPaint?.setColor(Color.parseColor(pc))
        bacgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bacgroundPaint?.setStyle(Paint.Style.FILL)
        bacgroundPaint?.setColor(backgroundColor)
        bacgroundPaint?.setStyle(Paint.Style.STROKE)
        bacgroundPaint?.setStrokeWidth(backgroundWidth * resources.displayMetrics.density)
        bacgroundPaint?.setStrokeCap(Paint.Cap.SQUARE)
        val bc = String.format("#%06X", 0xFFFFFF and backgroundColor)
        bacgroundPaint?.setColor(Color.parseColor(bc))
        ta.recycle()
        textPaint = TextPaint()
        textPaint?.setColor(progressTextColor)
        val c = String.format("#%06X", 0xFFFFFF and progressTextColor)
        textPaint?.setColor(Color.parseColor(c))
        textPaint?.setTextSize(textSize)
        textPaint?.setAntiAlias(true)

        //paint.setAntiAlias(true);
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRadius = Math.min(w, h) / 2f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val size = Math.min(w, h)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val mouthInset = mRadius / 3
        mArcBounds?.set(mouthInset, mouthInset, mRadius * 2 - mouthInset, mRadius * 2 - mouthInset)
        canvas?.drawArc(mArcBounds!!, 0f, 360f, false, bacgroundPaint!!)
        canvas?.drawArc(mArcBounds!!, 270f, drawUpto / getMaxValue() * 360, false, progressBarPaint!!)
        if (TextUtils.isEmpty(suffix)) {
            suffix = ""
        }
        if (TextUtils.isEmpty(prefix)) {
            prefix = ""
        }
        val drawnText = prefix + text + suffix
        if (!TextUtils.isEmpty(text)) {
            val textHeight = textPaint?.descent()?.plus(textPaint?.ascent()!!)
            canvas?.drawText(drawnText, (width - textPaint?.measureText(drawnText)!!) / 2.0f, (width - textHeight!!) / 2.0f, textPaint!!)
        }
    }

    fun setProgress(f: Float) {
        drawUpto = f
        invalidate()
    }

    fun getProgress(): Float {
        return drawUpto
    }

    fun getProgressPercentage(): Float {
        return drawUpto / getMaxValue() * 100
    }

    fun setProgressColor(color: Int) {
        progressColor = color
        progressBarPaint?.setColor(color)
        invalidate()
    }

    fun setProgressColor(color: String?) {
        progressBarPaint?.setColor(Color.parseColor(color))
        invalidate()
    }

    override fun setBackgroundColor(color: Int) {
        backgroundColor = color
        bacgroundPaint?.setColor(color)
        invalidate()
    }

    fun setBackgroundColor(color: String?) {
        bacgroundPaint?.setColor(Color.parseColor(color))
        invalidate()
    }

    fun getMaxValue(): Float {
        return maxValue
    }

    fun setMaxValue(max: Float) {
        maxValue = max
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        invalidate()
    }

    fun getStrokeWidth(): Float {
        return strokeWidth
    }

    fun setBackgroundWidth(width: Float) {
        backgroundWidth = width
        invalidate()
    }

    fun getBackgroundWidth(): Float {
        return backgroundWidth
    }

    fun setText(progressText: String?) {
        text = progressText
        invalidate()
    }

    fun getText(): String? {
        return text
    }

    fun setTextColor(color: Int) {
        progressTextColor = color
        textPaint?.setColor(color)
        invalidate()
    }

    fun setTextColor(color: String?) {
        textPaint?.setColor(Color.parseColor(color))
        invalidate()
    }

    fun getTextColor(): Int {
        return progressTextColor
    }

    fun setSuffix(suffix: String?) {
        this.suffix = suffix
        invalidate()
    }

    fun getSuffix(): String? {
        return suffix
    }

    fun getPrefix(): String? {
        return prefix
    }

    fun setPrefix(prefix: String?) {
        this.prefix = prefix
        invalidate()
    }
}