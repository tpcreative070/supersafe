package co.tpcreative.supersafe.ui.breakinalerts
import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.ui.breakinalertsimport.BreakInAlertsAdapter
import com.karumi.dexter.listener.PermissionRequest
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.hiddencamera.HiddenCameraUtils
import co.tpcreative.supersafe.model.EnumStatus
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import org.greenrobot.eventbus.ThreadMode

class BreakInAlertsActivity : BaseActivity(), BaseView<EmptyModel>, CompoundButton.OnCheckedChangeListener, BreakInAlertsAdapter.ItemSelectedListener {
    @BindView(R.id.btnSwitch)
    var btnSwitch: SwitchCompat? = null

    @BindView(R.id.recyclerView)
    var recyclerView: RecyclerView? = null

    @BindView(R.id.tvPremiumDescription)
    var tvPremiumDescription: AppCompatTextView? = null

    @BindView(R.id.tvStatus)
    var tvStatus: AppCompatTextView? = null
    private var adapter: BreakInAlertsAdapter? = null
    private var presenter: BreakInAlertsPresenter? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break_in_alerts)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        btnSwitch?.setOnCheckedChangeListener(this)
        val value: Boolean = PrefsController.getBoolean(getString(R.string.key_break_in_alert), false)
        btnSwitch?.setChecked(value)
        initRecycleView()
        presenter = BreakInAlertsPresenter()
        presenter?.bindView(this)
        presenter?.onGetData()
        tvPremiumDescription?.setText(getString(R.string.break_in_alerts))
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_break_in_alerts, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
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

    @OnClick(R.id.rlBreakInAlerts)
    fun onActionBreak(view: View?) {
        btnSwitch?.setChecked(!btnSwitch?.isChecked()!!)
    }

    override fun onCheckedChanged(compoundButton: CompoundButton?, b: Boolean) {
        if (b) {
            recyclerView?.setVisibility(View.VISIBLE)
        } else {
            recyclerView?.setVisibility(View.INVISIBLE)
        }
        if (HiddenCameraUtils.isFrontCameraAvailable(this)) {
            onAddPermissionCamera(b)
        } else {
            showMessage(getString(R.string.error_not_having_camera))
        }
        tvStatus?.setText(if (b) getString(R.string.enabled) else getString(R.string.disabled))
    }

    fun initRecycleView() {
        adapter = BreakInAlertsAdapter(getLayoutInflater(), this, this)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(getApplicationContext())
        recyclerView?.setLayoutManager(mLayoutManager)
        recyclerView?.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView?.setAdapter(adapter)
    }

    override fun onClickItem(position: Int) {
        presenter?.mList?.get(position)?.let { Navigator.onMoveBreakInAlertsDetail(this, it) }
    }

    fun onAddPermissionCamera(value: Boolean) {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report?.areAllPermissionsGranted()!!) {
                            PrefsController.putBoolean(getString(R.string.key_break_in_alert), value)
                        } else {
                            PrefsController.putBoolean(getString(R.string.key_break_in_alert), false)
                            btnSwitch?.setChecked(false)
                            Utils.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            PrefsController.putBoolean(getString(R.string.key_break_in_alert), false)
                            btnSwitch?.setChecked(false)
                            /*Miss add permission in manifest*/Utils.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token?.continuePermissionRequest()
                    }
                })
                .withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(error: DexterError?) {
                        Utils.Log(TAG, "error ask permission")
                    }
                }).onSameThread().check()
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
        return getApplicationContext()
    }

    override fun getActivity(): Activity? {
        return this
    }

    companion object {
        private val TAG = BreakInAlertsActivity::class.java.simpleName
    }
}