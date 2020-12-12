package co.tpcreative.supersafe.ui.breakinalerts
import android.Manifest
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration
import co.tpcreative.supersafe.common.extension.isBreakAlert
import co.tpcreative.supersafe.common.extension.putBreakAlert
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.viewmodel.BreakInAlertsViewModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_break_in_alerts.*
import kotlinx.android.synthetic.main.layout_premium_header.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun BreakInAlertsAct.initUI(){
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    btnSwitch?.setOnCheckedChangeListener(this)
    btnSwitch?.isChecked = Utils.isBreakAlert()
    initRecycleView()
    tvPremiumDescription?.text = getString(R.string.break_in_alerts)
    rlBreakInAlerts.setOnClickListener {
        btnSwitch?.isChecked = !btnSwitch?.isChecked!!
    }
    getData()
}

fun BreakInAlertsAct.initRecycleView() {
    adapter = BreakInAlertsAdapter(layoutInflater, this, this)
    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(applicationContext)
    recyclerView?.layoutManager = mLayoutManager
    recyclerView?.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    recyclerView?.adapter = adapter
}

fun BreakInAlertsAct.onAddPermissionCamera(value: Boolean) {
    Dexter.withContext(this)
            .withPermissions(
                    Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted()!!) {
                        Utils.putBreakAlert(value)
                    } else {
                        Utils.putBreakAlert(false)
                        btnSwitch?.isChecked = false
                        Utils.Log(TAG, "Permission is denied")
                    }
                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        Utils.putBreakAlert(false)
                        btnSwitch?.isChecked = false
                        /*Miss add permission in manifest*/Utils.Log(TAG, "request permission is failed")
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                    /* ... */
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener { Utils.Log(TAG, "error ask permission") }.onSameThread().check()
}

private fun BreakInAlertsAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(BreakInAlertsViewModel::class.java)
}

fun BreakInAlertsAct.getData() {
    viewModel.getData().observe(this, Observer {
        CoroutineScope(Dispatchers.Main).launch {
            adapter?.setDataSource(it)
        }
    })
}

fun BreakInAlertsAct.deleteAll() {
    viewModel.deleteAll().observe(this, Observer {
        getData()
    })
}
