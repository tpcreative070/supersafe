package co.tpcreative.supersafe.ui.signup
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
import co.tpcreative.supersafe.common.request.SignUpRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate
import com.rengwuxian.materialedittext.MaterialEditText
import com.snatik.storage.security.SecurityUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SignUpActivity : BaseActivityNoneSlide(), TextView.OnEditorActionListener, BaseView<User> {
    @BindView(R.id.edtName)
    var edtName: MaterialEditText? = null

    @BindView(R.id.edtEmail)
    var edtEmail: MaterialEditText? = null

    @BindView(R.id.btnFinish)
    var btnFinish: AppCompatButton? = null

    @BindView(R.id.progressBarCircularIndeterminate)
    var progressBarCircularIndeterminate: ProgressBarCircularIndeterminate? = null
    private var isEmail = false
    private var isName = false
    private var presenter: SignUpPresenter? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        edtName?.addTextChangedListener(mTextWatcher)
        edtEmail?.addTextChangedListener(mTextWatcher)
        edtEmail?.setOnEditorActionListener(this)
        edtName?.setOnEditorActionListener(this)
        presenter = SignUpPresenter()
        presenter?.bindView(this)
        Utils.Log(TAG, "onCreate")
        isName = true
        edtName?.setText(getString(R.string.free))
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

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter?.unbindView()
        ServiceManager.getInstance()?.onStartService()
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {}
    override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.showDialog(this@SignUpActivity, getString(R.string.internet))
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
                Utils.showDialog(this@SignUpActivity, getString(R.string.internet))
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
    private val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (getCurrentFocus() === edtName) {
                isName = if (Utils.isValid(s)) {
                    true
                } else {
                    false
                }
            }
            if (getCurrentFocus() === edtEmail) {
                isEmail = if (Utils.isValidEmail(s)) {
                    true
                } else {
                    false
                }
            }
            if (isEmail && isName) {
                btnFinish?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded))
                btnFinish?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                btnFinish?.setEnabled(true)
            } else {
                btnFinish?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded))
                btnFinish?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.colorDisableText))
                btnFinish?.setEnabled(false)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    @OnClick(R.id.btnFinish)
    fun onClickedFinish(view: View?) {
        if (isName && isEmail) {
            onSignUp()
        }
    }

    fun onSignUp() {
        val email: String = edtEmail?.getText().toString().toLowerCase().trim({ it <= ' ' })
        val name: String = edtName?.getText().toString().trim({ it <= ' ' })
        val request = SignUpRequest()
        request.user_id = email
        request.name = name
        request.password = SecurityUtil.key_password_default_encrypted
        request.device_id = SuperSafeApplication.Companion.getInstance().getDeviceId()
        presenter?.onSignUp(request)
        Utils.hideSoftKeyboard(this)
        Utils.Log(TAG, "onFished")
    }

    override fun getContext(): Context? {
        return getApplicationContext()
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onStartLoading(status: EnumStatus) {
        progressBarCircularIndeterminate?.setVisibility(View.VISIBLE)
        btnFinish?.setVisibility(View.INVISIBLE)
    }

    override fun onStopLoading(status: EnumStatus) {
        progressBarCircularIndeterminate?.setVisibility(View.INVISIBLE)
        btnFinish?.setVisibility(View.VISIBLE)
    }

    override fun onError(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.SIGN_UP -> {
                edtEmail?.setError(message)
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

    companion object {
        private val TAG = SignUpActivity::class.java.simpleName
    }
}