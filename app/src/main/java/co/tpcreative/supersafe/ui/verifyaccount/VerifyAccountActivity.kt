package co.tpcreative.supersafe.ui.verifyaccount
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.rengwuxian.materialedittext.MaterialEditText
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class VerifyAccountActivity : BaseActivity(), TextView.OnEditorActionListener, BaseView<EmptyModel> {
    @BindView(R.id.imgEdit)
    var imgEdit: AppCompatImageView? = null

    @BindView(R.id.tvTitle)
    var tvTitle: AppCompatTextView? = null

    @BindView(R.id.llGoogle)
    var llGoogle: RelativeLayout? = null

    @BindView(R.id.llAction)
    var llAction: LinearLayout? = null

    @BindView(R.id.llChangeEmail)
    var llChangeEmail: LinearLayout? = null

    @BindView(R.id.llVerifyCode)
    var llVerifyCode: LinearLayout? = null

    @BindView(R.id.edtEmail)
    var edtEmail: MaterialEditText? = null

    @BindView(R.id.edtCode)
    var edtCode: MaterialEditText? = null

    @BindView(R.id.btnCancel)
    var btnCancel: AppCompatButton? = null

    @BindView(R.id.btnSave)
    var btnSave: AppCompatButton? = null

    @BindView(R.id.btnSignIn)
    var btnSignIn: AppCompatButton? = null
    private var isNext = false

    @BindView(R.id.btnReSend)
    var btnResend: AppCompatButton? = null

    @BindView(R.id.btnSendVerifyCode)
    var btnSendVerifyCode: AppCompatButton? = null

    @BindView(R.id.progressBarCircularIndeterminateSignIn)
    var progressBarCircularIndeterminateSignIn: ProgressBarCircularIndeterminate? = null

    @BindView(R.id.progressBarCircularIndeterminateReSend)
    var progressBarCircularIndeterminateReSend: ProgressBarCircularIndeterminate? = null

    @BindView(R.id.progressBarCircularIndeterminateVerifyCode)
    var progressBarCircularIndeterminateVerifyCode: ProgressBarCircularIndeterminate? = null

    @BindView(R.id.tvEmail)
    var tvEmail: AppCompatTextView? = null
    private var presenter: VerifyAccountPresenter? = null
    private var isBack = true
    private var isSync = true
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_account)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        imgEdit?.setColorFilter(ContextCompat.getColor(this,R.color.colorBackground), PorterDuff.Mode.SRC_ATOP)
        edtCode?.addTextChangedListener(mTextWatcher)
        edtEmail?.addTextChangedListener(mTextWatcher)
        edtCode?.setOnEditorActionListener(this)
        edtEmail?.setOnEditorActionListener(this)
        presenter = VerifyAccountPresenter()
        presenter?.bindView(this)
        if (presenter?.mUser != null) {
            if (presenter?.mUser?.email != null) {
                tvEmail?.setText(presenter?.mUser?.email)
                val sourceString: String = getString(R.string.verify_title, "<font color='#000000'>" + presenter?.mUser?.email + "</font>")
                tvTitle?.setText(sourceString.toSpanned())
            }
        }
        val theme: ThemeApp? = ThemeApp.Companion.getInstance()?.getThemeInfo()
        progressBarCircularIndeterminateSignIn?.setBackgroundColor(ContextCompat.getColor(this,theme?.getAccentColor()!!))
        progressBarCircularIndeterminateReSend?.setBackgroundColor(ContextCompat.getColor(this,theme?.getAccentColor()!!))
        progressBarCircularIndeterminateVerifyCode?.setBackgroundColor(ContextCompat.getColor(this,theme?.getAccentColor()!!))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
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

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter?.unbindView()
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onEditorAction(textView: TextView?, i: Int, keyEvent: KeyEvent?): Boolean {
        if (i == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.Companion.isConnected()) {
                Utils.showDialog(this, getString(R.string.internet))
                return false
            }
            if (isNext) {
                Utils.Log(TAG, "Next")
                if (getCurrentFocus() === edtCode) {
                    onVerifyCode()
                }
                if (getCurrentFocus() === edtEmail) {
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
    private val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = s.toString().trim { it <= ' ' }
            if (getCurrentFocus() === edtEmail) {
                isNext = if (Utils.isValidEmail(value)) {
                    btnSave?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded))
                    btnSave?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                    true
                } else {
                    btnSave?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded))
                    btnSave?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.colorDisableText))
                    false
                }
            } else if (getCurrentFocus() === edtCode) {
                Utils.Log(TAG, "code")
                isNext = if (Utils.isValid(value)) {
                    btnSignIn?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded))
                    btnSignIn?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                    true
                } else {
                    btnSignIn?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded))
                    btnSignIn?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.colorDisableText))
                    false
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    @OnClick(R.id.llGoogle)
    fun onClickedGoogle(view: View?) {
        onShowDialog()
    }

    @OnClick(R.id.imgEdit)
    fun onClickedEdit(view: View?) {
        Utils.Log(TAG, "edit")
        onShowView(view!!)
        edtEmail?.requestFocus()
        edtEmail?.setText(presenter?.mUser?.email)
        edtEmail?.setSelection(edtEmail?.length()!!)
    }

    @OnClick(R.id.btnSendVerifyCode)
    fun onClickedSendVerifyCode(view: View?) {
        Utils.Log(TAG, "Verify code")
        SingletonManagerProcessing.getInstance()?.onStartProgressing(this, R.string.progressing)
        presenter?.onCheckUser(presenter?.mUser?.email, presenter?.mUser?.other_email)
    }

    @OnClick(R.id.btnCancel)
    fun onClickedCancel(view: View?) {
        Utils.Log(TAG, "onCancel")
        Utils.hideSoftKeyboard(this)
        onShowView(llAction!!)
    }

    @OnClick(R.id.btnSave)
    fun onClickedSave() {
        Utils.Log(TAG, "onSave")
        if (isNext) {
            onChangedEmail()
            /*Do something here*/
        }
    }

    @OnClick(R.id.tvPrivateCloud)
    fun onPrivateCloud() {
        val categories = Categories(0, getString(R.string.faq))
        val mList: MutableList<HelpAndSupport> = ArrayList<HelpAndSupport>()
        mList.add(HelpAndSupport(categories, getString(R.string.what_about_google_drive), getString(R.string.what_about_google_drive_content), null))
        Navigator.onMoveHelpAndSupportContent(this, mList[0])
    }

    fun onChangedEmail() {
        val new_user_id: String = edtEmail?.getText().toString().toLowerCase().trim({ it <= ' ' })
        tvEmail?.setText(new_user_id)
        val sourceString: String = getString(R.string.verify_title, "<font color='#000000'>$new_user_id</font>")
        tvTitle?.setText(sourceString.toSpanned())
        if (new_user_id == presenter?.mUser?.email) {
            return
        }
        val request = VerifyCodeRequest()
        request.new_user_id = new_user_id
        request.user_id = presenter?.mUser?.email
        request._id = presenter?.mUser?._id
        presenter?.onChangeEmail(request)
        Utils.hideSoftKeyboard(this)
        Utils.hideKeyboard(edtEmail)
    }

    @OnClick(R.id.btnReSend)
    fun onClickedResend() {
        try {
            SingletonManagerProcessing.getInstance()?.onStartProgressing(this, R.string.progressing)
            Utils.hideSoftKeyboard(this)
            val request = VerifyCodeRequest()
            request.user_id = presenter?.mUser?.email
            presenter?.onResendCode(request)
            Utils.Log(TAG, "onResend")
        } catch (e: Exception) {
        }
    }

    @OnClick(R.id.btnSignIn)
    fun onClickedSignIn(view: View?) {
        Utils.Log(TAG, "onSignIn")
        if (isNext) {
            /*Do something here*/
            onVerifyCode()
        }
    }

    fun onVerifyCode() {
        val request = VerifyCodeRequest()
        request.code = edtCode?.getText().toString().trim({ it <= ' ' })
        request.user_id = presenter?.mUser?.email
        request._id = presenter?.mUser?._id
        request.device_id = SuperSafeApplication.Companion.getInstance().getDeviceId()
        presenter?.onVerifyCode(request)
        Utils.hideSoftKeyboard(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
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
        when (view.getId()) {
            R.id.imgEdit -> {
                llAction?.setVisibility(View.GONE)
                llVerifyCode?.setVisibility(View.GONE)
                llChangeEmail?.setVisibility(View.VISIBLE)
                edtCode?.setText("")
                isBack = false
                isNext = false
            }
            R.id.btnSendVerifyCode -> {
                llAction?.setVisibility(View.GONE)
                llChangeEmail?.setVisibility(View.GONE)
                llVerifyCode?.setVisibility(View.VISIBLE)
                edtEmail?.setText("")
                edtCode?.setText("")
                isBack = false
                isNext = false
            }
            R.id.llAction -> {
                llAction?.setVisibility(View.VISIBLE)
                llChangeEmail?.setVisibility(View.GONE)
                llVerifyCode?.setVisibility(View.GONE)
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
                .setCheckBox(true, R.string.enable_cloud, object : CompoundButton.OnCheckedChangeListener {
                    override fun onCheckedChanged(compoundButton: CompoundButton?, b: Boolean) {
                        Utils.Log(TAG, "checked :$b")
                        isSync = b
                    }
                })
                .onPositive(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        Utils.Log(TAG, "positive")
                        ServiceManager.getInstance()?.onPickUpNewEmail(this@VerifyAccountActivity)
                    }
                })
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
                .onPositive(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        Utils.Log(TAG, "positive")
                        Navigator.onCheckSystem(this@VerifyAccountActivity, null)
                    }
                })
                .onNegative(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        finish()
                    }
                })
                .show()
    }

    override fun getContext(): Context? {
        return getApplicationContext()
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
                Navigator.onCheckSystem(this@VerifyAccountActivity, googleOauth)
            }
            else -> Utils.Log(TAG, "Nothing action")
        }
    }

    override fun onStartLoading(status: EnumStatus) {
        when (status) {
            EnumStatus.RESEND_CODE -> {
            }
            EnumStatus.VERIFY_CODE -> {
                progressBarCircularIndeterminateVerifyCode?.setVisibility(View.VISIBLE)
                btnSendVerifyCode?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded))
                btnSendVerifyCode?.setText("")
            }
        }
    }

    override fun onStopLoading(status: EnumStatus) {
        when (status) {
            EnumStatus.RESEND_CODE -> {
            }
            EnumStatus.VERIFY_CODE -> {
                progressBarCircularIndeterminateVerifyCode?.setVisibility(View.INVISIBLE)
                btnSendVerifyCode?.setBackground(ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded))
                btnSendVerifyCode?.setText(getString(R.string.send_verification_code))
            }
        }
    }

    override fun onError(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.CHANGE_EMAIL -> {
                edtEmail?.setError(message)
            }
            EnumStatus.VERIFY_CODE -> {
                edtCode?.setError(message)
            }
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
                SingletonManagerProcessing.Companion.getInstance()?.onStopProgressing(this@VerifyAccountActivity)
                Utils.showGotItSnackbar(tvEmail!!, R.string.we_sent_access_code_to_your_email)
            }
            EnumStatus.VERIFY_CODE -> {
                edtCode?.setText("")
                onShowDialogEnableSync()
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}

    companion object {
        private val TAG = VerifyAccountActivity::class.java.simpleName
    }
}