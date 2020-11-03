package co.tpcreative.supersafe.ui.signin
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.request.SignInRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import com.snatik.storage.security.SecurityUtil
import kotlinx.android.synthetic.main.activity_signin.*

fun SignInAct.initUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    presenter = SignInPresenter()
    presenter?.bindView(this)
    ServiceManager.getInstance()?.onStartService()
    edtEmail?.setOnEditorActionListener(this)
    edtEmail?.addTextChangedListener(mTextWatcher)
    btnNext.setOnClickListener {
        if (!SuperSafeReceiver.isConnected()) {
            Utils.showDialog(this, getString(R.string.internet))
            return@setOnClickListener
        }
        if (isNext) {
            onSignIn()
        }
    }
}

fun SignInAct.onSignIn() {
    val email: String = edtEmail?.text.toString().toLowerCase().trim({ it <= ' ' })
    val request = SignInRequest()
    request.user_id = email
    request.password = SecurityUtil.key_password_default_encrypted
    request.device_id = SuperSafeApplication.getInstance().getDeviceId()
    presenter?.onSignIn(request)
    Utils.hideSoftKeyboard(this)
}


