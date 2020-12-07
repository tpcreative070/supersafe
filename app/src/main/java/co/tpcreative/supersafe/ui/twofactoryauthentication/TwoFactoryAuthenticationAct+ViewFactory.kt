package co.tpcreative.supersafe.ui.twofactoryauthentication
import android.text.InputType
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.NetworkUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumValidationKey
import co.tpcreative.supersafe.viewmodel.EnumTwoFactoryAuthentication
import co.tpcreative.supersafe.viewmodel.TwoFactoryAuthenticationViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import kotlinx.android.synthetic.main.activity_two_factory_authentication.*
import kotlinx.android.synthetic.main.activity_two_factory_authentication.progressBarCircularIndeterminate
import kotlinx.android.synthetic.main.activity_two_factory_authentication.toolbar

fun TwoFactoryAuthenticationAct.initUI(){
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
        if (status==EnumTwoFactoryAuthentication.GENERATE){
            status = EnumTwoFactoryAuthentication.REQUEST_GENERATE
            onShowUI()
        }
        else if (status==EnumTwoFactoryAuthentication.REQUEST_GENERATE){
            viewModel.isEnabled = true
            addTwoFactoryAuthentication()
            Utils.hideSoftKeyboard(this)
        }
        else if (status== EnumTwoFactoryAuthentication.CHANGE){
            alertAskInputSecretPin()
        }
        else if (status ==EnumTwoFactoryAuthentication.REQUEST_CHANGE){
           changeTwoFactoryAuthentication()
            Utils.hideSoftKeyboard(this)
        }else {
            edtSecretPin.visibility = View.VISIBLE
            btnChange.text =  getString(R.string.change)
        }
    }
    if (NetworkUtil.pingIpAddress(this)){
        Utils.onBasicAlertNotify(this,"Alert","No Internet.Please check network connection")
    }else{
        getTwoFactoryAuthentication()
    }
    btnSwitch.setOnClickListener {
        if (btnSwitch.isChecked){
            status = EnumTwoFactoryAuthentication.ENABLE
            alertAskInputSecretPin()
        }else{
            btnSwitch.isChecked = true
            status = EnumTwoFactoryAuthentication.DISABLE
            disableTwoFactoryAuthentication()
        }
        Utils.Log(TAG,"${btnSwitch.isChecked}")
    }

    rlSwitch.setOnClickListener {
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

fun TwoFactoryAuthenticationAct.onShowUI(){
    if (status == EnumTwoFactoryAuthentication.GENERATE){
        edtSecretPin.visibility = View.GONE
        btnChange.text = getString(R.string.generate_secret_pin)
        btnSwitch.isEnabled = false
        rlSwitch.isEnabled = false
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

private fun TwoFactoryAuthenticationAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(TwoFactoryAuthenticationViewModel::class.java)
}

fun TwoFactoryAuthenticationAct.getTwoFactoryAuthentication() {
    viewModel.getTwoFactoryInfo().observe(this, Observer {
        when(it.status){
            Status.SUCCESS-> {
                if (it.data?.error == true){
                    status = EnumTwoFactoryAuthentication.GENERATE
                }else{
                    status = EnumTwoFactoryAuthentication.CHANGE
                    val mResult = it.data?.data?.twoFactoryAuthentication
                    btnSwitch.isChecked = mResult?.isEnabled ?: false
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

fun TwoFactoryAuthenticationAct.switchStatusTwoFactoryAuthentication() {
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

fun TwoFactoryAuthenticationAct.verifyTwoFactoryAuthentication() {
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

fun TwoFactoryAuthenticationAct.addTwoFactoryAuthentication() {
    viewModel.addTwoFactoryAuthentication().observe(this, Observer {
        when(it.status){
            Status.SUCCESS-> {
                val mResult = it.data?.data?.twoFactoryAuthentication
                btnSwitch.isChecked = mResult?.isEnabled ?: false
                Utils.onBasicAlertNotify(this,"Alert",String.format(getString(R.string.secret_pin_was_created),viewModel.secretPin))
                status = EnumTwoFactoryAuthentication.CHANGE
                edtSecretPin?.text = null
                onShowUI()
            }
            else -> {
                Utils.Log(TAG,it.message)
                Utils.onBasicAlertNotify(this,"Alert",getString(R.string.server_error_occurred))
            }
        }
    })
}

fun TwoFactoryAuthenticationAct.changeTwoFactoryAuthentication() {
    Utils.Log(TAG,"Call here")
    viewModel.changeTwoFactoryAuthentication().observe(this, Observer {
        when(it.status){
            Status.SUCCESS-> {
                btnSwitch.isChecked = viewModel.isEnabled
                Utils.onBasicAlertNotify(this,"Alert",String.format(getString(R.string.secret_pin_was_changed),viewModel.newSecretPin))
                status = EnumTwoFactoryAuthentication.CHANGE
                edtSecretPin?.text = null
                onShowUI()
            }
            else -> {
                Utils.Log(TAG,it.message)
                Utils.onBasicAlertNotify(this,"Alert",getString(R.string.server_error_occurred))
            }
        }
    })
}


fun TwoFactoryAuthenticationAct.disableTwoFactoryAuthentication() {
    val builder: MaterialDialog = MaterialDialog(this)
            .title(text = getString(R.string.confirm))
            .message(res = R.string.are_you_sure_you_want_disable_two_factory_authentication)
            .negativeButton(text = getString(R.string.cancel))
            .positiveButton(text = getString(R.string.ok))
            .positiveButton {
                viewModel.isEnabled = false
                alertAskInputSecretPin()
            }
            .negativeButton {
                Utils.Log(TAG,"clicked negative button")
            }
    builder.show()
}

fun TwoFactoryAuthenticationAct.alertAskInputSecretPin() {
    var mMessage = ""
    if (status == EnumTwoFactoryAuthentication.ENABLE){
       mMessage = getString(R.string.verify_secret_pin_to_enable_two_factory_authentication)
    }
    else if (status == EnumTwoFactoryAuthentication.DISABLE){
        mMessage = getString(R.string.verify_secret_pin_to_disable_two_factory_authentication)
    }
    else if (status == EnumTwoFactoryAuthentication.CHANGE){
        mMessage = getString(R.string.verify_secret_pin)
    }
    else{
        Utils.Log(TAG,"Nothing")
    }
    val builder: MaterialDialog = MaterialDialog(this)
            .title(R.string.key_alert)
            .message(text = mMessage)
            .negativeButton(R.string.cancel)
            .cancelable(true)
            .cancelOnTouchOutside(false)
            .negativeButton {
               getTwoFactoryAuthentication()
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
    builder.show()
}



