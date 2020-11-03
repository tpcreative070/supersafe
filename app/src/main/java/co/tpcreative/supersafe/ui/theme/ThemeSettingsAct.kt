package co.tpcreative.supersafe.ui.theme
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import kotlinx.android.synthetic.main.layout_premium_header.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ThemeSettingsAct : BaseActivity(), BaseView<EmptyModel>, ThemeSettingsAdapter.ItemSelectedListener {
    var adapter: ThemeSettingsAdapter? = null
    var isUpdated = false
    var presenter: ThemeSettingsPresenter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_settings)
        initUI()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter?.unbindView()
        if (isUpdated) {
            Utils.onPushEventBus(EnumStatus.RECREATE)
        }
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onClickItem(position: Int) {
        isUpdated = true
        presenter?.mThemeApp = presenter?.mList?.get(position)
        setStatusBarColored(this, presenter?.mThemeApp?.getPrimaryColor()!!, presenter?.mThemeApp?.getPrimaryDarkColor()!!)
        tvTitle?.setTextColor(ContextCompat.getColor(this,presenter?.mThemeApp?.getAccentColor()!!))
        imgIcon?.setColorFilter(presenter?.mThemeApp?.getAccentColor()!!, PorterDuff.Mode.SRC_ATOP)
        PrefsController.putInt(getString(R.string.key_theme_object), position)
        adapter?.notifyItemChanged(position)
    }

    override fun onBackPressed() {
        val intent: Intent = getIntent()
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

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.SHOW_DATA -> {
                adapter?.setDataSource(presenter?.mList)
            }
            EnumStatus.RELOAD -> {
            }
        }
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun getContext(): Context? {
        return applicationContext
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
}