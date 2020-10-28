package co.tpcreative.supersafe.ui.settings
import kotlinx.android.synthetic.main.activity_settings.*

fun SettingsAct.initUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
}