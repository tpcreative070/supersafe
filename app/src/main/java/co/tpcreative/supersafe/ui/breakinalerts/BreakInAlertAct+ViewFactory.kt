package co.tpcreative.supersafe.ui.breakinalerts
import android.Manifest
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.util.Utils
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_break_in_alerts.*
import kotlinx.android.synthetic.main.layout_premium_header.*

fun BreakInAlertsAct.initUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    btnSwitch?.setOnCheckedChangeListener(this)
    val value: Boolean = PrefsController.getBoolean(getString(R.string.key_break_in_alert), false)
    btnSwitch?.isChecked = value
    initRecycleView()
    presenter = BreakInAlertsPresenter()
    presenter?.bindView(this)
    presenter?.onGetData()
    tvPremiumDescription?.text = getString(R.string.break_in_alerts)
    rlBreakInAlerts.setOnClickListener {
        btnSwitch?.isChecked = !btnSwitch?.isChecked!!
    }
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
                        PrefsController.putBoolean(getString(R.string.key_break_in_alert), value)
                    } else {
                        PrefsController.putBoolean(getString(R.string.key_break_in_alert), false)
                        btnSwitch?.isChecked = false
                        Utils.Log(TAG, "Permission is denied")
                    }
                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        PrefsController.putBoolean(getString(R.string.key_break_in_alert), false)
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