package co.tpcreative.supersafe.ui.askpermission
import android.Manifest
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.extension.isRunning
import co.tpcreative.supersafe.common.extension.putScreenStatus
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
import co.tpcreative.supersafe.common.helper.EncryptDecryptPinHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumPinAction
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_ask_permission.*

fun AskPermissionAct.initUI(){
    TAG = this::class.java.simpleName
    btnGrantAccess.setOnClickListener {
        onAddPermission()
    }
    btnExit.setOnClickListener {
        finish()
    }
}

fun AskPermissionAct.onAddPermission() {
    Dexter.withContext(this)
            .withPermissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted()==true) {
                        if (SuperSafeApplication.getInstance().isRequestMigration() && SuperSafeApplication.getInstance().isLiveMigration()){
                            finish()
                        }else{
                            onNavigation()
                        }
                    } else {
                        Utils.Log(TAG, "Permission is denied")
                    }
                    // check for permanent denial of any permission
                    if (report?.isAnyPermissionPermanentlyDenied == true) {
                        /*Miss add permission in manifest*/
                        Utils.Log(TAG, "request permission is failed")
                        finish()
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                    /* ... */
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener { Utils.Log(TAG, "error ask permission") }.onSameThread().check()
}

fun AskPermissionAct.onNavigation(){
    if (Utils.isRunning()) {
        val mCurrentPin = SuperSafeApplication.getInstance().readKey()
        if (mCurrentPin?.isNotEmpty() == true) {
            EncryptDecryptFilesHelper.getInstance()
            EncryptDecryptPinHelper.getInstance()
            Utils.putScreenStatus(EnumPinAction.SPLASH_SCREEN.ordinal)
            Navigator.onMoveToMainTab(this, false)
        } else {
            Utils.clearAppDataAndReCreateData()
            Navigator.onMoveToDashBoard(this)
        }
    } else {
        Navigator.onMoveToDashBoard(this)
    }
    SuperSafeApplication.getInstance().initFolder()
    finish()
}