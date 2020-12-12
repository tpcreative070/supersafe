package co.tpcreative.supersafe.common.views.progressing
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import co.tpcreative.supersafe.R
class ProgressLayout : FrameLayout {
    var spotsCount = 0
        private set
    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context!!, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, 0)
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context!!, attrs, defStyleAttr, defStyleRes) {
        init(attrs, defStyleAttr, defStyleRes)
    }
    private fun init(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.Dialog,
                defStyleAttr, defStyleRes)
        spotsCount = a.getInt(R.styleable.Dialog_DialogSpotCount, DEFAULT_COUNT)
        a.recycle()
    }
    companion object {
        private const val DEFAULT_COUNT = 5
    }
}