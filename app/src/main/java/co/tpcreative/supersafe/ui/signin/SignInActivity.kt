package co.tpcreative.supersafe.ui.signin
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
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
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate
import com.rengwuxian.materialedittext.MaterialEditText
import com.snatik.storage.security.SecurityUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SignInActivity : BaseActivityNoneSlide(), TextView.OnEditorActionListener, BaseView<User> {
    @BindView(R.id.edtEmail)
    var edtEmail: MaterialEditText? = null

    @BindView(R.id.btnNext)
    var btnNext: AppCompatButton? = null

    @BindView(R.id.progressBarCircularIndeterminate)
    var progressBarCircularIndeterminate: ProgressBarCircularIndeterminate? = null
    private var isNext = false
    private var presenter: SignInPresenter? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        edtEmail?.setOnEditorActionListener(this)
        edtEmail?.addTextChangedListener(mTextWatcher)
        presenter = SignInPresenter()
        presenter?.bindView(this)
        ServiceManager.getInstance()?.onStartService()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
            }
        }
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter?.unbindView()
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}
    override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SuperSafeReceiver.Companion.isConnected()) {
                Utils.showDialog(this@SignInActivity, getString(R.string.internet))
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

    @OnClick(R.id.btnNext)
    fun onNext(view: View?) {
        if (!SuperSafeReceiver.Companion.isConnected()) {
            Utils.showDialog(this@SignInActivity, getString(R.string.internet))
            return
        }
        if (isNext) {
            onSignIn()
        }
    }

    fun onSignIn() {
        val email: String = edtEmail?.getText().toString().toLowerCase().trim({ it <= ' ' })
        val request = SignInRequest()
        request.user_id = email
        request.password = SecurityUtil.key_password_default_encrypted
        request.device_id = SuperSafeApplication.Companion.getInstance().getDeviceId()
        presenter?.onSignIn(request)
        Utils.hideSoftKeyboard(this)
    }

    /*Detecting textWatch*/
    private val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            isNext = if (Utils.isValidEmail(s)) {
                btnNext?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded))
                btnNext?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                true
            } else {
                btnNext?.setBackground(getResources().getDrawable(R.drawable.bg_button_disable_rounded))
                btnNext?.setTextColor(getResources().getColor(R.color.colorDisableText))
                false
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun getContext(): Context? {
        return getApplicationContext()
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onStartLoading(status: EnumStatus) {
        progressBarCircularIndeterminate?.setVisibility(View.VISIBLE)
        btnNext?.setVisibility(View.INVISIBLE)
    }

    override fun onStopLoading(status: EnumStatus) {
        progressBarCircularIndeterminate?.setVisibility(View.INVISIBLE)
        btnNext?.setVisibility(View.VISIBLE)
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
        private val TAG = SignInActivity::class.java.simpleName
    }
}