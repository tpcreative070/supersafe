package co.tpcreative.supersafe.ui.verifyaccount
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.request.VerifyCodeRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.Categories
import co.tpcreative.supersafe.model.HelpAndSupport
import co.tpcreative.supersafe.model.ThemeApp
import kotlinx.android.synthetic.main.activity_verify_account.*
import java.util.*

fun VerifyAccountAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    presenter = VerifyAccountPresenter()
    presenter?.bindView(this)
    imgEdit?.setColorFilter(ContextCompat.getColor(this, R.color.colorBackground), PorterDuff.Mode.SRC_ATOP)
    edtCode?.addTextChangedListener(mTextWatcher)
    edtEmail?.addTextChangedListener(mTextWatcher)
    edtCode?.setOnEditorActionListener(this)
    edtEmail?.setOnEditorActionListener(this)
    if (presenter?.mUser != null) {
        if (presenter?.mUser?.email != null) {
            tvEmail?.text = presenter?.mUser?.email
            val sourceString: String = getString(R.string.verify_title, "<font color='#000000'>" + presenter?.mUser?.email + "</font>")
            tvTitle?.text = sourceString.toSpanned()
        }
    }
    val theme: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    progressBarCircularIndeterminateSignIn?.setBackgroundColor(ContextCompat.getColor(this,theme?.getAccentColor()!!))
    progressBarCircularIndeterminateReSend?.setBackgroundColor(ContextCompat.getColor(this,theme?.getAccentColor()!!))
    progressBarCircularIndeterminateVerifyCode?.setBackgroundColor(ContextCompat.getColor(this,theme?.getAccentColor()!!))
    llGoogle.setOnClickListener {
        onShowDialog()
    }

    imgEdit.setOnClickListener {
        Utils.Log(TAG, "edit")
        onShowView(it)
        edtEmail?.requestFocus()
        edtEmail?.setText(presenter?.mUser?.email)
        edtEmail?.setSelection(edtEmail?.length()!!)
    }

    btnSendVerifyCode.setOnClickListener {
        Utils.Log(TAG, "Verify code")
        SingletonManagerProcessing.getInstance()?.onStartProgressing(this, R.string.progressing)
        presenter?.onCheckUser(presenter?.mUser?.email, presenter?.mUser?.other_email)
    }

    btnCancel.setOnClickListener {
        Utils.Log(TAG, "onCancel")
        Utils.hideSoftKeyboard(this)
        onShowView(llAction!!)
    }

    btnSave.setOnClickListener {
        Utils.Log(TAG, "onSave")
        if (isNext) {
            onChangedEmail()
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
            SingletonManagerProcessing.getInstance()?.onStartProgressing(this, R.string.progressing)
            Utils.hideSoftKeyboard(this)
            val request = VerifyCodeRequest()
            request.user_id = presenter?.mUser?.email
            presenter?.onResendCode(request)
            Utils.Log(TAG, "onResend")
        } catch (e: Exception) {
        }
    }

    btnSignIn.setOnClickListener {
        Utils.Log(TAG, "onSignIn")
        if (isNext) {
            /*Do something here*/
            onVerifyCode()
        }
    }
}


fun VerifyAccountAct.onChangedEmail() {
    val new_user_id: String = edtEmail?.text.toString().toLowerCase(Locale.ROOT).trim({ it <= ' ' })
    tvEmail?.text = new_user_id
    val sourceString: String = getString(R.string.verify_title, "<font color='#000000'>$new_user_id</font>")
    tvTitle?.text = sourceString.toSpanned()
    if (new_user_id == presenter?.mUser?.email) {
        return
    }
    val request = VerifyCodeRequest()
    request.new_user_id = new_user_id
    request.user_id = presenter?.mUser?.email
    request._id = presenter?.mUser?._id
    presenter?.onChangeEmail(request)
    Utils.hideSoftKeyboard(this)
    Utils.hideKeyboard(edtEmail)
}

fun VerifyAccountAct.onVerifyCode() {
    val request = VerifyCodeRequest()
    request.code = edtCode?.text.toString().trim({ it <= ' ' })
    request.user_id = presenter?.mUser?.email
    request._id = presenter?.mUser?._id
    request.device_id = SuperSafeApplication.getInstance().getDeviceId()
    presenter?.onVerifyCode(request)
    Utils.hideSoftKeyboard(this)
}