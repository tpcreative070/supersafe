package co.tpcreative.supersafe.ui.checksystem
import android.content.Context
import android.os.Handler
import android.view.View
import androidx.appcompat.widget.Toolbar
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGoogleApi
import co.tpcreative.supersafe.common.util.Utils
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class CheckSystemActivity : BaseGoogleApi(), BaseView<Any?> {
    @BindView(R.id.progressBarCircularIndeterminate)
    var progressBarCircularIndeterminate: ProgressBarCircularIndeterminate? = null
    private var presenter: CheckSystemPresenter? = null
    var handler: Handler? = Handler()
    var email: String? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_system)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
        getSupportActionBar().hide()
        presenter = CheckSystemPresenter()
        presenter.bindView(this)
        presenter.getIntent(this)
        onStartLoading(EnumStatus.OTHER)
        if (presenter.googleOauth != null) {
            val email: String = presenter.googleOauth.email
            if (email == presenter.mUser.email) {
                presenter.onCheckUser(presenter.googleOauth.email, presenter.googleOauth.email)
            } else {
                this.email = email
                val request = VerifyCodeRequest()
                request.new_user_id = this.email
                request.other_email = email
                request.user_id = presenter.mUser.email
                request._id = presenter.mUser._id
                presenter.onChangeEmail(request)
            }
        } else {
            handler.postDelayed(Runnable { presenter.onUserCloudChecking() }, 5000)
        }
        val themeApp: ThemeApp = ThemeApp.Companion.getInstance().getThemeInfo()
        progressBarCircularIndeterminate.setBackgroundColor(getResources().getColor(themeApp.getAccentColor()))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
        }
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
        //SuperSafeApplication.getInstance().writeKeyHomePressed(CheckSystemActivity.class.getSimpleName());
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Companion.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter.unbindView()
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun getContext(): Context? {
        return getApplicationContext()
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Navigator.ENABLE_CLOUD -> if (resultCode == Activity.RESULT_OK) {
                Utils.Companion.Log(TAG, "onBackPressed onActivity Result")
                onBackPressed()
            }
            else -> Utils.Companion.Log(TAG, "Nothing action")
        }
    }

    protected override fun onDriveClientReady() {
        Utils.Companion.Log(TAG, "onDriveClient")
        presenter.mUser.driveConnected = true
        Utils.Companion.setUserPreShare(presenter.mUser)
        val request = UserCloudRequest(presenter.mUser.email, presenter.mUser.email, SuperSafeApplication.Companion.getInstance().getDeviceId())
        presenter.onAddUserCloud(request)
    }

    override fun getActivity(): Activity? {
        return this
    }

    fun onVerifyInputCode(email: String?) {
        Utils.Companion.Log(TAG, " User..." + Gson().toJson(presenter.mUser))
        try {
            val dialog = MaterialDialog.Builder(this)
            val next = "<font color='#0091EA'>($email)</font>"
            val value: String = getString(R.string.description_pin_code, next)
            dialog.title(getString(R.string.verify_email))
            dialog.content(Html.fromHtml(value))
            dialog.theme(Theme.LIGHT)
            dialog.inputType(InputType.TYPE_CLASS_NUMBER)
            dialog.input(getString(R.string.pin_code), null, false, object : InputCallback {
                override fun onInput(dialog: MaterialDialog, input: CharSequence?) {
                    Utils.Companion.Log(TAG, "call input code")
                    val request = VerifyCodeRequest()
                    request.user_id = presenter.mUser.email
                    request.code = input.toString().trim { it <= ' ' }
                    request._id = presenter.mUser._id
                    request.device_id = SuperSafeApplication.Companion.getInstance().getDeviceId()
                    presenter.onVerifyCode(request)
                }
            })
            dialog.positiveText(getString(R.string.ok))
            dialog.negativeText(getString(R.string.cancel))
            dialog.onNegative(object : SingleButtonCallback {
                override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    onStopLoading(EnumStatus.OTHER)
                    onBackPressed()
                }
            })
            val editText = dialog.show().inputEditText
            editText.setOnTouchListener(object : OnTouchListener {
                override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                    view.setFocusable(true)
                    view.setFocusableInTouchMode(true)
                    return false
                }
            })
            editText.setFocusable(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected override fun onDriveSuccessful() {}
    protected override fun onDriveError() {}
    protected override fun onDriveSignOut() {}
    protected override fun onDriveRevokeAccess() {}
    override fun onStartLoading(status: EnumStatus?) {
        progressBarCircularIndeterminate.setVisibility(View.VISIBLE)
    }

    override fun onStopLoading(status: EnumStatus?) {
        progressBarCircularIndeterminate.setVisibility(View.GONE)
    }

    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RESEND_CODE -> {
                Navigator.onEnableCloud(this)
            }
            EnumStatus.CLOUD_ID_EXISTING -> {
                if (presenter.mUser != null) {
                    presenter.mUser.cloud_id = message
                    Utils.Companion.Log(TAG, "CLOUD_ID_EXISTING : $message")
                    Utils.Companion.setUserPreShare(presenter.mUser)
                }
                Navigator.onEnableCloud(this)
            }
            EnumStatus.CREATE -> {
                Utils.Companion.Log(TAG, "CREATE.............action")
                onBackPressed()
            }
            EnumStatus.SEND_EMAIL -> {
                if (presenter.googleOauth != null) {
                    onVerifyInputCode(presenter.googleOauth.email)
                }
            }
            EnumStatus.USER_ID_EXISTING -> {
                Utils.Companion.Log(TAG, "USER_ID_EXISTING : $message")
            }
            EnumStatus.CHANGE_EMAIL -> {
                presenter.onCheckUser(presenter.mUser.email, presenter.mUser.other_email)
            }
            EnumStatus.VERIFY_CODE -> {
                Utils.Companion.Log(TAG, "VERIFY_CODE...........action here")
                if (presenter.googleOauth != null) {
                    if (presenter.googleOauth.isEnableSync) {
                        Utils.Companion.Log(TAG, "Syn google drive")
                        signOut(object : ServiceManagerSyncDataListener {
                            override fun onCompleted() {
                                onStartLoading(status)
                                signIn(presenter.mUser.email)
                            }

                            override fun onError() {
                                onStartLoading(status)
                                signIn(presenter.mUser.email)
                            }

                            override fun onCancel() {}
                        })
                    } else {
                        Utils.Companion.Log(TAG, "Google drive Sync is disable")
                        onStopLoading(status)
                        onBackPressed()
                    }
                } else {
                    Toast.makeText(this, "Oauth is null", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onError(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RESEND_CODE -> {
            }
            EnumStatus.CREATE -> {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show()
                onStopLoading(status)
            }
            EnumStatus.SIGN_UP -> {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                onStopLoading(status)
            }
            EnumStatus.SIGN_IN -> {
                onStopLoading(status)
                Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
            EnumStatus.VERIFY_CODE -> {
                Toast.makeText(this, "Failed verify code", Toast.LENGTH_SHORT).show()
                if (presenter.mUser != null) {
                    onVerifyInputCode(presenter.mUser.email)
                }
            }
            EnumStatus.CHANGE_EMAIL -> {
                val request = SignInRequest()
                request.user_id = email
                request.password = SecurityUtil.key_password_default_encrypted
                request.device_id = SuperSafeApplication.Companion.getInstance().getDeviceId()
                presenter.onSignIn(request)
            }
            EnumStatus.CLOUD_ID_EXISTING -> {
                Navigator.onEnableCloud(this)
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: Any?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<*>?) {}
    protected override fun startServiceNow() {}
    protected override fun isSignIn(): Boolean {
        return true
    }

    companion object {
        private val TAG = CheckSystemActivity::class.java.simpleName
    }
}