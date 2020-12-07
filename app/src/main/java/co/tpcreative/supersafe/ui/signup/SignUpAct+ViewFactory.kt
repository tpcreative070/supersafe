package co.tpcreative.supersafe.ui.signup
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumValidationKey
import co.tpcreative.supersafe.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_sign_up.edtEmail
import kotlinx.android.synthetic.main.activity_sign_up.progressBarCircularIndeterminate
import kotlinx.android.synthetic.main.activity_sign_up.toolbar

fun SignUpAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    setupViewModel()
    viewModel.getIntent(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    edtEmail?.addTextChangedListener(mTextWatcher)
    edtEmail?.setOnEditorActionListener(this)
    viewModel.errorMessages.observe( this,{mResult->
        mResult?.let {
            if (it.values.isEmpty()){
                btnFinish?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnFinish?.setTextColor(ContextCompat.getColor(this,R.color.white))
                isNext = true
            }else{
                Utils.Log(TAG,"log -> $it")
                btnFinish?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnFinish?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                isNext = false
            }
        }
    })

    viewModel.isLoading.observe(this,{
        if (it){
            progressBarCircularIndeterminate?.visibility = View.VISIBLE
            btnFinish?.visibility = View.INVISIBLE
        }else{
            progressBarCircularIndeterminate?.visibility = View.INVISIBLE
            btnFinish?.visibility = View.VISIBLE
        }
    })

    viewModel.errorResponseMessage.observe(this,{mResult ->
        mResult?.let {
            edtEmail.error = it[EnumValidationKey.EDIT_TEXT_EMAIL.name]
            if (it.values.isEmpty()){
                btnFinish?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
                btnFinish?.setTextColor(ContextCompat.getColor(this,R.color.white))
                isNext = true
            }else{
                btnFinish?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
                btnFinish?.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
                isNext = false
            }
        }
    })

    btnFinish.setOnClickListener {
        if (!SuperSafeReceiver.isConnected()) {
            Utils.showDialog(this, getString(R.string.internet))
            return@setOnClickListener
        }
        if (isNext) {
            onSignUp()
        }
    }
}

private fun SignUpAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(UserViewModel::class.java)
}

fun SignUpAct.onSignUp() {
    viewModel.signUp().observe(this,{
        when(it.status){
            Status.SUCCESS -> {
                SuperSafeApplication.getInstance().checkingMigrationAfterVerifiedPin(viewModel.pinValue,true,true)
                Navigator.onMoveToMainTab(this,true)
                finish()
            }
            Status.ERROR -> {
                Utils.Log(TAG,"Error ${it.message}")
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    })
    Utils.hideSoftKeyboard(this)
}
