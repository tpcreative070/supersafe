package co.tpcreative.supersafe.ui.signin
import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.encypt.SecurityUtil
import co.tpcreative.supersafe.common.extension.getUserInfo
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumValidationKey
import co.tpcreative.supersafe.model.User
import co.tpcreative.supersafe.ui.twofactorauthentication.*
import co.tpcreative.supersafe.viewmodel.UserViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.activity_signin.edtEmail
import kotlinx.android.synthetic.main.activity_signin.progressBarCircularIndeterminate
import kotlinx.android.synthetic.main.activity_signin.toolbar
import kotlinx.android.synthetic.main.activity_signin.tvSupport
import kotlinx.android.synthetic.main.activity_signin.view.*

fun SignInAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    setupViewModel()
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    ServiceManager.getInstance()?.onStartService()
    edtEmail?.setOnEditorActionListener(this)
    edtEmail?.addTextChangedListener(mTextWatcher)
    val support: String? = Utils.getFontString(R.string.send_an_email_to, SecurityUtil.MAIL)
    tvSupport?.text = support?.toSpanned()
    llSupport.visibility = View.INVISIBLE
    btnNext.setOnClickListener {
        if (!SuperSafeReceiver.isConnected()) {
            Utils.showDialog(this, getString(R.string.internet))
            return@setOnClickListener
        }
        if (isNext) {
            onSignIn()
        }
    }

    viewModel.errorMessages.observe( this,{ mResult->
        Utils.Log(TAG,"observe...")
        mResult?.let {
            if (it.isEmpty()){
                btnNext?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnNext?.setTextColor(ContextCompat.getColor(this,R.color.white))
                isNext = true
            }else{
                btnNext?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnNext?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                isNext = false
            }
        }
    })

    viewModel.isLoading.observe(this,{
        if (it){
            progressBarCircularIndeterminate?.visibility = View.VISIBLE
            btnNext?.visibility = View.INVISIBLE
        }else{
            progressBarCircularIndeterminate?.visibility = View.INVISIBLE
            btnNext?.visibility = View.VISIBLE
        }
    })

    viewModel.errorResponseMessage.observe(this,{mResult ->
        mResult?.let {
            edtEmail.error = it.get(EnumValidationKey.EDIT_TEXT_EMAIL.name)
            if (it.values.isEmpty()){
                btnNext?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnNext?.setTextColor(ContextCompat.getColor(this,R.color.white))
                isNext = true
            }else{
                btnNext?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnNext?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                isNext = false
            }
        }
    })
}

private fun SignInAct.showEnableView(mNext : Boolean){
    if (mNext){
        btnNext?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
        btnNext?.setTextColor(ContextCompat.getColor(this,R.color.white))
        isNext = true
    }else{
        btnNext?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
        btnNext?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
        isNext = false
    }
}

private fun SignInAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(UserViewModel::class.java)
}

fun SignInAct.onSignIn() {
    viewModel.signIn().observe(this, Observer{
        when(it.status){
            Status.SUCCESS -> {
                if (it.data?.data?.twoFactorAuthentication?.isEnabled ==true){
                    showEnableView(false)
                    alertAskInputSecretPin()
                }else{
                    val mUser: User? = Utils.getUserInfo()
                    Navigator.onMoveToVerify(this, mUser)
                    Utils.Log(TAG,"Success ${it.toJson()}")
                }
            }
            Status.ERROR -> {
                Utils.Log(TAG,"Error ${it.message}")
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    })
    Utils.hideSoftKeyboard(this)
}

fun SignInAct.verifyTwoFactoryAuthentication() {
    viewModel.verifyTwoFactoryAuthentication().observe(this, Observer {
        when(it.status){
            Status.SUCCESS-> {
                sendEmail()
            }
            else ->{
                Utils.Log(TAG,it.message)
                Utils.onBasicAlertNotify(this,"Alert",it.message ?:"")
                alertAskInputSecretPin()
                llSupport.visibility = View.VISIBLE
            }
        }
    })
}

fun SignInAct.sendEmail(){
    viewModel.sendEmail().observe(this, Observer {
        when(it.status){
            Status.SUCCESS ->{
                val mUser: User? = Utils.getUserInfo()
                Navigator.onMoveToVerify(this, mUser)
                Utils.Log(TAG,"Success ${it.toJson()}")
            }else  ->{
                  Utils.Log(TAG,it.message)
                  showEnableView(true)
            }
        }
    })
}

fun SignInAct.alertAskInputSecretPin() {
    val builder: MaterialDialog = MaterialDialog(this)
            .title(R.string.verify_secret_pin)
            .negativeButton(R.string.cancel)
            .cancelable(true)
            .cancelOnTouchOutside(false)
            .negativeButton {
                showEnableView(true)
            }
            .positiveButton(R.string.verify)
            .input(hintRes = R.string.enter_secret_pin, inputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD), allowEmpty = false,maxLength = 6){ dialog, text->
                viewModel.secretPin = text.toString()
                verifyTwoFactoryAuthentication()
            }
    val input: EditText = builder.getInputField()
    input.setPadding(0,50,0,20)
    input.setBackgroundColor( ContextCompat.getColor(this,R.color.transparent))
    builder.show()
}



