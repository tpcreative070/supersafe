package co.tpcreative.supersafe.ui.trash
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import co.tpcreative.supersafe.model.ThemeApp;

class TrashActivity : BaseActivity(), BaseView<EmptyModel>, TrashAdapter.ItemSelectedListener {
    @BindView(R.id.tv_Audios)
    var tv_Audios: AppCompatTextView? = null

    @BindView(R.id.tv_Videos)
    var tv_Videos: AppCompatTextView? = null

    @BindView(R.id.tv_Photos)
    var tv_Photos: AppCompatTextView? = null

    @BindView(R.id.tv_Others)
    var tv_Others: AppCompatTextView? = null

    @BindView(R.id.btnUpgradeVersion)
    var btnUpgradeVersion: AppCompatButton? = null

    @BindView(R.id.btnTrash)
    var btnTrash: AppCompatButton? = null

    @BindView(R.id.recyclerView)
    var recyclerView: RecyclerView? = null

    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @BindView(R.id.rlRecyclerView)
    var rlRecyclerView: RelativeLayout? = null

    @BindView(R.id.llUpgrade)
    var llUpgrade: LinearLayout? = null

    @BindView(R.id.rlEmptyTrash)
    var rlEmptyTrash: RelativeLayout? = null
    private var adapter: TrashAdapter? = null
    private var presenter: TrashPresenter? = null
    private var actionMode: ActionMode? = null
    private var countSelected = 0
    private var isSelectAll = false
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        initRecycleView(getLayoutInflater())
        presenter = TrashPresenter()
        presenter?.bindView(this)
        presenter?.getData(this)
    }

    fun onUpdatedView() {
        if (!Utils.isPremium()) {
            llUpgrade?.setVisibility(View.VISIBLE)
            rlRecyclerView?.setVisibility(View.GONE)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
        }
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
        //SuperSafeApplication.getInstance().writeKeyHomePressed(TrashActivity.class.getSimpleName());
        onUpdatedView()
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter?.unbindView()
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    fun initRecycleView(layoutInflater: LayoutInflater) {
        adapter = TrashAdapter(layoutInflater, getApplicationContext(), this)
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(getApplicationContext(), 3)
        recyclerView?.setLayoutManager(mLayoutManager)
        recyclerView?.addItemDecoration(GridSpacingItemDecoration(3, 4, true))
        recyclerView?.setItemAnimator(DefaultItemAnimator())
        recyclerView?.setAdapter(adapter)
    }

    override fun onClickItem(position: Int) {
        if (actionMode == null) {
            actionMode = toolbar?.startActionMode(callback)
        }
        toggleSelection(position)
        actionMode?.setTitle(countSelected.toString() + " " + getString(R.string.selected))
        if (countSelected == 0) {
            actionMode?.finish()
        }
    }

    @OnClick(R.id.btnTrash)
    fun onTrash(view: View?) {
        if (presenter?.mList?.size!! > 0) {
            if (countSelected == 0) {
                onShowDialog(getString(R.string.empty_all_trash), true)
            } else {
                onShowDialog(getString(R.string.restore), false)
            }
        }
    }

    @OnClick(R.id.btnUpgradeVersion)
    fun onUpgradeToRecover() {
        Navigator.onMoveToPremium(getApplicationContext())
    }

    fun onShowDialog(message: String?, isEmpty: Boolean) {
        val builder: MaterialDialog.Builder = MaterialDialog.Builder(this)
                .title(getString(R.string.confirm))
                .theme(Theme.LIGHT)
                .content(message!!)
                .titleColor(ContextCompat.getColor(getContext()!!,R.color.black))
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .onPositive(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        presenter?.onDeleteAll(isEmpty)
                    }
                })
        builder.show()
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun getContext(): Context? {
        return getApplicationContext()
    }

    override fun getActivity(): Activity? {
        return this
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
        when (item.getItemId()) {
            R.id.action_select_items -> {
                if (actionMode == null) {
                    actionMode = toolbar?.startActionMode(callback)
                }
                countSelected = 0
                actionMode?.setTitle(countSelected.toString() + " " + getString(R.string.selected))
                Utils.Log(TAG, "Action here")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*Action mode*/
    private val callback: ActionMode.Callback? = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val menuInflater = mode?.getMenuInflater()
            menuInflater?.inflate(R.menu.menu_select, menu)
            actionMode = mode
            countSelected = 0
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val window: Window = getWindow()
                window.statusBarColor = ContextCompat.getColor(getContext()!!, R.color.material_orange_900)
            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val i = item?.getItemId()
            if (i == R.id.menu_item_select_all) {
                isSelectAll = !isSelectAll
                selectAll()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            if (countSelected > 0) {
                deselectAll()
            }
            actionMode = null
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
                if (themeApp != null) {
                    val window: Window = getWindow()
                    window.statusBarColor = ContextCompat.getColor(getContext()!!, themeApp.getPrimaryDarkColor())
                }
            }
        }
    }

    private fun toggleSelection(position: Int) {
        presenter?.mList?.get(position)?.isChecked = !(presenter?.mList?.get(position)?.isChecked!!)
        if (presenter?.mList?.get(position)?.isChecked!!) {
            countSelected++
        } else {
            countSelected--
        }
        onShowUI()
        adapter?.notifyItemChanged(position)
    }

    private fun deselectAll() {
        var i = 0
        val l: Int = presenter?.mList?.size!!
        while (i < l) {
            presenter?.mList?.get(i)?.isChecked = false
            i++
        }
        countSelected = 0
        onShowUI()
        adapter?.notifyDataSetChanged()
    }

    fun selectAll() {
        var countSelect = 0
        for (i in presenter?.mList?.indices!!) {
            presenter?.mList?.get(i)?.isChecked = isSelectAll
            if (presenter?.mList?.get(i)?.isChecked!!) {
                countSelect++
            }
        }
        countSelected = countSelect
        onShowUI()
        adapter?.notifyDataSetChanged()
        actionMode?.setTitle(countSelected.toString() + " " + getString(R.string.selected))
    }

    fun onShowUI() {
        if (countSelected == 0) {
            btnTrash?.setText(getString(R.string.key_empty_trash))
        } else {
            btnTrash?.setText(getString(R.string.key_restore))
        }
    }

    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RELOAD -> {
                val photos: String = kotlin.String.format(getString(R.string.photos_default), "" + presenter?.photos)
                tv_Photos?.setText(photos)
                val videos: String = kotlin.String.format(getString(R.string.videos_default), "" + presenter?.videos)
                tv_Videos?.setText(videos)
                val audios: String = kotlin.String.format(getString(R.string.audios_default), "" + presenter?.audios)
                tv_Audios?.setText(audios)
                val others: String = kotlin.String.format(getString(R.string.others_default), "" + presenter?.others)
                tv_Others?.setText(others)
                adapter?.setDataSource(presenter?.mList)
            }
            EnumStatus.DONE -> {
                if (actionMode != null) {
                    actionMode?.finish()
                }
                presenter?.getData(this)
                btnTrash?.setText(getString(R.string.key_empty_trash))
                SingletonPrivateFragment.getInstance()?.onUpdateView()
                ServiceManager.getInstance()?.onPreparingSyncData()
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}

    companion object {
        private val TAG = TrashActivity::class.java.simpleName
    }
}