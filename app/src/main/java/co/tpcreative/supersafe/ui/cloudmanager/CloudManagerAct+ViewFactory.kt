package co.tpcreative.supersafe.ui.cloudmanager
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.ConvertUtils
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.AppBarStateChangeListener
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.CloudManagerViewModel
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.appbar.AppBarLayout
import com.snatik.storage.Storage
import de.mrapp.android.dialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_cloud_manager.*
import kotlinx.android.synthetic.main.activity_cloud_manager.collapsing_toolbar
import kotlinx.android.synthetic.main.activity_cloud_manager.toolbar
import kotlinx.android.synthetic.main.custom_view_dialog.view.*


fun CloudManagerAct.initUI(){
    window.statusBarColor = Color.TRANSPARENT
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    storage = Storage(this)
    presenter = CloudManagerPresenter()
    presenter?.bindView(this)
    onShowUI()
    onUpdatedView()
    //presenter?.onGetDriveAbout()
    btnSwitchPauseSync?.setOnCheckedChangeListener(this)
    switch_SaveSpace?.setOnCheckedChangeListener(this)
    val lefFiles: String = kotlin.String.format(getString(R.string.left), "" + Navigator.LIMIT_UPLOAD)
    tvLeft?.text = lefFiles
    val updated: String = kotlin.String.format(getString(R.string.left), "0")
    tvUploaded?.text = updated

    llPause.setOnClickListener {
        btnSwitchPauseSync?.isChecked = !btnSwitchPauseSync!!.isChecked
    }

    rlSaveSpace.setOnClickListener {
        switch_SaveSpace?.isChecked = !switch_SaveSpace!!.isChecked
    }

    btnRemoveLimit.setOnClickListener {
        Navigator.onMoveToPremium(this)
    }

    viewModel.isLoading.observe(this, Observer {
        if (it) {
            SingletonManagerProcessing.getInstance()?.onStartProgressing(this, R.string.progressing)
        } else {
            SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
        }
    })

    appbar.addOnOffsetChangedListener(object: AppBarStateChangeListener() {
        override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
            Utils.Log(TAG, state?.name)
            when(state) {
                State.COLLAPSED -> { collapsing_toolbar.title = getString(R.string.cloud_manager)/* Do something */ }
                State.EXPANDED -> { collapsing_toolbar.title = ""/* Do something */ }
                State.IDLE -> { collapsing_toolbar.title = "" /* Do something */ }
            }
        }
    })

    getData()
}

