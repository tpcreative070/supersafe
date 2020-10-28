package co.tpcreative.supersafe.ui.checksystem
import android.annotation.SuppressLint
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_check_system.*

fun CheckSystemAct.initUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.hide()
    presenter = CheckSystemPresenter()
    presenter?.bindView(this)
    presenter?.getIntent(this)
    onStartLoading(EnumStatus.OTHER)
    if (presenter?.googleOauth != null) {
        val email: String? = presenter!!.googleOauth?.email
        if (email == presenter?.mUser?.email) {
            presenter?.onCheckUser(presenter?.googleOauth?.email, presenter?.googleOauth?.email)
        } else {
            this.email = email
            val request = VerifyCodeRequest()
            request.new_user_id = this.email
            request.other_email = email
            request.user_id = presenter?.mUser?.email
            request._id = presenter?.mUser?._id
            presenter?.onChangeEmail(request)
        }
    } else {
        handler?.postDelayed(Runnable { presenter?.onUserCloudChecking() }, 5000)
    }
    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    progressBarCircularIndeterminate?.setBackgroundColor(ContextCompat.getColor(this,themeApp?.getAccentColor()!!))
}

@SuppressLint("ClickableViewAccessibility", "CheckResult")
fun CheckSystemAct.onVerifyInputCode(email: String?) {
    Utils.Log(TAG, " User..." + Gson().toJson(presenter?.mUser))
    try {
        val dialog = MaterialDialog(this)
        val next = "<font color='#0091EA'>($email)</font>"
        val value: String = getString(R.string.description_pin_code, next)
        dialog.title(text = getString(R.string.verify_email))
        dialog.message(text = value.toSpanned())
        dialog.input(inputType = InputType.TYPE_CLASS_NUMBER,hint = getString(R.string.pin_code),hintRes = null,allowEmpty = false) { dialog,input ->
            Utils.Log(TAG, "call input code")
            val request = VerifyCodeRequest()
            request.user_id = presenter?.mUser?.email
            request.code = input.toString().trim { it <= ' ' }
            request._id = presenter?.mUser?._id
            request.device_id = SuperSafeApplication.getInstance().getDeviceId()
            presenter?.onVerifyCode(request)
        }
        dialog.positiveButton(text = getString(R.string.ok))
        dialog.negativeButton(text = getString(R.string.cancel))
        dialog.negativeButton {
            onStopLoading(EnumStatus.OTHER)
            onBackPressed()
        }
        val editText = dialog.getInputField()
        editText.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                view?.setFocusable(true)
                view?.setFocusableInTouchMode(true)
                return false
            }
        })
        editText.setFocusable(false)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}