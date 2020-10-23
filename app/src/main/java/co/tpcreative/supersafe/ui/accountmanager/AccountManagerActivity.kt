package co.tpcreative.supersafe.ui.accountmanager
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGoogleApi
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.AppLists
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import co.tpcreative.supersafe.ui.accountmanager.AccountManagerAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AccountManagerActivity : BaseGoogleApi(), BaseView<EmptyModel>, AccountManagerAdapter.ItemSelectedListener {
    @BindView(R.id.tvEmail)
    var tvEmail: AppCompatTextView? = null

    @BindView(R.id.tvStatus)
    var tvStatus: AppCompatTextView? = null

    @BindView(R.id.tvLicenseStatus)
    var tvLicenseStatus: AppCompatTextView? = null

    @BindView(R.id.btnSignOut)
    var btnSignOut: AppCompatButton? = null

    @BindView(R.id.tvStatusAccount)
    var tvStatusAccount: AppCompatTextView? = null

    @BindView(R.id.tvPremiumLeft)
    var tvPremiumLeft: AppCompatTextView? = null

    @BindView(R.id.recyclerView)
    var recyclerView: RecyclerView? = null

    @BindView(R.id.rlPremiumComplimentary)
    var rlPremiumComplimentary: RelativeLayout? = null

    @BindView(R.id.rlPremium)
    var rlPremium: RelativeLayout? = null

    @BindView(R.id.scrollView)
    var scrollView: NestedScrollView? = null
    private var presenter: AccountManagerPresenter? = null
    private var adapter: AccountManagerAdapter? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_manager)
        initRecycleView()
        presenter = AccountManagerPresenter()
        presenter?.bindView(this)
        presenter?.getData()
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        onUpdatedView()
        scrollView?.smoothScrollTo(0, 0)
    }

    fun onUpdatedView() {
        val mUser: User? = Utils.getUserInfo()
        if (mUser != null) {
            tvEmail?.setText(mUser.email)
            if (mUser.verified) {
                tvStatusAccount?.setTextColor(ContextCompat.getColor(this,R.color.ColorBlueV1))
                tvStatusAccount?.setText(getString(R.string.verified))
            } else {
                tvStatusAccount?.setTextColor(ContextCompat.getColor(this,R.color.red))
                tvStatusAccount?.setText(getString(R.string.unverified))
            }
        }
        val isPremium: Boolean = Utils.isPremium()
        if (isPremium) {
            tvLicenseStatus?.setTextColor(ContextCompat.getColor(this,R.color.ColorBlueV1))
            tvLicenseStatus?.setText(getString(R.string.premium))
            rlPremium?.setVisibility(View.VISIBLE)
            rlPremiumComplimentary?.setVisibility(View.GONE)
        } else {
            rlPremium?.setVisibility(View.GONE)
            rlPremiumComplimentary?.setVisibility(View.VISIBLE)
            tvLicenseStatus?.setText(getString(R.string.free))
            val sourceString: String? = Utils.getFontString(R.string.upgrade_premium_to_use_full_features, getString(R.string.premium_uppercase))
            tvPremiumLeft?.setText(Html.fromHtml(sourceString))
        }
    }

    fun initRecycleView() {
        adapter = AccountManagerAdapter(getLayoutInflater(), this, this)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        if (recyclerView == null) {
            Utils.Log(TAG, "recyclerview is null")
        }
        recyclerView?.setLayoutManager(mLayoutManager)
        recyclerView?.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView?.setAdapter(adapter)
    }

    override fun onClickItem(position: Int) {
        val app: AppLists = presenter?.mList!!.get(position)
        val isInstalled: Boolean = app.isInstalled
        if (!isInstalled) {
            val uri = Uri.parse("market://details?id=" + app.packageName)
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + app.packageName)))
            }
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

    @OnClick(R.id.btnUpgrade)
    fun onClickedUpgrade(view: View?) {
        Navigator.onMoveToPremium(this)
    }

    @OnClick(R.id.rlPremiumComplimentary)
    fun onClickedUpgrade() {
        Navigator.onMoveToPremium(this)
    }

    @OnClick(R.id.btnSignOut)
    fun onSignOut(view: View?) {
        Utils.Log(TAG, "sign out")
        val mUser: User? = Utils.getUserInfo()
        if (mUser != null) {
            signOut(object : ServiceManager.ServiceManagerSyncDataListener {
                override fun onCompleted() {
                    mUser.verified = false
                    mUser.driveConnected = false
                    Utils.setUserPreShare(mUser)
                    onBackPressed()
                }

                override fun onError() {}
                override fun onCancel() {}
            })
        }
    }

    override fun onDriveClientReady() {}
    override fun onDriveSuccessful() {
        Utils.Log(TAG, "onDriveSuccessful")
        btnSignOut?.setVisibility(View.VISIBLE)
    }

    override fun onDriveError() {
        Utils.Log(TAG, "onDriveError")
    }

    override fun onDriveSignOut() {
        Utils.Log(TAG, "onDriveSignOut")
    }

    override fun onDriveRevokeAccess() {
        Utils.Log(TAG, "onDriveRevokeAccess")
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun startServiceNow() {}
    override fun getContext(): Context? {
        return getApplicationContext()
    }

    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RELOAD -> {
                adapter?.setDataSource(presenter?.mList)
            }
        }
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    override fun isSignIn(): Boolean {
        return false
    }

    companion object {
        private val TAG = AccountManagerActivity::class.java.simpleName
    }
}