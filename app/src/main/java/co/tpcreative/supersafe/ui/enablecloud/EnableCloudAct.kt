package co.tpcreative.supersafe.ui.enablecloud
import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import co.tpcreative.supersafe.model.User
import kotlinx.android.synthetic.main.activity_enable_cloud.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class EnableCloudAct : BaseGoogleApi(), BaseView<EmptyModel> {
    var presenter: EnableCloudPresenter? = null
    var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_cloud)
        initUI()
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

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
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

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }

    override fun onDriveClientReady() {
        Utils.Log(TAG, "Google drive ready")
        val request = UserCloudRequest(presenter?.mUser?.email, presenter?.mUser?.cloud_id, SuperSafeApplication.getInstance().getDeviceId())
        presenter?.onAddUserCloud(request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        btnUserAnotherAccount?.isEnabled = true
        btnLinkGoogleDrive?.isEnabled = true
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
        return applicationContext
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
    override fun isSignIn(): Boolean {
        return true
    }
}