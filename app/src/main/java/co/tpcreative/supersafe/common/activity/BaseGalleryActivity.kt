package co.tpcreative.supersafe.common.activity
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.ThemeUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifier
import co.tpcreative.supersafe.common.extension.getScreenStatus
import co.tpcreative.supersafe.common.extension.isFaceDown
import co.tpcreative.supersafe.common.extension.putScreenStatus
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.ui.move_album.MoveAlbumFragment
import co.tpcreative.supersafe.ui.move_album.openAlbum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseGalleryActivity : AppCompatActivity(), MoveAlbumFragment.OnGalleryAttachedListener, SensorFaceUpDownChangeNotifier.Listener {
    private var fragment: MoveAlbumFragment? = null
    var TAG : String = this::class.java.simpleName
    fun attachFragment(layoutId: Int) {
        fragment = MoveAlbumFragment.newInstance() as MoveAlbumFragment
        val fragmentManager: FragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(layoutId, fragment!!).commit()
    }

    fun openAlbum() {
        fragment?.openAlbum()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
            val result: Boolean = Utils.isFaceDown()
            if (result) {
                Navigator.onMoveToFaceDown(this)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop....")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        SensorFaceUpDownChangeNotifier.getInstance()?.remove(this)
        Utils.Log(TAG, "onPause")
    }

    override fun onResume() {
        Utils.Log(TAG, "Action here........onResume")
        SensorFaceUpDownChangeNotifier.getInstance()?.addListener(this)
        super.onResume()
    }

    protected fun onRegisterHomeWatcher() {
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }

    protected fun setDisplayHomeAsUpEnabled(check: Boolean) {
        actionBar?.setDisplayHomeAsUpEnabled(check)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onCallLockScreen() = CoroutineScope(Dispatchers.IO).launch {
        when (val action = EnumPinAction.values()[Utils.getScreenStatus()]) {
            /*First opening app*/
            EnumPinAction.SPLASH_SCREEN -> {
                Utils.putScreenStatus(EnumPinAction.SCREEN_LOCK.ordinal)
                Navigator.onMoveToVerifyPin(this@BaseGalleryActivity, EnumPinAction.NONE)
                Utils.Log(TAG, "Lock screen")
            }
            /*Sleep screen or unlock device*/
            EnumPinAction.SCREEN_LOCK -> {
                if (!SingletonManager.getInstance().isVisitLockScreen()) {
                    Navigator.onMoveToVerifyPin(this@BaseGalleryActivity, EnumPinAction.NONE)
                    SingletonManager.getInstance().setVisitLockScreen(true)
                    Utils.Log(TAG, "Lock screen")
                } else {
                    Utils.Log(TAG, "Verify pin already")
                }
            }
            else -> {
                Utils.Log(TAG, "Nothing to do " + action.name)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        /*Opening app*/
        if (Utils.getScreenStatus() == EnumPinAction.SPLASH_SCREEN.ordinal ){
            onCallLockScreen()
            return
        }
        else if (Utils.getScreenStatus() == EnumPinAction.SCREEN_LOCK.ordinal && !SingletonManager.getInstance().isVisitLockScreen()){
            onCallLockScreen()
            return
        }
        /*Do somethings*/
        Utils.onScanFile(this,"scan.log")
    }

    protected abstract fun onStopListenerAWhile()

    companion object {
        val TAG = BaseGalleryActivity::class.java.simpleName
    }
}