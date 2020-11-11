package co.tpcreative.supersafe.ui.signup
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.services.SuperSafeReceiver
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.activity_sign_up.*

fun SignUpAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    setupViewModel()
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    edtEmail?.addTextChangedListener(mTextWatcher)
    edtEmail?.setOnEditorActionListener(this)

    viewModel.errorMessages.observe( this,{
        Utils.Log(TAG,"log...$it")
        if (it.isNotEmpty()){
            btnFinish?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
            btnFinish.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
            isNext = false
        }else{
            btnFinish?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
            btnFinish?.setTextColor(ContextCompat.getColor(this,R.color.white))
            isNext = true
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

    viewModel.errorResponseMessage.observe(this,{
        if (it.isNotEmpty()){
            edtEmail.error = it
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
