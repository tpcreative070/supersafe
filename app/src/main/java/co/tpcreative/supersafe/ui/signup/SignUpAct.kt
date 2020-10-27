package co.tpcreative.supersafe.ui.signup
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
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SignUpAct : BaseActivityNoneSlide(), TextView.OnEditorActionListener, BaseView<User> {
    var isEmail = false
    var isName = false
    var presenter: SignUpPresenter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        initUI()
        Utils.Log(TAG, "onCreate")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
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
        ServiceManager.getInstance()?.onStartService()
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}
    override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.showDialog(this@SignUpAct, getString(R.string.internet))
                return false
            }
            if (isEmail && isName) {
                Utils.Log(TAG, "Next")
                Utils.hideSoftKeyboard(this)
                onSignUp()
                return true
            }
            return false
        } else if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.showDialog(this@SignUpAct, getString(R.string.internet))
                return false
            }
            if (isEmail && isName) {
                Utils.hideSoftKeyboard(this)
                onSignUp()
                return true
            }
            return false
        }
        return false
    }

    /*Detecting textWatch*/
    val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (getCurrentFocus() === edtName) {
                isName = Utils.isValid(s)
            }
            if (currentFocus === edtEmail) {
                isEmail = Utils.isValidEmail(s)
            }
            if (isEmail && isName) {
                btnFinish?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded)
                btnFinish?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                btnFinish?.isEnabled = true
            } else {
                btnFinish?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded)
                btnFinish?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.colorDisableText))
                btnFinish?.isEnabled = false
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun getContext(): Context? {
        return applicationContext
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onStartLoading(status: EnumStatus) {
        progressBarCircularIndeterminate?.visibility = View.VISIBLE
        btnFinish?.visibility = View.INVISIBLE
    }

    override fun onStopLoading(status: EnumStatus) {
        progressBarCircularIndeterminate?.visibility = View.INVISIBLE
        btnFinish?.visibility = View.VISIBLE
    }

    override fun onError(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.SIGN_UP -> {
                edtEmail?.error = message
            }
        }
    }

    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: User?) {
        when (status) {
            EnumStatus.SIGN_UP -> {
                Navigator.onMoveToMainTab(this)
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<User>?) {}
}