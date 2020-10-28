package co.tpcreative.supersafe.ui.enterpin
import android.graphics.drawable.Drawable

/**
 * The customization options for the buttons in [PinLockView]
 * passed to the [PinLockAdapter] to decorate the individual views
 *
 * Created by aritraroy on 01/06/16.
 */
class CustomizationOptionsBundle {
    private var textColor = 0
    private var textColorVerify = 0
    private var textSize = 0
    private var buttonSize = 0
    private var buttonBackgroundDrawable: Drawable? = null
    private var verifyButtonDrawable: Drawable? = null
    private var verifyButtonWidthSize = 0
    private var verifyButtonHeightSize = 0
    private var showVerifyButton = false
    private var verifyButtonPressesColor = 0
    fun getVerifyButtonNormalColor(): Int {
        return verifyButtonNormalColor
    }

    fun setVerifyButtonNormalColor(verifyButtonNormalColor: Int) {
        this.verifyButtonNormalColor = verifyButtonNormalColor
    }

    private var verifyButtonNormalColor = 0
    fun getTextColorVerify(): Int {
        return textColorVerify
    }

    fun setTextColorVerify(textColorVerify: Int) {
        this.textColorVerify = textColorVerify
    }

    fun getTextColor(): Int {
        return textColor
    }

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
    }

    fun getTextSize(): Int {
        return textSize
    }

    fun setTextSize(textSize: Int) {
        this.textSize = textSize
    }

    fun getButtonSize(): Int {
        return buttonSize
    }

    fun setButtonSize(buttonSize: Int) {
        this.buttonSize = buttonSize
    }

    fun getButtonBackgroundDrawable(): Drawable? {
        return buttonBackgroundDrawable
    }

    fun setButtonBackgroundDrawable(buttonBackgroundDrawable: Drawable?) {
        this.buttonBackgroundDrawable = buttonBackgroundDrawable
    }

    fun getVerifyButtonDrawable(): Drawable? {
        return verifyButtonDrawable
    }

    fun setVerifyButtonDrawable(verifyButtonDrawable: Drawable?) {
        this.verifyButtonDrawable = verifyButtonDrawable
    }

    fun getVerifyButtonWidthSize(): Int {
        return verifyButtonWidthSize
    }

    fun getVerifyButtonHeightSize(): Int {
        return verifyButtonHeightSize
    }

    fun setVerifyButtonWidthSize(verifyButtonWidthSize: Int) {
        this.verifyButtonWidthSize = verifyButtonWidthSize
    }

    fun setVerifyButtonHeightSize(verifyButtonHeightSize: Int) {
        this.verifyButtonHeightSize = verifyButtonHeightSize
    }

    fun isShowVerifyButton(): Boolean {
        return showVerifyButton
    }

    fun setShowVerifyButton(showVerifyButton: Boolean) {
        this.showVerifyButton = showVerifyButton
    }

    fun getVerifyButtonPressesColor(): Int {
        return verifyButtonPressesColor
    }

    fun setVerifyButtonPressesColor(verifyButtonPressesColor: Int) {
        this.verifyButtonPressesColor = verifyButtonPressesColor
    }
}