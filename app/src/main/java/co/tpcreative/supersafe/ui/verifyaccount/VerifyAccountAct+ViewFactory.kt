package co.tpcreative.supersafe.ui.verifyaccount
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.VerifyAccountViewModel
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import kotlinx.android.synthetic.main.activity_verify_account.*
import kotlinx.android.synthetic.main.activity_verify_account.btnReSend
import kotlinx.android.synthetic.main.activity_verify_account.edtCode
import kotlinx.android.synthetic.main.activity_verify_account.progressBarCircularIndeterminateReSend
import kotlinx.android.synthetic.main.activity_verify_account.toolbar
import kotlinx.android.synthetic.main.activity_verify_account.tvTitle
import java.util.*

fun VerifyAccountAct.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    val result: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    imgEdit?.setColorFilter(ContextCompat.getColor(this,result?.getAccentColor()!!), PorterDuff.Mode.SRC_ATOP)
    edtCode?.addTextChangedListener(mTextWatcher)
    edtEmail?.addTextChangedListener(mTextWatcher)
    edtCode?.setOnEditorActionListener(this)
    edtEmail?.setOnEditorActionListener(this)
    tvEmail?.text = Utils.getUserId()
    val sourceString: String = getString(R.string.verify_title, "<font color='#9E9E9E'>" +Utils.getUserId() + "</font>")
    tvTitle?.text = sourceString.toSpanned()
    val theme: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    progressBarCircularIndeterminateSignIn?.setBackgroundColor(ContextCompat.getColor(this,theme?.getAccentColor()!!))
    progressBarCircularIndeterminateReSend?.setBackgroundColor(ContextCompat.getColor(this,theme?.getAccentColor()!!))
    progressBarCircularIndeterminateVerifyCode?.setBackgroundColor(ContextCompat.getColor(this,theme?.getAccentColor()!!))
    imgEdit.setOnClickListener {
        Utils.Log(TAG, "edit")
        onShowView(it)
        edtEmail?.requestFocus()
        edtEmail?.setText(Utils.getUserId())
        edtEmail?.setSelection(edtEmail?.length()!!)
    }

    btnSendVerifyCode.setOnClickListener {
        Utils.Log(TAG, "Send code")
        sendCode()
    }

    btnCancel.setOnClickListener {
        Utils.Log(TAG, "onCancel")
        val mUser = Utils.getUserInfo()
        viewModel.email = mUser?.email ?:""
        Utils.hideSoftKeyboard(this)
        onShowView(llAction!!)
    }

    btnSave.setOnClickListener {
        Utils.Log(TAG, "onSave")
        if (isNext) {
            changeEmail()
        }
    }

    tvPrivateCloud.setOnClickListener {
        val categories = Categories(0, getString(R.string.faq))
        val mList: MutableList<HelpAndSupport> = ArrayList<HelpAndSupport>()
        mList.add(HelpAndSupport(categories, getString(R.string.what_about_google_drive), getString(R.string.what_about_google_drive_content), null))
        Navigator.onMoveHelpAndSupportContent(this, mList[0])
    }

    btnReSend.setOnClickListener {
        try {
            resendCode()
            Utils.Log(TAG, "onResend")
        } catch (e: Exception) {
        }
    }

    btnSignIn.setOnClickListener {
        Utils.Log(TAG, "onSignIn")
        if (isNext) {
            /*Do something here*/
            verifyCode()
        }
    }

    viewModel.isLoading.observe(this,{
        if (it){
            if (progressing == EnumStepProgressing.VERIFY_CODE){
                progressBarCircularIndeterminateVerifyCode?.visibility = View.VISIBLE
                btnSendVerifyCode?.visibility = View.INVISIBLE
                btnSendVerifyCode?.text = ""
            }else{
                SingletonManagerProcessing.getInstance()?.onStartProgressing(this, R.string.progressing)
            }
        }else{
            if (progressing == EnumStepProgressing.VERIFY_CODE){
                progressBarCircularIndeterminateVerifyCode?.visibility = View.INVISIBLE
                btnSendVerifyCode?.visibility = View.VISIBLE
                btnSendVerifyCode?.text = getString(R.string.send_verification_code)
            }
            else{
                SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
            }
        }
    })

    viewModel.errorMessages.observe(this,{mResult ->
        mResult?.let {
            if (it.values.isEmpty()){
                btnSave?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnSave?.setTextColor(ContextCompat.getColor(this,R.color.white))

                btnSignIn?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnSignIn?.setTextColor(ContextCompat.getColor(this,R.color.white))

                isNext = true
            }else{
                btnSave?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnSave?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))

                btnSignIn?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnSignIn?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                isNext = false
            }
        }
    })

    viewModel.errorResponseMessage.observe(this,{mResult ->
        mResult?.let {
            edtCode.error = it.get(EnumValidationKey.EDIT_TEXT_CODE.name)
            edtEmail.error = it.get(EnumValidationKey.EDIT_TEXT_EMAIL.name)
            if (it.values.isEmpty()){
                btnSave?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnSave?.setTextColor(ContextCompat.getColor(this,R.color.white))

                btnSignIn?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnSignIn?.setTextColor(ContextCompat.getColor(this,R.color.white))
                isNext = true
            }else{
                btnSave?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnSave?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))

                btnSignIn?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnSignIn?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                isNext = false
            }
        }
    })
    val mUser = Utils.getUserInfo()
    Utils.Log(TAG,"Code ==> ${mUser?.code}")
}

