package co.tpcreative.supersafe.ui.signin
import android.view.View
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
import co.tpcreative.supersafe.model.User
import co.tpcreative.supersafe.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.activity_signin.*

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
    viewModel.errorMessages.observe( this,{
        Utils.Log(TAG,"log...$it")
        if (it.isNotEmpty()){
            btnNext?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_disable_rounded)
            btnNext.setTextColor(ContextCompat.getColor(this,R.color.colorDisableText))
            isNext = false
        }else{
            btnNext?.background = ContextCompat.getDrawable(this,R.drawable.bg_button_rounded)
            btnNext?.setTextColor(ContextCompat.getColor(this,R.color.white))
            isNext = true
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

    viewModel.errorResponseMessage.observe(this,{
        if (it.isNotEmpty()){
            edtEmail.error = it
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
                val mUser: User? = Utils.getUserInfo()
                Navigator.onMoveToVerify(this, mUser)
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


