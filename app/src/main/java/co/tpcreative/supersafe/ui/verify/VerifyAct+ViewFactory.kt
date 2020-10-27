package co.tpcreative.supersafe.ui.verify
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import kotlinx.android.synthetic.main.activity_verify.*

fun VerifyAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    presenter = VerifyPresenter()
    presenter?.bindView(this)
    presenter?.getIntent(this)
    val result: String? = presenter?.user?.email?.let { Utils.getFontString(R.string.verify_title, it) }
    tvTitle?.text = result?.toSpanned()
    edtCode?.setOnEditorActionListener(this)
    edtCode?.addTextChangedListener(mTextWatcher)
    btnLogin.setOnClickListener {
        if (isNext) {
            onVerifyCode()
        }
    }
    btnReSend.setOnClickListener {
        val request = VerifyCodeRequest()
        request.user_id = presenter?.user?.email
        presenter?.onResendCode(request)
    }
}

fun VerifyAct.onVerifyCode() {
    val request = VerifyCodeRequest()
    request.code = edtCode.text.toString().trim({ it <= ' ' })
    request.user_id = presenter?.user?.email
    request._id = presenter?.user?._id
    request.device_id = SuperSafeApplication.getInstance().getDeviceId()
    presenter?.onVerifyCode(request)
    Utils.hideSoftKeyboard(this)
}