package co.tpcreative.supersafe.ui.enablecloud
import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGoogleApi
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.request.UserCloudRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.model.User
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class EnableCloudActivity : BaseGoogleApi(), BaseView<EmptyModel> {
    private var presenter: EnableCloudPresenter? = null
    private var progressDialog: ProgressDialog? = null

    @BindView(R.id.btnLinkGoogleDrive)
    var btnLinkGoogleDrive: AppCompatButton? = null

    @BindView(R.id.btnUserAnotherAccount)
    var btnUserAnotherAccount: AppCompatButton? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_cloud)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.hide()
        presenter = EnableCloudPresenter()
        presenter?.bindView(this)
        presenter?.onUserInfo()
        Utils.Log(TAG, "Enable cloud...........")
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

    override fun onBackPressed() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }

    @OnClick(R.id.btnLinkGoogleDrive)
    fun onGoogleDrive(view: View?) {
        btnUserAnotherAccount?.setEnabled(false)
        btnLinkGoogleDrive?.setEnabled(false)
        val cloud_id: String? = presenter?.mUser?.cloud_id
        if (cloud_id == null) {
            ServiceManager.getInstance()?.onPickUpNewEmail(this)
        } else {
            ServiceManager.getInstance()?.onPickUpExistingEmail(this, cloud_id)
        }
    }

    @OnClick(R.id.btnUserAnotherAccount)
    fun onUserAnotherAccount(view: View?) {
        Utils.Log(TAG, "user another account")
        btnUserAnotherAccount?.setEnabled(false)
        btnLinkGoogleDrive?.setEnabled(false)
        onShowWarningAnotherAccount()
    }

    protected override fun onDriveClientReady() {
        Utils.Log(TAG, "Google drive ready")
        val request = UserCloudRequest(presenter?.mUser?.email, presenter?.mUser?.cloud_id, SuperSafeApplication.getInstance().getDeviceId())
        presenter?.onAddUserCloud(request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        btnUserAnotherAccount?.setEnabled(true)
        btnLinkGoogleDrive?.setEnabled(true)
        when (requestCode) {
            Navigator.ENABLE_CLOUD -> if (resultCode == Activity.RESULT_OK) {
                finish()
            }
            Navigator.REQUEST_CODE_EMAIL -> if (resultCode == Activity.RESULT_OK) {
                val accountName: String? = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                Utils.Log(TAG, "accountName : $accountName")
                val cloud_id: String? = presenter?.mUser?.cloud_id
                if (cloud_id == null) {
                    presenter?.mUser?.cloud_id = accountName
                    Utils.Log(TAG, "Call Sign out")
                    signOut(object : ServiceManager.ServiceManagerSyncDataListener {
                        override fun onCompleted() {
                            signIn(accountName)
                        }
                        override fun onError() {
                            signIn(accountName)
                        }
                        override fun onCancel() {}
                    })
                } else {
                    if (accountName == cloud_id) {
                        presenter?.mUser?.cloud_id = accountName
                        Utils.Log(TAG, "Call Sign out")
                        signOut(object : ServiceManager.ServiceManagerSyncDataListener {
                            override fun onCompleted() {
                                onShowProgressDialog()
                                signIn(accountName)
                            }
                            override fun onError() {
                                onShowProgressDialog()
                                signIn(accountName)
                            }
                            override fun onCancel() {}
                        })
                    } else {
                        onShowWarning(cloud_id)
                    }
                }
            }
            Navigator.REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT -> if (resultCode == Activity.RESULT_OK) {
                val accountName: String? = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                Utils.Log(TAG, "accountName : $accountName")
                presenter?.mUser?.cloud_id = accountName
                Utils.Log(TAG, "Call Sign out")
                signOut(object : ServiceManager.ServiceManagerSyncDataListener {
                    override fun onCompleted() {
                        onShowProgressDialog()
                        signIn(accountName)
                    }
                    override fun onError() {
                        onShowProgressDialog()
                        signIn(accountName)
                    }
                    override fun onCancel() {}
                })
            }
            else -> Utils.Log(TAG, "Nothing action")
        }
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun getContext(): Context? {
        return getApplicationContext()
    }

    fun onShowWarning(cloud_id: String?) {
        val value = String.format(getString(R.string.choose_the_same_account), cloud_id)
        MaterialStyledDialog.Builder(this)
                .setTitle(R.string.not_the_same_account)
                .setDescription(value)
                .setHeaderDrawable(R.drawable.ic_drive_cloud)
                .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
                .setHeaderColor(R.color.colorPrimary)
                .setCancelable(true)
                .setPositiveText(R.string.select_again)
                .setNegativeText(R.string.cancel)
                .setCheckBox(false, R.string.enable_cloud)
                .onPositive(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        Utils.Log(TAG, "positive")
                    }
                })
                .onNegative(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {}
                })
                .show()
    }

    fun onShowWarningAnotherAccount() {
        val builder = StringBuilder()
        builder.append("If you use another Google Drive")
        builder.append("\n")
        builder.append("1. Files sync with previous Google Drive will stop")
        builder.append("\n")
        builder.append("2. All of your local files will be synced to the new Google Drive")
        val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        MaterialStyledDialog.Builder(this)
                .setTitle(R.string.user_another_google_drive_title)
                .setDescription(builder.toString())
                .setHeaderDrawable(R.drawable.ic_drive_cloud)
                .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
                .setHeaderColor(themeApp?.getAccentColor()!!)
                .setCancelable(true)
                .setPositiveText(R.string.user_another)
                .setNegativeText(R.string.cancel)
                .setCheckBox(false, R.string.enable_cloud)
                .onPositive(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        Utils.Log(TAG, "positive")
                        val cloud_id: String? = presenter?.mUser?.cloud_id
                        if (cloud_id == null) {
                            ServiceManager.getInstance()?.onPickUpNewEmailNoTitle(this@EnableCloudActivity, presenter?.mUser?.email)
                        } else {
                            ServiceManager.getInstance()?.onPickUpNewEmailNoTitle(this@EnableCloudActivity, cloud_id)
                        }
                    }
                })
                .onNegative(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        btnUserAnotherAccount?.setEnabled(true)
                        btnLinkGoogleDrive?.setEnabled(true)
                    }
                })
                .show()
    }

    fun onShowProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog?.isShowing()!!) {
                return
            }
        }
        progressDialog = ProgressDialog(this@EnableCloudActivity)
        progressDialog?.setMessage(getString(R.string.loading))
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    fun onStopProgressDialog() {
        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
    }

    override fun onDriveSuccessful() {
        Utils.Log(TAG, "onDriveSuccessful")
    }

    override fun onDriveError() {
        Utils.Log(TAG, "onDriveError")
        onStopProgressDialog()
    }

    override fun onDriveSignOut() {
        Utils.Log(TAG, "onDriveSignOut")
    }

    override fun onDriveRevokeAccess() {
        Utils.Log(TAG, "onDriveRevokeAccess")
    }

    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onError(message: String?, status: EnumStatus?) {
        Utils.Log(TAG, "" + message)
        when (status) {
            EnumStatus.CREATE -> {
                onStopProgressDialog()
            }
        }
    }

    override fun startServiceNow() {
        ServiceManager.getInstance()?.onStartService()
        onStopProgressDialog()
    }

    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.CREATE -> {
                onStopProgressDialog()
                val mUser: User? = Utils.getUserInfo()
                mUser?.cloud_id = message
                mUser?.driveConnected = true
                Utils.setUserPreShare(mUser)
                presenter?.mUser = mUser
                Utils.Log(TAG, "Finsh enable cloud.........................")
                ServiceManager.getInstance()?.onPreparingSyncData()
                ServiceManager.getInstance()?.onGetUserInfo()
                onBackPressed()
            }
        }
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    protected override fun isSignIn(): Boolean {
        return true
    }

    companion object {
        private val TAG = EnableCloudActivity::class.java.simpleName
    }
}