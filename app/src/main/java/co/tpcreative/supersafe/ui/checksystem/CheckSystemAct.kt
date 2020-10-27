package co.tpcreative.supersafe.ui.checksystem
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGoogleApi
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.request.SignInRequest
import co.tpcreative.supersafe.common.request.UserCloudRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.model.EnumStatus
import com.snatik.storage.security.SecurityUtil
import kotlinx.android.synthetic.main.activity_check_system.*
import org.greenrobot.eventbus.ThreadMode

class CheckSystemAct : BaseGoogleApi(), BaseView<EmptyModel> {
    var presenter: CheckSystemPresenter? = null
    var handler: Handler? = Handler(Looper.getMainLooper())
    var email: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_system)
        initUI()
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

    override fun getContext(): Context? {
        return applicationContext
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
                Utils.Log(TAG, "onBackPressed onActivity Result")
                onBackPressed()
            }
            else -> Utils.Log(TAG, "Nothing action")
        }
    }

    override fun onDriveClientReady() {
        Utils.Log(TAG, "onDriveClient")
        presenter?.mUser?.driveConnected = true
        Utils.setUserPreShare(presenter?.mUser)
        val request = UserCloudRequest(presenter?.mUser?.email, presenter?.mUser?.email, SuperSafeApplication.getInstance().getDeviceId())
        presenter?.onAddUserCloud(request)
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onDriveSuccessful() {}
    override fun onDriveError() {}
    override fun onDriveSignOut() {}
    override fun onDriveRevokeAccess() {}
    override fun onStartLoading(status: EnumStatus) {
        progressBarCircularIndeterminate?.visibility = View.VISIBLE
    }

    override fun onStopLoading(status: EnumStatus) {
        progressBarCircularIndeterminate?.visibility = View.GONE
    }

    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RESEND_CODE -> {
                Navigator.onEnableCloud(this)
            }
            EnumStatus.CLOUD_ID_EXISTING -> {
                if (presenter?.mUser != null) {
                    presenter?.mUser?.cloud_id = message
                    Utils.Log(TAG, "CLOUD_ID_EXISTING : $message")
                    Utils.setUserPreShare(presenter?.mUser)
                }
                Navigator.onEnableCloud(this)
            }
            EnumStatus.CREATE -> {
                Utils.Log(TAG, "CREATE.............action")
                onBackPressed()
            }
            EnumStatus.SEND_EMAIL -> {
                if (presenter?.googleOauth != null) {
                    onVerifyInputCode(presenter?.googleOauth?.email)
                }
            }
            EnumStatus.USER_ID_EXISTING -> {
                Utils.Log(TAG, "USER_ID_EXISTING : $message")
            }
            EnumStatus.CHANGE_EMAIL -> {
                presenter?.onCheckUser(presenter?.mUser?.email, presenter?.mUser?.other_email)
            }
            EnumStatus.VERIFY_CODE -> {
                Utils.Log(TAG, "VERIFY_CODE...........action here")
                if (presenter?.googleOauth != null) {
                    if (presenter?.googleOauth?.isEnableSync!!) {
                        Utils.Log(TAG, "Syn google drive")
                        signOut(object : ServiceManager.ServiceManagerSyncDataListener {
                            override fun onCompleted() {
                                onStartLoading(status)
                                signIn(presenter?.mUser?.email)
                            }

                            override fun onError() {
                                onStartLoading(status)
                                signIn(presenter?.mUser?.email)
                            }

                            override fun onCancel() {}
                        })
                    } else {
                        Utils.Log(TAG, "Google drive Sync is disable")
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
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
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
                if (presenter?.mUser != null) {
                    onVerifyInputCode(presenter?.mUser?.email)
                }
            }
            EnumStatus.CHANGE_EMAIL -> {
                val request = SignInRequest()
                request.user_id = email
                request.password = SecurityUtil.key_password_default_encrypted
                request.device_id = SuperSafeApplication.getInstance().getDeviceId()
                presenter?.onSignIn(request)
            }
            EnumStatus.CLOUD_ID_EXISTING -> {
                Navigator.onEnableCloud(this)
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    override fun startServiceNow() {}
    override fun isSignIn(): Boolean {
        return true
    }
    companion object {
        private val TAG = CheckSystemAct::class.java.simpleName
    }
}