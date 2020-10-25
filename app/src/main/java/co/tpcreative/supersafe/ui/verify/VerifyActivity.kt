package co.tpcreative.supersafe.ui.verify
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumStatus
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate
import com.rengwuxian.materialedittext.MaterialEditText
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class VerifyActivity : BaseActivityNoneSlide(), BaseView<EmptyModel>, TextView.OnEditorActionListener {
    @BindView(R.id.tvTitle)
    var tvTitle: AppCompatTextView? = null

    @BindView(R.id.btnLogin)
    var btnLogin: AppCompatButton? = null

    @BindView(R.id.btnReSend)
    var btnResend: AppCompatButton? = null

    @BindView(R.id.edtCode)
    var edtCode: MaterialEditText? = null

    @BindView(R.id.progressBarCircularIndeterminate)
    var progressBarCircularIndeterminate: ProgressBarCircularIndeterminate? = null

    @BindView(R.id.progressBarCircularIndeterminateReSend)
    var progressBarCircularIndeterminateReSend: ProgressBarCircularIndeterminate? = null
    private var isNext = false
    private var presenter: VerifyPresenter? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        presenter = VerifyPresenter()
        presenter?.bindView(this)
        presenter?.getIntent(this)
        val result: String? = presenter?.user?.email?.let { Utils.getFontString(R.string.verify_title, it) }
        tvTitle?.setText(result?.toSpanned())
        edtCode?.setOnEditorActionListener(this)
        edtCode?.addTextChangedListener(mTextWatcher)
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

    @OnClick(R.id.btnLogin)
    fun onLogin(view: View?) {
        if (isNext) {
            onVerifyCode()
        }
    }

    @OnClick(R.id.btnReSend)
    fun onResend(view: View?) {
        val request = VerifyCodeRequest()
        request.user_id = presenter?.user?.email
        presenter?.onResendCode(request)
    }

    override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (!SuperSafeReceiver.Companion.isConnected()) {
                Utils.showDialog(this@VerifyActivity, getString(R.string.internet))
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

    fun onVerifyCode() {
        val request = VerifyCodeRequest()
        request.code = edtCode?.getText().toString().trim({ it <= ' ' })
        request.user_id = presenter?.user?.email
        request._id = presenter?.user?._id
        request.device_id = SuperSafeApplication.getInstance().getDeviceId()
        presenter?.onVerifyCode(request)
        Utils.hideSoftKeyboard(this)
    }

    /*Detecting textWatch*/
    private val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = s.toString().trim { it <= ' ' }
            isNext = if (Utils.isValid(value)) {
                btnLogin?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded))
                btnLogin?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                true
            } else {
                btnLogin?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded))
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
                progressBarCircularIndeterminateReSend?.setVisibility(View.VISIBLE)
                btnResend?.setVisibility(View.INVISIBLE)
            }
            EnumStatus.VERIFY_CODE -> {
                progressBarCircularIndeterminate?.setVisibility(View.VISIBLE)
                btnLogin?.setVisibility(View.INVISIBLE)
            }
        }
    }

    override fun onStopLoading(status: EnumStatus) {
        when (status) {
            EnumStatus.RESEND_CODE -> {
                progressBarCircularIndeterminateReSend?.setVisibility(View.INVISIBLE)
                btnResend?.setVisibility(View.VISIBLE)
            }
            EnumStatus.VERIFY_CODE -> {
                progressBarCircularIndeterminate?.setVisibility(View.INVISIBLE)
                btnLogin?.setVisibility(View.VISIBLE)
            }
        }
    }

    override fun getContext(): Context? {
        return getApplicationContext()
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onError(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.VERIFY_CODE -> {
                edtCode?.setError(message)
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

    companion object {
        private val TAG = VerifyActivity::class.java.simpleName
    }
}