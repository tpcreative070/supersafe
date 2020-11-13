package co.tpcreative.supersafe.ui.verifyaccount
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import kotlinx.android.synthetic.main.activity_verify_account.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class VerifyAccountAct : BaseActivity(), TextView.OnEditorActionListener, BaseView<EmptyModel> {
    var isNext = false
    var presenter: VerifyAccountPresenter? = null
    var isBack = true
    var isSync = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_account)
        initUI()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        isSync = true
        onRegisterHomeWatcher()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter?.unbindView()
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onEditorAction(textView: TextView?, i: Int, keyEvent: KeyEvent?): Boolean {
        if (i == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.showDialog(this, getString(R.string.internet))
                return false
            }
            if (isNext) {
                Utils.Log(TAG, "Next")
                if (currentFocus === edtCode) {
                    onVerifyCode()
                }
                if (currentFocus === edtEmail) {
                    Utils.hideSoftKeyboard(this)
                    onChangedEmail()
                }
                return true
            }
            return false
        }
        return false
    }

    /*Detecting textWatch*/
    val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = s.toString().trim { it <= ' ' }
            if (currentFocus === edtEmail) {
                isNext = if (Utils.isValidEmail(value)) {
                    btnSave?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded)
                    btnSave?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                    true
                } else {
                    btnSave?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded)
                    btnSave?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.colorDisableText))
                    false
                }
            } else if (currentFocus === edtCode) {
                Utils.Log(TAG, "code")
                isNext = if (Utils.isValid(value)) {
                    btnSignIn?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded)
                    btnSignIn?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                    true
                } else {
                    btnSignIn?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded)
                    btnSignIn?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.colorDisableText))
                    false
                }
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                Utils.Log(TAG, "home")
                if (isBack) {
                    finish()
                    return true
                }
                Utils.hideSoftKeyboard(this)
                isBack = true
                onShowView(llAction!!)
                return false
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        Utils.Log(TAG, "home")
        if (isBack) {
            super.onBackPressed()
        }
        Utils.hideSoftKeyboard(this)
        isBack = true
        onShowView(llAction!!)
    }

    fun onShowView(view: View) {
        when (view.id) {
            R.id.imgEdit -> {
                llAction?.visibility = View.GONE
                llVerifyCode?.visibility = View.GONE
                llChangeEmail?.visibility = View.VISIBLE
                edtCode?.setText("")
                isBack = false
                isNext = false
            }
            R.id.btnSendVerifyCode -> {
                llAction?.visibility = View.GONE
                llChangeEmail?.visibility = View.GONE
                llVerifyCode?.visibility = View.VISIBLE
                edtEmail?.setText("")
                edtCode?.setText("")
                isBack = false
                isNext = false
            }
            R.id.llAction -> {
                llAction?.visibility = View.VISIBLE
                llChangeEmail?.visibility = View.GONE
                llVerifyCode?.visibility = View.GONE
                edtEmail?.setText("")
                edtCode?.setText("")
                isBack = true
                isNext = false
            }
        }
    }

    fun onShowDialog() {
        val theme: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        MaterialStyledDialog.Builder(this)
                .setTitle(R.string.signin_with_google)
                .setDescription(R.string.choose_google_account)
                .setHeaderDrawable(R.drawable.ic_google_transparent_margin_60)
                .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
                .setHeaderColor(theme?.getPrimaryColor()!!)
                .setCancelable(true)
                .setPositiveText(R.string.ok)
                .setNegativeText(R.string.cancel)
                .setCheckBox(true, R.string.enable_cloud) { compoundButton, b ->
                    Utils.Log(TAG, "checked :$b")
                    isSync = b
                }
                .onPositive {
                    Utils.Log(TAG, "positive")
                    ServiceManager.getInstance()?.onPickUpNewEmail(this@VerifyAccountAct)
                }
                .show()
    }

    fun onShowDialogEnableSync() {
        val theme: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        MaterialStyledDialog.Builder(this)
                .setTitle(R.string.enable_cloud_sync)
                .setDescription(R.string.message_prompt)
                .setHeaderDrawable(R.drawable.ic_drive_cloud)
                .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
                .setHeaderColor(theme?.getPrimaryColor()!!)
                .setCancelable(true)
                .setPositiveText(R.string.enable_now)
                .setNegativeText(R.string.cancel)
                .setCheckBox(false, R.string.enable_cloud)
                .onPositive {
                    Utils.Log(TAG, "positive")
                    Navigator.onCheckSystem(this@VerifyAccountAct, null)
                }
                .onNegative {  finish() }
                .show()
    }

    override fun getContext(): Context? {
        return applicationContext
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Navigator.ENABLE_CLOUD -> if (resultCode == Activity.RESULT_OK) {
                finish()
            }
            Navigator.REQUEST_CODE_EMAIL -> if (resultCode == Activity.RESULT_OK) {
                val accountName: String? = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                Utils.Log(TAG, "accountName : $accountName")
                val googleOauth = GoogleOauth()
                googleOauth.email = accountName
                googleOauth.isEnableSync = isSync
                Navigator.onCheckSystem(this@VerifyAccountAct, googleOauth)
            }
            else -> Utils.Log(TAG, "Nothing action")
        }
    }

    override fun onStartLoading(status: EnumStatus) {
        when (status) {
            EnumStatus.RESEND_CODE -> {
            }
            EnumStatus.VERIFY_CODE -> {
                progressBarCircularIndeterminateVerifyCode?.visibility = View.VISIBLE
                btnSendVerifyCode?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded)
                btnSendVerifyCode?.text = ""
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    override fun onStopLoading(status: EnumStatus) {
        when (status) {
            EnumStatus.RESEND_CODE -> {
            }
            EnumStatus.VERIFY_CODE -> {
                progressBarCircularIndeterminateVerifyCode?.visibility = View.INVISIBLE
                btnSendVerifyCode?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded)
                btnSendVerifyCode?.text = getString(R.string.send_verification_code)
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    override fun onError(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.CHANGE_EMAIL -> {
                edtEmail?.error = message
            }
            EnumStatus.VERIFY_CODE -> {
                edtCode?.error = message
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.CHANGE_EMAIL -> {
                onShowView(llAction!!)
            }
            EnumStatus.SEND_EMAIL -> {
                onShowView(btnSendVerifyCode!!)
                SingletonManagerProcessing.getInstance()?.onStopProgressing(this@VerifyAccountAct)
                Utils.onBasicAlertNotify(this,getString(R.string.key_alert),getString(R.string.we_sent_access_code_to_your_email))
            }
            EnumStatus.VERIFY_CODE -> {
                edtCode?.setText("")
                onShowDialogEnableSync()
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
}