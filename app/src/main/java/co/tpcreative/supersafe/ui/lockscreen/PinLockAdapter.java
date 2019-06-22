package co.tpcreative.supersafe.ui.lockscreen;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.util.Utils;

public class PinLockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = PinLockAdapter.class.getSimpleName();
    private static final int VIEW_TYPE_NUMBER = 0;
    private static final int VIEW_TYPE_VERIFY = 1;
    private CustomizationOptionsBundle mCustomizationOptionsBundle;
    private OnNumberClickListener mOnNumberClickListener;
    private OnVerifyClickListener mOnVerifyClickListener;
    private int mPinLength;
    private int BUTTON_ANIMATION_DURATION = 150;
    private int[] mKeyValues;

    public PinLockAdapter() {
        this.mKeyValues = getAdjustKeyValues(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_NUMBER) {
            View view = inflater.inflate(R.layout.layout_number_item, parent, false);
            return new NumberViewHolder(view);
        }else {
            View view = inflater.inflate(R.layout.layout_verify_item, parent, false);
            return new VerifyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_NUMBER) {
            NumberViewHolder vh1 = (NumberViewHolder) holder;
            configureNumberButtonHolder(vh1, position);
        }else{
            VerifyViewHolder vh2 = (VerifyViewHolder) holder;
            configureDeleteButtonHolder(vh2);
        }
    }

    private void configureNumberButtonHolder(NumberViewHolder holder, int position) {
        if (holder != null) {
            if (position == 9) {
                holder.mNumberButton.setVisibility(View.GONE);
            } else {
                if(position > mKeyValues.length || position == mKeyValues.length){
                    return;
                }
                holder.mNumberButton.setText(String.valueOf(mKeyValues[position]));
                holder.mNumberButton.setVisibility(View.VISIBLE);
                holder.mNumberButton.setTag(mKeyValues[position]);
            }
            if (mCustomizationOptionsBundle != null) {
                holder.mNumberButton.setTextColor(mCustomizationOptionsBundle.getTextColor());
                if (mCustomizationOptionsBundle.getButtonBackgroundDrawable() != null) {
                    holder.mNumberButton.setBackground(
                            mCustomizationOptionsBundle.getButtonBackgroundDrawable());
                }
                holder.mNumberButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        mCustomizationOptionsBundle.getTextSize());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        mCustomizationOptionsBundle.getButtonSize(),
                        mCustomizationOptionsBundle.getButtonSize());
                holder.mNumberButton.setTextSize(40);
                holder.mNumberButton.setLayoutParams(params);
            }
        }
    }

    private void configureDeleteButtonHolder(VerifyViewHolder holder) {
        if (holder != null) {
            if (mCustomizationOptionsBundle.isShowVerifyButton() && mPinLength > 0) {
                holder.mButtonImage.setVisibility(View.VISIBLE);
                if (mCustomizationOptionsBundle.getVerifyButtonDrawable() != null) {
                    holder.mButtonImage.setImageDrawable(mCustomizationOptionsBundle.getVerifyButtonDrawable());
                }
                Utils.Log(TAG, "onVerify changed color");
                holder.mButtonImage.setColorFilter(mCustomizationOptionsBundle.getTextColorVerify(), PorterDuff.Mode.SRC_ATOP);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        mCustomizationOptionsBundle.getVerifyButtonWidthSize(),
                        mCustomizationOptionsBundle.getVerifyButtonHeightSize());
                holder.mButtonImage.setLayoutParams(params);
            } else {
                holder.mButtonImage.setColorFilter(mCustomizationOptionsBundle.getVerifyButtonNormalColor(), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mKeyValues.length + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mKeyValues.length) {
            return VIEW_TYPE_VERIFY;
        }
        return super.getItemViewType(position);
    }

    public void setPinLength(int pinLength) {
        this.mPinLength = pinLength;
    }

    public void setKeyValues(int[] keyValues) {
        this.mKeyValues = getAdjustKeyValues(keyValues);
        notifyDataSetChanged();
    }

    private int[] getAdjustKeyValues(int[] keyValues) {
        int[] adjustedKeyValues = new int[keyValues.length + 1];
        for (int i = 0; i < keyValues.length; i++) {
            if (i < 9) {
                adjustedKeyValues[i] = keyValues[i];
            } else {
                adjustedKeyValues[i] = -1;
                adjustedKeyValues[i + 1] = keyValues[i];
            }
        }
        return adjustedKeyValues;
    }

    public void setOnItemClickListener(OnNumberClickListener onNumberClickListener) {
        this.mOnNumberClickListener = onNumberClickListener;
    }

    public void setOnVerifyClickListener(OnVerifyClickListener onVerifyClickListener) {
        this.mOnVerifyClickListener = onVerifyClickListener;
    }

    public void setCustomizationOptions(CustomizationOptionsBundle customizationOptionsBundle) {
        this.mCustomizationOptionsBundle = customizationOptionsBundle;
    }

    public interface OnNumberClickListener {
        void onNumberClicked(int keyValue);
    }

    public interface OnVerifyClickListener {
        void onVerifyClicked();
    }

    public class NumberViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.buttonNumber)
        Button mNumberButton;
        public NumberViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
        @OnClick(R.id.buttonNumber)
        public void onNumberButton(View view){
            if (mOnNumberClickListener != null) {
                mOnNumberClickListener.onNumberClicked((Integer) view.getTag());
                mNumberButton.startAnimation(scale());
            }
        }
    }

    public class VerifyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.buttonVerify)
        LinearLayout mVerifyButton;
        @BindView(R.id.buttonImage)
        ImageView mButtonImage;
        public VerifyViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
        @OnClick(R.id.buttonVerify)
        public void onVerifyButton(){
            if (mCustomizationOptionsBundle.isShowVerifyButton() && mPinLength > 0) {
                if (mOnVerifyClickListener != null) {
                    mOnVerifyClickListener.onVerifyClicked();
                    mVerifyButton.startAnimation(scale());
                    Utils.Log(TAG, "Verified button");
                } else {
                    Utils.Log(TAG, "mOnVerifyClickListener Null");
                }
            } else {
                Utils.Log(TAG, "Pin length Null");
            }
        }
    }

    private Animation scale() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(.75F, 1f, .75F, 1f,
                Animation.RELATIVE_TO_SELF, .5F, Animation.RELATIVE_TO_SELF, .5F);
        scaleAnimation.setDuration(BUTTON_ANIMATION_DURATION);
        scaleAnimation.setFillAfter(true);
        return scaleAnimation;
    }
}
