package co.tpcreative.supersafe.ui.settings
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.activity_album_settings.*

fun AlbumSettingsAct.initUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    AlbumSettingsAct.presenter = AlbumSettingsPresenter()
    AlbumSettingsAct.presenter?.bindView(this)
    AlbumSettingsAct.presenter?.getData(this)
    AlbumSettingsAct.storage = Storage(applicationContext)
    AlbumSettingsAct.storage?.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
}