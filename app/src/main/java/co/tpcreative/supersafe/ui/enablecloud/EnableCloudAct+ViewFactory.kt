package co.tpcreative.supersafe.ui.enablecloud
import android.app.ProgressDialog
import android.widget.ImageView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ThemeApp
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import kotlinx.android.synthetic.main.activity_enable_cloud.*

fun EnableCloudAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.hide()
    presenter = EnableCloudPresenter()
    presenter?.bindView(this)
    presenter?.onUserInfo()
    Utils.Log(TAG, "Enable cloud...........")
    btnLinkGoogleDrive.setOnClickListener {
        btnUserAnotherAccount?.isEnabled = false
        btnLinkGoogleDrive?.isEnabled = false
        val cloud_id: String? = presenter?.mUser?.cloud_id
        if (cloud_id == null) {
            ServiceManager.getInstance()?.onPickUpNewEmail(this)
        } else {
            ServiceManager.getInstance()?.onPickUpExistingEmail(this, cloud_id)
        }
    }

    btnUserAnotherAccount.setOnClickListener {
        Utils.Log(TAG, "user another account")
        btnUserAnotherAccount?.isEnabled = false
        btnLinkGoogleDrive?.isEnabled = false
        onShowWarningAnotherAccount()
    }
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
                val cloud_id: String? = presenter?.mUser?.cloud_id
                if (cloud_id == null) {
                    ServiceManager.getInstance()?.onPickUpNewEmailNoTitle(this@onShowWarningAnotherAccount, presenter?.mUser?.email)
                } else {
                    ServiceManager.getInstance()?.onPickUpNewEmailNoTitle(this@onShowWarningAnotherAccount, cloud_id)
                }
            }
            .onNegative {
                btnUserAnotherAccount?.isEnabled = true
                btnLinkGoogleDrive?.isEnabled = true
            }
            .show()
}

fun EnableCloudAct.onShowProgressDialog() {
    SingletonManagerProcessing.getInstance()?.onStartProgressing(this,R.string.loading)
}

fun EnableCloudAct.onStopProgressDialog() {
   SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
}
