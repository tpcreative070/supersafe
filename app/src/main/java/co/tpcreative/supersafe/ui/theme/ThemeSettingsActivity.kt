package co.tpcreative.supersafe.ui.theme
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ThemeSettingsActivity : BaseActivity(), BaseView<EmptyModel>, ThemeSettingsAdapter.ItemSelectedListener {
    private var adapter: ThemeSettingsAdapter? = null

    @BindView(R.id.recyclerView)
    var recyclerView: RecyclerView? = null

    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @BindView(R.id.tvPremiumDescription)
    var tvPremiumDescription: AppCompatTextView? = null

    @BindView(R.id.imgIcon)
    var imgIcon: AppCompatImageView? = null

    @BindView(R.id.tvTitle)
    var tvTitle: AppCompatTextView? = null
    private var isUpdated = false
    private var presenter: ThemeSettingsPresenter? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_settings)
        toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        initRecycleView(getLayoutInflater())
        presenter = ThemeSettingsPresenter()
        presenter?.bindView(this)
        presenter?.getData()
        tvPremiumDescription?.setText(getString(R.string.customize_your_theme))
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
        if (isUpdated) {
            EventBus.getDefault().post(EnumStatus.RECREATE)
        }
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    fun initRecycleView(layoutInflater: LayoutInflater) {
        adapter = ThemeSettingsAdapter(layoutInflater, getApplicationContext(), this)
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(getApplicationContext(), 4)
        recyclerView?.setLayoutManager(mLayoutManager)
        recyclerView?.addItemDecoration(GridSpacingItemDecoration(4, 4, true))
        recyclerView?.setItemAnimator(DefaultItemAnimator())
        recyclerView?.setAdapter(adapter)
    }

    override fun onClickItem(position: Int) {
        isUpdated = true
        presenter?.mThemeApp = presenter?.mList?.get(position)
        setStatusBarColored(this, presenter?.mThemeApp?.getPrimaryColor()!!, presenter?.mThemeApp?.getPrimaryDarkColor()!!)
        tvTitle?.setTextColor(ContextCompat.getColor(this,presenter?.mThemeApp?.getAccentColor()!!))
        imgIcon?.setColorFilter(presenter?.mThemeApp?.getAccentColor()!!, PorterDuff.Mode.SRC_ATOP)
        PrefsController.putString(getString(R.string.key_theme_object), Gson().toJson(presenter?.mThemeApp))
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
        when (item.getItemId()) {
            R.id.home -> {
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
        return getApplicationContext()
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
}