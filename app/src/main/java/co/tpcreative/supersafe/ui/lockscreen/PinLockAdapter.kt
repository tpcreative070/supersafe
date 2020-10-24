package co.tpcreative.supersafe.ui.lockscreen
import android.graphics.PorterDuff
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.util.Utils

class PinLockAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mCustomizationOptionsBundle: CustomizationOptionsBundle? = null
    private var mOnNumberClickListener: OnNumberClickListener? = null
    private var mOnVerifyClickListener: OnVerifyClickListener? = null
    private var mPinLength = 0
    private val BUTTON_ANIMATION_DURATION = 150
    private var mKeyValues: IntArray?
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.getContext())
        return if (viewType == VIEW_TYPE_NUMBER) {
            val view: View = inflater.inflate(R.layout.layout_number_item, parent, false)
            NumberViewHolder(view)
        } else {
            val view: View = inflater.inflate(R.layout.layout_verify_item, parent, false)
            VerifyViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.getItemViewType() == VIEW_TYPE_NUMBER) {
            val vh1 = holder as NumberViewHolder?
            if (vh1 != null) {
                configureNumberButtonHolder(vh1, position)
            }
        } else {
            val vh2 = holder as VerifyViewHolder?
            configureDeleteButtonHolder(vh2)
        }
    }

    private fun configureNumberButtonHolder(holder: NumberViewHolder, position: Int) {
        if (holder != null) {
            if (position == 9) {
                holder.mNumberButton?.setVisibility(View.GONE)
            } else {
                if (position > mKeyValues?.size!! || position == mKeyValues?.size) {
                    return
                }
                holder.mNumberButton?.setText(mKeyValues!!.get(position).toString())
                holder.mNumberButton?.setVisibility(View.VISIBLE)
                holder.mNumberButton?.setTag(mKeyValues!!.get(position))
            }
            if (mCustomizationOptionsBundle != null) {
                mCustomizationOptionsBundle?.getTextColor()?.let { holder.mNumberButton?.setTextColor(it) }
                if (mCustomizationOptionsBundle?.getButtonBackgroundDrawable() != null) {
                    holder.mNumberButton?.setBackground(
                            mCustomizationOptionsBundle!!.getButtonBackgroundDrawable())
                }
                mCustomizationOptionsBundle?.getTextSize()?.toFloat()?.let {
                    holder.mNumberButton?.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            it)
                }
                val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        mCustomizationOptionsBundle!!.getButtonSize(),
                        mCustomizationOptionsBundle!!.getButtonSize())
                holder.mNumberButton?.setTextSize(40f)
                holder.mNumberButton?.setLayoutParams(params)
            }
        }
    }

    private fun configureDeleteButtonHolder(holder: VerifyViewHolder?) {
        if (holder != null) {
            if (mCustomizationOptionsBundle?.isShowVerifyButton()!! && mPinLength > 0) {
                holder.mButtonImage?.setVisibility(View.VISIBLE)
                if (mCustomizationOptionsBundle?.getVerifyButtonDrawable() != null) {
                    holder.mButtonImage?.setImageDrawable(mCustomizationOptionsBundle?.getVerifyButtonDrawable())
                }
                Utils.Log(TAG, "onVerify changed color")
                mCustomizationOptionsBundle?.getTextColorVerify()?.let { holder.mButtonImage?.setColorFilter(it, PorterDuff.Mode.SRC_ATOP) }
                val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        mCustomizationOptionsBundle?.getVerifyButtonWidthSize()!!,
                        mCustomizationOptionsBundle?.getVerifyButtonHeightSize()!!)
                holder.mButtonImage?.setLayoutParams(params)
            } else {
                mCustomizationOptionsBundle?.getVerifyButtonNormalColor()?.let { holder.mButtonImage?.setColorFilter(it, PorterDuff.Mode.SRC_ATOP) }
            }
        }
    }

    override fun getItemCount(): Int {
        return mKeyValues?.size?.plus(1)!!
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == mKeyValues?.size) {
            VIEW_TYPE_VERIFY
        } else super.getItemViewType(position)
    }

    fun setPinLength(pinLength: Int) {
        mPinLength = pinLength
    }

    fun setKeyValues(keyValues: IntArray?) {
        mKeyValues = getAdjustKeyValues(keyValues)
        notifyDataSetChanged()
    }

    private fun getAdjustKeyValues(keyValues: IntArray?): IntArray? {
        val adjustedKeyValues = keyValues?.size?.plus(1)?.let { IntArray(it) }
        for (i in keyValues?.indices!!) {
            if (i < 9) {
                adjustedKeyValues?.set(i, keyValues.get(i))
            } else {
                adjustedKeyValues?.set(i, -1)
                adjustedKeyValues?.set(i + 1, keyValues.get(i))
            }
        }
        return adjustedKeyValues
    }

    fun setOnItemClickListener(onNumberClickListener: OnNumberClickListener?) {
        mOnNumberClickListener = onNumberClickListener
    }

    fun setOnVerifyClickListener(onVerifyClickListener: OnVerifyClickListener?) {
        mOnVerifyClickListener = onVerifyClickListener
    }

    fun setCustomizationOptions(customizationOptionsBundle: CustomizationOptionsBundle?) {
        mCustomizationOptionsBundle = customizationOptionsBundle
    }

    interface OnNumberClickListener {
        fun onNumberClicked(keyValue: Int)
    }

    interface OnVerifyClickListener {
        fun onVerifyClicked()
    }

    inner class NumberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.buttonNumber)
        var mNumberButton: Button? = null

        @OnClick(R.id.buttonNumber)
        fun onNumberButton(view: View?) {
            if (mOnNumberClickListener != null) {
                mOnNumberClickListener?.onNumberClicked(view?.getTag() as Int)
                mNumberButton?.startAnimation(scale())
            }
        }
        init {
            ButterKnife.bind(this, itemView)
        }
    }

    inner class VerifyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.buttonVerify)
        var mVerifyButton: LinearLayout? = null

        @BindView(R.id.buttonImage)
        var mButtonImage: ImageView? = null

        @OnClick(R.id.buttonVerify)
        fun onVerifyButton() {
            if (mCustomizationOptionsBundle?.isShowVerifyButton()!! && mPinLength > 0) {
                if (mOnVerifyClickListener != null) {
                    mOnVerifyClickListener?.onVerifyClicked()
                    mVerifyButton?.startAnimation(scale())
                    Utils.Log(TAG, "Verified button")
                } else {
                    Utils.Log(TAG, "mOnVerifyClickListener Null")
                }
            } else {
                Utils.Log(TAG, "Pin length Null")
            }
        }

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    private fun scale(): Animation? {
        val scaleAnimation = ScaleAnimation(.75f, 1f, .75f, 1f,
                Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f)
        scaleAnimation.setDuration(BUTTON_ANIMATION_DURATION.toLong())
        scaleAnimation.setFillAfter(true)
        return scaleAnimation
    }

    companion object {
        private val TAG = PinLockAdapter::class.java.simpleName
        private const val VIEW_TYPE_NUMBER = 0
        private const val VIEW_TYPE_VERIFY = 1
    }

    init {
        mKeyValues = getAdjustKeyValues(intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0))
    }
}