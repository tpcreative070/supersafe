package co.tpcreative.supersafe.ui.enterpin
import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.IntDef
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.ui.enterpin.IndicatorDots.IndicatorType.Companion.FILL
import co.tpcreative.supersafe.ui.enterpin.IndicatorDots.IndicatorType.Companion.FILL_WITH_ANIMATION
import co.tpcreative.supersafe.ui.enterpin.IndicatorDots.IndicatorType.Companion.FIXED
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * It represents a set of indicator dots which when attached with [PinLockView]
 * can be used to indicate the current length of the input
 *
 *
 * Created by aritraroy on 01/06/16.
 */
class IndicatorDots @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(FIXED, FILL, FILL_WITH_ANIMATION)
    annotation class IndicatorType {
        companion object {
            const val  FIXED = 0
            const val FILL = 1
            const val FILL_WITH_ANIMATION = 2
        }
    }

    private var mDotDiameter = 0
    private var mDotSpacing = 0
    private var mFillDrawable = 0
    private var mEmptyDrawable = 0
    private var mPinLength = 0
    private var mIndicatorType = 0
    private var activity: Activity? = null
    private var mPreviousLength = 0
    private fun initView(context: Context?) {
        if (mIndicatorType == 0) {
            for (i in 0 until mPinLength) {
                val dot = View(context)
                emptyDot(dot)
                val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(mDotDiameter,
                        mDotDiameter)
                params.setMargins(mDotSpacing, 0, mDotSpacing, 0)
                dot.layoutParams = params
                addView(dot)
            }
        } else if (mIndicatorType == 2) {
            val layoutTransition = LayoutTransition()
            layoutTransition.setDuration(DEFAULT_ANIMATION_DURATION.toLong())
            layoutTransition.setStartDelay(LayoutTransition.APPEARING, 0)
            setLayoutTransition(layoutTransition)
        }
    }

    protected override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // If the indicator type is not fixed
        if (mIndicatorType != 0) {
            val params: ViewGroup.LayoutParams = this.getLayoutParams()
            params.height = mDotDiameter
            requestLayout()
        }
    }

    fun updateDot(length: Int) {
        mPreviousLength = if (mIndicatorType == 0) {
            if (length > 0) {
                if (length > mPreviousLength) {
                    fillDot(getChildAt(length - 1))
                } else {
                    emptyDot(getChildAt(length))
                }
                length
            } else {
                // When {@code mPinLength} is 0, we need to reset all the views back to empty
                for (i in 0 until getChildCount()) {
                    val v: View = getChildAt(i)
                    emptyDot(v)
                }
                0
            }
        } else {
            if (length > 0) {
                if (length > mPreviousLength) {
                    val dot = View(getContext())
                    fillDot(dot)
                    val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(mDotDiameter,
                            mDotDiameter)
                    params.setMargins(mDotSpacing, 0, mDotSpacing, 0)
                    dot.layoutParams = params
                    addView(dot, length - 1)
                    Utils.Log(TAG, "mDotSpacing: $length")
                } else {
                    removeViewAt(length)
                }
                length
            } else {
                removeAllViews()
                0
            }
        }
    }

    private fun emptyDot(dot: View?) {
        dot?.setBackgroundResource(mEmptyDrawable)
    }

    private fun fillDot(dot: View?) {
        dot?.setBackgroundResource(mFillDrawable)
    }

    fun getPinLength(): Int {
        return mPinLength
    }

    fun setPinLength(pinLength: Int) {
        mPinLength = pinLength
        removeAllViews()
        initView(getContext())
    }

    @IndicatorType
    fun getIndicatorType(): Int {
        return mIndicatorType
    }

    fun setIndicatorType(@IndicatorType type: Int) {
        mIndicatorType = type
        removeAllViews()
        initView(getContext())
    }

    fun getActivity(): Activity? {
        return activity
    }

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    companion object {
        private val TAG = IndicatorDots::class.java.simpleName
        private const val DEFAULT_PIN_LENGTH = 4
        private const val DEFAULT_ANIMATION_DURATION = 200
    }

    init {
        val typedArray: TypedArray? = context?.obtainStyledAttributes(attrs, R.styleable.PinLockView)
        try {
            mDotDiameter = typedArray?.getDimension(R.styleable.PinLockView_dotDiameter, ResourceUtils.getDimensionInPx(getContext(), R.dimen.dot_diameter))!!.toInt()
            mDotSpacing = typedArray.getDimension(R.styleable.PinLockView_dotSpacing, ResourceUtils.getDimensionInPx(getContext(), R.dimen.dot_spacing)).toInt()
            mFillDrawable = typedArray.getResourceId(R.styleable.PinLockView_dotFilledBackground,
                    R.drawable.dot_filled)
            mEmptyDrawable = typedArray.getResourceId(R.styleable.PinLockView_dotEmptyBackground,
                    R.drawable.dot_empty)
            mPinLength = typedArray.getInt(R.styleable.PinLockView_pinLength, DEFAULT_PIN_LENGTH)
            mIndicatorType = typedArray.getInt(R.styleable.PinLockView_indicatorType,
                    FIXED)
        } finally {
            typedArray?.recycle()
        }
        initView(context)
    }
}