package co.tpcreative.supersafe.ui.twofactorauthentication
import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.isEnabledTwoFactoryAuthentication
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.NetworkUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumValidationKey
import co.tpcreative.supersafe.viewmodel.EnumTwoFactoryAuthentication
import co.tpcreative.supersafe.viewmodel.TwoFactorAuthenticationViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import kotlinx.android.synthetic.main.activity_two_factor_authentication.*
import kotlinx.android.synthetic.main.activity_two_factor_authentication.progressBarCircularIndeterminate
import kotlinx.android.synthetic.main.activity_two_factor_authentication.toolbar

fun TwoFactorAuthenticationAct.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    btnSwitch?.setOnCheckedChangeListener(this)
    edtSecretPin?.setOnEditorActionListener(this)
    edtSecretPin?.addTextChangedListener(mTextWatcher)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    title = getString(R.string.secret_pin)
    btnSwitch?.isChecked = Utils.isEnabledTwoFactoryAuthentication()
    btnChange.setOnClickListener {
        if (NetworkUtil.pingIpAddress(this)){
            Utils.onBasicAlertNotify(this,"Alert",getString(R.string.no_connection))
            return@setOnClickListener
        }
        if (status==EnumTwoFactoryAuthentication.GENERATE){
            status = EnumTwoFactoryAuthentication.REQUEST_GENERATE
            onShowUI()
        }
        else if (status==EnumTwoFactoryAuthentication.REQUEST_GENERATE){
            if (isNext){
                viewModel.isEnabled = true
                addTwoFactoryAuthentication()
                Utils.hideSoftKeyboard(this)
            }
        }
        else if (status== EnumTwoFactoryAuthentication.CHANGE){
            alertAskInputSecretPin()
        }
        else if (status ==EnumTwoFactoryAuthentication.REQUEST_CHANGE){
            if (isNext){
                changeTwoFactoryAuthentication()
                Utils.hideSoftKeyboard(this)
            }
        }
        else if (status == EnumTwoFactoryAuthentication.ENABLE || status ==EnumTwoFactoryAuthentication.DISABLE){
            status = EnumTwoFactoryAuthentication.CHANGE
            alertAskInputSecretPin()
        }
        else {
            edtSecretPin.visibility = View.VISIBLE
            btnChange.text =  getString(R.string.change)
        }
    }
    if (NetworkUtil.pingIpAddress(this)){
        Utils.onBasicAlertNotify(this,"Alert",getString(R.string.no_connection))
    }else{
        getTwoFactoryAuthentication()
    }
    btnSwitch.setOnClickListener {
        if (NetworkUtil.pingIpAddress(this)){
            Utils.onBasicAlertNotify(this,"Alert",getString(R.string.no_connection))
            return@setOnClickListener
        }
        if (btnSwitch.isChecked){
            status = EnumTwoFactoryAuthentication.ENABLE
            alertAskInputSecretPin()
        }else{
            status = EnumTwoFactoryAuthentication.DISABLE
            disableTwoFactoryAuthentication()
        }
        Utils.Log(TAG,"${btnSwitch.isChecked}")
    }

    rlSwitch.setOnClickListener {
        if (NetworkUtil.pingIpAddress(this)){
              Utils.onBasicAlertNotify(this,"Alert",getString(R.string.no_connection))
            return@setOnClickListener
        }
        btnSwitch.isChecked = !btnSwitch.isChecked
        if (btnSwitch.isChecked){
            status = EnumTwoFactoryAuthentication.ENABLE
            alertAskInputSecretPin()
        }else{
            btnSwitch.isChecked = true
            status = EnumTwoFactoryAuthentication.DISABLE
            disableTwoFactoryAuthentication()
        }
    }

    viewModel.errorMessages.observe( this,{ mResult->
        Utils.Log(TAG,"observe...")
        mResult?.let {
            if (it.isEmpty()){
                btnChange?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnChange?.setTextColor(ContextCompat.getColor(this,R.color.white))
                edtSecretPin.error = null
                isNext = true
            }else{
                btnChange?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnChange?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                isNext = false
                edtSecretPin.error = it[EnumValidationKey.EDIT_TEXT_SECRET_PIN.name]
            }
        }
    })

    viewModel.isLoading.observe(this,{
        if (it){
            progressBarCircularIndeterminate?.visibility = View.VISIBLE
            btnChange?.visibility = View.INVISIBLE
        }else{
            progressBarCircularIndeterminate?.visibility = View.INVISIBLE
            btnChange?.visibility = View.VISIBLE
        }
    })

    viewModel.errorResponseMessage.observe(this,{mResult ->
        mResult?.let {
            edtSecretPin.error = it[EnumValidationKey.EDIT_TEXT_SECRET_PIN.name]
            if (it.values.isEmpty()){
                btnChange?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnChange?.setTextColor(ContextCompat.getColor(this,R.color.white))
                isNext = true
            }else{
                btnChange?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnChange?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                isNext = false
            }
        }
    })
    onShowUI()
}