fun CloudManagerAct.onShowUI() {
    tvSupersafeSpace?.visibility = View.VISIBLE
    tvOtherSpace?.visibility= View.VISIBLE
    tvFreeSpace?.visibility = View.VISIBLE
    val mUser: User? = Utils.getUserInfo()
    var isThrow = false
    if (mUser != null) {
        val driveAbout: DriveAbout? = mUser.driveAbout
        tvDriveAccount?.text = mUser.cloud_id
        try {
            Utils.Log(TAG, "supersafe space ${driveAbout?.toJson()}")
            val superSafeSpace: String? = driveAbout?.inAppUsed?.let { ConvertUtils.byte2FitMemorySize(it) }
            tvValueSupersafeSpace?.text = superSafeSpace
        } catch (e: Exception) {
            e.printStackTrace()
            tvValueOtherSpace?.text = getString(R.string.calculating)
            isThrow = true
        }
        try {
            val storageQuota: StorageQuota? = driveAbout?.storageQuota
            if (storageQuota != null) {
                val superSafeSpace: String? = ConvertUtils.byte2FitMemorySize(storageQuota.usage)
                tvValueOtherSpace?.text = superSafeSpace
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tvValueOtherSpace?.text = getString(R.string.calculating)
            isThrow = true
        }
        try {
            val storageQuota: StorageQuota? = driveAbout?.storageQuota
            if (storageQuota != null) {
                val result = storageQuota.limit - storageQuota.usage
                val superSafeSpace: String? = ConvertUtils.byte2FitMemorySize(result)
                tvValueFreeSpace?.text = superSafeSpace
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tvValueFreeSpace?.text = getString(R.string.calculating)
            isThrow = true
        }
        try {
            if (mUser.syncData != null) {
                val lefFiles: String? = kotlin.String.format(getString(R.string.left), "" + mUser.syncData?.left)
                tvLeft?.text = lefFiles
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val lefFiles: String = kotlin.String.format(getString(R.string.left), "" + Navigator.LIMIT_UPLOAD)
            tvLeft?.text = lefFiles
            isThrow = true
        }
        try {
            if (mUser.syncData != null) {
                val uploadedFiles: String? = kotlin.String.format(getString(R.string.uploaded), "" + (Navigator.LIMIT_UPLOAD - mUser?.syncData?.left!!))
                tvUploaded?.text = uploadedFiles
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val uploadedFiles: String = kotlin.String.format(getString(R.string.uploaded), "0")
            tvUploaded?.text = uploadedFiles
            isThrow = true
        }
        if (isThrow) {
            tvSupersafeSpace?.visibility = View.INVISIBLE
            tvOtherSpace?.visibility  = View.INVISIBLE
            tvFreeSpace?.visibility = View.INVISIBLE
        }
    }
}

fun CloudManagerAct.onUpdatedView() {
    if (Utils.isPremium()) {
        llPremium?.visibility = View.GONE
        llTitle?.visibility = View.VISIBLE
    } else {
        llPremium?.visibility = View.VISIBLE
        llTitle?.visibility = View.GONE
    }
}

fun CloudManagerAct.onShowSwitch() {
    val pause_cloud_sync: Boolean = PrefsController.getBoolean(getString(R.string.key_pause_cloud_sync), false)
    btnSwitchPauseSync?.isChecked = pause_cloud_sync
    val saving_space: Boolean = PrefsController.getBoolean(getString(R.string.key_saving_space), false)
    switch_SaveSpace?.isChecked = saving_space
    if (saving_space) {
        presenter?.onGetSaveData()
    } else {
        tvDeviceSaving?.text = ConvertUtils.byte2FitMemorySize(0)
    }
}

fun CloudManagerAct.onShowDialog() {
    val inflater: LayoutInflater = getContext()?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view: View = inflater.inflate(R.layout.custom_view_dialog, null)
    view.tvSpaceRequired?.text = kotlin.String.format(getString(R.string.space_required), presenter?.sizeFile?.let { ConvertUtils.byte2FitMemorySize(it) })
    val builder : com.afollestad.materialdialogs.MaterialDialog = com.afollestad.materialdialogs.MaterialDialog(this)
            .title(text = getString(R.string.download_private_cloud_files))
            .customView(view = view, scrollable = false)
            .cancelable(false)
            .negativeButton(text = getString(R.string.cancel))
            .negativeButton {
                Utils.Log(TAG, "negative")
                switch_SaveSpace?.isChecked = true
            }
            .positiveButton(text = getString(R.string.download))
            .positiveButton {
                Utils.Log(TAG, "positive")
                PrefsController.putBoolean(getString(R.string.key_saving_space), false)
                presenter?.onDisableSaverSpace(EnumStatus.DOWNLOAD)
            }
    builder.show()
}

fun CloudManagerAct.onShowPremium() {
    try {
        val builder = getContext()?.let { MaterialDialog.Builder(it, Utils.getCurrentTheme()) }
        val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        builder?.setHeaderBackground(themeApp?.getAccentColor()!!)
        builder?.setTitle(getString(R.string.this_is_premium_feature))
        builder?.setMessage(getString(R.string.upgrade_now))
        builder?.setCustomHeader(R.layout.custom_header)
        builder?.setPadding(40, 40, 40, 0)
        builder?.setMargin(60, 0, 60, 0)
        builder?.showHeader(true)
        builder?.setPositiveButton(getString(R.string.get_premium)) { dialogInterface, i -> getContext()?.let { Navigator.onMoveToPremium(it) } }
        builder?.setNegativeButton(getText(R.string.later)) { dialogInterface, i -> PrefsController.putBoolean(getString(R.string.key_saving_space), false) }
        builder?.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun CloudManagerAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(CloudManagerViewModel::class.java)
}

private fun CloudManagerAct.getData(){
    viewModel.getDriveAbout().observe(this, Observer {
        when (it.status) {
            Status.SUCCESS -> onShowUI()
            else -> Utils.Log(TAG, it.message)
        }
    })
}
