package co.tpcreative.suppersafe.ui.lockscreen;

import android.graphics.drawable.Drawable;

/**
 * The customization options for the buttons in {@link PinLockView}
 * passed to the {@link PinLockAdapter} to decorate the individual views
 *
 * Created by aritraroy on 01/06/16.
 */
public class CustomizationOptionsBundle {

    private int textColor;
    private int textColorVerify;
    private int textSize;
    private int buttonSize;
    private Drawable buttonBackgroundDrawable;
    private Drawable verifyButtonDrawable;
    private int verifyButtonWidthSize, verifyButtonHeightSize;
    private boolean showVerifyButton;
    private int verifyButtonPressesColor;

    public CustomizationOptionsBundle() {
    }

    public int getTextColorVerify() {
        return textColorVerify;
    }

    public void setTextColorVerify(int textColorVerify) {
        this.textColorVerify = textColorVerify;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getButtonSize() {
        return buttonSize;
    }

    public void setButtonSize(int buttonSize) {
        this.buttonSize = buttonSize;
    }

    public Drawable getButtonBackgroundDrawable() {
        return buttonBackgroundDrawable;
    }

    public void setButtonBackgroundDrawable(Drawable buttonBackgroundDrawable) {
        this.buttonBackgroundDrawable = buttonBackgroundDrawable;
    }

    public Drawable getVerifyButtonDrawable() {
        return verifyButtonDrawable;
    }

    public void setVerifyButtonDrawable(Drawable verifyButtonDrawable) {
        this.verifyButtonDrawable = verifyButtonDrawable;
    }

    public int getVerifyButtonWidthSize() {
        return verifyButtonWidthSize;
    }

    public int getVerifyButtonHeightSize() {
        return verifyButtonHeightSize;
    }

    public void setVerifyButtonWidthSize(int verifyButtonWidthSize) {
        this.verifyButtonWidthSize = verifyButtonWidthSize;
    }

    public void setVerifyButtonHeightSize(int verifyButtonHeightSize) {
        this.verifyButtonHeightSize = verifyButtonHeightSize;
    }

    public boolean isShowVerifyButton() {
        return showVerifyButton;
    }

    public void setShowVerifyButton(boolean showVerifyButton) {
        this.showVerifyButton = showVerifyButton;
    }

    public int getVerifyButtonPressesColor() {
        return verifyButtonPressesColor;
    }

    public void setVerifyButtonPressesColor(int verifyButtonPressesColor) {
        this.verifyButtonPressesColor = verifyButtonPressesColor;
    }
}
