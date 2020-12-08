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
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumValidationKey
import co.tpcreative.supersafe.model.User
import co.tpcreative.supersafe.ui.main_tab.onShowDialog
import co.tpcreative.supersafe.ui.twofactoryauthentication.*
import co.tpcreative.supersafe.viewmodel.EnumTwoFactoryAuthentication
import co.tpcreative.supersafe.viewmodel.UserViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.activity_signin.edtEmail
import kotlinx.android.synthetic.main.activity_signin.progressBarCircularIndeterminate
import kotlinx.android.synthetic.main.activity_signin.toolbar
import kotlinx.android.synthetic.main.activity_two_factory_authentication.*

fun SignInAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    setupViewModel()
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
                if (it.data?.data?.twoFactoryAuthentication?.isEnabled ==true){
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
            }else  -> Utils.Log(TAG,it.message)
        }
    })
}

fun SignInAct.alertAskInputSecretPin() {
    val builder: MaterialDialog = MaterialDialog(this)
            .title(R.string.key_alert)
            .message(text = getString(R.string.verify_secret_pin))
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
    input.setBackgroundColor( ContextCompat.getColor(this,R.color.transparent))
    builder.show()
}



