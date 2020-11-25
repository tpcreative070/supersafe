package co.tpcreative.supersafe.ui.resetpin
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseVerifyPinActivity
import co.tpcreative.supersafe.common.controller.SingletonResetPin
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.ResetPinViewModel
import kotlinx.android.synthetic.main.activity_reset_pin.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ResetPinAct : BaseVerifyPinActivity(),TextView.OnEditorActionListener {
    var isNext = false
    var isRestoreFiles: Boolean? = null
    lateinit var viewModel : ResetPinViewModel
    var progressing : EnumStepProgressing = EnumStepProgressing.NONE
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
                runOnUiThread(Runnable { edtCode?.hint = kotlin.String.format(getString(R.string.waiting_left), SingletonResetPin.getInstance()?.waitingLeft.toString() + "") })
            }
            EnumStatus.WAITING_DONE -> {
                runOnUiThread(Runnable {
                    edtCode?.hint = getString(R.string.code)
                    btnReset.isEnabled = true
                    btnSendRequest.isEnabled = true
                })
            }
            else -> Utils.Log(TAG,"Nothing")
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
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    val mTextWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.code = s.toString()
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
                verifyCode()
                return true
            }
            return false
        }
        return false
    }

    override fun onBiometricSuccessful() {
    }
}