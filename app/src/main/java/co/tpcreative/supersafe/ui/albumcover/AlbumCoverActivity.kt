package co.tpcreative.supersafe.ui.albumcover
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
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
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import de.mrapp.android.dialog.MaterialDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AlbumCoverActivity : BaseActivity(), BaseView<EmptyModel>, CompoundButton.OnCheckedChangeListener, AlbumCoverAdapter.ItemSelectedListener, AlbumCoverDefaultAdapter.ItemSelectedListener {
    private var presenter: AlbumCoverPresenter? = null

    @BindView(R.id.btnSwitch)
    var btnSwitch: SwitchCompat? = null

    @BindView(R.id.recyclerViewDefault)
    var recyclerViewDefault: RecyclerView? = null

    @BindView(R.id.recyclerViewCustom)
    var recyclerViewCustom: RecyclerView? = null

    @BindView(R.id.llRecyclerView)
    var llRecyclerView: LinearLayout? = null

    @BindView(R.id.tvPremiumDescription)
    var tvPremiumDescription: AppCompatTextView? = null
    private var adapterDefault: AlbumCoverDefaultAdapter? = null
    private var adapterCustom: AlbumCoverAdapter? = null
    private var isReload = false
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_cover)
        presenter = AlbumCoverPresenter()
        presenter?.bindView(this)
        presenter?.getData(this)
        initRecycleViewDefault(getLayoutInflater())
        initRecycleViewCustom(getLayoutInflater())
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        btnSwitch?.setOnCheckedChangeListener(this)
        presenter?.getData()
        tvPremiumDescription?.setText(getString(R.string.premium_cover_description))
    }

    fun initRecycleViewDefault(layoutInflater: LayoutInflater) {
        adapterDefault = AlbumCoverDefaultAdapter(layoutInflater, getApplicationContext(), this)
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(getApplicationContext(), 3)
        recyclerViewDefault?.setLayoutManager(mLayoutManager)
        recyclerViewDefault?.addItemDecoration(GridSpacingItemDecoration(3, 4, true))
        recyclerViewDefault?.setItemAnimator(DefaultItemAnimator())
        recyclerViewDefault?.setAdapter(adapterDefault)
        recyclerViewDefault?.setNestedScrollingEnabled(false)
    }

    fun initRecycleViewCustom(layoutInflater: LayoutInflater) {
        adapterCustom = AlbumCoverAdapter(layoutInflater, getApplicationContext(), presenter?.mMainCategories, this)
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(getApplicationContext(), 3)
        recyclerViewCustom?.setLayoutManager(mLayoutManager)
        recyclerViewCustom?.addItemDecoration(GridSpacingItemDecoration(3, 4, true))
        recyclerViewCustom?.setItemAnimator(DefaultItemAnimator())
        recyclerViewCustom?.setAdapter(adapterCustom)
        recyclerViewCustom?.setNestedScrollingEnabled(false)
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

    override fun onClickItem(position: Int) {
        Utils.Log(TAG, "position...$position")
        try {
            presenter?.mMainCategories?.items_id = presenter?.mList?.get(position)?.items_id
            presenter?.mMainCategories?.mainCategories_Local_Id = ""
            presenter?.mMainCategories?.let { SQLHelper.updateCategory(it) }
            presenter?.getData()
            isReload = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClickedDefaultItem(position: Int) {
        Utils.Log(TAG, "position...$position")
        try {
            presenter?.mMainCategories?.items_id = ""
            presenter?.mMainCategories?.mainCategories_Local_Id = presenter?.mListMainCategories?.get(position)?.mainCategories_Local_Id
            presenter?.mMainCategories?.let { SQLHelper.updateCategory(it) }
            presenter?.getData()
            isReload = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                if (isReload) {
                    val intent: Intent = getIntent()
                    setResult(Activity.RESULT_OK, intent)
                    Utils.Log(TAG, "onBackPressed")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        when (compoundButton.getId()) {
            R.id.btnSwitch -> {
                if (!Utils.isPremium()) {
                    btnSwitch?.setChecked(false)
                    onShowPremium()
                } else {
                    presenter?.mMainCategories?.isCustom_Cover = b
                    presenter?.mMainCategories?.let { SQLHelper.updateCategory(it) }
                    llRecyclerView?.setVisibility(if (b) View.VISIBLE else View.INVISIBLE)
                    Utils.Log(TAG, "action here")
                }
            }
        }
    }

    @OnClick(R.id.rlSwitch)
    fun onActionSwitch(view: View?) {
        btnSwitch?.setChecked(!btnSwitch?.isChecked()!!)
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun onError(message: String?) {}
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RELOAD -> {
                if (presenter?.mMainCategories != null) {
                    if (Utils.isPremium()) {
                        setTitle(presenter?.mMainCategories?.categories_name)
                        presenter?.mMainCategories?.isCustom_Cover?.let { btnSwitch?.setChecked(it) }
                        llRecyclerView?.setVisibility(if (presenter?.mMainCategories?.isCustom_Cover!!) View.VISIBLE else View.INVISIBLE)
                    } else {
                        setTitle(presenter?.mMainCategories?.categories_name)
                        presenter!!.mMainCategories?.isCustom_Cover = false
                        presenter?.mMainCategories?.isCustom_Cover?.let { btnSwitch?.setChecked(it) }
                        llRecyclerView?.setVisibility(if (presenter?.mMainCategories?.isCustom_Cover!!) View.VISIBLE else View.INVISIBLE)
                        SQLHelper.updateCategory(presenter?.mMainCategories!!)
                    }
                }
            }
            EnumStatus.GET_LIST_FILE -> {
                Utils.Log(TAG, "load data")
                adapterDefault?.setDataSource(presenter?.mListMainCategories)
                adapterCustom?.setDataSource(presenter?.mList)
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    override fun getContext(): Context? {
        return this
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onBackPressed() {
        if (isReload) {
            val intent: Intent = getIntent()
            setResult(Activity.RESULT_OK, intent)
            Utils.Log(TAG, "onBackPressed")
        }
        super.onBackPressed()
    }

    fun onShowPremium() {
        try {
            val builder = getContext()?.let { MaterialDialog.Builder(it) }
            val themeApp: ThemeApp? = ThemeApp.Companion.getInstance()?.getThemeInfo()
            builder?.setHeaderBackground(themeApp?.getAccentColor()!!)
            builder?.setTitle(getString(R.string.this_is_premium_feature))
            builder?.setMessage(getString(R.string.upgrade_now))
            builder?.setCustomHeader(R.layout.custom_header)
            builder?.setPadding(40, 40, 40, 0)
            builder?.setMargin(60, 0, 60, 0)
            builder?.showHeader(true)
            builder?.setPositiveButton(getString(R.string.get_premium), object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    getContext()?.let { Navigator.onMoveToPremium(it) }
                }
            })
            builder?.setNegativeButton(getText(R.string.later), object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {}
            })
            val dialog = builder?.show()
            builder?.setOnShowListener(object : DialogInterface.OnShowListener {
                override fun onShow(dialogInterface: DialogInterface?) {
                    val positive = dialog?.findViewById<AppCompatButton?>(android.R.id.button1)
                    val negative = dialog?.findViewById<AppCompatButton?>(android.R.id.button2)
                    val textView: TextView? = dialog?.findViewById<View?>(R.id.message) as TextView?
                    if (positive != null && negative != null && textView != null) {
                        positive.setTextColor(ContextCompat.getColor(getContext()!!,themeApp!!.getAccentColor()))
                        negative.setTextColor(ContextCompat.getColor(getContext()!!,themeApp!!.getAccentColor()))
                        textView.setTextSize(16f)
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = AlbumCoverActivity::class.java.simpleName
    }
}