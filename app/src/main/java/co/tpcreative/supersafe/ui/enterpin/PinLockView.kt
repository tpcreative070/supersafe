package co.tpcreative.supersafe.ui.enterpin
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
class PinLockView : RecyclerView {
    private var mPin: String? = ""
    private var mPinLength = 0
    private var mHorizontalSpacing = 0
    private var mVerticalSpacing = 0
    private var mTextColor = 0
    private var mVerifyButtonPressedColor = 0
    private var mTextColorNormal = 0
    private val mVerifyButtonNormalColor = 0
    private var mTextSize = 0
    private var mButtonSize = 0
    private var mVerifyButtonWidthSize = 0
    private var mVerifyButtonHeightSize = 0
    private var mButtonBackgroundDrawable: Drawable? = null
    private var mVerifyButtonDrawable: Drawable? = null
    private var mShowVerifyButton = false
    private var mIndicatorDots: IndicatorDots? = null
    private var mAdapter: PinLockAdapter? = null
    private var mPinLockListener: PinLockListener? = null
    private var mCustomizationOptionsBundle: CustomizationOptionsBundle? = null
    private var mCustomKeySet: IntArray? = null
    private val mOnNumberClickListener: PinLockAdapter.OnNumberClickListener = object : PinLockAdapter.OnNumberClickListener {
        override fun onNumberClicked(keyValue: Int) {
            if (mPin?.length!! < getPinLength()) {
                mPin = mPin + keyValue.toString()
                if (isIndicatorDotsAttached()) {
                    mIndicatorDots?.updateDot(mPin?.length!!)
                }
                if (mPin?.length == 1) {
                    mAdapter?.setPinLength(mPin!!.length)
                    mAdapter?.getItemCount()?.minus(1)?.let { mAdapter?.notifyItemChanged(it) }
                }
                if (mPinLockListener != null) {
                    if (mPin?.length == mPinLength) {
                        mPinLockListener?.onComplete(mPin)
                    } else {
                        mPinLockListener?.onPinChange(mPin!!.length, mPin)
                    }
                } else {
                    Utils.Log(TAG, "mPinLockListener is null")
                }
            } else {
                if (!isShowDeleteButton()) {
                    resetPinLockView()
                    mPin += keyValue.toString()
                    if (isIndicatorDotsAttached()) {
                        mIndicatorDots?.updateDot(mPin?.length!!)
                    }
                    if (mPinLockListener != null) {
                        mPinLockListener?.onPinChange(mPin?.length!!, mPin)
                    }
                } else {
                    if (mPinLockListener != null) {
                        mPinLockListener?.onComplete(mPin)
                    } else {
                        Utils.Log(TAG, "mPinLockListener is null")
                    }
                }
            }
        }
    }

    /*Delete on item clicked*/
    fun onDeleteClicked() {
        if (mPin?.length!! > 0) {
            mPin = mPin?.substring(0, mPin?.length!! - 1)
            if (isIndicatorDotsAttached()) {
                mIndicatorDots?.updateDot(mPin?.length!!)
            }
            if (mPin?.length == 0) {
                mAdapter?.setPinLength(mPin?.length!!)
                mAdapter?.itemCount?.minus(1)?.let { mAdapter?.notifyItemChanged(it) }
            }
            if (mPinLockListener != null) {
                if (mPin?.length == 0) {
                    mPinLockListener?.onEmpty()
                    clearInternalPin()
                } else {
                    mPinLockListener?.onPinChange(mPin?.length!!, mPin)
                }
            }
        } else {
            if (mPinLockListener != null) {
                mPinLockListener?.onEmpty()
            }
        }
    }

