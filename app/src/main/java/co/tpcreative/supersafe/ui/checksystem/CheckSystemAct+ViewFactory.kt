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
import com.afollestad.materialdialogs.Theme
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

@SuppressLint("ClickableViewAccessibility")
fun CheckSystemAct.onVerifyInputCode(email: String?) {
    Utils.Log(TAG, " User..." + Gson().toJson(presenter?.mUser))
    try {
        val dialog = MaterialDialog.Builder(this)
        val next = "<font color='#0091EA'>($email)</font>"
        val value: String = getString(R.string.description_pin_code, next)
        dialog.title(getString(R.string.verify_email))
        dialog.content(value.toSpanned())
        dialog.theme(Theme.LIGHT)
        dialog.inputType(InputType.TYPE_CLASS_NUMBER)
        dialog.input(getString(R.string.pin_code), null, false, object : MaterialDialog.InputCallback {
            override fun onInput(dialog: MaterialDialog, input: CharSequence?) {
                Utils.Log(TAG, "call input code")
                val request = VerifyCodeRequest()
                request.user_id = presenter?.mUser?.email
                request.code = input.toString().trim { it <= ' ' }
                request._id = presenter?.mUser?._id
                request.device_id = SuperSafeApplication.getInstance().getDeviceId()
                presenter?.onVerifyCode(request)
            }
        })
        dialog.positiveText(getString(R.string.ok))
        dialog.negativeText(getString(R.string.cancel))
        dialog.onNegative { dialog, which ->
            onStopLoading(EnumStatus.OTHER)
            onBackPressed()
        }
        val editText = dialog.show().inputEditText
        editText?.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                view?.setFocusable(true)
                view?.setFocusableInTouchMode(true)
                return false
            }
        })
        editText?.setFocusable(false)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}