fun TwoFactorAuthenticationAct.onShowUI(){
    if (status == EnumTwoFactoryAuthentication.GENERATE){
        edtSecretPin.visibility = View.GONE
        btnChange.text = getString(R.string.generate_secret_pin)
        btnSwitch.isEnabled = false
        rlSwitch.isEnabled = false
        isNext = false
    }
    else if (status==EnumTwoFactoryAuthentication.REQUEST_GENERATE){
        edtSecretPin.visibility = View.VISIBLE
        edtSecretPin.hint = getString(R.string.enter_secret_pin)
        btnChange.text = getString(R.string.generate_secret_pin)
        btnSwitch.isEnabled = false
        rlSwitch.isEnabled = false
    }
    else if(status==EnumTwoFactoryAuthentication.CHANGE){
        edtSecretPin.visibility = View.GONE
        btnChange.text = getString(R.string.change_secret_pin)
        btnSwitch.isEnabled = true
        rlSwitch.isEnabled = true
        isNext = false
    }
    else if (status == EnumTwoFactoryAuthentication.REQUEST_CHANGE){
        edtSecretPin.visibility = View.VISIBLE
        edtSecretPin.hint = getString(R.string.enter_new_secret_pin)
        btnChange.text = getString(R.string.send_request)
        btnSwitch.isEnabled = true
        rlSwitch.isEnabled = true
    }
    else{
        edtSecretPin.visibility = View.GONE
        btnChange.visibility = View.GONE
        btnSwitch.isEnabled = false
        rlSwitch.isEnabled = false
    }
}

private fun TwoFactorAuthenticationAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(TwoFactorAuthenticationViewModel::class.java)
}

fun TwoFactorAuthenticationAct.getTwoFactoryAuthentication() {
    viewModel.getTwoFactoryInfo().observe(this, Observer {
        when(it.status){
            Status.SUCCESS-> {
                if (it.data?.error == true){
                    status = EnumTwoFactoryAuthentication.GENERATE
                }else{
                    status = EnumTwoFactoryAuthentication.CHANGE
                    val mResult = it.data?.data?.twoFactorAuthentication
                    btnSwitch.isChecked = mResult?.isEnabled ?: false
                    if (mResult?.isEnabled==true){
                        tvStatus.text = getString(R.string.enabled)
                    }else{
                        tvStatus.text = getString(R.string.disable)
                    }
                }
                onShowUI()
            }
            else -> {
                status = EnumTwoFactoryAuthentication.NONE
                Utils.Log(TAG,it.message)
            }
        }
    })
}

fun TwoFactorAuthenticationAct.switchStatusTwoFactoryAuthentication() {
    viewModel.switchStatusTwoFactoryInfo().observe(this, Observer {
        when(it.status){
            Status.SUCCESS-> {
                btnSwitch.isChecked = viewModel.isEnabled
            }
            else -> {
                Utils.Log(TAG,it.message)
                Utils.onBasicAlertNotify(this,"Alert",getString(R.string.server_error_occurred))
            }
        }
    })
}

fun TwoFactorAuthenticationAct.verifyTwoFactoryAuthentication() {
    viewModel.verifyTwoFactoryAuthentication().observe(this, Observer {
        when(it.status){
            Status.SUCCESS-> {
                when(status){
                    EnumTwoFactoryAuthentication.ENABLE ->{
                        viewModel.isEnabled = true
                        switchStatusTwoFactoryAuthentication()
                    }
                    EnumTwoFactoryAuthentication.DISABLE ->{
                        viewModel.isEnabled = false
                        switchStatusTwoFactoryAuthentication()
                    }
                    EnumTwoFactoryAuthentication.CHANGE ->{
                        status = EnumTwoFactoryAuthentication.REQUEST_CHANGE
                        onShowUI()
                    }
                    else -> Utils.Log(TAG,"Nothing")
                }
            }
            else ->{
                Utils.Log(TAG,it.message)
                Utils.onBasicAlertNotify(this,"Alert",it.message ?:"")
                getTwoFactoryAuthentication()
                when(status){
                    EnumTwoFactoryAuthentication.ENABLE ->{
                        btnSwitch.isChecked = false
                    }
                    EnumTwoFactoryAuthentication.DISABLE ->{
                        btnSwitch.isChecked = false
                    }
                    else -> Utils.Log(TAG,"Nothing")
                }
            }
        }
    })
}

