package co.tpcreative.supersafe.ui.lockscreen;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration;

/**
 * Represents a numeric lock view which can used to taken numbers as input.
 * The length of the input can be customized using {@link PinLockView#setPinLength(int)}, the default value being 4
 * <p/>
 * It can also be used as dial pad for taking number inputs.
 * Optionally, {@link IndicatorDots} can be attached to this view to indicate the length of the input taken
 * Created by aritraroy on 31/05/16.
 */
public class PinLockView extends RecyclerView {
    private static final String TAG = PinLockView.class.getSimpleName();
    private static final int DEFAULT_PIN_LENGTH = 4;
    private static final int[] DEFAULT_KEY_SET = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
    private String mPin = "";
    private int mPinLength;
    private int mHorizontalSpacing, mVerticalSpacing;
    private int mTextColor, mVerifyButtonPressedColor;
    private int mTextColorNormal, mVerifyButtonNormalColor;
    private int mTextSize, mButtonSize, mVerifyButtonWidthSize, mVerifyButtonHeightSize;
    private Drawable mButtonBackgroundDrawable;
    private Drawable mVerifyButtonDrawable;
    private boolean mShowVerifyButton;
    private IndicatorDots mIndicatorDots;
    private PinLockAdapter mAdapter;
    private PinLockListener mPinLockListener;
    private CustomizationOptionsBundle mCustomizationOptionsBundle;
    private int[] mCustomKeySet;
    private PinLockAdapter.OnNumberClickListener mOnNumberClickListener
            = new PinLockAdapter.OnNumberClickListener() {

        @Override
        public void onNumberClicked(int keyValue) {
            if (mPin.length() < getPinLength()) {
                mPin = mPin.concat(String.valueOf(keyValue));
                if (isIndicatorDotsAttached()) {
                    mIndicatorDots.updateDot(mPin.length());
                }
                if (mPin.length() == 1) {
                    mAdapter.setPinLength(mPin.length());
                    mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
                }
                if (mPinLockListener != null) {
                    if (mPin.length() == mPinLength) {
                        mPinLockListener.onComplete(mPin);
                    } else {
                        mPinLockListener.onPinChange(mPin.length(), mPin);
                    }
                }
                else{
                    Utils.Log(TAG,"mPinLockListener is null");
                }
            } else {
                if (!isShowDeleteButton()) {
                    resetPinLockView();
                    mPin = mPin.concat(String.valueOf(keyValue));
                    if (isIndicatorDotsAttached()) {
                        mIndicatorDots.updateDot(mPin.length());
                    }
                    if (mPinLockListener != null) {
                        mPinLockListener.onPinChange(mPin.length(), mPin);
                    }
                } else {
                    if (mPinLockListener != null) {
                        mPinLockListener.onComplete(mPin);
                    }
                    else{
                        Utils.Log(TAG,"mPinLockListener is null");
                    }
                }
            }
        }
    };

    /*Delete on item clicked*/

    public void onDeleteClicked() {
        if (mPin.length() > 0) {
            mPin = mPin.substring(0, mPin.length() - 1);
            if (isIndicatorDotsAttached()) {
                mIndicatorDots.updateDot(mPin.length());
            }
            if (mPin.length() == 0) {
                mAdapter.setPinLength(mPin.length());
                mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
            }
            if (mPinLockListener != null) {
                if (mPin.length() == 0) {
                    mPinLockListener.onEmpty();
                    clearInternalPin();
                } else {
                    mPinLockListener.onPinChange(mPin.length(), mPin);
                }
            }
        } else {
            if (mPinLockListener != null) {
                mPinLockListener.onEmpty();
            }
        }
    }

    /*Verify on item clicked*/
    private PinLockAdapter.OnVerifyClickListener mOnVerifyClickListener
            = new PinLockAdapter.OnVerifyClickListener() {
        @Override
        public void onVerifyClicked() {
            Log.d(TAG, "onVerify");
            if (mPinLockListener != null) {
                mPinLockListener.onComplete(mPin);
            }
            else {
                Utils.Log(TAG,"mPinLockListener is null");
            }
        }
    };

    public PinLockView(Context context) {
        super(context);
        init(null, 0);
    }

