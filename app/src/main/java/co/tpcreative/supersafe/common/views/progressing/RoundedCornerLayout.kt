package co.tpcreative.supersafe.common.views.progressing
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
class RoundedCornerLayout : FrameLayout {
    private var cornerRadius = 0f
    var target = 0
    var xFactor: Float
        get() = x / target
        set(xFactor) {
            x = target * xFactor
        }
    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs, defStyle)
    }
    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val metrics = context.resources.displayMetrics
        cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CORNER_RADIUS, metrics)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }
    override fun dispatchDraw(canvas: Canvas) {
        val count = canvas.save()
        val path = Path()
        path.addRoundRect(RectF(0F, 0F, canvas.width.toFloat(), canvas.height.toFloat()), cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(count)
    }
    companion object {
        private const val CORNER_RADIUS = 6.0f
    }
}