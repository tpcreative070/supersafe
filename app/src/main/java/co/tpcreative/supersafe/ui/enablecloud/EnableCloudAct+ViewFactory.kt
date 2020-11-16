package co.tpcreative.supersafe.ui.enablecloud
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.viewmodel.EnableCloudViewModel
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import kotlinx.android.synthetic.main.activity_enable_cloud.*
import kotlinx.android.synthetic.main.activity_enable_cloud.toolbar

fun EnableCloudAct.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.hide()
    Utils.Log(TAG, "Enable cloud...........")
    btnLinkGoogleDrive.setOnClickListener {
        btnUserAnotherAccount?.isEnabled = false
        btnLinkGoogleDrive?.isEnabled = false
        val mCLoudId: String? = Utils.getUserCloudId()
        if (mCLoudId == null) {
            ServiceManager.getInstance()?.onPickUpNewEmail(this)
        } else {
            ServiceManager.getInstance()?.onPickUpExistingEmail(this, mCLoudId)
        }
    }

    btnUserAnotherAccount.setOnClickListener {
        Utils.Log(TAG, "user another account")
        btnUserAnotherAccount?.isEnabled = false
        btnLinkGoogleDrive?.isEnabled = false
        onShowWarningAnotherAccount()
    }

    viewModel.isLoading.observe(this,{
        if (it){
            SingletonManagerProcessing.getInstance()?.onStartProgressing(this,R.string.loading)
        }else{
            SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
        }
    })
}

fun EnableCloudAct.onShowWarning(cloud_id: String?) {
    val value = String.format(getString(R.string.choose_the_same_account), cloud_id)
    MaterialStyledDialog.Builder(this)
            .setTitle(R.string.not_the_same_account)
            .setDescription(value)
            .setHeaderDrawable(R.drawable.ic_drive_cloud)
            .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
            .setHeaderColor(R.color.colorPrimary)
            .setCancelable(true)
            .setPositiveText(R.string.select_again)
            .setNegativeText(R.string.cancel)
            .onPositive {

            }
            .onNegative {
            }
            .setCheckBox(checkbox = false, titleRes = R.string.enable_cloud)
            .show()
}

fun EnableCloudAct.onShowWarningAnotherAccount() {
    val builder = StringBuilder()
    builder.append("If you use another Google Drive")
    builder.append("\n")
    builder.append("1. Files sync with previous Google Drive will stop")
    builder.append("\n")
    builder.append("2. All of your local files will be synced to the new Google Drive")
    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    MaterialStyledDialog.Builder(this)
            .setTitle(R.string.user_another_google_drive_title)
            .setDescription(builder.toString())
            .setHeaderDrawable(R.drawable.ic_drive_cloud)
            .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
            .setHeaderColor(themeApp?.getAccentColor()!!)
            .setCancelable(true)
            .setPositiveText(R.string.user_another)
            .setNegativeText(R.string.cancel)
            .setCheckBox(checkbox = false, titleRes = R.string.enable_cloud)
            .onPositive {
                Utils.Log(TAG, "positive")
                val mCloudId: String? = Utils.getUserCloudId()
                if (mCloudId == null) {
                    ServiceManager.getInstance()?.onPickUpNewEmailNoTitle(this@onShowWarningAnotherAccount, Utils.getUserId())
                } else {
                    ServiceManager.getInstance()?.onPickUpNewEmailNoTitle(this@onShowWarningAnotherAccount, mCloudId)
                }
            }
            .onNegative {
                btnUserAnotherAccount?.isEnabled = true
                btnLinkGoogleDrive?.isEnabled = true
            }
            .show()
}

fun EnableCloudAct.onShowProgressDialog() {
   viewModel.isLoading.postValue(true)
}

fun EnableCloudAct.onStopProgressDialog() {
    viewModel.isLoading.postValue(false)
}

private fun EnableCloudAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(EnableCloudViewModel::class.java)
}

fun EnableCloudAct.addUserCloud() {
    viewModel.addUserCloud().observe(this, Observer{
        when(it.status){
            Status.SUCCESS -> {
                onBackPressed()
                ServiceManager.getInstance()?.onPreparingSyncData()
                Utils.Log(TAG,"Success ${it.data?.toJson()}")
            }
            else -> {
                Utils.Log(TAG,"Nothing")
                Navigator.onEnableCloud(this)
            }
        }
    })
}
