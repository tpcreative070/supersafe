package co.tpcreative.supersafe.ui.help
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.getUserInfo
import co.tpcreative.supersafe.common.extension.setIconTint
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.viewmodel.HelpAndSupportViewModel
import kotlinx.android.synthetic.main.activity_help_and_support_content.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

fun HelpAndSupportContentAct.iniUI(){
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    getData()
    val mUser = Utils.getUserInfo()
    tvEmail?.text = mUser?.email
    edtSupport?.addTextChangedListener(mTextWatcher)
    edtSupport?.setOnEditorActionListener(this)
    viewModel.errorMessages.observe(this, Observer {
        it?.let {
            if (it.values.isEmpty()){
                isNext = true
                menuItem?.isEnabled = true
                menuItem?.setIconTint(R.color.white)
            }else{
                isNext = false
                menuItem?.isEnabled = false
                menuItem?.setIconTint(R.color.material_gray_700)
            }
        }
    })
    viewModel.isLoading.observe(this, Observer {
        if (it){
            onStartProgressing()
        }else{
            onStopProgressing()
        }
    })
}

fun HelpAndSupportContentAct.sendEmail(){
    viewModel.isLoading.postValue(true)
    viewModel.sendEmail().observe(this, Observer {
        CoroutineScope(Dispatchers.Main).launch {
            when(it.status){
                Status.SUCCESS -> {
                    viewModel.isLoading.postValue(false)
                    Utils.onBasicAlertNotify(this@sendEmail,getString(R.string.key_alert),getString(R.string.thank_you))
                }else -> {
                Utils.onBasicAlertNotify(this@sendEmail,getString(R.string.key_alert),getString(R.string.send_email_failed))
            }
            }
            edtSupport?.setText("")
        }
    })
}

fun HelpAndSupportContentAct.getData(){
    viewModel.getData(this).observe(this, Observer {
        CoroutineScope(Dispatchers.Main).launch {
            val mResult = async {
                contentHelpAndSupport = it
                if (contentHelpAndSupport?.content == getString(R.string.contact_support_content)) {
                    llEmail?.visibility = View.VISIBLE
                    edtSupport?.visibility = View.VISIBLE
                    webview?.visibility = View.GONE
                } else {
                    tvTitle?.text = contentHelpAndSupport?.title
                    llEmail?.visibility = View.GONE
                    edtSupport?.visibility = View.GONE
                    webview?.visibility = View.VISIBLE
                    webview?.loadUrl(contentHelpAndSupport?.content ?: "")
                }
            }
            mResult.await()
        }
    })
}

private fun HelpAndSupportContentAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(HelpAndSupportViewModel::class.java)
}
