package co.tpcreative.supersafe.ui.verify
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import kotlinx.android.synthetic.main.activity_verify.*

fun VerifyAct.initUI(){
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
    request.code = edtCode.getText().toString().trim({ it <= ' ' })
    request.user_id = presenter?.user?.email
    request._id = presenter?.user?._id
    request.device_id = SuperSafeApplication.getInstance().getDeviceId()
    presenter?.onVerifyCode(request)
    Utils.hideSoftKeyboard(this)
}