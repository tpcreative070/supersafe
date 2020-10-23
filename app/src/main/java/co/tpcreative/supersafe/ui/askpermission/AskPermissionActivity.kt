package co.tpcreative.supersafe.ui.askpermission
import android.Manifest
import android.os.Bundle
import android.util.Log
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.User
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class AskPermissionActivity : BaseActivityNoneSlide() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_permission)
    }

    protected override fun onStopListenerAWhile() {}
    override fun onOrientationChange(isFaceDown: Boolean) {}
    fun onAddPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report?.areAllPermissionsGranted()!!) {
                            Navigator.onMoveToDashBoard(this@AskPermissionActivity)
                            val mUSer: User? = Utils.getUserInfo()
                            if (mUSer != null) {
                                mUSer.driveConnected = false
                                Utils.setUserPreShare(mUSer)
                            }
                            PrefsController.putBoolean(getString(R.string.key_grant_access), true)
                            SuperSafeApplication.Companion.getInstance().initFolder()
                            finish()
                        } else {
                            Utils.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token?.continuePermissionRequest()
                    }
                })
                .withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(error: DexterError?) {
                        Log.d(TAG, "error ask permission")
                    }
                }).onSameThread().check()
    }

    @OnClick(R.id.btnGrantAccess)
    fun onGrantAccess() {
        onAddPermission()
    }

    @OnClick(R.id.btnExit)
    fun onExit() {
        finish()
    }

    companion object {
        private val TAG = AskPermissionActivity::class.java.simpleName
    }
}