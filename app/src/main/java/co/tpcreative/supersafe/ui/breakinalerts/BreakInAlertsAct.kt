package co.tpcreative.supersafe.ui.breakinalerts
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.util.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.model.BreakInAlertsModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.viewmodel.BreakInAlertsViewModel
import kotlinx.android.synthetic.main.activity_break_in_alerts.*
import org.greenrobot.eventbus.ThreadMode

class BreakInAlertsAct : BaseActivity(), CompoundButton.OnCheckedChangeListener, BreakInAlertsAdapter.ItemSelectedListener {
    var adapter: BreakInAlertsAdapter? = null
    lateinit var viewModel : BreakInAlertsViewModel
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
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
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
                if (dataSource.isEmpty()) {
                    return false
                }
                Utils.showDialog(this, R.string.are_you_sure_delete_all_items, object : ServiceManager.ServiceManagerSyncDataListener {
                    override fun onCompleted() {
                        deleteAll()
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
        if (Utils.isCameraAvailable(this)) {
            onAddPermissionCamera(b)
        } else {
            Utils.onBasicAlertNotify(this,getString(R.string.key_alert),getString(R.string.error_not_having_camera))
        }
        tvStatus?.text = (if (b) getString(R.string.enabled) else getString(R.string.disabled))
    }

    override fun onClickItem(position: Int) {
        Navigator.onMoveBreakInAlertsDetail(this, dataSource[position])
    }

    val dataSource : MutableList<BreakInAlertsModel>
        get() {
            return adapter?.getDataSource() ?: mutableListOf()
        }

    companion object {
        private val TAG = BreakInAlertsAct::class.java.simpleName
    }
}