    public PinLockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PinLockView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attributeSet, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.PinLockView);
        try {
            mPinLength = typedArray.getInt(R.styleable.PinLockView_pinLength, DEFAULT_PIN_LENGTH);
            mHorizontalSpacing = (int) typedArray.getDimension(R.styleable.PinLockView_keypadHorizontalSpacing, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_horizontal_spacing));
            mVerticalSpacing = (int) typedArray.getDimension(R.styleable.PinLockView_keypadVerticalSpacing, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_vertical_spacing));
            mTextColor = typedArray.getColor(R.styleable.PinLockView_keypadTextColor, ResourceUtils.getColor(getContext(), R.color.text_numberpressed));
            mTextSize = (int) typedArray.getDimension(R.styleable.PinLockView_keypadTextSize, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_text_size));
            mButtonSize = (int) typedArray.getDimension(R.styleable.PinLockView_keypadButtonSize, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_button_size));
            mVerifyButtonWidthSize = (int) typedArray.getDimension(R.styleable.PinLockView_keypadVerifyButtonSize, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_verify_button_size_height));
            mVerifyButtonHeightSize = (int) typedArray.getDimension(R.styleable.PinLockView_keypadVerifyButtonSize, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_verify_button_size_height));
            mButtonBackgroundDrawable = typedArray.getDrawable(R.styleable.PinLockView_keypadButtonBackgroundDrawable);
            mVerifyButtonDrawable = typedArray.getDrawable(R.styleable.PinLockView_keypadVerifyButtonDrawable);
            mShowVerifyButton = typedArray.getBoolean(R.styleable.PinLockView_keypadShowVerifyButton, true);
            mVerifyButtonPressedColor = typedArray.getColor(R.styleable.PinLockView_keypadVerifyButtonPressedColor, ResourceUtils.getColor(getContext(), R.color.teal_a700));
            mTextColorNormal = typedArray.getColor(R.styleable.PinLockView_keypadVerifyButtonPressedColor, ResourceUtils.getColor(getContext(), R.color.material_gray_400));
        } finally {
            typedArray.recycle();
        }
        mCustomizationOptionsBundle = new CustomizationOptionsBundle();
        mCustomizationOptionsBundle.setTextColor(mTextColor);
        mCustomizationOptionsBundle.setTextSize(mTextSize);
        mCustomizationOptionsBundle.setButtonSize(mButtonSize);
        mCustomizationOptionsBundle.setButtonBackgroundDrawable(mButtonBackgroundDrawable);
        mCustomizationOptionsBundle.setVerifyButtonDrawable(mVerifyButtonDrawable);
        mCustomizationOptionsBundle.setVerifyButtonWidthSize(mVerifyButtonWidthSize);
        mCustomizationOptionsBundle.setVerifyButtonHeightSize(mVerifyButtonHeightSize);
        mCustomizationOptionsBundle.setShowVerifyButton(mShowVerifyButton);
        mCustomizationOptionsBundle.setVerifyButtonPressesColor(mVerifyButtonPressedColor);
        mCustomizationOptionsBundle.setTextColorVerify(mVerifyButtonPressedColor);
        mCustomizationOptionsBundle.setVerifyButtonNormalColor(mTextColorNormal);
        initView();
    }

    private void initView() {
        mAdapter = new PinLockAdapter();
        mAdapter.setOnItemClickListener(mOnNumberClickListener);
        mAdapter.setOnVerifyClickListener(mOnVerifyClickListener);
        mAdapter.setCustomizationOptions(mCustomizationOptionsBundle);
        addItemDecoration(new ItemSpaceDecoration(0, mVerticalSpacing, 3, false));
        setLayoutManager(new GridLayoutManager(getContext(), 3));
        addItemDecoration(new GridSpacingItemDecoration(3, 4, true));
        setItemAnimator(new DefaultItemAnimator());
        setAdapter(mAdapter);
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    /**
     * Sets a {@link PinLockListener} to the to listen to pin update events
     *
     * @param pinLockListener the listener
     */
    public void setPinLockListener(PinLockListener pinLockListener) {
        this.mPinLockListener = pinLockListener;
    }

    /**
     * Get the length of the current pin length
     *
     * @return the length of the pin
     */
    public int getPinLength() {
        return mPinLength;
    }

    /**
     * Sets the pin length dynamically
     *
     * @param pinLength the pin length
     */
    public void setPinLength(int pinLength) {
        this.mPinLength = pinLength;

        if (isIndicatorDotsAttached()) {
            mIndicatorDots.setPinLength(pinLength);
        }
    }

    /**
     * Get the text color in the buttons
     *
     * @return the text color
     */
    public int getTextColor() {
        return mTextColor;
    }

    /**
     * Set the text color of the buttons dynamically
     *
     * @param textColor the text color
     */
    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        mCustomizationOptionsBundle.setTextColor(textColor);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Get the size of the text in the buttons
     *
     * @return the size of the text in pixels
     */
    public int getTextSize() {
        return mTextSize;
    }

    /**
     * Set the size of text in pixels
     *
     * @param textSize the text size in pixels
     */
    public void setTextSize(int textSize) {
        this.mTextSize = textSize;
        mCustomizationOptionsBundle.setTextSize(textSize);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Get the size of the pin buttons
     *
     * @return the size of the button in pixels
     */
    public int getButtonSize() {
        return mButtonSize;
    }

    /**
     * Set the size of the pin buttons dynamically
     *
     * @param buttonSize the button size
     */
    public void setButtonSize(int buttonSize) {
        this.mButtonSize = buttonSize;
        mCustomizationOptionsBundle.setButtonSize(buttonSize);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Get the current background drawable of the buttons, can be null
     *
     * @return the background drawable
     */
    public Drawable getButtonBackgroundDrawable() {
        return mButtonBackgroundDrawable;
    }

    /**
     * Set the background drawable of the buttons dynamically
     *
     * @param buttonBackgroundDrawable the background drawable
     */
    public void setButtonBackgroundDrawable(Drawable buttonBackgroundDrawable) {
        this.mButtonBackgroundDrawable = buttonBackgroundDrawable;
        mCustomizationOptionsBundle.setButtonBackgroundDrawable(buttonBackgroundDrawable);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Get the drawable of the delete button
     *
     * @return the delete button drawable
     */
    public Drawable getDeleteButtonDrawable() {
        return mVerifyButtonDrawable;
    }

    /**
     * Set the drawable of the delete button dynamically
     *
     * @param deleteBackgroundDrawable the delete button drawable
     */
    public void setDeleteButtonDrawable(Drawable deleteBackgroundDrawable) {
        this.mVerifyButtonDrawable = deleteBackgroundDrawable;
        mCustomizationOptionsBundle.setVerifyButtonDrawable(deleteBackgroundDrawable);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Get the delete button width size in pixels
     *
     * @return size in pixels
     */
    public int getDeleteButtonWidthSize() {
        return mVerifyButtonWidthSize;
    }

    /**
     * Get the delete button size height in pixels
     *
     * @return size in pixels
     */
    public int getDeleteButtonHeightSize() {
        return mVerifyButtonHeightSize;
    }

    /**
     * Set the size of the delete button width in pixels
     *
     * @param deleteButtonWidthSize size in pixels
     */
    public void setDeleteButtonWidthSize(int deleteButtonWidthSize) {
        this.mVerifyButtonWidthSize = deleteButtonWidthSize;
        mCustomizationOptionsBundle.setVerifyButtonWidthSize(deleteButtonWidthSize);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Set the size of the delete button height in pixels
     *
     * @param deleteButtonHeightSize size in pixels
     */
    public void setDeleteButtonHeightSize(int deleteButtonHeightSize) {
        this.mVerifyButtonHeightSize = deleteButtonHeightSize;
        mCustomizationOptionsBundle.setVerifyButtonWidthSize(deleteButtonHeightSize);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Is the delete button shown
     *
     * @return returns true if shown, false otherwise
     */
    public boolean isShowDeleteButton() {
        return mShowVerifyButton;
    }

    /**
     * Dynamically set if the delete button should be shown
     *
     * @param showDeleteButton true if the delete button should be shown, false otherwise
     */
    public void setShowDeleteButton(boolean showDeleteButton) {
        this.mShowVerifyButton = showDeleteButton;
        mCustomizationOptionsBundle.setShowVerifyButton(showDeleteButton);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Get the delete button pressed/focused state color
     *
     * @return color of the button
     */
    public int getDeleteButtonPressedColor() {
        return mVerifyButtonPressedColor;
    }

    /**
     * Set the pressed/focused state color of the delete button
     *
     * @param deleteButtonPressedColor the color of the delete button
     */
    public void setDeleteButtonPressedColor(int deleteButtonPressedColor) {
        this.mVerifyButtonPressedColor = deleteButtonPressedColor;
        mCustomizationOptionsBundle.setVerifyButtonPressesColor(deleteButtonPressedColor);
        mAdapter.notifyDataSetChanged();
    }

    public int[] getCustomKeySet() {
        return mCustomKeySet;
    }

    public void setCustomKeySet(int[] customKeySet) {
        this.mCustomKeySet = customKeySet;
        if (mAdapter != null) {
            mAdapter.setKeyValues(customKeySet);
        }
    }

    public void enableLayoutShuffling() {
        this.mCustomKeySet = ShuffleArrayUtils.shuffle(DEFAULT_KEY_SET);
        if (mAdapter != null) {
            mAdapter.setKeyValues(mCustomKeySet);
        }
    }

    private void clearInternalPin() {
        mPin = "";
    }

    /**
     * Resets the {@link PinLockView}, clearing the entered pin
     * and resetting the {@link IndicatorDots} if attached
     */
    public void resetPinLockView() {
        clearInternalPin();
        mAdapter.setPinLength(mPin.length());
        mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
        if (mIndicatorDots != null) {
            mIndicatorDots.updateDot(mPin.length());
        }
    }

    /**
     * Returns true if {@link IndicatorDots} are attached to {@link PinLockView}
     *
     * @return true if attached, false otherwise
     */
    public boolean isIndicatorDotsAttached() {
        return mIndicatorDots != null;
    }

    /**
     * Attaches {@link IndicatorDots} to {@link PinLockView}
     *
     * @param mIndicatorDots the view to attach
     */
    public void attachIndicatorDots(IndicatorDots mIndicatorDots) {
        this.mIndicatorDots = mIndicatorDots;
    }
}
