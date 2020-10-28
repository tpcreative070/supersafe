package co.tpcreative.supersafe.ui.breakinalerts
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.hiddencamera.HiddenCameraUtils
import co.tpcreative.supersafe.model.EnumStatus
import kotlinx.android.synthetic.main.activity_break_in_alerts.*
import org.greenrobot.eventbus.ThreadMode

class BreakInAlertsAct : BaseActivity(), BaseView<EmptyModel>, CompoundButton.OnCheckedChangeListener, BreakInAlertsAdapter.ItemSelectedListener {
    var adapter: BreakInAlertsAdapter? = null
    var presenter: BreakInAlertsPresenter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break_in_alerts)
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
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_break_in_alerts, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_select_all -> {
                if (presenter?.mList == null || presenter?.mList!!.size == 0) {
                }
                Utils.showDialog(this, getString(R.string.are_you_sure_delete_all_items), object : ServiceManager.ServiceManagerSyncDataListener {
                    override fun onCompleted() {
                        presenter?.onDeleteAll()
                    }
                    override fun onError() {}
                    override fun onCancel() {}
                })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCheckedChanged(compoundButton: CompoundButton?, b: Boolean) {
        if (b) {
            recyclerView?.visibility = View.VISIBLE
        } else {
            recyclerView?.visibility = View.INVISIBLE
        }
        if (HiddenCameraUtils.isFrontCameraAvailable(this)) {
            onAddPermissionCamera(b)
        } else {
            showMessage(getString(R.string.error_not_having_camera))
        }
        tvStatus?.text = (if (b) getString(R.string.enabled) else getString(R.string.disabled))
    }

    override fun onClickItem(position: Int) {
        presenter?.mList?.get(position)?.let { Navigator.onMoveBreakInAlertsDetail(this, it) }
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun onError(message: String?) {}
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RELOAD -> {
                adapter?.setDataSource(presenter?.mList)
            }
            EnumStatus.DELETE -> {
                presenter?.onGetData()
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    override fun getContext(): Context? {
        return applicationContext
    }

    override fun getActivity(): Activity? {
        return this
    }

    companion object {
        private val TAG = BreakInAlertsAct::class.java.simpleName
    }
}