fun TwoFactorAuthenticationAct.addTwoFactoryAuthentication() {
    viewModel.addTwoFactoryAuthentication().observe(this, Observer {
        when(it.status){
            Status.SUCCESS-> {
                val mResult = it.data?.data?.twoFactorAuthentication
                btnSwitch.isChecked = mResult?.isEnabled ?: false
                Utils.onBasicAlertNotify(this,"Alert",String.format(getString(R.string.secret_pin_was_created),viewModel.newSecretPin))
                if (mResult?.isEnabled==true){
                    tvStatus.text = getString(R.string.enabled)
                }else{
                    tvStatus.text = getString(R.string.disable)
                }
                viewModel.isEnabled = mResult?.isEnabled ?: false
                status = EnumTwoFactoryAuthentication.CHANGE
                onShowUI()
                disableUI()
                Utils.Log(TAG,it.toJson())
            }
            else -> {
                Utils.Log(TAG,it.message)
                Utils.onBasicAlertNotify(this,"Alert",getString(R.string.server_error_occurred))
            }
        }
    })
}

fun TwoFactorAuthenticationAct.changeTwoFactoryAuthentication() {
    Utils.Log(TAG,"Call here")
    viewModel.changeTwoFactoryAuthentication().observe(this, Observer {
        when(it.status){
            Status.SUCCESS-> {
                btnSwitch.isChecked = viewModel.isEnabled
                Utils.onBasicAlertNotify(this,"Alert",String.format(getString(R.string.secret_pin_was_changed),viewModel.newSecretPin))
                status = EnumTwoFactoryAuthentication.CHANGE
                if (viewModel.isEnabled){
                    tvStatus.text = getString(R.string.enabled)
                }else{
                    tvStatus.text = getString(R.string.disable)
                }
                onShowUI()
                disableUI()
            }
            else -> {
                Utils.Log(TAG,it.message)
                Utils.onBasicAlertNotify(this,"Alert",getString(R.string.server_error_occurred))
            }
        }
    })
}

fun TwoFactorAuthenticationAct.disableUI(){
    edtSecretPin.removeTextChangedListener(mTextWatcher)
    edtSecretPin.text?.clear()
    btnChange?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
    btnChange?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
    edtSecretPin?.addTextChangedListener(mTextWatcher)
}

fun TwoFactorAuthenticationAct.disableTwoFactoryAuthentication() {
    val builder: MaterialDialog = MaterialDialog(this)
            .title(text = getString(R.string.confirm))
            .message(res = R.string.are_you_sure_you_want_disable_two_factor_authentication)
            .negativeButton(text = getString(R.string.cancel))
            .positiveButton(text = getString(R.string.ok))
            .cancelable(false)
            .positiveButton {
                viewModel.isEnabled = false
                alertAskInputSecretPin()
            }
            .negativeButton {
                Utils.Log(TAG,"clicked negative button")
                //getTwoFactoryAuthentication()
                btnSwitch.isChecked = !btnSwitch.isChecked
            }
    builder.show()
}

fun TwoFactorAuthenticationAct.alertAskInputSecretPin() {
    var mMessage = ""
    if (status == EnumTwoFactoryAuthentication.ENABLE){
       mMessage = getString(R.string.verify_secret_pin_to_enable_two_factor_authentication)
    }
    else if (status == EnumTwoFactoryAuthentication.DISABLE){
        mMessage = getString(R.string.verify_secret_pin_to_disable_two_factor_authentication)
    }
    else if (status == EnumTwoFactoryAuthentication.CHANGE){
        mMessage = getString(R.string.verify_secret_pin)
    }
    else{
        Utils.Log(TAG,"Nothing")
    }
    val builder: MaterialDialog = MaterialDialog(this)
            .title(text = mMessage)
            .negativeButton(R.string.cancel)
            .cancelable(true)
            .cancelOnTouchOutside(false)
            .negativeButton {
                if (status == EnumTwoFactoryAuthentication.DISABLE || status == EnumTwoFactoryAuthentication.ENABLE){
                    btnSwitch.isChecked = !btnSwitch.isChecked
                }
            }
            .positiveButton(R.string.verify)
            .input(hintRes = R.string.enter_secret_pin, inputType = (InputType.TYPE_CLASS_NUMBER or InputType.TYPE_TEXT_VARIATION_PASSWORD),maxLength = 6, allowEmpty = false){ dialog, text->
                viewModel.secretPin = text.toString()
                if (status==EnumTwoFactoryAuthentication.DISABLE){
                    viewModel.isEnabled = false
                }
                else if(status == EnumTwoFactoryAuthentication.ENABLE){
                    viewModel.isEnabled = true
                }else {
                    Utils.Log(TAG,"Nothing")
                }
                verifyTwoFactoryAuthentication()
            }
    val input: EditText = builder.getInputField()
    input.setPadding(0,50,0,20)
    input.setBackgroundColor( ContextCompat.getColor(this,R.color.transparent))
    builder.show()
}