fun VerifyAccountAct.onShowDialogEnableSync() {
    val theme: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    MaterialStyledDialog.Builder(this)
            .setTitle(R.string.enable_cloud_sync)
            .setDescription(R.string.message_prompt)
            .setHeaderDrawable(R.drawable.ic_drive_cloud)
            .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
            .setHeaderColor(theme?.getPrimaryColor()!!)
            .setCancelable(true)
            .setPositiveText(R.string.enable_now)
            .setNegativeText(R.string.cancel)
            .setCheckBox(false, R.string.enable_cloud)
            .onPositive {
                Utils.Log(TAG, "positive")
                Navigator.onCheckSystem(this, null)
            }
            .onNegative {  finish() }
            .show()
}

private fun VerifyAccountAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(VerifyAccountViewModel::class.java)
}

fun VerifyAccountAct.sendCode(){
    progressing = EnumStepProgressing.SEND_CODE
    viewModel.sendCode().observe(this, Observer{
        when(it.status){
            Status.SUCCESS -> {
                onShowView(btnSendVerifyCode!!)
                Utils.Log(TAG,"Current email ${viewModel.email}")
                Utils.onBasicAlertNotify(this,getString(R.string.key_alert),String.format(getString(R.string.we_sent_access_code_to_your_email),viewModel.email))
                Utils.Log(TAG,"Success ${it.toJson()}")
            }
            Status.ERROR -> {
                Utils.Log(TAG,"Error ${it}")
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    })
}

fun VerifyAccountAct.verifyCode() {
    progressing = EnumStepProgressing.VERIFY_CODE
    viewModel.verifyCode().observe(this, Observer{
        when(it.status){
            Status.SUCCESS -> {
                edtCode?.setText("")
                onShowDialogEnableSync()
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

private fun VerifyAccountAct.resendCode(){
    progressing = EnumStepProgressing.RESEND_CODE
    viewModel.resendCode().observe(this, Observer{
        when(it.status){
            Status.SUCCESS -> {
                onShowView(btnSendVerifyCode!!)
                Utils.Log(TAG,"Current email ${viewModel.email}")
                Utils.onBasicAlertNotify(this,getString(R.string.key_alert),String.format(getString(R.string.we_sent_access_code_to_your_email),viewModel.email))
                Utils.Log(TAG,"Success ${it.toJson()}")
            }
            Status.ERROR -> {
                Utils.Log(TAG,"Error ${it.message}")
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    })
}

fun VerifyAccountAct.changeEmail(){
    tvEmail?.text = viewModel.email
    var sourceString: String = getString(R.string.verify_title, "<font color='#9E9E9E'>${viewModel.email}</font>")
    tvTitle?.text = sourceString.toSpanned()
    if (viewModel.email == Utils.getUserId()) {
        return
    }
    progressing = EnumStepProgressing.CHANGE_EMAIL
    viewModel.changeEmail().observe(this, Observer{
        when(it.status){
            Status.SUCCESS -> {
                onShowView(llAction!!)
                Utils.Log(TAG,"Success ${it.toJson()}")
            }
            Status.ERROR -> {
                Utils.Log(TAG,"Error ${it.message}")
                viewModel.email = Utils.getUserId() ?: ""
                tvEmail?.text = viewModel.email
                sourceString = getString(R.string.verify_title, "<font color='#9E9E9E'>${viewModel.email}</font>")
                tvTitle?.text = sourceString.toSpanned()
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    })
    Utils.hideSoftKeyboard(this)
}