    /*Verify on item clicked*/
    private val mOnVerifyClickListener: PinLockAdapter.OnVerifyClickListener = object : PinLockAdapter.OnVerifyClickListener {
        override fun onVerifyClicked() {
            if (mPinLockListener != null) {
                mPinLockListener?.onComplete(mPin)
            } else {
                Utils.Log(TAG, "mPinLockListener is null")
            }
        }
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attributeSet: AttributeSet?, defStyle: Int) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PinLockView)
        try {
            mPinLength = typedArray.getInt(R.styleable.PinLockView_pinLength, DEFAULT_PIN_LENGTH)
            mHorizontalSpacing = typedArray.getDimension(R.styleable.PinLockView_keypadHorizontalSpacing, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_horizontal_spacing)).toInt()
            mVerticalSpacing = typedArray.getDimension(R.styleable.PinLockView_keypadVerticalSpacing, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_vertical_spacing)).toInt()
            mTextColor = typedArray.getColor(R.styleable.PinLockView_keypadTextColor, ResourceUtils.getColor(getContext(), R.color.text_numberpressed))
            mTextSize = typedArray.getDimension(R.styleable.PinLockView_keypadTextSize, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_text_size)).toInt()
            mButtonSize = typedArray.getDimension(R.styleable.PinLockView_keypadButtonSize, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_button_size)).toInt()
            mVerifyButtonWidthSize = typedArray.getDimension(R.styleable.PinLockView_keypadVerifyButtonSize, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_verify_button_size_height)).toInt()
            mVerifyButtonHeightSize = typedArray.getDimension(R.styleable.PinLockView_keypadVerifyButtonSize, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_verify_button_size_height)).toInt()
            mButtonBackgroundDrawable = typedArray.getDrawable(R.styleable.PinLockView_keypadButtonBackgroundDrawable)
            mVerifyButtonDrawable = typedArray.getDrawable(R.styleable.PinLockView_keypadVerifyButtonDrawable)
            mShowVerifyButton = typedArray.getBoolean(R.styleable.PinLockView_keypadShowVerifyButton, true)
            mVerifyButtonPressedColor = typedArray.getColor(R.styleable.PinLockView_keypadVerifyButtonPressedColor, ResourceUtils.getColor(getContext(), R.color.teal_a700))
            mTextColorNormal = typedArray.getColor(R.styleable.PinLockView_keypadVerifyButtonPressedColor, ResourceUtils.getColor(getContext(), R.color.material_gray_400))
        } finally {
            typedArray.recycle()
        }
        mCustomizationOptionsBundle = CustomizationOptionsBundle()
        mCustomizationOptionsBundle?.setTextColor(mTextColor)
        mCustomizationOptionsBundle?.setTextSize(mTextSize)
        mCustomizationOptionsBundle?.setButtonSize(mButtonSize)
        mCustomizationOptionsBundle?.setButtonBackgroundDrawable(mButtonBackgroundDrawable)
        mCustomizationOptionsBundle?.setVerifyButtonDrawable(mVerifyButtonDrawable)
        mCustomizationOptionsBundle?.setVerifyButtonWidthSize(mVerifyButtonWidthSize)
        mCustomizationOptionsBundle?.setVerifyButtonHeightSize(mVerifyButtonHeightSize)
        mCustomizationOptionsBundle?.setShowVerifyButton(mShowVerifyButton)
        mCustomizationOptionsBundle?.setVerifyButtonPressesColor(mVerifyButtonPressedColor)
        mCustomizationOptionsBundle?.setTextColorVerify(mVerifyButtonPressedColor)
        mCustomizationOptionsBundle?.setVerifyButtonNormalColor(mTextColorNormal)
        initView()
    }

    private fun initView() {
        mAdapter = PinLockAdapter()
        mAdapter?.setOnItemClickListener(mOnNumberClickListener)
        mAdapter?.setOnVerifyClickListener(mOnVerifyClickListener)
        mAdapter?.setCustomizationOptions(mCustomizationOptionsBundle)
        addItemDecoration(ItemSpaceDecoration(0, mVerticalSpacing, 3, false))
        layoutManager = GridLayoutManager(context, 3)
        addItemDecoration(GridSpacingItemDecoration(3, 4, true))
        itemAnimator = DefaultItemAnimator()
        adapter = mAdapter
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    /**
     * Sets a [PinLockListener] to the to listen to pin update events
     *
     * @param pinLockListener the listener
     */
    fun setPinLockListener(pinLockListener: PinLockListener?) {
        mPinLockListener = pinLockListener
    }

    /**
     * Get the length of the current pin length
     *
     * @return the length of the pin
     */
    fun getPinLength(): Int {
        return mPinLength
    }

    /**
     * Sets the pin length dynamically
     *
     * @param pinLength the pin length
     */
    fun setPinLength(pinLength: Int) {
        mPinLength = pinLength
        if (isIndicatorDotsAttached()) {
            mIndicatorDots?.setPinLength(pinLength)
        }
    }

    /**
     * Get the text color in the buttons
     *
     * @return the text color
     */
    fun getTextColor(): Int {
        return mTextColor
    }

    /**
     * Set the text color of the buttons dynamically
     *
     * @param textColor the text color
     */
    fun setTextColor(textColor: Int) {
        mTextColor = textColor
        mCustomizationOptionsBundle?.setTextColor(textColor)
        mAdapter?.notifyDataSetChanged()
    }

    /**
     * Get the size of the text in the buttons
     *
     * @return the size of the text in pixels
     */
    fun getTextSize(): Int {
        return mTextSize
    }

    /**
     * Set the size of text in pixels
     *
     * @param textSize the text size in pixels
     */
    fun setTextSize(textSize: Int) {
        mTextSize = textSize
        mCustomizationOptionsBundle?.setTextSize(textSize)
        mAdapter?.notifyDataSetChanged()
    }

    /**
     * Get the size of the pin buttons
     *
     * @return the size of the button in pixels
     */
    fun getButtonSize(): Int {
        return mButtonSize
    }

    /**
     * Set the size of the pin buttons dynamically
     *
     * @param buttonSize the button size
     */
    fun setButtonSize(buttonSize: Int) {
        mButtonSize = buttonSize
        mCustomizationOptionsBundle?.setButtonSize(buttonSize)
        mAdapter?.notifyDataSetChanged()
    }

    /**
     * Get the current background drawable of the buttons, can be null
     *
     * @return the background drawable
     */
    fun getButtonBackgroundDrawable(): Drawable? {
        return mButtonBackgroundDrawable
    }

    /**
     * Set the background drawable of the buttons dynamically
     *
     * @param buttonBackgroundDrawable the background drawable
     */
    fun setButtonBackgroundDrawable(buttonBackgroundDrawable: Drawable?) {
        mButtonBackgroundDrawable = buttonBackgroundDrawable
        mCustomizationOptionsBundle?.setButtonBackgroundDrawable(buttonBackgroundDrawable)
        mAdapter?.notifyDataSetChanged()
    }

    /**
     * Get the drawable of the delete button
     *
     * @return the delete button drawable
     */
    fun getDeleteButtonDrawable(): Drawable? {
        return mVerifyButtonDrawable
    }

    /**
     * Set the drawable of the delete button dynamically
     *
     * @param deleteBackgroundDrawable the delete button drawable
     */
    fun setDeleteButtonDrawable(deleteBackgroundDrawable: Drawable?) {
        mVerifyButtonDrawable = deleteBackgroundDrawable
        mCustomizationOptionsBundle?.setVerifyButtonDrawable(deleteBackgroundDrawable)
        mAdapter?.notifyDataSetChanged()
    }

    /**
     * Get the delete button width size in pixels
     *
     * @return size in pixels
     */
    fun getDeleteButtonWidthSize(): Int {
        return mVerifyButtonWidthSize
    }

    /**
     * Get the delete button size height in pixels
     *
     * @return size in pixels
     */
    fun getDeleteButtonHeightSize(): Int {
        return mVerifyButtonHeightSize
    }

    /**
     * Set the size of the delete button width in pixels
     *
     * @param deleteButtonWidthSize size in pixels
     */
    fun setDeleteButtonWidthSize(deleteButtonWidthSize: Int) {
        mVerifyButtonWidthSize = deleteButtonWidthSize
        mCustomizationOptionsBundle?.setVerifyButtonWidthSize(deleteButtonWidthSize)
        mAdapter?.notifyDataSetChanged()
    }

    /**
     * Set the size of the delete button height in pixels
     *
     * @param deleteButtonHeightSize size in pixels
     */
    fun setDeleteButtonHeightSize(deleteButtonHeightSize: Int) {
        mVerifyButtonHeightSize = deleteButtonHeightSize
        mCustomizationOptionsBundle?.setVerifyButtonWidthSize(deleteButtonHeightSize)
        mAdapter?.notifyDataSetChanged()
    }

    /**
     * Is the delete button shown
     *
     * @return returns true if shown, false otherwise
     */
    fun isShowDeleteButton(): Boolean {
        return mShowVerifyButton
    }

    /**
     * Dynamically set if the delete button should be shown
     *
     * @param showDeleteButton true if the delete button should be shown, false otherwise
     */
    fun setShowDeleteButton(showDeleteButton: Boolean) {
        mShowVerifyButton = showDeleteButton
        mCustomizationOptionsBundle?.setShowVerifyButton(showDeleteButton)
        mAdapter?.notifyDataSetChanged()
    }

    /**
     * Get the delete button pressed/focused state color
     *
     * @return color of the button
     */
    fun getDeleteButtonPressedColor(): Int {
        return mVerifyButtonPressedColor
    }

    /**
     * Set the pressed/focused state color of the delete button
     *
     * @param deleteButtonPressedColor the color of the delete button
     */
    fun setDeleteButtonPressedColor(deleteButtonPressedColor: Int) {
        mVerifyButtonPressedColor = deleteButtonPressedColor
        mCustomizationOptionsBundle?.setVerifyButtonPressesColor(deleteButtonPressedColor)
        mAdapter?.notifyDataSetChanged()
    }

    fun getCustomKeySet(): IntArray? {
        return mCustomKeySet
    }

    fun setCustomKeySet(customKeySet: IntArray?) {
        mCustomKeySet = customKeySet
        if (mAdapter != null) {
            mAdapter?.setKeyValues(customKeySet)
        }
    }

    fun enableLayoutShuffling() {
        mCustomKeySet = ShuffleArrayUtils.shuffle(DEFAULT_KEY_SET)
        if (mAdapter != null) {
            mAdapter?.setKeyValues(mCustomKeySet)
        }
    }

    private fun clearInternalPin() {
        mPin = ""
    }

    /**
     * Resets the [PinLockView], clearing the entered pin
     * and resetting the [IndicatorDots] if attached
     */
    fun resetPinLockView() {
        clearInternalPin()
        mPin?.length?.let { mAdapter?.setPinLength(it) }
        mAdapter?.itemCount?.minus(1)?.let { mAdapter?.notifyItemChanged(it) }
        if (mIndicatorDots != null) {
            mPin?.length?.let { mIndicatorDots?.updateDot(it) }
        }
    }

    /**
     * Returns true if [IndicatorDots] are attached to [PinLockView]
     *
     * @return true if attached, false otherwise
     */
    fun isIndicatorDotsAttached(): Boolean {
        return mIndicatorDots != null
    }

    /**
     * Attaches [IndicatorDots] to [PinLockView]
     *
     * @param mIndicatorDots the view to attach
     */
    fun attachIndicatorDots(mIndicatorDots: IndicatorDots?) {
        this.mIndicatorDots = mIndicatorDots
    }

    companion object {
        private val TAG = PinLockView::class.java.simpleName
        private const val DEFAULT_PIN_LENGTH = 4
        private val DEFAULT_KEY_SET: IntArray? = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0)
    }
}