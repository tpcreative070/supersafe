package co.tpcreative.supersafe.ui.resetpin
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.SingletonResetPin
import co.tpcreative.supersafe.common.encypt.SecurityUtil
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.NetworkUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.ui.main_tab.onShowDialog
import co.tpcreative.supersafe.viewmodel.ResetPinViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import kotlinx.android.synthetic.main.activity_reset_pin.*
import kotlinx.android.synthetic.main.activity_reset_pin.btnSendRequest
import kotlinx.android.synthetic.main.activity_reset_pin.edtCode
import kotlinx.android.synthetic.main.activity_reset_pin.toolbar
import kotlinx.android.synthetic.main.activity_reset_pin.tvStep1

fun ResetPinAct.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    val result: String? = Utils.getFontString(R.string.request_an_access_code, Utils.getUserId() ?: "")
    tvStep1?.text = result?.toSpanned()
    val support: String? = Utils.getFontString(R.string.send_an_email_to, SecurityUtil.MAIL)
    tvSupport?.text = support?.toSpanned()
    edtCode?.addTextChangedListener(mTextWatcher)
    edtCode?.setOnEditorActionListener(this)
    try {
        val bundle: Bundle? = intent.extras
        isRestoreFiles = bundle?.get(ResetPinAct::class.java.simpleName) as Boolean
    } catch (e: Exception) {
        e.printStackTrace()
    }

    btnReset.setOnClickListener {
        verifyCode()
    }

    btnSendRequest.setOnClickListener {
        if (NetworkUtil.pingIpAddress(this)){
            Utils.onBasicAlertNotify(this,"Alert","No Internet.Please check network connection")
        }else{
            getTwoFactoryAuthentication()
        }
    }

    llSupport.setOnClickListener {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + SecurityUtil.MAIL))
            intent.putExtra(Intent.EXTRA_SUBJECT, "SuperSafe App Support")
            intent.putExtra(Intent.EXTRA_TEXT, "")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    viewModel.isLoading.observe(this, Observer {
        if (it){
            if (progressing == EnumStepProgressing.REQUEST_CODE){
                progressbar_circular_request_code?.visibility = View.VISIBLE
            }else{
                progressbar_circular_reset_pin.visibility = View.VISIBLE
            }
        }else{
            if (progressing == EnumStepProgressing.VERIFY_CODE){
                progressbar_circular_request_code?.visibility = View.INVISIBLE
            }else{
                progressbar_circular_reset_pin.visibility = View.INVISIBLE
            }
        }
    })

    viewModel.errorMessages.observe(this,{mResult ->
        mResult?.let {
            if (it.values.isEmpty()){
                btnReset?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnReset?.setTextColor(ContextCompat.getColor(this,R.color.white))
                btnReset?.isEnabled = true
                isNext = true
            }else{
                btnReset?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnReset?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                btnReset?.isEnabled = false
                isNext = false
            }
        }
    })

    viewModel.errorResponseMessage.observe(this,{mResult ->
        mResult?.let {
            edtCode.error = it[EnumValidationKey.EDIT_TEXT_CODE.name]
            if (it.values.isEmpty()){
                btnReset?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnReset?.setTextColor(ContextCompat.getColor(this,R.color.white))
                btnReset?.isEnabled = true
                isNext = true
            }else{
                btnReset?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnReset?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                btnReset?.isEnabled = false
                isNext = false
            }
        }
    })
}

private fun ResetPinAct.sendRequestCode(){
    btnSendRequest?.isEnabled = false
    btnSendRequest?.text = ""
    progressing = EnumStepProgressing.REQUEST_CODE
    viewModel.sendRequestCode().observe(this, Observer{
        when(it.status){
            Status.SUCCESS -> {
                onShowDialogWaitingCode()
            }
            else -> {
                Utils.onBasicAlertNotify(this,getString(R.string.key_alert),getString(R.string.request_code_occurred_error))
            }
        }
        btnSendRequest?.isEnabled = true
        btnSendRequest.text = getString(R.string.send_verification_code)
    })
}

fun ResetPinAct.verifyCode(){
    progressing = EnumStepProgressing.VERIFY_CODE
    btnReset?.isEnabled = false
    btnReset?.text = ""
    viewModel.verifyCode().observe(this, Observer {
        when(it.status){
            Status.SUCCESS -> {
                if (isRestoreFiles!!) {
                    Navigator.onMoveToResetPin(this, EnumPinAction.RESTORE)
                } else {
                    Navigator.onMoveToResetPin(this, EnumPinAction.NONE)
                }
            }
            else -> {
                Utils.onBasicAlertNotify(this,getString(R.string.key_alert),getString(R.string.verify_occurred_error))
            }
        }
        btnReset?.isEnabled = true
        btnReset?.text = getString(R.string.reset_pin)
    })
}

fun ResetPinAct.onShowDialogWaitingCode() {
    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    MaterialStyledDialog.Builder(this)
            .setTitle(R.string.send_code_later)
            .setDescription(R.string.send_code_later_detail)
            .setHeaderDrawable(R.drawable.baseline_email_white_48)
            .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
            .setHeaderColor(themeApp?.getPrimaryColor()!!)
            .setCancelable(true)
            .setPositiveText(R.string.continue_value)
            .setNegativeText(R.string.cancel)
            .setCheckBox(false, R.string.enable_cloud)
            .onPositive {
                Utils.Log(TAG, "positive")
                SingletonResetPin.getInstance()?.onStartTimer(300000)
                btnSendRequest.isEnabled = false
                btnReset.isEnabled = false
                edtCode.isEnabled = false
            }
            .onNegative { finish() }
            .show()
}

private fun ResetPinAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(ResetPinViewModel::class.java)
}

fun ResetPinAct.verifyTwoFactoryAuthentication() {
    viewModel.verifyTwoFactoryAuthentication().observe(this, Observer {
        when(it.status){
            Status.SUCCESS-> {
                sendRequestCode()
            }
            else ->{
                Utils.Log(TAG,it.message)
                Utils.onBasicAlertNotify(this,"Alert",it.message ?:"")
                alertAskInputSecretPin()
            }
        }
    })
}

fun ResetPinAct.getTwoFactoryAuthentication() {
    viewModel.getTwoFactoryInfo().observe(this, Observer {
        when(it.status){
            Status.SUCCESS-> {
                if (it.data?.data?.twoFactoryAuthentication?.isEnabled==true){
                    alertAskInputSecretPin()
                }else{
                    sendRequestCode()
                }
            }
            else -> {
                Utils.Log(TAG,it.message)
            }
        }
    })
}

fun ResetPinAct.alertAskInputSecretPin() {
    val builder: MaterialDialog = MaterialDialog(this)
            .title(R.string.verify_secret_pin)
            .negativeButton(R.string.cancel)
            .cancelable(true)
            .cancelOnTouchOutside(false)
            .negativeButton {

            }
            .positiveButton(R.string.verify)
            .input(hintRes = R.string.enter_secret_pin, inputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD), allowEmpty = false){ dialog, text->
                viewModel.secretPin = text.toString()
                verifyTwoFactoryAuthentication()
            }
    val input: EditText = builder.getInputField()
    input.setPadding(0,50,0,20)
    input.setBackgroundColor( ContextCompat.getColor(this,R.color.transparent))
    builder.show()
}



