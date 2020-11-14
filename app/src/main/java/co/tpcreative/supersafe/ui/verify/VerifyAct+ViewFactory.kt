package co.tpcreative.supersafe.ui.verify
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumValidationKey
import co.tpcreative.supersafe.viewmodel.VerifyViewModel
import kotlinx.android.synthetic.main.activity_verify.*
import kotlinx.android.synthetic.main.activity_verify.progressBarCircularIndeterminate
import kotlinx.android.synthetic.main.activity_verify.toolbar

fun VerifyAct.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    val result: String? = Utils.getFontString(R.string.verify_title, Utils.getUserId() ?: "")
    tvTitle?.text = result?.toSpanned()
    edtCode?.setOnEditorActionListener(this)
    edtCode?.addTextChangedListener(mTextWatcher)
    btnLogin.setOnClickListener {
        if (isNext) {
            isVerify = true
            onVerifyCode()
        }
    }
    btnReSend.setOnClickListener {
        isVerify = false
        resendCode()
    }

    viewModel.isLoading.observe(this,{
        if (it){
            if (isVerify){
                progressBarCircularIndeterminate?.visibility = View.VISIBLE
                btnLogin?.visibility = View.INVISIBLE
            }else{
                progressBarCircularIndeterminateReSend?.visibility = View.VISIBLE
                btnReSend.visibility = View.INVISIBLE
            }
        }else{
            if (isVerify){
                progressBarCircularIndeterminate?.visibility = View.INVISIBLE
                btnLogin?.visibility = View.VISIBLE
            }else{
                progressBarCircularIndeterminateReSend?.visibility = View.INVISIBLE
                btnReSend.visibility = View.VISIBLE
            }
        }
    })

    viewModel.errorMessages.observe(this,{mResult ->
        mResult?.let {
            if (it.values.isEmpty()){
                btnLogin?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnLogin?.setTextColor(ContextCompat.getColor(this,R.color.white))
                isNext = true
            }else{
                btnLogin?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnLogin?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                isNext = false
            }
        }
    })

    viewModel.errorResponseMessage.observe(this,{mResult ->
        mResult?.let {
            edtCode.error = it.get(EnumValidationKey.EDIT_TEXT_CODE.name)
            if (it.values.isEmpty()){
                btnLogin?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnLogin?.setTextColor(ContextCompat.getColor(this,R.color.white))
                isNext = true
            }else{
                btnLogin?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnLogin?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                isNext = false
            }
        }
    })
}

fun VerifyAct.onVerifyCode() {
    viewModel.verifyCode().observe(this, Observer{
        when(it.status){
            Status.SUCCESS -> {
                Navigator.onMoveSetPin(this, EnumPinAction.NONE)
                finish()
                Utils.Log(TAG,"Success ${it.toJson()}")
            }
            Status.ERROR -> {
                Utils.Log(TAG,"Error ${it.message}")
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    })
    Utils.hideSoftKeyboard(this)
}

private fun VerifyAct.resendCode(){
    viewModel.resendCode().observe(this, Observer{
        when(it.status){
            Status.SUCCESS -> {
                Utils.Log(TAG,"Success ${it.toJson()}")
            }
            Status.ERROR -> {
                Utils.Log(TAG,"Error ${it.message}")
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    })
}

private fun VerifyAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(VerifyViewModel::class.java)
}
