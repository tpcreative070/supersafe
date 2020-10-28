package co.tpcreative.supersafe.ui.signup
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.request.SignUpRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import com.snatik.storage.security.SecurityUtil
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.*

fun SignUpAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    presenter = SignUpPresenter()
    presenter?.bindView(this)
    edtName?.addTextChangedListener(mTextWatcher)
    edtEmail?.addTextChangedListener(mTextWatcher)
    edtEmail?.setOnEditorActionListener(this)
    edtName?.setOnEditorActionListener(this)
    isName = true
    edtName?.setText(getString(R.string.free))
    btnFinish.setOnClickListener {
        if (isName && isEmail) {
            onSignUp()
        }
    }
}

fun SignUpAct.onSignUp() {
    val email: String = edtEmail?.text.toString().toLowerCase(Locale.ROOT).trim({ it <= ' ' })
    val name: String = edtName?.text.toString().trim({ it <= ' ' })
    val request = SignUpRequest()
    request.user_id = email
    request.name = name
    request.password = SecurityUtil.key_password_default_encrypted
    request.device_id = SuperSafeApplication.getInstance().getDeviceId()
    presenter?.onSignUp(request)
    Utils.hideSoftKeyboard(this)
    Utils.Log(TAG, "onFished")
}