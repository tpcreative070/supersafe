package co.tpcreative.supersafe.ui.signin
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
import co.tpcreative.supersafe.common.request.SignInRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmailToken
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import com.snatik.storage.security.SecurityUtil
import kotlinx.android.synthetic.main.activity_signin.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SignInAct : BaseActivityNoneSlide(), TextView.OnEditorActionListener, BaseView<User> {
    var isNext = false
    var presenter: SignInPresenter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        initUI()
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
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}
    override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SuperSafeReceiver.Companion.isConnected()) {
                Utils.showDialog(this@SignInAct, getString(R.string.internet))
                return false
            }
            if (isNext) {
                onSignIn()
                return true
            }
            return false
        }
        return false
    }

    /*Detecting textWatch*/
    val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            isNext = if (Utils.isValidEmail(s)) {
                btnNext?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded)
                btnNext?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                true
            } else {
                btnNext?.background = ContextCompat.getDrawable(this@SignInAct,R.drawable.bg_button_disable_rounded)
                btnNext.setTextColor(ContextCompat.getColor(this@SignInAct,R.color.colorDisableText))
                false
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun getContext(): Context? {
        return this
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onStartLoading(status: EnumStatus) {
        progressBarCircularIndeterminate?.visibility = View.VISIBLE
        btnNext?.visibility = View.INVISIBLE
    }

    override fun onStopLoading(status: EnumStatus) {
        progressBarCircularIndeterminate?.setVisibility(View.INVISIBLE)
        btnNext?.visibility = View.VISIBLE
        Utils.Log(TAG, "Stop")
    }

    override fun onError(message: String?, status: EnumStatus?) {
        Utils.Log(TAG, message + " " + status?.name)
        when (status) {
            EnumStatus.SIGN_IN -> {
                onStopLoading(EnumStatus.SIGN_IN)
                edtEmail?.setError(message)
            }
        }
    }

    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        Utils.Log(TAG, message + " " + status?.name)
        when (status) {
            EnumStatus.SEND_EMAIL -> {
                val mUser: User? = Utils.getUserInfo()
                Navigator.onMoveToVerify(this, mUser)
                onStopLoading(EnumStatus.SIGN_IN)
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: User?) {
        when (status) {
            EnumStatus.SIGN_IN -> {
                val mUser: User? = Utils.getUserInfo()
                val emailToken: EmailToken? = mUser?.let { EmailToken.getInstance()?.convertObject(it, EnumStatus.SIGN_IN) }
                if (emailToken != null) {
                    presenter?.onSendMail(emailToken)
                }
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<User>?) {}

    companion object {
        private val TAG = SignInAct::class.java.simpleName
    }
}