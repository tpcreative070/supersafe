package co.tpcreative.supersafe.ui.settings
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.ui.settings.AlbumSettingsAct.Companion.viewModel
import co.tpcreative.supersafe.viewmodel.AlbumSettingsViewModel
import kotlinx.android.synthetic.main.activity_album_settings.*

fun AlbumSettingsAct.initUI(){
    val TAG = this::class.java.simpleName
    setupViewModel()
    getData()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

}

private fun AlbumSettingsAct.getData(){
    viewModel.getData(this).observe(this, Observer {
        title = it.categories_name
        onSetUpPreference()
    })
}

fun AlbumSettingsAct.getReload(){
    viewModel.getData().observe(this, Observer {
        title = it.categories_name
        onSetUpPreference()
    })
}

private fun AlbumSettingsAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(AlbumSettingsViewModel::class.java)
}
