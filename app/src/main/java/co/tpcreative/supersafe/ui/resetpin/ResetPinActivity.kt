package co.tpcreative.supersafe.ui.resetpin
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseVerifyPinActivity
import co.tpcreative.supersafe.common.controller.SingletonResetPin
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.google.gson.Gson
import com.snatik.storage.security.SecurityUtil
import fr.castorflex.android.circularprogressbar.CircularProgressBar
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ResetPinActivity : BaseVerifyPinActivity(), BaseView<EmptyModel>, TextView.OnEditorActionListener {
    @BindView(R.id.tvStep1)
    var tvStep1: AppCompatTextView? = null

    @BindView(R.id.edtCode)
    var edtCode: AppCompatEditText? = null

    @BindView(R.id.btnSendRequest)
    var btnSendRequest: AppCompatButton? = null

    @BindView(R.id.btnReset)
    var btnReset: AppCompatButton? = null

    @BindView(R.id.progressbar_circular)
    var mCircularProgressBar: CircularProgressBar? = null

    @BindView(R.id.llSupport)
    var llSupport: LinearLayout? = null

    @BindView(R.id.tvSupport)
    var tvSupport: AppCompatTextView? = null
    private var presenter: ResetPinPresenter? = null
    private var isNext = false
    private var isRestoreFiles: Boolean? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_pin)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        presenter = ResetPinPresenter()
        presenter?.bindView(this)
        if (presenter?.mUser != null) {
            val email: String? = presenter?.mUser?.email
            if (email != null) {
                val result: String? = Utils.getFontString(R.string.request_an_access_code, email)
                tvStep1?.setText(result?.toSpanned())
                val support: String? = Utils.getFontString(R.string.send_an_email_to, SecurityUtil.MAIL)
                tvSupport?.setText(support?.toSpanned())
            }
        }
        edtCode?.addTextChangedListener(mTextWatcher)
        edtCode?.setOnEditorActionListener(this)
        try {
            val bundle: Bundle? = getIntent().getExtras()
            isRestoreFiles = bundle?.get(ResetPinActivity::class.java.simpleName) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    fun setProgressValue() {
        var circularProgressDrawable: CircularProgressDrawable? = null
        val b = CircularProgressDrawable.Builder(this)
                .colors(getResources().getIntArray(R.array.gplus_colors))
                .sweepSpeed(2f)
                .rotationSpeed(2f)
                .strokeWidth(Utils.dpToPx(3).toFloat())
                .style(CircularProgressDrawable.STYLE_ROUNDED)
        mCircularProgressBar?.setIndeterminateDrawable(b.build().also { circularProgressDrawable = it })
        // /!\ Terrible hack, do not do this at home!
        mCircularProgressBar?.getWidth()?.let {
            circularProgressDrawable?.setBounds(0,
                0,
                    it,
                    mCircularProgressBar?.height!!)
        }

        //mCircularProgressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(themeApp.getAccentColor()),
        //        PorterDuff.Mode.SRC_IN);
        mCircularProgressBar?.setVisibility(View.INVISIBLE)
        mCircularProgressBar?.setVisibility(View.VISIBLE)
        Utils.Log(TAG, "Action here set progress")
    }

    private val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = s.toString().trim { it <= ' ' }
            isNext = if (Utils.isValid(value)) {
                btnReset?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded))
                btnReset?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                btnReset?.setEnabled(true)
                true
            } else {
                btnReset?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded))
                btnReset?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.colorDisableText))
                btnReset?.setEnabled(false)
                false
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    fun onVerifyCode() {
        if (isNext) {
            val code: String = edtCode?.getText().toString().trim({ it <= ' ' })
            val request = VerifyCodeRequest()
            request.code = code
            request.user_id = presenter?.mUser?.email
            request._id = presenter?.mUser?._id
            request.device_id = SuperSafeApplication.getInstance().getDeviceId()
            presenter?.onVerifyCode(request)
        }
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

    @OnClick(R.id.btnReset)
    fun onSendReset(view: View?) {
        onVerifyCode()
    }

    @OnClick(R.id.btnSendRequest)
    fun onSentRequest() {
        btnSendRequest?.setEnabled(false)
        btnSendRequest?.setText("")
        onStartLoading(EnumStatus.OTHER)
        if (presenter?.mUser != null) {
            if (presenter?.mUser?.email != null) {
                val request = VerifyCodeRequest()
                request.user_id = presenter?.mUser?.email
                presenter?.onRequestCode(request)
            }
        }
    }

    @OnClick(R.id.llSupport)
    fun onSupport() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + SecurityUtil.MAIL))
            intent.putExtra(Intent.EXTRA_SUBJECT, "SuperSafe App Support")
            intent.putExtra(Intent.EXTRA_TEXT, "")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onStartLoading(status: EnumStatus) {
        setProgressValue()
    }

    override fun onStopLoading(status: EnumStatus) {
        Utils.Log(TAG, "Stop progressing")
        if (mCircularProgressBar != null) {
            mCircularProgressBar?.progressiveStop()
        }
    }

    override fun getContext(): Context? {
        return getApplicationContext()
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onError(message: String?, status: EnumStatus?) {
        onStopLoading(EnumStatus.OTHER)
        when (status) {
            EnumStatus.REQUEST_CODE -> {
                btnSendRequest?.setText(getString(R.string.send_verification_code))
                btnSendRequest?.setEnabled(true)
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
                btnSendRequest?.setText(getString(R.string.send_verification_code))
                btnSendRequest?.setEnabled(false)
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
    fun onShowDialogWaitingCode() {
        val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        val mUser: User? = Utils.getUserInfo()
        Utils.Log(TAG, "Preparing " + Gson().toJson(mUser))
        MaterialStyledDialog.Builder(this)
                .setTitle(R.string.send_code_later)
                .setDescription(R.string.send_code_later_detail)
                .setHeaderDrawable(R.drawable.baseline_email_white_48)
                .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
                .setHeaderColor(themeApp?.getPrimaryColor()!!)
                .setCancelable(true)
                .setPositiveText(R.string.continue_value)
                .setNegativeText(R.string.cancel)
                .setCheckBox(false, R.string.enable_cloud)
                .onPositive(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        Utils.Log(TAG, "positive")
                        val mUser: User? = Utils.getUserInfo()
                        Utils.Log(TAG, "Pressed " + Gson().toJson(mUser))
                        SingletonResetPin.Companion.getInstance()?.onStartTimer(300000)
                    }
                })
                .onNegative(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        finish()
                    }
                })
                .show()
    }

    companion object {
        private val TAG = ResetPinActivity::class.java.canonicalName
    }
}