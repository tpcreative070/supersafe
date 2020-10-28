package co.tpcreative.supersafe.ui.unlockalbum
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.util.UtilsListener
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import kotlinx.android.synthetic.main.activity_unlock_all_album.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class UnlockAllAlbumAct : BaseActivity(), BaseView<EmptyModel>, TextView.OnEditorActionListener {
    var presenter: UnlockAllAlbumPresenter? = null
    var isNext = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlock_all_album)
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

    val mTextWatcher: TextWatcher? = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val value = s.toString().trim { it <= ' ' }
            isNext = if (Utils.isValid(value)) {
                btnUnlock?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_rounded)
                btnUnlock?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.white))
                btnUnlock?.isEnabled = true
                true
            } else {
                btnUnlock?.background = ContextCompat.getDrawable(getContext()!!,R.drawable.bg_button_disable_rounded)
                btnUnlock?.setTextColor(ContextCompat.getColor(getContext()!!,R.color.colorDisableText))
                btnUnlock?.isEnabled = false
                false
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    fun onVerifyCode() {
        if (isNext) {
            val code: String = edtCode?.text.toString().trim({ it <= ' ' })
            val request = VerifyCodeRequest()
            val mUser: User? = Utils.getUserInfo()
            if (mUser != null) {
                request.code = code
                request.user_id = mUser.email
                request._id = mUser._id
                request.device_id = SuperSafeApplication.getInstance().getDeviceId()
                presenter?.onVerifyCode(request)
            }
        }
    }

    override fun onEditorAction(textView: TextView?, actionId: Int, keyEvent: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.onBasicAlertNotify(this,message = getString(R.string.internet),title = "Alert")
                return false
            }
            return false
        }
        return false
    }

    override fun onStartLoading(status: EnumStatus) {
        setProgressValue(status)
    }

    override fun onStopLoading(status: EnumStatus) {
        when (status) {
            EnumStatus.REQUEST_CODE -> {
                if (progressbar_circular != null) {
                    progressbar_circular?.progressiveStop()
                }
            }
            EnumStatus.UNLOCK_ALBUMS -> {
                if (progressbar_circular_unlock_albums != null) {
                    progressbar_circular_unlock_albums?.progressiveStop()
                }
            }
        }
    }

    override fun getContext(): Context? {
        return applicationContext
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onError(message: String?, status: EnumStatus?) {
        onStopLoading(EnumStatus.REQUEST_CODE)
        onStopLoading(EnumStatus.UNLOCK_ALBUMS)
        when (status) {
            EnumStatus.REQUEST_CODE -> {
                btnSendRequest?.text = getString(R.string.send_verification_code)
                btnSendRequest?.setEnabled(true)
                btnUnlock?.text = getString(R.string.unlock_all_albums)
                btnUnlock?.isEnabled = true
                Utils.onBasicAlertNotify(this,message = message!!,title = "Alert")
            }
            EnumStatus.SEND_EMAIL -> {
                Utils.onBasicAlertNotify(this,message = message!!,title = "Alert")
            }
            EnumStatus.VERIFY -> {
                btnUnlock?.text = getString(R.string.unlock_all_albums)
                Utils.onBasicAlertNotify(this,message = message!!,title = "Alert")
            }
        }
    }

    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.REQUEST_CODE -> {
                btnSendRequest?.isEnabled = false
            }
            EnumStatus.SEND_EMAIL -> {
                onStopLoading(EnumStatus.REQUEST_CODE)
                btnSendRequest?.text = getString(R.string.send_verification_code)
                Utils.onBasicAlertNotify(this,message = "Code has been sent to your email. Please check it",title = "Alert")
            }
            EnumStatus.VERIFY -> {
                btnUnlock?.text = getString(R.string.unlock_all_albums)
                onStopLoading(EnumStatus.UNLOCK_ALBUMS)
                if (presenter?.mListCategories != null) {
                    var i = 0
                    while (i < presenter?.mListCategories?.size!!) {
                        presenter?.mListCategories?.get(i)?.pin = ""
                        presenter?.mListCategories?.get(i)?.let { SQLHelper.updateCategory(it) }
                        i++
                    }
                }
                SingletonPrivateFragment.getInstance()?.onUpdateView()
                Utils.onAlertNotify(this,"Unlocked Album",getString(R.string.unlocked_successful),object  : UtilsListener {
                    override fun onNegative() {
                        TODO("Not yet implemented")
                    }
                    override fun onPositive() {
                        finish()
                    }
                })
            }
            else -> {
                btnSendRequest?.isEnabled = true
                btnUnlock?.isEnabled = true
            }
        }
    }

    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
}