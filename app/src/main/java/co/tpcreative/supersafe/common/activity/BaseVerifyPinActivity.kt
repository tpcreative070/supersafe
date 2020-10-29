package co.tpcreative.supersafe.common.activity
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import co.infinum.goldfinger.Goldfinger
import co.infinum.goldfinger.Goldfinger.PromptParams
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifier
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.ThemeUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ThemeApp
import com.snatik.storage.Storage
import spencerstudios.com.bungeelib.Bungee

abstract class BaseVerifyPinActivity : AppCompatActivity(), SensorFaceUpDownChangeNotifier.Listener {
    protected var actionBar: ActionBar? = null
    protected var storage: Storage? = null
    var onStartCount = 0
    var TAG : String = this::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar = getSupportActionBar()
        storage = Storage(this)
        if (savedInstanceState == null) {
            Bungee.fade(this)
        } else {
            onStartCount = 2
        }
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    override fun getTheme(): Resources.Theme? {
        val theme: Resources.Theme = super.getTheme()
        val result: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        if (result != null) {
            theme.applyStyle(ThemeUtil.getSlideThemeId(result.getId()), true)
        }
        return theme
    }

    protected fun onFaceDown(isFaceDown: Boolean) {
        if (isFaceDown) {
            val result: Boolean = PrefsController.getBoolean(getString(R.string.key_face_down_lock), false)
            if (result) {
                Navigator.onMoveToFaceDown(SuperSafeApplication.getInstance())
            }
        }
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        SensorFaceUpDownChangeNotifier.getInstance()?.remove(this)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        SensorFaceUpDownChangeNotifier.getInstance()?.addListener(this)
        Utils.Log(TAG, "Action here........onResume")
        super.onResume()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }

    protected fun showMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    protected fun setDisplayHomeAsUpEnabled(check: Boolean) {
        actionBar?.setDisplayHomeAsUpEnabled(check)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        if (SingletonManager.getInstance().isAnimation()) {
            if (onStartCount > 1) {
                Bungee.fade(this)
            } else if (onStartCount == 1) {
                onStartCount++
            }
        } else {
            Bungee.zoom(this)
            SingletonManager.getInstance().setAnimation(true)
        }
    }

    companion object {
        val TAG = BaseVerifyPinActivity::class.java.simpleName
    }

    /*Biometric*/
    open fun buildPromptParams(): PromptParams? {
        return PromptParams.Builder(this)
                .title("Biometric")
                .description("Authenticate Fingerprint to unlock") /* Device credentials can be used here */ //            .deviceCredentialsAllowed(true)
                .negativeButtonText("Cancel")
                .build()
    }

    open fun handleGoldfingerResult(result: Goldfinger.Result) {
//        if (result.type() == Goldfinger.Type.SUCCESS || result.type() == Goldfinger.Type.ERROR) {
//            val formattedResult = String.format("%s - %s", result.type().toString(), result.reason().toString())
//        }
        if (result.type() == Goldfinger.Type.SUCCESS){
            onBiometricSuccessful()
        }
    }
    abstract fun onBiometricSuccessful()
}