package co.tpcreative.supersafe.ui.trash
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.viewmodel.TrashViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlinx.android.synthetic.main.activity_trash.*

class TrashAct : BaseActivity(), TrashAdapter.ItemSelectedListener {
    var adapter: TrashAdapter? = null
    var actionMode: ActionMode? = null
    var countSelected = 0
    lateinit var viewModel : TrashViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)
        initUI()
    }

    fun onUpdatedView() {
        if (!Utils.isPremium()) {
            llUpgrade?.visibility = View.VISIBLE
            rlRecyclerView?.visibility = View.GONE
        }
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
        onRegisterHomeWatcher()
        onUpdatedView()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        if (viewModel.isRequestSyncData){
            SingletonPrivateFragment.getInstance()?.onUpdateView()
            ServiceManager.getInstance()?.onPreparingSyncData()
        }
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onClickItem(position: Int) {
        if (actionMode == null) {
            actionMode = toolbar?.startActionMode(callback)
        }
        toggleSelection(position)
        actionMode?.title = (countSelected.toString() + " " + getString(R.string.selected))
        if (countSelected == 0) {
            actionMode?.finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (Utils.isPremium()) {
            if (toolbar == null) {
                return false
            }
            toolbar?.inflateMenu(R.menu.menu_trash)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_select_items -> {
                if (actionMode == null) {
                    actionMode = toolbar?.startActionMode(callback)
                }
                countSelected = 0
                actionMode?.title = countSelected.toString() + " " + getString(R.string.selected)
                Utils.Log(TAG, "Action here")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*Action mode*/
    private val callback: ActionMode.Callback? = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater
            menuInflater.inflate(R.menu.menu_select, menu)
            actionMode = mode
            countSelected = 0
            window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.material_orange_900)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val i = item?.itemId
            if (i == R.id.menu_item_select_all) {
                viewModel.isSelectAll.postValue(!(viewModel.isSelectAll.value ?: false))
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            viewModel.isSelectAll.postValue(false)
            actionMode = null
            val themeApp: co.tpcreative.supersafe.model.ThemeApp? = co.tpcreative.supersafe.model.ThemeApp.getInstance()?.getThemeInfo()
            if (themeApp != null) {
                window.statusBarColor = ContextCompat.getColor(applicationContext, themeApp.getPrimaryDarkColor())
            }
        }
    }

    val dataSource : MutableList<ItemModel>
        get() {
        return adapter?.getDataSource() ?: mutableListOf()
    }
}