package co.tpcreative.supersafe.ui.cloudmanager
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.extension.isSaverSpace
import co.tpcreative.supersafe.common.extension.putSaverSpace
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.ConvertUtils
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.AppBarStateChangeListener
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.CloudManagerViewModel
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.appbar.AppBarLayout
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
    onUpdatedView()
    switch_SaveSpace?.setOnCheckedChangeListener(this)
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

    val savingSpace: Boolean = Utils.isSaverSpace()
    switch_SaveSpace?.isChecked = savingSpace


    viewModel.superSafeSpace.observe(this, Observer {
        tvValueSupersafeSpace.text = it
        tvSupersafeSpace.visibility = View.VISIBLE
    })

    viewModel.otherSpace.observe(this, Observer {
        tvValueOtherSpace.text = it
        tvOtherSpace.visibility = View.VISIBLE
    })

    viewModel.freeSpace.observe(this, Observer {
        tvValueFreeSpace.text = it
        tvFreeSpace.visibility = View.VISIBLE
    })

    viewModel.leftItems.observe(this, Observer {
        tvLeft.text = it
    })

    viewModel.uploadedItems.observe(this, Observer {
        tvUploaded.text = it
    })

    viewModel.driveAccount.observe(this, Observer {
        tvDriveAccount.text = it
    })

    viewModel.deviceSaving.observe(this, Observer {
        tvDeviceSaving.text = it
    })
    getData()
    getDriveAboutData()
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

fun CloudManagerAct.onShowDialog(size : Long) {
    val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view: View = inflater.inflate(R.layout.custom_view_dialog, null)
    view.tvSpaceRequired?.text = kotlin.String.format(getString(R.string.space_required),  ConvertUtils.byte2FitMemorySize(size))
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
                Utils.putSaverSpace(false)
               disableSaverSpace(EnumStatus.DOWNLOAD)
            }
    builder.show()
}

fun CloudManagerAct.onShowPremium() {
    try {
        val builder = MaterialDialog.Builder(this, Utils.getCurrentThemeMode())
        val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        builder.setHeaderBackground(themeApp?.getAccentColor()!!)
        builder.setTitle(getString(R.string.this_is_premium_feature))
        builder.setMessage(getString(R.string.upgrade_now))
        builder.setCustomHeader(R.layout.custom_header)
        builder.setPadding(40, 40, 40, 0)
        builder.setMargin(60, 0, 60, 0)
        builder.showHeader(true)
        builder.setPositiveButton(getString(R.string.get_premium)) { _, i -> Navigator.onMoveToPremium(this) }
        builder.setNegativeButton(getText(R.string.later)) { _, i -> Utils.putSaverSpace(false) }
        builder.show()
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

private fun CloudManagerAct.getDriveAboutData(){
    viewModel.getDriveAbout().observe(this, Observer {
        when (it.status) {
            Status.SUCCESS -> {
                Utils.Log(TAG,"Get drive successful")
                getData()
            }
            else -> Utils.Log(TAG, it.message)
        }
    })
}

fun CloudManagerAct.getData(){
    viewModel.getSaveData().observe(this, Observer {
        Utils.Log(TAG,"get data completely")
    })
}

fun CloudManagerAct.disableSaverSpace(enumStatus: EnumStatus) {
    viewModel.disableSaverSpace(enumStatus).observe(this, Observer {
        when(enumStatus){
            EnumStatus.GET_LIST_FILE -> {
                onShowDialog(it)
            }
            EnumStatus.DOWNLOAD -> {
                isDownload = true
                viewModel.deviceSaving.postValue(ConvertUtils.byte2FitMemorySize(0))
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    })
}

fun CloudManagerAct.enableSaverSpace(){
    getData()
}
