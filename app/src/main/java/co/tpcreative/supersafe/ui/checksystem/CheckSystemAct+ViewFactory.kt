package co.tpcreative.supersafe.ui.checksystem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.viewmodel.CheckSystemViewModel
import kotlinx.android.synthetic.main.activity_check_system.*
import kotlinx.android.synthetic.main.activity_check_system.toolbar

fun CheckSystemAct.initUI(){
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.hide()
    handler?.postDelayed(Runnable { checkUserCloud() }, 5000)
    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()

    viewModel.isLoading.observe(this,{
        if (it){
            progressBarCircularIndeterminate?.visibility = View.VISIBLE
            progressBarCircularIndeterminate?.setBackgroundColor(ContextCompat.getColor(this,themeApp?.getAccentColor()!!))
        }else{
            progressBarCircularIndeterminate?.visibility = View.INVISIBLE
        }
    })
    viewModel.isLoading.postValue(true)
}

private fun CheckSystemAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(CheckSystemViewModel::class.java)
}

fun CheckSystemAct.checkUserCloud() {
    viewModel.checkUserCloud().observe(this, Observer{
        when(it.status){
            Status.SUCCESS -> {
                Utils.Log(TAG,"Success ${it.toJson()}")
                Navigator.onEnableCloud(this)
            }
            else -> {
                Utils.Log(TAG,"Nothing")
                Navigator.onEnableCloud(this)
            }
        }
    })
}

fun CheckSystemAct.addUserCloud() {
    viewModel.addUserCloud().observe(this, Observer{
        when(it.status){
            Status.SUCCESS -> {
                onBackPressed()
                Utils.Log(TAG,"Success ${it.toJson()}")
            }
            else -> {
                Utils.Log(TAG,"Nothing")
                Navigator.onEnableCloud(this)
            }
        }
    })
}