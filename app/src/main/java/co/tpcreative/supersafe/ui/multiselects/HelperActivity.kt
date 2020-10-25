package co.tpcreative.supersafe.ui.multiselects
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import com.google.android.material.snackbar.Snackbar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class HelperActivity : BaseActivity() {
    protected var mView: View? = null
    private val maxLines = 4
    private val permissions: Array<String> = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    protected fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            permissionGranted()
        } else {
            ActivityCompat.requestPermissions(this, permissions, Navigator.PERMISSION_REQUEST_CODE)
        }
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showRequestPermissionRationale()
        } else {
            showAppPermissionSettings()
        }
    }

    private fun showRequestPermissionRationale() {
        val snackbar: Snackbar = Snackbar.make(
                mView!!,
                getString(R.string.permission_info),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.permission_ok), View.OnClickListener {
                    ActivityCompat.requestPermissions(
                            this@HelperActivity,
                            permissions,
                            Navigator.PERMISSION_REQUEST_CODE)
                })
        snackbar.show()
    }

    private fun showAppPermissionSettings() {
        val snackbar: Snackbar = Snackbar.make(
                mView!!,
                getString(R.string.permission_force),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.permission_settings), View.OnClickListener {
                    val uri = Uri.fromParts(
                            getString(R.string.permission_package),
                            this@HelperActivity.getPackageName(),
                            null)
                    val intent = Intent()
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    intent.setData(uri)
                    startActivityForResult(intent, Navigator.PERMISSION_REQUEST_CODE)
                })

        /*((TextView) snackbar.getView()
                .findViewById(android.support.design.R.id.snackbar_text)).setMaxLines(maxLines);*/snackbar.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != Navigator.PERMISSION_REQUEST_CODE || grantResults.size == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
            permissionDenied()
        } else {
            permissionGranted()
        }
    }

    protected open fun permissionGranted() {}
    private fun permissionDenied() {
        hideViews()
        requestPermission()
    }

    protected open fun hideViews() {}
    protected fun setView(view: View?) {
        this.mView = view
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
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }
}