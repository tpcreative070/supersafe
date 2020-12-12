package co.tpcreative.supersafe.ui.enablecloud
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGoogleApi
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.viewmodel.EnableCloudViewModel
import kotlinx.android.synthetic.main.activity_enable_cloud.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class EnableCloudAct : BaseGoogleApi(){
    lateinit var viewModel : EnableCloudViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_cloud)
        initUI()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            else -> Utils.Log(TAG,"Nothing")
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
        CoroutineScope(Dispatchers.Main).launch {
            addUserCloud()
        }
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
                if (Utils.getUserCloudId() == null) {
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
                    if (accountName == Utils.getUserCloudId()) {
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
                        onShowWarning(Utils.getUserCloudId())
                    }
                }
            }
            Navigator.REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT -> if (resultCode == Activity.RESULT_OK) {
                val accountName: String? = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                Utils.Log(TAG, "accountName : $accountName")
                Utils.Log(TAG, "Call Sign out")
                signOut(object : ServiceManager.ServiceManagerSyncDataListener {
                    override fun onCompleted() {
                        onShowProgressDialog()
                        signIn(accountName)
                        Utils.deletedItemsOnAnotherCloudId()
                    }
                    override fun onError() {
                        onShowProgressDialog()
                        signIn(accountName)
                        Utils.deletedItemsOnAnotherCloudId()
                    }
                    override fun onCancel() {}
                })
            }
            else -> Utils.Log(TAG, "Nothing action")
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

    override fun startServiceNow() {
        onStopProgressDialog()
    }

    override fun isSignIn(): Boolean {
        return true
    }
}