package co.tpcreative.supersafe.common.views
import android.content.Context
import android.util.AttributeSet
import android.view.View

class SquaredView : View {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredWidth)
    }

    fun toggle() {
        visibility = if (visibility == VISIBLE) INVISIBLE else VISIBLE
    }
}