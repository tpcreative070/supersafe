package co.tpcreative.supersafe.ui.resetpin
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseVerifyPinActivity
import co.tpcreative.supersafe.common.controller.SingletonResetPin
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_reset_pin.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ResetPinAct : BaseVerifyPinActivity(), BaseView<EmptyModel>, TextView.OnEditorActionListener {
    var presenter: ResetPinPresenter? = null
    var isNext = false
    var isRestoreFiles: Boolean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_pin)
        initUI()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.WAITING_LEFT -> {
                runOnUiThread(Runnable { edtCode?.setHint(kotlin.String.format(getString(R.string.waiting_left), SingletonResetPin.getInstance()?.waitingLeft.toString() + "")) })
            }
            EnumStatus.WAITING_DONE -> {
                runOnUiThread(Runnable { edtCode?.setHint(getString(R.string.code)) })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter?.unbindView()
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = s.toString().trim { it <= ' ' }
            isNext = if (Utils.isValid(value)) {
                btnReset?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded)
                btnReset?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                btnReset?.isEnabled = true
                true
            } else {
                btnReset?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded)
                btnReset?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.colorDisableText))
                btnReset?.isEnabled = false
                false
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.showDialog(this, getString(R.string.internet))
                return false
            }
            if (isNext) {
                onVerifyCode()
                return true
            }
            return false
        }
        return false
    }

    override fun onStartLoading(status: EnumStatus) {
        setProgressValue()
    }

    override fun onStopLoading(status: EnumStatus) {
        Utils.Log(TAG, "Stop progressing")
        if (progressbar_circular != null) {
            progressbar_circular?.progressiveStop()
        }
    }

    override fun getContext(): Context? {
        return applicationContext
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onError(message: String?, status: EnumStatus?) {
        onStopLoading(EnumStatus.OTHER)
        when (status) {
            EnumStatus.REQUEST_CODE -> {
                btnSendRequest?.setText(getString(R.string.send_verification_code))
                btnSendRequest?.isEnabled = true
                Utils.showGotItSnackbar(tvStep1!!, R.string.request_code_occurred_error)
            }
            EnumStatus.VERIFY -> {
                Utils.showGotItSnackbar(tvStep1!!, R.string.verify_occurred_error)
            }
        }
    }

    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.REQUEST_CODE -> {
                val mUser: User? = Utils.getUserInfo()
                Utils.Log(TAG, Gson().toJson(mUser))
                btnSendRequest?.text = getString(R.string.send_verification_code)
                btnSendRequest?.isEnabled = false
                onStopLoading(EnumStatus.OTHER)
                onShowDialogWaitingCode()
            }
            EnumStatus.VERIFY -> {
                if (isRestoreFiles!!) {
                    Navigator.onMoveToResetPin(this, EnumPinAction.RESTORE)
                } else {
                    Navigator.onMoveToResetPin(this, EnumPinAction.NONE)
                }
            }
        }
    }

    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
}