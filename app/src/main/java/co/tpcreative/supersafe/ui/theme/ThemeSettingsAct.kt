package co.tpcreative.supersafe.ui.theme
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.viewmodel.ThemeSettingsViewModel
import kotlinx.android.synthetic.main.layout_premium_header.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ThemeSettingsAct : BaseActivity(), ThemeSettingsAdapter.ItemSelectedListener {
    var adapter: ThemeSettingsAdapter? = null
    var isUpdated = false
    lateinit var viewModel : ThemeSettingsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_settings)
        initUI()
        Utils.setRequestSyncData(true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        if (isUpdated) {
            Utils.onPushEventBus(EnumStatus.RECREATE)
        }
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onClickItem(position: Int) {
        isUpdated = true
        viewModel.mThemeApp = dataSource[position]
        setStatusBarColored(this, themeApp.getPrimaryColor(), themeApp.getPrimaryDarkColor())
        tvTitle?.setTextColor(ContextCompat.getColor(this,themeApp.getAccentColor()))
        Utils.putThemeColor(position)
        adapter?.notifyItemChanged(position)
    }

    override fun onBackPressed() {
        val intent: Intent = intent
        if (isUpdated) {
            setResult(Activity.RESULT_OK, intent)
        }
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    private val dataSource : MutableList<ThemeApp>
        get() {
            return adapter?.getDataSource() ?: mutableListOf()
        }

    val themeApp : ThemeApp
    get() {
        return viewModel.mThemeApp ?: ThemeApp()
    }
}