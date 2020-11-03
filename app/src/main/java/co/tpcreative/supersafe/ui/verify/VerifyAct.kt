package co.tpcreative.supersafe.ui.verify
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumStatus
import kotlinx.android.synthetic.main.activity_verify.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class VerifyAct : BaseActivityNoneSlide(), BaseView<EmptyModel>, TextView.OnEditorActionListener {
    var isNext = false
    var presenter: VerifyPresenter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)
        initUI()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
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
        presenter?.unbindView()
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}

    override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.showDialog(this@VerifyAct, getString(R.string.internet))
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

    /*Detecting textWatch*/
    val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = s.toString().trim { it <= ' ' }
            isNext = if (Utils.isValid(value)) {
                btnLogin?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded)
                btnLogin?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                true
            } else {
                btnLogin?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded)
                btnLogin?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.colorDisableText))
                false
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun onStartLoading(status: EnumStatus) {
        when (status) {
            EnumStatus.RESEND_CODE -> {
                progressBarCircularIndeterminateReSend?.visibility = View.VISIBLE
                btnReSend?.visibility = View.INVISIBLE
            }
            EnumStatus.VERIFY_CODE -> {
                progressBarCircularIndeterminate?.visibility = View.VISIBLE
                btnLogin?.visibility = View.INVISIBLE
            }
        }
    }

    override fun onStopLoading(status: EnumStatus) {
        when (status) {
            EnumStatus.RESEND_CODE -> {
                progressBarCircularIndeterminateReSend?.visibility = View.INVISIBLE
                btnReSend.visibility = View.VISIBLE
            }
            EnumStatus.VERIFY_CODE -> {
                progressBarCircularIndeterminate?.visibility = View.INVISIBLE
                btnLogin?.visibility = View.VISIBLE
            }
        }
    }

    override fun getContext(): Context? {
        return this
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onError(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.VERIFY_CODE -> {
                edtCode?.error = message
            }
        }
    }

    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.VERIFY_CODE -> {
                Navigator.onMoveSetPin(this, EnumPinAction.NONE)
                finish()
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
}